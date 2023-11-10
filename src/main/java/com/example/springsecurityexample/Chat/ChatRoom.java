package com.example.springsecurityexample.Chat;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashSet;
import java.util.Set;

@Data
@Slf4j
public class ChatRoom {

    private String roomId;//채팅방 아이디
    private String name;//채팅방 이름
    private Set<WebSocketSession> sessions = new HashSet<>();

    @Builder
    public ChatRoom(String roomId,String name){
        this.roomId = roomId;
        this.name = name;
    }
    public void handleAction(WebSocketSession session, ChatDTO message, ChatService service) {
        // MessageType == ENTER ; 채팅방에 입장하면 "{UserName}님이 입장했습니다" 출력
        // MessageType == TALK ; 채팅방에 메세지 출력
        // MessageType == LEAVE ; 채팅방에 "{UserName}님이 나갔습니다" 출력

        if(message.getType().equals(ChatDTO.MessageType.ENTER)){
            sessions.add(session);
            message.setMessage(message.getSender() + " 님이 입장했습니다.");
            sendMessage(message,service);
        } else if (message.getType().equals(ChatDTO.MessageType.TALK)) {
            message.setMessage(message.getMessage());
            sendMessage(message,service);
        } else if (message.getType().equals(ChatDTO.MessageType.LEAVE)) {
            sessions.remove(session);
            message.setMessage(message.getSender() + " 님이 나갔습니다.");
            sendMessage(message, service);
        }
    }

    public <T> void sendMessage(T message, ChatService service){
        // 병렬처리
        sessions.parallelStream().forEach(sessions -> service.sendMessage(sessions,message));
    }
}
