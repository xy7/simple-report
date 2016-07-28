package com.seasun.data.simple_report.collect;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ComReadTest {
	
	@Test
	public void testTime(){
		LocalDateTime time = LocalDateTime.now();
		for(int i=0;i<512;i++){
			long k = (long)i * 1000000000/512;
			LocalDateTime timeIndex = time.plusNanos(k);
			System.out.println(i + "\t" + k + "\t" + timeIndex);
		}
		

	}

	@Test
	public void test() {
		
		String s = "{\"timestamp\":\"2016-07-14 05:42:57\",\"poorSignalLevel\":0,\"eSense\":{\"attention\":56,\"meditation\":7},\"eegPower\":{\"delta\":25523,\"theta\":12495,\"lowAlpha\":1360,\"highAlpha\":1335,\"lowBeta\":842,\"highBeta\":1812,\"lowGamma\":655,\"highGamma\":440,},\"deviceId\":\"74:E5:43:89:5F:F8\"}";
		JSONObject obj = JSON.parseObject(s);
		

		System.out.println(obj);
	}
	
	@Test
	public void testQueue(){
		ArrayBlockingQueue<JSONObject>[] jsonQueues = new ArrayBlockingQueue[5];

		for(ArrayBlockingQueue queue:jsonQueues){
			queue = new ArrayBlockingQueue<JSONObject>(10);
		
			if(queue != null)
				System.out.println(queue.size());
		}
	}

}
