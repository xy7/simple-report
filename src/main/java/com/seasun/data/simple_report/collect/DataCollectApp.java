package com.seasun.data.simple_report.collect;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.seasun.data.simple_report.base.EventType;
import com.seasun.data.simple_report.base.MaxDropQueue;

@Component
public class DataCollectApp implements EventHandle{
	private static final Log log = LogFactory.getLog(DataCollectApp.class);
	
	@Autowired
	public NamedParameterJdbcTemplate jdbc;
	
	@Resource(name="realtimeQueues")
	public Map<EventType, MaxDropQueue<Map<String, Object> > > queues;

	@Override
	public void poorSignalEvent(LocalDateTime time, int sig, String deviceId) {
		log.debug(time + " SignalEvent " + sig);
		Map<String, Object> paramMap = new HashMap<>(2);
		paramMap.put("poorSignalLevel", sig);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("deviceId", deviceId);
		queues.get(EventType.POOR_SIGNAL_LEVEL).put(paramMap);
		jdbc.update("insert into poor_signal(signal_level, receive_time, device_id) values(:poorSignalLevel, :time, :deviceId)", paramMap);
	}
	
	@Override
	public void esenseEvent(LocalDateTime time, int attentionLevel, int meditationLevel, String deviceId){
		log.debug(time + " attentionLevel: " + attentionLevel + " meditationLevel: " + meditationLevel);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("attention", attentionLevel);
		paramMap.put("meditation", meditationLevel);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		paramMap.put("deviceId", deviceId);
		queues.get(EventType.E_SENSE).put(paramMap);
		jdbc.update("insert into esense(attention, meditation, receive_time, device_id) values(:attention, :meditation, :time, :deviceId)", paramMap);
	}
	

	@Override
	public void blinkEvent(LocalDateTime time, int blinkStrength, String deviceId) {
		log.debug(time + " blinkStrength: " + blinkStrength);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("blinkStrength", blinkStrength);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		paramMap.put("deviceId", deviceId);
		queues.get(EventType.BLINK_STRENGTH).put(paramMap);
		jdbc.update("insert into blink(blink_strength, receive_time, device_id) values(:blinkStrength, :time, :deviceId)", paramMap);
	}

	@Override
	public void eegPowerEvent(LocalDateTime time
			, int delta, int theta, int low_alpha, int high_alpha
			, int low_beta, int high_beta,int low_gamma, int mid_gamma, String deviceId) {
		
		log.debug(time + " eegPower:");
		log.debug("delta Level: " + delta);
		log.debug("theta Level: " + theta);
		log.debug("low_alpha Level: " + low_alpha);
		log.debug("high_alpha Level: " + high_alpha);
		log.debug("low_beta Level: " + low_beta);
		log.debug("high_beta Level: " + high_beta);
		log.debug("low_gamma Level: " + low_gamma);
		log.debug("mid_gamma Level: " + mid_gamma);
		
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("delta", delta);
		paramMap.put("theta", theta);
		paramMap.put("lowAlpha", low_alpha);
		paramMap.put("highAlpha", high_alpha);
		paramMap.put("lowBeta", low_beta);
		paramMap.put("highBeta", high_beta);
		paramMap.put("lowGamma", low_gamma);
		paramMap.put("highGamma", mid_gamma);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		paramMap.put("deviceId", deviceId);
		queues.get(EventType.EEG_POWER).put(paramMap);
		String sql = "insert into eeg_power(delta, theta, low_alpha, high_alpha, low_beta"
				+ ", high_beta, low_gamma, mid_gamma"
				+ " , receive_time, device_id) values(:delta, :theta, :lowAlpha, :highAlpha, :lowBeta"
				+ ", :highBeta, :lowGamma, :highGamma"
				+ ", :time, :deviceId)";
		jdbc.update(sql, paramMap);
	}
	
	
	private List<Map<String, Object>> rawEegParams = new LinkedList<>(); 
	
	long num = 0;
	long sum = 0;

	//@Transactional
	@Override
	public void rawEegEvent(LocalDateTime time, int raw, int index, String deviceId) {
		//log.debug(time + " rawEvent Level: " + raw + " index: " + index);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("rawEeg", raw);
		paramMap.put("index", index);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		paramMap.put("deviceId", deviceId);
		if(raw > -2048 && raw < 2047 && index%128 == 0)
			queues.get(EventType.RAW_EEG).put(paramMap);
		String sql = "insert DELAYED into raw_eeg2(raw_eeg, index_, receive_time, device_id) values(:rawEeg, :index, :time, :deviceId)";
		
		rawEegParams.add(paramMap);
		//jdbc.update(sql, paramMap);
		if(index >= 511){
			long begin = System.currentTimeMillis();
			jdbc.batchUpdate(sql, rawEegParams.toArray(new HashMap[0]));
			long cost = System.currentTimeMillis() - begin;
			num ++;
			sum += cost;
			log.debug("batchUpdate cost time: " + cost + " avg:" + sum/num + " num: " + num );

			rawEegParams = new LinkedList<>();
		}

	}
}
