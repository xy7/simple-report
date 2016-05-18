package com.seasun.data.simple_report.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.alibaba.fastjson.JSONObject;
import com.seasun.data.simple_report.base.MaxDropQueue;

@Controller
public class WebsocketController {
	
	@RequestMapping("/websocket")
	public String home(Map<String, Object> model) {
		return "websocket_welcome";
    }
	
	@Autowired
	@Qualifier("rawEegQueue")
	public MaxDropQueue<Map<String, Object>> rawEegQueue;
	
	@Autowired
	private SimpMessagingTemplate template;

	@MessageMapping("/rawEeg")
    //@SendTo("/topic/greetings")
    public void rawEeg(HelloMessage message) throws Exception {
        
        
        while(true){
        	Map<String, Object> paramMap = rawEegQueue.take();

        	JSONObject json = new JSONObject(paramMap);
        	System.out.println("rawEegQueue: " + json);
            template.convertAndSend("/realDataResp/rawEeg", json);
            
            Thread.sleep(1000); // simulated delay
        }
    }
	
	public static class Greeting {

	    private String content;

	    public Greeting(String content) {
	        this.content = content;
	    }

	    public String getContent() {
	        return content;
	    }

	}
	
	public static class HelloMessage {

	    private String name;

	    public String getName() {
	        return name;
	    }

	}
}
