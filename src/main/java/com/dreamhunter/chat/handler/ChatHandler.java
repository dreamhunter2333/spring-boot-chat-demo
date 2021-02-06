package com.dreamhunter.chat.handler;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.json.JSONUtil;
import com.dreamhunter.chat.model.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChatHandler implements WebSocketHandler {

    // 保存当前服务器客户端信息
    private static final Map<String, WebSocketSession> userMap = new ConcurrentHashMap<>();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        // 获取用户 id
        UrlBuilder builder = UrlBuilder.ofHttp(session.getHandshakeInfo().getUri().toString(), null);
        CharSequence userIdChar = builder.getQuery().get("user_id");
        if (userIdChar == null) {
            throw new RuntimeException("用户 id 不存在");
        }
        // TODO: 使用 token
        String userId = userIdChar.toString();
        userMap.put(userId, session);

        return session.receive()
                .flatMap(webSocketMessage -> sendOthers(session, webSocketMessage))
                .then().doFinally(signal -> userMap.remove(userId)); // 用户关闭连接后删除对应连接
    }

    public Mono<Void> sendOthers(WebSocketSession session, WebSocketMessage webSocketMessage) {
        String payload = webSocketMessage.getPayloadAsText();
        Message message;
        try {
            message = objectMapper.readValue(payload, Message.class);
            String targetId = message.getTargetId();
            if (userMap.containsKey(targetId)) {
                WebSocketSession targetSession = userMap.get(targetId);
                if (targetSession != null) {
                    WebSocketMessage textMessage = targetSession.textMessage(JSONUtil.toJsonStr(message));
                    return targetSession.send(Mono.just(textMessage));
                }
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return session.send(Mono.just(session.textMessage(e.getMessage())));
        }
        return session.send(Mono.just(session.textMessage("目标用户不在线")));
    }
}
