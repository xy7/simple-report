package com.seasun.data.simple_report;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		//这里设置的simple broker是指可以订阅的地址，也就是服务器可以发送的地址
		config.enableSimpleBroker("/realDataResp");
		//设置了一个应用程序访问地址的前缀，可能是为了和其他的普通请求区分开吧
		config.setApplicationDestinationPrefixes("/realDataReq");
//		config.enableStompBrokerRelay("/realDataResp")
//			.setSystemHeartbeatReceiveInterval(250)
//			.setSystemHeartbeatSendInterval(250);
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		//注册消息连接点，这样在网页中就可以通过websocket连接上服务了  
		registry.addEndpoint("/realDataEndPoint").withSockJS();
	}

}
