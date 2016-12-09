package com.seasun.data.simple_report.collect;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.seasun.data.simple_report.base.EventType;

@Component
public class JsonEventHandleDb implements JsonEventHandle{
	
	private static final Log log = LogFactory.getLog(JsonEventHandleDb.class);
	
	@Autowired
	public NamedParameterJdbcTemplate jdbc;
	
	//字段待修改
	public void poorSignalEvent(Map<String, Object> paramMap) {
		String sql = "insert into poor_signal(signal_level, receive_time, device_id) values(:poorSignalLevel, :time, :deviceId)";
		jdbc.update(sql, paramMap);
	}

	public void esenseEvent(Map<String, Object> paramMap) {
		String sql = "insert into esense(attention, meditation, receive_time, device_id) "
				+ " values(:attention, :meditation, :time, :deviceId)";
		jdbc.update(sql, paramMap);
		
	}

	public void blinkEvent(Map<String, Object> paramMap) {
		String sql = "insert into blink(blink_strength, receive_time, device_id) values(:blinkStrength, :time, :deviceId)";
		jdbc.update(sql, paramMap);
		
	}

	public void eegPowerEvent(Map<String, Object> paramMap) {
		String sql = "insert into eeg_power(delta, theta, low_alpha, high_alpha, low_beta"
				+ ", high_beta, low_gamma, mid_gamma"
				+ " , receive_time, device_id) values(:delta, :theta, :lowAlpha, :highAlpha, :lowBeta"
				+ ", :highBeta, :lowGamma, :highGamma"
				+ ", :time, :deviceId)";
		jdbc.update(sql, paramMap);
		
	}
	
	private List<Map<String, Object>> rawEegParams = new LinkedList<>(); 
	private long num = 0;
	private long sum = 0;
	private int index = 0;

	public void rawEegEvent(Map<String, Object> paramMap) {
		rawEegParams.add(paramMap);
		index ++;
		if(index >= 512){
			index = 0;
			String sql = "insert DELAYED into raw_eeg2(raw_eeg, index_, receive_time, device_id) values(:rawEeg, :index, :time, :deviceId)";
			long begin = System.currentTimeMillis();
			jdbc.batchUpdate(sql, rawEegParams.toArray(new HashMap[0]));
			long cost = System.currentTimeMillis() - begin;
			num ++;
			sum += cost;
			log.info("batchUpdate cost time: " + cost + " avg:" + sum/num + " num: " + num );
			rawEegParams = new LinkedList<>();
		}
	
	}

	@Override
	public void handle(EventType eventtype, Map<String, Object> paramMap) {
		switch (eventtype) {
		case POOR_SIGNAL_LEVEL:
			poorSignalEvent(paramMap);
			break;
		case EEG_POWER:
			eegPowerEvent(paramMap);
			break;
		case E_SENSE:
			esenseEvent(paramMap);
			break;
		case BLINK_STRENGTH:
			blinkEvent(paramMap);
			break;
		case RAW_EEG:
			rawEegEvent(paramMap);
			break;
		default:
			break;

		}
	}

}
