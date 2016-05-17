package com.seasun.data.simple_report.base;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import org.apache.commons.io.Charsets;

public class Utils {
	private static Properties props = new Properties();
	private static boolean loadFile = false;
	
	private static PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out, Charsets.UTF_8), true);

	public static boolean loadConfigFile(String file){
		if(loadFile)
			return true;
		
	    //InputStream in = LogisticRegressionTrain.class.getResourceAsStream(file); //配置文件的相对路径以类文件所在目录作为当前目录
		InputStream in = null;
		try {
			in = new FileInputStream(file); //配置文件的相对路径以工作目录
		} catch (FileNotFoundException e) {
			loadFile = true;
			System.out.println("file not found: " + file);
			return false;
		}
	    
	    try {
			props.load(in);
			in.close();
		} catch (IOException e1) {
			e1.printStackTrace();
			System.out.println("load config file error");
			return false;
		}
	    
	    loadFile = true;

	    return true;
	}

	
	public static <T> T getOrDefault(String key, T def){
//		if(!def.getClass().isPrimitive())
//			return null;

		if(!loadFile)
			loadConfigFile("./config.properties");
		
		String value = props.getProperty(key);
		if(value == null){
			out.printf("get %s default: %s %n", key, def);
			return def;
		}
		
		out.printf("get %s : %s %n", key, value);
		if(def instanceof Double){
			Double res = Double.parseDouble(value);
			return (T)res;
		} else if(def instanceof Integer){
			Integer res = Integer.parseInt(value);
			return (T)res;
		} else if(def instanceof Long){
			Long res = Long.parseLong(value);
			return (T)res;
		} else if(def instanceof Float){
			Float res = Float.parseFloat(value);
			return (T)res;
		} else if(def instanceof String){
			return (T)value;
		} else if(def instanceof Boolean){
			Boolean res = Boolean.parseBoolean(value);
			return (T)res;
		}
		
		return null;
	}
	
	public static String SHA1(String decript) {
		try {
			MessageDigest digest = java.security.MessageDigest
					.getInstance("SHA-1");
			digest.update(decript.getBytes());
			byte messageDigest[] = digest.digest();
			// Create Hex String
			StringBuffer hexString = new StringBuffer();
			// 字节数组转换为 十六进制 数
			for (int i = 0; i < messageDigest.length; i++) {
				String shaHex = Integer.toHexString(messageDigest[i] & 0xFF);
				if (shaHex.length() < 2) {
					hexString.append(0);
				}
				hexString.append(shaHex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public static void main(String[] args) {
		System.out.println(SHA1("seasun_raw_data_collect_1") );
		System.out.println(SHA1("seasun_raw_data_collect_1").length() );
	}
}
