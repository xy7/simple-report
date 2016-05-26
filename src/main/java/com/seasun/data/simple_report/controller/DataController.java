package com.seasun.data.simple_report.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;
import com.seasun.data.simple_report.base.MaxDropQueue;

@Controller
public class DataController {
	private static final Log log = LogFactory.getLog(DataController.class);
	
	//页面请求
	@RequestMapping("/history")
	public String index(Map<String, Object> model) {
		model.put("name", "mind ware data figure");
		return "history";
    }
	
	@RequestMapping("/realtime")
	public String home(Map<String, Object> model) {
		return "realtime";
    }
	
	//历史数据
	@Autowired
	private NamedParameterJdbcTemplate jdbc;

	@RequestMapping("/getEsenceData")
	public @ResponseBody Map<String, Object> getEsenceData(
			@RequestParam(value = "start", required = false) String start
			, @RequestParam(value = "end", required = false) String end){

		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("start", start);
		paramMap.put("end", end);
		String sql = "select * from esense where receive_time >= :start "
				+ " and receive_time <= :end"
				+ " order by receive_time";
		List<Map<String, Object>> dbRes = jdbc.queryForList(sql, paramMap);
		Map<String, Object> res = new HashMap<>();
		List<String> x = new ArrayList<>();
		List<Integer> attention = new ArrayList<>();
		List<Integer> meditation = new ArrayList<>();
		for(Map<String, Object> e:dbRes){
			x.add(e.getOrDefault("receive_time", "0").toString());
			attention.add(Integer.parseInt(e.getOrDefault("attention", "0").toString()));
			meditation.add(Integer.parseInt(e.getOrDefault("meditation", "0").toString()));
		}
		res.put("x", x);
		res.put("attention", attention);
		res.put("meditation", meditation);
		res.put("fresh_time", LocalDateTime.now().toString());
		res.put("code", 0);
		return res;
	}
	
	@RequestMapping("/getRawEegData")
	public @ResponseBody Map<String, Object> getRawEegData(
			@RequestParam(value = "start", required = false) String start
			, @RequestParam(value = "end", required = false) String end){
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("start", start);
		paramMap.put("end", end);
		String sql = "select * from raw_eeg where receive_time >= :start "
				+ " and receive_time <= :end"
				+ " order by receive_time, index_";
		List<Map<String, Object>> dbRes = jdbc.queryForList(sql, paramMap);
		Map<String, Object> res = new HashMap<>();
		List<String> x = new ArrayList<>();
		List<Double> y = new ArrayList<>();
		for(Map<String, Object> e:dbRes){
			x.add(e.getOrDefault("receive_time", "0").toString());
			y.add(Double.parseDouble(e.getOrDefault("raw_eeg", "0").toString()));
		}
		res.put("x", x);
		res.put("y", y);
		res.put("fresh_time", LocalDateTime.now().toString());
		res.put("code", 0);
		return res;
	}
	
	
	//实时数据
	@Resource(name="allTypeQueues")
	public Map<String, MaxDropQueue<Map<String, Object> > > queues;
	
	@Autowired
	private SimpMessagingTemplate template;

	@MessageMapping("/type")
    //@SendTo("/topic/greetings")
    public void getRealtimeData(@RequestParam String type) throws Exception {

		MaxDropQueue<Map<String, Object>> queue = queues.get(type);
		log.info("getRealtimeData type: " + type);
		if(queue == null){
			log.error("queue is null");
			return;
		}
		log.info("queue size: " + queue.size());

        while(true){
        	Map<String, Object> paramMap = queue.take();

        	JSONObject json = new JSONObject(paramMap);
        	log.debug(type + " queue out: " + json);
            template.convertAndSend("/realDataResp/" + type, json);
            
            //Thread.sleep(1000); // simulated delay
        }
    }
	
}
