package com.seasun.data.simple_report.base;

public enum EventType {
	
	POOR_SIGNAL_LEVEL("poorSignalLevel")
	, EEG_POWER("eegPower")
	, E_SENSE("eSense")
	, BLINK_STRENGTH("blinkStrength")
	, RAW_EEG("rawEeg")
	, GSR("GSR"); //皮电数据
	
	private final String value; //与TGSP协议中的字段名保持一致
	
	EventType(String value){
		this.value = value;
	}
	
	public String getValue(){
		return value;
	}
	
	public static EventType parse(String value){
		for(EventType e:EventType.values()){
			if(e.getValue().equals(value)){
				return e;
			}
		}
		return null;
	}

}
