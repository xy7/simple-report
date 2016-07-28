package com.seasun.data.simple_report.collect;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;
import com.seasun.data.simple_report.base.MaxDropQueue;


//废弃不用
@Component
public class JsonEventHandleRealTime implements JsonEventHandle{
	
	private static final Log log = LogFactory.getLog(JsonEventHandleRealTime.class);
	
	@Resource(name="allTypeQueues")
	public Map<String, MaxDropQueue<Map<String, Object> > > queues;

	@Override
	public void poorSignalEvent(Map<String, Object> paramMap) {
		queues.get("signal").put(paramMap);
	}

	@Override
	public void esenseEvent(Map<String, Object> paramMap) {
		queues.get("esense").put(paramMap);
	}

	@Override
	public void blinkEvent(Map<String, Object> paramMap) {
		queues.get("blink").put(paramMap);
	}

	@Override
	public void eegPowerEvent(Map<String, Object> paramMap) {
		queues.get("eeg").put(paramMap);
	}

	@Override
	public void rawEegEvent(Map<String, Object> paramMap) {
		
		int raw = (int)paramMap.get("RAW_EEG");
		int index = (int)paramMap.get("index");
		if(raw > -2048 && raw < 2047 && index%128 == 0)
			queues.get("rawEeg").put(paramMap);
	}

}
