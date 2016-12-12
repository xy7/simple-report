package com.seasun.data.simple_report.collect;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.seasun.data.simple_report.SimpleReportApp;
import com.seasun.data.simple_report.base.EventType;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SimpleReportApp.class)
public class JsonEventHandleDbTest {

	@Autowired
	private JsonEventHandleDb db;
	
	@Test
	public void gsrEventTest() {
		JSONObject data = new JSONObject();
		int[] vs = {0,1,2,3,4,5,6,7,8,9};
		data.put("GSR", vs);
		
		JSONObject json = new JSONObject();
		json.put("time", "2016-01-01 10:10:10");
		json.put("deviceId", "test");
		JSONArray arr = data.getJSONArray(EventType.GSR.getValue());
		for(int i=0; i<arr.size(); i++){
			int val = arr.getIntValue(i);
			json.put(EventType.GSR.getValue()+i, val);
		}
		
		System.out.println(json);
		
		db.gsrEvent(json);
		
		
	}

}
