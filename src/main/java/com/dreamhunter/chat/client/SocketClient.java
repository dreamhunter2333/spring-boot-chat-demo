package com.dreamhunter.chat.client;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.FluxSink;

@Slf4j
@Data
public class SocketClient {

    private String streamId;
    private FluxSink<WebSocketMessage> sink;
    private WebSocketSession session;

    public SocketClient(FluxSink<WebSocketMessage> sink, WebSocketSession session) {
        this.sink = sink;
        this.session = session;
    }

    public SocketClient(String streamId, FluxSink<WebSocketMessage> sink, WebSocketSession session) {
        this.streamId = streamId;
        this.sink = sink;
        this.session = session;
    }

    public void sendData(String data) {
        sink.next(session.textMessage(data));
    }
}
