package com.seasun.data.simple_report.collect;

import java.util.Map;

import com.seasun.data.simple_report.base.EventType;

public interface JsonEventHandle {
	public void handle(EventType eventtype, Map<String, Object> paramMap);

}
