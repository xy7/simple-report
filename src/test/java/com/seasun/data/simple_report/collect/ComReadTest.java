package com.seasun.data.simple_report.collect;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import org.junit.Test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ComReadTest {
	
	@Test
	public void testTime(){
		LocalDateTime time = LocalDateTime.parse("2016-07-20T10:00:00");
		LocalDateTime scmTime = LocalDateTime.parse("2016-07-14T06:55:21");
		ComRead com = new ComRead();
		com.monitorDelay(time, scmTime);
		
		for(int i = 0; i < 10; i++){
			time = time.plusSeconds(i*20);
			//scmTime.plusSeconds(i);
			com.monitorDelay(time, scmTime);
		}

	}

	@Test
	public void test() {
		
		String s = "{\"timestamp\":\"2016-07-14 05:42:57\",\"poorSignalLevel\":0,\"eSense\":{\"attention\":56,\"meditation\":7},\"eegPower\":{\"delta\":25523,\"theta\":12495,\"lowAlpha\":1360,\"highAlpha\":1335,\"lowBeta\":842,\"highBeta\":1812,\"lowGamma\":655,\"highGamma\":440,},\"deviceId\":\"74:E5:43:89:5F:F8\"}";
		JSONObject obj = JSON.parseObject(s);
		

		System.out.println(obj);
	}

}
