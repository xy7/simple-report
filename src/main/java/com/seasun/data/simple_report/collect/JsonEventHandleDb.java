package com.seasun.data.simple_report.collect;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

@Component
public class JsonEventHandleDb implements JsonEventHandle{
	
	private static final Log log = LogFactory.getLog(JsonEventHandleDb.class);
	
	@Autowired
	public NamedParameterJdbcTemplate jdbc;
	
	//字段待修改

	@Override
	public void poorSignalEvent(Map<String, Object> paramMap) {
		String sql = "insert into poor_signal(signal_level, receive_time, device_id) values(:poorSignalLevel, :time, :deviceId)";
		jdbc.update(sql, paramMap);
	}

	@Override
	public void esenseEvent(Map<String, Object> paramMap) {
		String sql = "insert into esense(attention, meditation, receive_time, device_id) "
				+ " values(:attention, :meditation, :time, :deviceId)";
		jdbc.update(sql, paramMap);
		
	}

	@Override
	public void blinkEvent(Map<String, Object> paramMap) {
		String sql = "insert into blink(blink_strength, receive_time, device_id) values(:blinkStrength, :time, :deviceId)";
		jdbc.update(sql, paramMap);
		
	}

	@Override
	public void eegPowerEvent(Map<String, Object> paramMap) {
		String sql = "insert into eeg_power(delta, theta, low_alpha, high_alpha, low_beta"
				+ ", high_beta, low_gamma, mid_gamma"
				+ " , receive_time, device_id) values(:delta, :theta, :low_alpha, :high_alpha, :low_beta"
				+ ", :high_beta, :low_gamma, :mid_gamma"
				+ ", :time, :deviceId)";
		jdbc.update(sql, paramMap);
		
	}
	
	private List<Map<String, Object>> rawEegParams = new LinkedList<>(); 
	private long num = 0;
	private long sum = 0;
	private int index = 0;
	
	@Override
	public void rawEegEvent(Map<String, Object> paramMap) {
		rawEegParams.add(paramMap);
		index ++;
		if(index >= 512){
			index = 0;
			long begin = System.currentTimeMillis();
			String sql = "insert DELAYED into raw_eeg2(raw_eeg, index_, receive_time, device_id) values(:rawEeg, :index, :time, :deviceId)";
			jdbc.batchUpdate(sql, rawEegParams.toArray(new HashMap[0]));
			long cost = System.currentTimeMillis() - begin;
			num ++;
			sum += cost;
			log.debug("batchUpdate cost time: " + cost + " avg:" + sum/num + " num: " + num );
		}
	
	}

}
