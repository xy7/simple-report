package com.seasun.data.simple_report.collect;

import java.util.Map;

public interface JsonEventHandle {

	public void poorSignalEvent(Map<String, Object> paramMap);
	
	public void esenseEvent(Map<String, Object> paramMap);

	public void blinkEvent(Map<String, Object> paramMap);

	public void eegPowerEvent(Map<String, Object> paramMap);

	public void rawEegEvent(Map<String, Object> paramMap);

}
