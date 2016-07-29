package com.seasun.data.simple_report;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import com.seasun.data.simple_report.base.EventType;
import com.seasun.data.simple_report.base.MaxDropQueue;
import com.seasun.data.simple_report.collect.ComRead;
import com.seasun.data.simple_report.collect.JsonComRead;
import com.seasun.data.simple_report.collect.ThinkGearSocket;

@SpringBootApplication
public class SimpleReportApp {
	
	/*@Bean(name = "allTypeQueues") //old impl
	public Map<String, MaxDropQueue<Map<String, Object>>> getAllTypeQueues() {
		Map<String, MaxDropQueue<Map<String, Object>>> queues = new HashMap<>(5);
		queues.put("signal", new MaxDropQueue<Map<String, Object>>(1));
		queues.put("rawEeg", new MaxDropQueue<Map<String, Object>>(50));
		queues.put("eeg", new MaxDropQueue<Map<String, Object>>(1));
		queues.put("esense", new MaxDropQueue<Map<String, Object>>(1));
		queues.put("blink", new MaxDropQueue<Map<String, Object>>(1));
		return queues;
	}*/
	@Bean(name = "realtimeQueues") //new impl
	public Map<EventType, MaxDropQueue<Map<String, Object>>> getRealtimeQueues() {
		Map<EventType, MaxDropQueue<Map<String, Object>>> queues = new HashMap<>(5);
		for(EventType e:EventType.values()){
			queues.put(e, new MaxDropQueue<Map<String, Object>>(50));
		}
		
		return queues;
	}
	
	@Bean(name = "dbQueues") //后续考虑替换为durableQueue以保证安全
	public Map<EventType, BlockingQueue<Map<String, Object>>> getAllTypeQueuesDb() {
		Map<EventType, BlockingQueue<Map<String, Object>>> jsonQueues = new HashMap<>(5);
		
		for(EventType e:EventType.values()){
			if(e == EventType.RAW_EEG)
				jsonQueues.put(e, new ArrayBlockingQueue<>(4000));
			else 
				jsonQueues.put(e, new ArrayBlockingQueue<>(50));
		}

		return jsonQueues;
	}

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = SpringApplication.run(SimpleReportApp.class, args);
		//ctx.getBean(ThinkGearSocket.class).start();//receive data from socket which sended from TGC
		//ctx.getBean(ComRead.class).start();
		ctx.getBean(JsonComRead.class).start();//receive data from com3 directly

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
