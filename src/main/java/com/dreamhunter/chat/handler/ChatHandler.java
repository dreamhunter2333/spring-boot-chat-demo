package com.dreamhunter.chat.handler;

import com.dreamhunter.chat.client.SocketClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatHandler implements WebSocketHandler {
    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    StreamMessageListenerContainer streamContainer;
    @Autowired
    MessageListener streamListener;

    // 保存当前服务器客户端信息
    static Map<String, SocketClient> clients = new ConcurrentHashMap<>();

    // 会话，相当于websocket房间号
    List<String> streams = new ArrayList<>();

    @Value("{server.port}")
    String port;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        // TODO 从session中获取streamId
//        session.getHandshakeInfo()
        String streamId = "111";

        String id = session.getId();
        //  出站
        Mono<Void> output = session.send(
                Flux.create(sink -> clients.put(id, new SocketClient(streamId, sink,session)))
        );
        //入站
        Mono<Void> input = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(msg ->  msg)
                .doOnNext(msg->sendOthers(streamId,id,msg)).then();

        //初始化redis消息

        // groupID相当于一台服务器。
        if(streamContainer.isRunning()&&streams.contains(streamId)){
        }else {

            Map<String, String> msg = new HashMap<>();
            msg.put("name", id+" has join!");
            // 插入一条记录
            stringRedisTemplate.opsForStream().add(streamId,msg);
//            stringRedisTemplate.opsForStream().destroyGroup(streamId, port);
//            stringRedisTemplate.opsForStream().createGroup(streamId,port);

            streamContainer.receive(
                    Consumer.from(port, "consumer-1"),
                    StreamOffset.create(streamId, ReadOffset.lastConsumed()),
                    streamListener
            );
            streamContainer.start();
            streams.add(streamId);
        }

        // 合并
        return  Mono.zip(input, output).then();
    }

    public static Map<String, SocketClient> getSessions() {
        return clients;
    }

    public void sendOthers(String streamId,String id,String message) {
        Map<String, String> msg = new HashMap<>();
        msg.put(id, message);
        stringRedisTemplate.opsForStream().add(streamId, msg);
    }
}
