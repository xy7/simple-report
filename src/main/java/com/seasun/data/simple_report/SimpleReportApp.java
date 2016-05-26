package com.seasun.data.simple_report;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.seasun.data.simple_report.base.MaxDropQueue;
import com.seasun.data.simple_report.collect.DataCollectApp;

@SpringBootApplication
public class SimpleReportApp {

	@Bean(name = "allTypeQueues")
	public Map<String, MaxDropQueue<Map<String, Object>>> getAllTypeQueues() {
		Map<String, MaxDropQueue<Map<String, Object>>> queues = new HashMap<>(5);
		queues.put("signal", new MaxDropQueue<Map<String, Object>>(1));
		queues.put("rawEeg", new MaxDropQueue<Map<String, Object>>(50));
		queues.put("eeg", new MaxDropQueue<Map<String, Object>>(1));
		queues.put("esense", new MaxDropQueue<Map<String, Object>>(1));
		queues.put("blink", new MaxDropQueue<Map<String, Object>>(1));
		return queues;
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = SpringApplication.run(SimpleReportApp.class, args);

		// test
//		DataCollectApp dca = ctx.getBean(DataCollectApp.class);
//
//		int i = 0;
//		int j = 0;
//		while (true) {
//			i = (++i) % 100;
//			int rawEeg = i;
//			dca.rawEegEvent(LocalDateTime.now(), rawEeg, 1);
//			//if (i == 1) {
//				j = (++j) % 100;
//				int att = j;
//				int med = 100 - j;
//				dca.esenseEvent(LocalDateTime.now(), att, med);
//			//}
//			Thread.sleep(20);
//		}
	}

}
