package com.example.springsecurityexample.Chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@Getter @Setter
public class ChatDTO {
    public enum MessageType {
        ENTER, TALK, LEAVE
    }
    private MessageType type;
    private String roomId;
    private String message;
//    private int notReadNumber;
    private String notReadNumber;
    private String senddate;
    private String sender;

    @JsonCreator
    public ChatDTO(@JsonProperty("type") MessageType type, @JsonProperty("roomId") String roomId, @JsonProperty("sender") String sender, @JsonProperty("message") String message, @JsonProperty("sendDate") String sendDate, @JsonProperty("notReadNumber") String notReadNumber) {
        this.type = type;
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.senddate = sendDate;
        this.notReadNumber = notReadNumber;
    }
}
