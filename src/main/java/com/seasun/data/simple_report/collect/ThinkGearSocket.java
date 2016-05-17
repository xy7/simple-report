package com.seasun.data.simple_report.collect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import processing.core.PApplet;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

public class ThinkGearSocket implements Runnable {
	private static final Log log = LogFactory.getLog(ThinkGearSocket.class);

	public PApplet parent;
	public Socket neuroSocket;
	public OutputStream outStream;
	public InputStream inStream;
	public BufferedReader stdIn;

	public String appName = "";
	public String appKey = "";
	public EventHandle eventHandle;
	private Thread t;

	private int index = 0;

	public final static String VERSION = "1.0";

	private boolean running = true;

	public ThinkGearSocket(EventHandle eventHandle) {
		this.eventHandle = eventHandle;
	}

	public ThinkGearSocket(EventHandle eventHandle, String _appName, String _appKey) {
		this.appName = _appName;// these were mentioned in the documentation as
		this.appKey = _appKey; // required, but test prove they are not.
		this.eventHandle = eventHandle;
	}

	public boolean isRunning() {
		return running;
	}

	/**
	 * return the version of the library.
	 * 
	 * @return String
	 */
	public static String version() {
		return VERSION;
	}

	public void start() throws ConnectException {

		try {
			neuroSocket = new Socket("127.0.0.1", 13854);
			// neuroSocket.getChannel().configureBlocking(false);
			// neuroSocket.setKeepAlive(true);
		} catch (ConnectException e) {
			// e.printStackTrace();
			System.out.println("Oi plonker! Is ThinkkGear running?");
			running = false;
			throw e;
		} catch (UnknownHostException e) {
			log.error(e);
		} catch (IOException e2) {
			log.error(e2);
		}

		try {
			inStream = neuroSocket.getInputStream();
			outStream = neuroSocket.getOutputStream();
			stdIn = new BufferedReader(new InputStreamReader(neuroSocket.getInputStream()));
			running = true;
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (appName != "" && appKey != "") {
			JSONObject appAuth = new JSONObject();
			try {
				appAuth.put("appName", appName);
			} catch (JSONException e1) {
				log.error(e1);
			}
			try {
				appAuth.put("appKey", appKey);
			} catch (JSONException e1) {
				log.error(e1);
			}
			// throws some error
			sendMessage(appAuth.toString());
			log.info("appAuth" + appAuth);
		}

		JSONObject format = new JSONObject();
		try {
			format.put("enableRawOutput", true);
		} catch (JSONException e) {
			log.error(e);
		}
		try {
			format.put("format", "Json");
		} catch (JSONException e) {
			log.error(e);
		}
		// System.out.println("format "+format);
		sendMessage(format.toString());
		t = new Thread(this);
		t.start();

	}

	@SuppressWarnings("deprecation")
	public void stop() {

		if (running) {
			t.interrupt();
			try {

				neuroSocket.close();

				inStream.close();
				outStream.close();
				stdIn.close();
				stdIn = null;
			} catch (IOException e) {
				log.error("Socket close issue: " + e);
			}

		}
		running = false;
	}

	public void sendMessage(String msg) {
		PrintWriter out = new PrintWriter(outStream, true);
		log.debug(msg);
		out.println(msg);
	}

	public void run() {

		while (true) {
			if (running && neuroSocket.isConnected()) {
				String userInput;

				try {
					while ((userInput = stdIn.readLine()) != null) {

						LocalDateTime ldt = LocalDateTime.now();

						String[] packets = userInput.split("/\r/");
						for (int s = 0; s < packets.length; s++) {
							if (((String) packets[s]).indexOf("{") > -1) {
								JSONObject obj = JSON.parseObject((String) packets[s]);
								parsePacket(ldt, obj);
							}
						}

					}
				} catch (SocketException e) {
					// System.out.println("For some reason stdIn throws error even if closed");
					// maybe it takes a cycle to close properly?
					// e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					// e.printStackTrace();
				}
				// eventHandle.delay();
			} else {
				running = false;
			}
		}
	}

	private void parsePacket(LocalDateTime time, JSONObject data) {

		boolean haveValidData = false;

		if (data.containsKey("rawEeg")) {
			haveValidData = true;
			if (index == 512) {
				index = 0;
			}
			eventHandle.rawEegEvent(time, data.getIntValue("rawEeg"), index);
			index++;
		}

		if (data.containsKey("blinkStrength")) {
			haveValidData = true;
			eventHandle.blinkEvent(time, data.getIntValue("blinkStrength"));
		}

		if (data.containsKey("eSense")) {
			haveValidData = true;
			JSONObject esense = data.getJSONObject("eSense");
			eventHandle.esenseEvent(time, esense.getIntValue("attention"), esense.getIntValue("meditation"));
		}
		if (data.containsKey("eegPower")) {
			haveValidData = true;
			JSONObject eegPower = JSON.parseObject("eegPower");
			eventHandle.eegPowerEvent(time
					, eegPower.getIntValue("delta"), eegPower.getIntValue("theta")
					, eegPower.getIntValue("lowAlpha"), eegPower.getIntValue("highAlpha")
					, eegPower.getIntValue("lowBeta"), eegPower.getIntValue("highBeta")
					, eegPower.getIntValue("lowGamma"), eegPower.getIntValue("highGamma"));
		}

		if (data.containsKey("poorSignalLevel")) {
			haveValidData = true;
			eventHandle.poorSignalEvent(time, data.getIntValue("poorSignalLevel"));
		}

		if (!haveValidData) {
			log.debug(time + " validData: " + data);
		}

	}

}
