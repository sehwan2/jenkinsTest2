package com.example.springsecurityexample.Chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;

@Slf4j
@Data
@Service
public class ChatService {
    private final ObjectMapper mapper;
    private Map<String, ChatRoom> chatRooms;
    private final JdbcTemplate jdbcTemplate;
    private Map<String, ChatDTO> chatDTOs;


    @PostConstruct
    private void init(){
        chatRooms = new LinkedHashMap<>();
        chatDTOs = new LinkedHashMap<>();
    }

    public ChatRoom createRoom(String name) {
        String roomId = UUID.randomUUID().toString();

        //Builder를 사용하여 ChatRoom 을 Build
        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .name(name)
                .build();

        //랜덤 아이디와 room 정보를 Map 에 저장
        chatRooms.put(name, room);

        return room;
    }

    public List<ChatRoom> findRooms(String userId){
        // query로 내가 속한 채팅방들을 list에 저장
        String query = new StringBuilder("SELECT roomid, roomname ")
                .append("FROM chatroom ")
                .append("WHERE roomid IN (")
                                    .append("SELECT roomid ")
                                    .append("FROM participants ")
                                    .append("WHERE userid = '").append(userId)
                                    .append("');").toString();
        List<Map<String, Object>> results = jdbcTemplate.queryForList(query);

        // list에 담겨있는 채팅방들을 chatrooms에 roomid를 키로 저장
        chatRooms.clear();
        for (Map<String, Object> row : results) {
            String roomid = row.get("roomid").toString();
            String roomname = row.get("roomname").toString();
            ChatRoom chatRoom = new ChatRoom(roomid, roomname);
            chatRooms.put(roomid, chatRoom);
        }
        return new ArrayList<>(chatRooms.values());
    }
    public ChatRoom findRoom(String roomId){
        return chatRooms.get(roomId);
    }
    public void leaveRoom(String userId, String roomId) {
        String query = new StringBuilder("DELETE FROM participants ")
                .append("WHERE roomid = '").append(roomId)
                .append("' AND userId = '").append(userId)
                .append("';").toString();
        jdbcTemplate.execute(query);
    }

    public List<ChatDTO> getMessages(String sendDate, String roomId) {
        String query = new StringBuilder("SELECT roomid, message, notreadnumber, senddate, sender ")
                .append("FROM chatcontents ")
                .append("WHERE roomid = '").append(roomId).
                append("' AND senddate LIKE '").append(sendDate)
                .append("%';").toString();
        List<Map<String, Object>> results =  jdbcTemplate.queryForList(query);

        chatDTOs.clear();
        for (Map<String, Object> row : results) {
            String roomid = row.get("roomid").toString();
            String message = row.get("message").toString();
            String notreadnumber = row.get("notreadnumber").toString();
            String senddate = row.get("senddate").toString();
            String sender = row.get("sender").toString();
            ChatDTO chatDTO = new ChatDTO(ChatDTO.MessageType.TALK, roomid, sender, message, senddate, notreadnumber);
            chatDTOs.put(senddate, chatDTO);
        }
        return new ArrayList<>(chatDTOs.values());
    }

    public <T> void sendMessage(WebSocketSession session, T message){
        try{
            session.sendMessage(new TextMessage(mapper.writeValueAsString(message)));;
        }catch (IOException e){
            log.error(e.getMessage(),e);
        }
    }

}

