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
public class test {
	
	@Autowired
	private NamedParameterJdbcTemplate jdbc;

	@RequestMapping("/index")
	public String home(Map<String, Object> model) {
		model.put("time", new Date());
		model.put("name", "jack");
		
		Map<String, Object> paramMap = new HashMap<>();
		
		return "welcome";
    }
	
	@RequestMapping("/getData")
	public @ResponseBody Map<String, Object> getData(@RequestParam(value = "date", required = false) String date){
		Map<String, Object> paramMap = new HashMap<>();
		paramMap.put("date", date);
		String sql = "select * from raw_eeg where DATE_FORMAT(receive_time, '%Y-%m-%d') = :date order by receive_time, index_";
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
	
	@RequestMapping("/getDataTest")
	public @ResponseBody String getDataTest(@RequestParam(value = "name", required = false) String name){
		return name + "----123";
	}
	
}
