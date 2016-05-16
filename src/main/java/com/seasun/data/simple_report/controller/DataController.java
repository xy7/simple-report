package com.seasun.data.simple_report.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DataController {
	
	@Autowired
	private NamedParameterJdbcTemplate jdbc;

	@RequestMapping("/index")
	public String home(Map<String, Object> model) {
		model.put("name", "mind ware data figure");
		return "welcome";
    }
	
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
			@RequestParam(value = "date", required = false) String date){
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("date", date);
		String sql = "select * from raw_eeg where DATE_FORMAT(receive_time, '%Y-%m-%d') = :date "
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
		return res;
	}
	
	//获取最近的数据，date为最后一次查询到的最新时间
	@RequestMapping("/getNewData")
	public @ResponseBody Map<String, Object> getNewData(@RequestParam(value = "date", required = false) String date){
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("date", date);
		String sql = "select * from raw_eeg where receive_time > :date order by receive_time, index_";
		List<Map<String, Object>> dbRes = jdbc.queryForList(sql, paramMap);
		Map<String, Object> res = new HashMap<>();
		List<String> x = new ArrayList<>();
		List<Double> y = new ArrayList<>();
		int size = dbRes.size();
		for(int i=0;i<size;i++){
			Map<String, Object> e = dbRes.get(i);
			x.add(e.getOrDefault("receive_time", "0").toString());
			y.add(Double.parseDouble(e.getOrDefault("raw_eeg", "0").toString()));
			if(i == size - 1)
				res.put("fresh_time", e.getOrDefault("receive_time", "0").toString());
		}
		
		res.put("x", x);
		res.put("y", y);
		
		return res;
	}
	
	@RequestMapping("/getDataTest")
	public @ResponseBody String getDataTest(@RequestParam(value = "name", required = false) String name){
		return name + "----123";
	}
	
}
