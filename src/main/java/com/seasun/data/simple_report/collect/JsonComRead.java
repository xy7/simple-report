package com.seasun.data.simple_report.collect;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.seasun.data.simple_report.base.EventType;
import com.seasun.data.simple_report.base.MaxDropQueue;

@Component
public class JsonComRead implements SerialPortEventListener, Runnable {

	private static final Log log = LogFactory.getLog(JsonComRead.class);

	@Autowired(required = true)
	@Qualifier("jsonEventHandleDb")
	private JsonEventHandle dbHandle;
	
	@Resource(name="realtimeQueues")
	public Map<EventType, MaxDropQueue<Map<String, Object> > > realtimeQueues;
	
	@Resource(name="dbQueues")
	public Map<EventType, BlockingQueue<Map<String, Object>> > dbQueues;

	boolean bindSucess = false;

	@SuppressWarnings({ "rawtypes" })
	private Enumeration portList;
	private CommPortIdentifier portId;
	private InputStream inputStream;
	private BufferedReader reader;

	private long diffSecondsAvg = Long.MAX_VALUE;

	public void start() throws TooManyListenersException, IOException, UnsupportedCommOperationException {
		Thread daemon = null;
		Thread parseDaemon = null;
		while (!bindSucess) {

			portList = CommPortIdentifier.getPortIdentifiers(); // 得到当前连接上的端口
			while (portList.hasMoreElements()) {
				portId = (CommPortIdentifier) portList.nextElement();
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {// 判断如果端口类型是串口
					log.info(portId.getName());
					if (portId.getName().equals("COM4") || portId.getName().equals("COM3")) {
						log.info("bind " + portId.getName());
						try {
							SerialPort serialPort = (SerialPort) portId.open("SerialReader", 2000);
							int rate = 256000; // 波特率
							int dataBits = SerialPort.DATABITS_8;// 数据位
							int stopBits = SerialPort.STOPBITS_1;// 停止位
							int parity = SerialPort.PARITY_NONE;// 检验 无
							serialPort.setSerialPortParams(rate, dataBits, stopBits, parity);
							inputStream = serialPort.getInputStream();
							reader = new BufferedReader(new InputStreamReader(inputStream), 32);
							daemon = startDbProcessThread();
							parseDaemon = startParseProcessThread();
							new Thread(this).start();
						} catch (PortInUseException e) {
							e.printStackTrace();
							log.error("port in use");
						}

						bindSucess = true;
						break;
					}
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				log.error(e);
			}

		}

		while (true) {
			log.info("dbDaemon state: " + daemon.getState() );
			log.info("parseDaemon state: " + parseDaemon.getState());
			log.info("raw blocking queue size: " + dbQueues.get(EventType.RAW_EEG).size());

			try {
				Thread.sleep(1000 * 20);
			} catch (InterruptedException e) {
				log.error(e);
			}
		}

	}

	@Override
	public void serialEvent(SerialPortEvent event) {
		switch (event.getEventType()) {
		case SerialPortEvent.BI: // 10
		case SerialPortEvent.OE: // 7
		case SerialPortEvent.FE: // 9
		case SerialPortEvent.PE: // 8
		case SerialPortEvent.CD: // 6
		case SerialPortEvent.CTS: // 3
		case SerialPortEvent.DSR: // 4
		case SerialPortEvent.RI: // 5
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2
			break;
		case SerialPortEvent.DATA_AVAILABLE: // 1
			readAndProcessData();
			break;
		}
	}

	private void parsePacket(LocalDateTime time, JSONObject data) throws InterruptedException {

		boolean haveValidData = false;

		String deviceId = data.getString("deviceId");
		deviceId = deviceId.substring(0, deviceId.length() < 20 ? deviceId.length() : 20);
		String timeStamp = time.toString().replace("T", " ");

		if (data.containsKey(EventType.RAW_EEG.getValue())) {
			haveValidData = true;
			JSONArray ja = data.getJSONArray(EventType.RAW_EEG.getValue());
			int index = 0;
			for(Object o:ja){
				LocalDateTime timeIndex = time.plusNanos((long)index * 1000000000/512);
				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("deviceId", deviceId);
				paramMap.put("time", timeIndex.toString().replace("T", " "));
				paramMap.put("index", index);
				paramMap.put("longTime", timeIndex.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()); //js使用
				paramMap.put(EventType.RAW_EEG.getValue(), Integer.parseInt(o.toString()));
				if(index % 128 == 0){
					//log.info("put realTimeQueue: " + paramMap);
					realtimeQueues.get(EventType.RAW_EEG).put(paramMap);
				}
				dbQueues.get(EventType.RAW_EEG).put(paramMap);
				index++;
				
			}
		}

		if (data.containsKey(EventType.BLINK_STRENGTH.getValue())) {
			haveValidData = true;
			JSONObject json = new JSONObject();
			json.put("deviceId", deviceId);
			json.put("time", timeStamp);
			json.put(EventType.BLINK_STRENGTH.getValue(), data.getIntValue(EventType.BLINK_STRENGTH.getValue()));
			realtimeQueues.get(EventType.BLINK_STRENGTH).put(json);
			dbQueues.get(EventType.BLINK_STRENGTH).put(json);
		}

		if (data.containsKey(EventType.E_SENSE.getValue())) {
			haveValidData = true;
			JSONObject esense = data.getJSONObject(EventType.E_SENSE.getValue());
			esense.put("deviceId", deviceId);
			esense.put("time", timeStamp);
			realtimeQueues.get(EventType.E_SENSE).put(esense);
			dbQueues.get(EventType.E_SENSE).put(esense);
		}
		if (data.containsKey(EventType.EEG_POWER.getValue())) {
			haveValidData = true;
			JSONObject eegPower = data.getJSONObject(EventType.EEG_POWER.getValue());
			eegPower.put("deviceId", deviceId);
			eegPower.put("time", timeStamp);
			realtimeQueues.get(EventType.EEG_POWER).put(eegPower);
			dbQueues.get(EventType.EEG_POWER).put(eegPower);
		}

		if (data.containsKey(EventType.POOR_SIGNAL_LEVEL)) {
			haveValidData = true;
			JSONObject json = new JSONObject();
			json.put("deviceId", deviceId);
			json.put("time", timeStamp);
			json.put(EventType.POOR_SIGNAL_LEVEL.getValue(), data.getIntValue(EventType.POOR_SIGNAL_LEVEL.getValue()));
			realtimeQueues.get(EventType.POOR_SIGNAL_LEVEL).put(json);
			dbQueues.get(EventType.POOR_SIGNAL_LEVEL).put(json);

		}

		if (!haveValidData) {
			log.debug(time + " validData: " + data);
		}

	}

	public void monitorDelay(LocalDateTime time, JSONObject data) {
		// 单片机接收时间
		LocalDateTime scmTime = LocalDateTime.parse(data.getString("timestamp").replace(" ", "T"));
		if (diffSecondsAvg == Long.MAX_VALUE) {
			log.info("init  scm time: " + scmTime + " receive time: " + time);
		}
		// 单片机时钟和server时钟的差，用于监控延时变化情况
		long absDiffSeconds = Math.abs(scmTime.until(time, ChronoUnit.SECONDS));
		// log.info(diffSecondsAvg + "-" + absDiffSeconds);
		if (absDiffSeconds < diffSecondsAvg) {
			diffSecondsAvg = absDiffSeconds;
		} else if (absDiffSeconds - diffSecondsAvg > 5) {
			log.error("diff seconds turned  more than 5 seconds, scm time: " + scmTime + " receive time: " + time);
		}
	}

	@Override
	public void run() {
		while (true)
			readAndProcessData();
	}
	
	private ArrayBlockingQueue<JSONObject> jsonQueue = new ArrayBlockingQueue<>(4096);

	public void readAndProcessData() {
		try {
			String userInput;
			while ((userInput = reader.readLine()) != null) {
				LocalDateTime ldt = LocalDateTime.now();
				log.debug(ldt + " : " + userInput);

				if (userInput.indexOf("{") > -1) {
					JSONObject obj = JSON.parseObject(userInput);
					monitorDelay(ldt, obj);

					try {
						//parsePacket(ldt, obj);
						obj.put("receive_time", ldt.toString());
						jsonQueue.put(obj);
						//log.debug("put sucess: " + jsonQueue.size());
					} catch (InterruptedException e) {
						log.error(e);
					}

				}
			}
		} catch (IOException e) {
			//log.error("no data, device may close or disconnect - " + e.toString());
		} catch (JSONException e) {
			log.error("json parse error - " + e.toString());
		}
	}
	
	public Thread startParseProcessThread(){
		Thread d = new Thread(
				new Runnable(){

					@Override
					public void run() {
						log.debug("process start:");
						while(true){
							if(jsonQueue.size() > 0){
								try {
									JSONObject obj = jsonQueue.take();
									//log.debug("process: " + obj);
									LocalDateTime ldt = LocalDateTime.parse( obj.getString("receive_time") );
									parsePacket(ldt, obj);	
								} catch (InterruptedException e) {
									log.error(e);
								}
							}
						}
						
					}}
				);
		d.setDaemon(true);
		d.start();
		return d;
	}
	
	public Thread startDbProcessThread() {
		Thread d = new Thread(
				new Runnable() {

					@Override
					public void run() {
						log.debug("process start:");
						while (true) {
							//long begin = System.currentTimeMillis();
							for (Map.Entry<EventType, BlockingQueue<Map<String, Object>>> e : dbQueues.entrySet()) {
								BlockingQueue<Map<String, Object>> queue = e.getValue();
								if (queue.size() == 0){
									continue;
								}
									
								EventType eventType = e.getKey();
								Map<String, Object> paramMap = null;
//								try {
//									paramMap = queue.take();	
//								} catch (InterruptedException e1) {
//									log.error(e1.toString());
//								}
								
								paramMap = queue.poll();
								if(paramMap == null)
									continue;
								dbHandle.handle(eventType, paramMap);
							}
							//log.info("dbprocess thread cost time: " + (System.currentTimeMillis() - begin) ); 
						}
						
						

					}
				}
				);
		//d.setDaemon(true);
		d.setPriority(Thread.MIN_PRIORITY);
		d.start();
		return d;
	}
}
