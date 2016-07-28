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
import com.seasun.data.simple_report.base.MaxDropQueue;

@Component
public class JsonComRead implements SerialPortEventListener, Runnable {
	private static final String POOR_SIGNAL_LEVEL = "poorSignalLevel";

	private static final String EEG_POWER = "eegPower";

	private static final String E_SENSE = "eSense";

	private static final String BLINK_STRENGTH = "blinkStrength";

	private static final String RAW_EEG = "rawEeg";

	private static final Log log = LogFactory.getLog(JsonComRead.class);

	@Autowired(required = true)
	@Qualifier("jsonEventHandleDb")
	private JsonEventHandle dbHandle;

//	@Autowired(required = true)
//	@Qualifier("jsonEventHandleRealTime")
//	private JsonEventHandle realtimeHandle;
	@Resource(name="allTypeQueues")
	public Map<String, MaxDropQueue<Map<String, Object> > > queues;

	boolean bindSucess = false;

	@SuppressWarnings({ "rawtypes" })
	private Enumeration portList;
	private CommPortIdentifier portId;
	private InputStream inputStream;
	private BufferedReader reader;

	private long diffSecondsAvg = Long.MAX_VALUE;

	public void start() throws TooManyListenersException, IOException, UnsupportedCommOperationException {
		Thread daemon = null;
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
							daemon = statProcessThread();
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

			log.info("daemon state: " + daemon.getState());

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

		if (data.containsKey(RAW_EEG)) {
			haveValidData = true;
			JSONArray ja = data.getJSONArray("rawEeg");
			int index = 0;
			for(Object o:ja){
				LocalDateTime timeIndex = time.plusNanos((long)index * 1000000000/512);
				Map<String, Object> paramMap = new HashMap<>();
				paramMap.put("deviceId", deviceId);
				paramMap.put("time", timeStamp);
				paramMap.put("index", index);
				paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()); //js使用
				paramMap.put(RAW_EEG, Integer.parseInt(o.toString()));
				if(index % 128 == 0)
				queues.get(RAW_EEG).put(paramMap);
				jsonQueues.get(RAW_EEG).put(paramMap);
				index++;
			}
		}

		if (data.containsKey(BLINK_STRENGTH)) {
			haveValidData = true;
			JSONObject json = new JSONObject();
			json.put("deviceId", deviceId);
			json.put("time", timeStamp);
			json.put(BLINK_STRENGTH, data.getIntValue(BLINK_STRENGTH));
			queues.get(RAW_EEG).put(json);
			jsonQueues.get(BLINK_STRENGTH).put(json);
		}

		if (data.containsKey(E_SENSE)) {
			haveValidData = true;
			JSONObject esense = data.getJSONObject(E_SENSE);
			esense.put("deviceId", deviceId);
			esense.put("time", timeStamp);
			queues.get(RAW_EEG).put(esense);
			jsonQueues.get(E_SENSE).put(esense);
		}
		if (data.containsKey(EEG_POWER)) {
			haveValidData = true;
			JSONObject eegPower = data.getJSONObject(EEG_POWER);
			eegPower.put("deviceId", deviceId);
			eegPower.put("time", timeStamp);
			queues.get(RAW_EEG).put(eegPower);
			jsonQueues.get(EEG_POWER).put(eegPower);
		}

		if (data.containsKey(POOR_SIGNAL_LEVEL)) {
			haveValidData = true;
			JSONObject json = new JSONObject();
			json.put("deviceId", deviceId);
			json.put("time", timeStamp);
			json.put(POOR_SIGNAL_LEVEL, data.getIntValue(POOR_SIGNAL_LEVEL));
			queues.get(RAW_EEG).put(json);
			jsonQueues.get(POOR_SIGNAL_LEVEL).put(json);

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

	private static Map<String, ArrayBlockingQueue<Map>> jsonQueues = new HashMap<>(5);
	static {
		jsonQueues.put(RAW_EEG, new ArrayBlockingQueue<>(4096));
		jsonQueues.put(BLINK_STRENGTH, new ArrayBlockingQueue<>(4096));
		jsonQueues.put(E_SENSE, new ArrayBlockingQueue<>(4096));
		jsonQueues.put(EEG_POWER, new ArrayBlockingQueue<>(4096));
		jsonQueues.put(POOR_SIGNAL_LEVEL, new ArrayBlockingQueue<>(4096));
	}

	@Override
	public void run() {
		while (true)
			readAndProcessData();
	}

	public Thread statProcessThread() {
		Thread d = new Thread(
				new Runnable() {

					@Override
					public void run() {
						log.debug("process start:");
						while (true) {
							for (Map.Entry<String, ArrayBlockingQueue<Map>> e : jsonQueues.entrySet()) {
								ArrayBlockingQueue<Map> queue = e.getValue();
								if (queue.size() == 0){
									continue;
								}
									
								String type = e.getKey();
								Map<String, Object> paramMap = null;
								try {
									paramMap = queue.take();
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
								switch (type) {
									case POOR_SIGNAL_LEVEL:
										dbHandle.poorSignalEvent(paramMap);
										break;
									case EEG_POWER:
										dbHandle.eegPowerEvent(paramMap);
										break;
									case E_SENSE:
										dbHandle.esenseEvent(paramMap);
										break;
									case BLINK_STRENGTH:
										dbHandle.blinkEvent(paramMap);
										break;
									case RAW_EEG:
										dbHandle.rawEegEvent(paramMap);
										break;
									default:
										break;

								}

							}
						}

					}
				}
				);
		d.setDaemon(true);
		d.start();
		return d;
	}

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
						parsePacket(ldt, obj);
					} catch (InterruptedException e) {
						log.error(e);
					}
				}
			}
		} catch (SocketException e) {
			log.error(e.toString());
		} catch (IOException e) {
			// log.error(e.toString());
		} catch (JSONException e) {
			log.error(e.toString());
		}
	}
}
