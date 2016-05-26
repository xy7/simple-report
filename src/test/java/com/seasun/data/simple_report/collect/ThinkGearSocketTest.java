package com.seasun.data.simple_report.collect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class ThinkGearSocketTest {

	@Test
	public void test() throws UnknownHostException, IOException {
		Socket neuroSocket = new Socket("127.0.0.1", 13854);
		InputStream inStream = neuroSocket.getInputStream();
		OutputStream outStream = neuroSocket.getOutputStream();
		BufferedReader stdIn = new BufferedReader(new InputStreamReader(neuroSocket.getInputStream()));
		PrintWriter out = new PrintWriter(outStream, true);
		
		String format = "{\"enableRawOutput\":false,\"format\":\"Json\"}";
		//System.out.println("format: " + format);
		//out.println(format);
		
		String auth = "{\"appName\":\"Brainwave Shooters\""
				+ ",\"appKey\":\"9f54141b4b4c567c558d3a76cb8d715cbde03096\"}";
				//+ ",\"enableRawOutput\":true,\"format\":\"Json\"}";
		//System.out.println("auth: " + auth);
		//out.println(auth);
		
//		JSONObject format = new JSONObject();
//		format.put("enableRawOutput", true);
//		format.put("format", "Json");
//		out.println(format.toString());
//		
//		JSONObject json = new JSONObject();
//		json.put("getAppNames", null);
//		
//		
//		out.println(json.toString());
		
		System.out.println("start:");
		int i = 0;
		while (true) {
			String userInput;

			try {
				while ((userInput = stdIn.readLine()) != null) {
					System.out.println(userInput);
//					if( (++i) > 10)
//						break;
				}
			} catch(Exception e){
				e.printStackTrace();
			}
			
//			if( (++i) > 10)
//				break;
		}
	}
	
	@Test
	public void StringLengthTest(){
		String s = "{\"rawEeg\":-2048}";
		System.out.println(s + " length: " + s.length());
	}

}
