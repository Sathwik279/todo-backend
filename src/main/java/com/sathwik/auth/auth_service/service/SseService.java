package com.sathwik.auth.auth_service.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SseService {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String userId){
        //create an emitter that stays open for 30 min
        SseEmitter emitter = new SseEmitter(30*60*1000L);
        emitters.put(userId,emitter);

        emitter.onCompletion(()->emitters.remove(userId));
        emitter.onTimeout(()->emitters.remove(userId));
        emitter.onError((e)->emitters.remove(userId));
        return emitter;
    }

    public void sendAiUpdateToUser(String userId,String todoId,String aiContent){
        SseEmitter emitter = emitters.get(userId);
        if(emitter !=null){
            try{
                Map<String,String> payload = Map.of(
                        "todoId",todoId,
                        "aiContent",aiContent
                );
                emitter.send(SseEmitter.event().name("AI_UPDATE").data(payload));
            }catch(IOException e){
                emitters.remove(userId);
            }
        }
    }
}
