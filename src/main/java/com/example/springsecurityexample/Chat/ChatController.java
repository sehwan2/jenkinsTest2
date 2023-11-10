package com.example.springsecurityexample.Chat;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

    private final ChatService service;

    @PostMapping
    public ChatRoom createRoom(@RequestParam String name) {
        return service.createRoom(name);
    }

    @GetMapping(path="/rooms/{userName}")
    public List<ChatRoom> findRooms(@PathVariable("userName") String userName) {
        return service.findRooms(userName);
    }

    @GetMapping(path="/rooms/{userName}/roomname/{roomId}")
    public ChatRoom findRoom(@PathVariable("roomId") String roomId) {
        return service.findRoom(roomId);
    }

    @GetMapping(path="/message/{sendDate}/{roomId}")
    public List<ChatDTO> getMessages(@PathVariable("sendDate") String sendDate, @PathVariable("roomId") String roomId) {
        return service.getMessages(sendDate, roomId);
    }

    @DeleteMapping(path="/room/{userId}/{roomId}")
    public void leaveRoom(@PathVariable("userId") String userId, @PathVariable("roomId") String roomId) {
        service.leaveRoom(userId, roomId);
    }
}
