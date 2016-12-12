package com.seasun.data.simple_report.collect;

import java.time.LocalDateTime;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class JsonComReadTest {

	@Test
	public void parsePacketTest() throws Exception {
		JsonComRead read = new JsonComRead();
		JSONObject data = new JSONObject();
		int[] vs = {0,1,2,3,4,5,6,7,8,9};
		data.put("GSR", vs);
		read.parsePacket(LocalDateTime.now(), data);
	}

}
