package com.virtualoffice.backend.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration

/* 
 بتشغل الويب سوكيت و تخليك تقدر تستخدم ال STOMP
*/
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer
{


    private WebSocketAuthInterceptor webSocketAuthInterceptor;
    private String[] allowedOrigins;

    public WebSocketConfig(WebSocketAuthInterceptor webSocketAuthInterceptor, @Value("${app.allowed.origins}") String allowedOrigins)
    {
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
        this.allowedOrigins = allowedOrigins.split(",");
    }



    @Override
    public void configureMessageBroker(MessageBrokerRegistry config)
    {
        /*

         شغل 
         Message Broker
         و اي 
         destination 
         بيبدأ بـ  
         /topic
         يعتبر مكان العميل يقدر يشترك فيه ويستقبل منه رسائل

         زي مثلا
         /topic/room/5
         ده عنوان لما السيرفر يبعت عليه اي حاجه 
         كل الناس اللي ف العنوان ده يشوفو الرساله ديه

        */
        config.enableSimpleBroker("/topic");

        // ده العكس ده المسار اللي يستقبل الرسايل من الكلاينت 
        config.setApplicationDestinationPrefixes("/app");
    }




    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry)
    {
        // ده اتصال الفرونت بال ويب سوكيت نفسه عشان نعرف نبعت رسايل و نستقبلها
        registry.addEndpoint("/ws").setAllowedOrigins(allowedOrigins).withSockJS();
    }


    

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration)
    {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
