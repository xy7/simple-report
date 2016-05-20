package com.seasun.data.simple_report;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.seasun.data.simple_report.base.MaxDropQueue;
import com.seasun.data.simple_report.collect.DataCollectApp;

@SpringBootApplication
public class SimpleReportApp {
	
	@Bean(name = "signalQueue")
	public MaxDropQueue<Map<String, Object>> getSignalQueue(){
		return new MaxDropQueue<Map<String, Object>>(2);
	}
	
	@Bean(name = "rawEegQueue")
	public MaxDropQueue<Map<String, Object>> getRawEegQueue(){
		return new MaxDropQueue<Map<String, Object>>(100);
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = SpringApplication.run(SimpleReportApp.class, args);
		
		//test
//		DataCollectApp dca = ctx.getBean(DataCollectApp.class);
//		
//		int i = 0;
//		while(true){
//			dca.rawEegEvent(LocalDateTime.now(), (++i)%100, 1);
//			Thread.sleep(1000);
//		}
	}

}