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
import java.time.temporal.ChronoUnit;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

@Component
public class ComRead implements SerialPortEventListener, Runnable {
	private static final Log log = LogFactory.getLog(ComRead.class);
	
	@Autowired(required=true)
	@Qualifier("dataCollectApp")
	private EventHandle eventHandle;
	
	boolean bindSucess =false;

	@SuppressWarnings({"rawtypes"})
	private Enumeration portList;
	private CommPortIdentifier portId;
	private InputStream inputStream;
	private BufferedReader reader;
	
	private long diffSecondsAvg = Long.MAX_VALUE;

	public void start() throws TooManyListenersException, IOException, UnsupportedCommOperationException {
		Thread daemon = null;
		while (!bindSucess){
			
			portList = CommPortIdentifier.getPortIdentifiers(); // 得到当前连接上的端口
			while (portList.hasMoreElements()) {
				portId = (CommPortIdentifier) portList.nextElement();
				if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {// 判断如果端口类型是串口
					log.info(portId.getName());
					if (portId.getName().equals("COM4") || portId.getName().equals("COM3")) {
						log.info("bind " + portId.getName());
						try {
							SerialPort serialPort = (SerialPort) portId.open("SerialReader", 2000);
				            int rate = 256000; //波特率
				            int dataBits = SerialPort.DATABITS_8;//数据位
				            int stopBits = SerialPort.STOPBITS_1;//停止位
				            int parity = SerialPort.PARITY_NONE;//检验 无
				            serialPort.setSerialPortParams( rate, dataBits, stopBits, parity );
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
		
		while(true){
			
			log.info("daemon state: " + daemon.getState());
			
			try {
				Thread.sleep(1000*20);
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
	
	private void parsePacket(LocalDateTime time, JSONObject data) {

		boolean haveValidData = false;
		
		String deviceId = data.getString("deviceId");
		deviceId = deviceId.substring(0, deviceId.length()<20?deviceId.length():20);

		if (data.containsKey("rawEeg")) {
			haveValidData = true;
			JSONArray ja = data.getJSONArray("rawEeg");
			int index = 0;
			for(Object o:ja){
				LocalDateTime timeIndex = time.plusNanos((long)index * 1000000000/512);
				eventHandle.rawEegEvent(timeIndex, Integer.parseInt(o.toString()), index, deviceId);
				index++;
			}
			
		}

		if (data.containsKey("blinkStrength")) {
			haveValidData = true;
			eventHandle.blinkEvent(time, data.getIntValue("blinkStrength"), deviceId);
		}

		if (data.containsKey("eSense")) {
			haveValidData = true;
			JSONObject esense = data.getJSONObject("eSense");
			eventHandle.esenseEvent(time, esense.getIntValue("attention"), esense.getIntValue("meditation"), deviceId);
		}
		if (data.containsKey("eegPower")) {
			haveValidData = true;
			JSONObject eegPower = data.getJSONObject("eegPower");
			eventHandle.eegPowerEvent(time
					, eegPower.getIntValue("delta"), eegPower.getIntValue("theta")
					, eegPower.getIntValue("lowAlpha"), eegPower.getIntValue("highAlpha")
					, eegPower.getIntValue("lowBeta"), eegPower.getIntValue("highBeta")
					, eegPower.getIntValue("lowGamma"), eegPower.getIntValue("highGamma"), deviceId);
		}

		if (data.containsKey("poorSignalLevel")) {
			haveValidData = true;
			eventHandle.poorSignalEvent(time, data.getIntValue("poorSignalLevel"), deviceId);
		}

		if (!haveValidData) {
			log.debug(time + " validData: " + data);
		}

	}

	public void monitorDelay(LocalDateTime time, JSONObject data) {
		//单片机接收时间
		LocalDateTime scmTime = LocalDateTime.parse(data.getString("timestamp").replace(" ", "T"));
		if(diffSecondsAvg == Long.MAX_VALUE){
			log.info("init  scm time: " + scmTime + " receive time: " + time);
		}
		//单片机时钟和server时钟的差，用于监控延时变化情况
		long absDiffSeconds = Math.abs(scmTime.until(time, ChronoUnit.SECONDS));
		//log.info(diffSecondsAvg + "-" + absDiffSeconds);
		if( absDiffSeconds < diffSecondsAvg){
			diffSecondsAvg = absDiffSeconds;
		} else if(absDiffSeconds - diffSecondsAvg > 5){
			log.error("diff seconds turned  more than 5 seconds, scm time: " + scmTime + " receive time: " + time);
		}
	}

	private ArrayBlockingQueue<JSONObject> jsonQueue = new ArrayBlockingQueue<>(4096);
	
	@Override
	public void run() {
		while(true)
			readAndProcessData();
	}
	
	public Thread statProcessThread(){
		Thread d = new Thread(
				new Runnable(){

					@Override
					public void run() {
						log.debug("process start:");
						while(true){
							if(jsonQueue.size() > 0){
								try {
									JSONObject obj = jsonQueue.take();
									log.debug("process: " + obj);
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
	
	public void readAndProcessData() {	
		try {
			String userInput;
			while ((userInput = reader.readLine()) != null) {
				LocalDateTime ldt = LocalDateTime.now();
				//log.info(ldt + " : " + userInput);
				
				if (userInput.indexOf("{") > -1) {
					JSONObject obj = JSON.parseObject(userInput);
					monitorDelay(ldt, obj);
					
					//parsePacket(ldt, obj);
					obj.put("receive_time", ldt.toString());
					try {
						jsonQueue.put(obj);
						log.debug("put sucess: " + jsonQueue.size());
					} catch (InterruptedException e) {
						log.error(e);
					}
				}
			}
		} catch (SocketException e) {
			log.error(e.toString());
		} catch (IOException e) {
			//log.error(e.toString());
		} catch (JSONException e) {
			log.error(e.toString());
		}
	}
}
