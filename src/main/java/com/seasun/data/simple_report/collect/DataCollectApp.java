package com.seasun.data.simple_report.collect;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import com.seasun.data.simple_report.base.MaxDropQueue;

@Component
public class DataCollectApp implements EventHandle, InitializingBean{
	private static final Log log = LogFactory.getLog(DataCollectApp.class);

	public ThinkGearSocket neuroSocket;
	
	@Autowired
	public NamedParameterJdbcTemplate jdbc;
	
	@Resource(name="allTypeQueues")
	public Map<String, MaxDropQueue<Map<String, Object> > > queues;
	
	public void setup() {
		
		/*  //目前有问题
		String appName = "seasun_raw_data_collect_1";
		String appKey = util.Utils.SHA1(appName);
		ThinkGearSocket neuroSocket = new ThinkGearSocket(this, appName, appKey);
		*/
		ThinkGearSocket neuroSocket = new ThinkGearSocket(this);
		try {
			neuroSocket.start();
		} catch (Exception e) {
			log.error(e);
			log.error("Is ThinkGear running??");
		}
	}

	@Override
	public void poorSignalEvent(LocalDateTime time, int sig) {
		log.debug(time + " SignalEvent " + sig);
		Map<String, Object> paramMap = new HashMap<>(2);
		paramMap.put("sig", sig);
		paramMap.put("time", time.toString().replace("T", " "));
		queues.get("signal").put(paramMap);
		jdbc.update("insert into poor_signal(signal_level, receive_time) values(:sig, :time)", paramMap);
	}
	
	@Override
	public void esenseEvent(LocalDateTime time, int attentionLevel, int meditationLevel){
		log.debug(time + " attentionLevel: " + attentionLevel + " meditationLevel: " + meditationLevel);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("attention", attentionLevel);
		paramMap.put("meditation", meditationLevel);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		queues.get("esense").put(paramMap);
		jdbc.update("insert into esense(attention, meditation, receive_time) values(:attention, :meditation, :time)", paramMap);
	}
	

	@Override
	public void blinkEvent(LocalDateTime time, int blinkStrength) {
		log.debug(time + " blinkStrength: " + blinkStrength);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("blinkStrength", blinkStrength);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		queues.get("blink").put(paramMap);
		jdbc.update("insert into blink(blink_strength, receive_time) values(:blinkStrength, :time)", paramMap);
	}

	@Override
	public void eegPowerEvent(LocalDateTime time
			, int delta, int theta, int low_alpha, int high_alpha
			, int low_beta, int high_beta,int low_gamma, int mid_gamma) {
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
		paramMap.put("low_alpha", low_alpha);
		paramMap.put("high_alpha", high_alpha);
		paramMap.put("low_beta", low_beta);
		paramMap.put("high_beta", high_beta);
		paramMap.put("low_gamma", low_gamma);
		paramMap.put("mid_gamma", mid_gamma);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		queues.get("eeg").put(paramMap);
		jdbc.update("insert into eeg_power(delta, theta, low_alpha, high_alpha, low_beta"
				+ ", high_beta, low_gamma, mid_gamma"
				+ " , receive_time) values(:delta, :theta, :low_alpha, :high_alpha, :low_beta"
				+ ", :high_beta, :low_gamma, :mid_gamma"
				+ ", :time)", paramMap);
	}

	@Override
	public void rawEegEvent(LocalDateTime time, int raw, int index) {
		log.debug(time + " rawEvent Level: " + raw + " index: " + index);
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("raw", raw);
		paramMap.put("index", index);
		paramMap.put("time", time.toString().replace("T", " "));
		paramMap.put("longTime", time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
		queues.get("rawEeg").put(paramMap);
		jdbc.update("insert into raw_eeg(raw_eeg, index_, receive_time) values(:raw, :index, :time)", paramMap);
	}
	


	public void stop() {
		neuroSocket.stop();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		setup();
	}
}
