package com.example.springsecurityexample.DbManager;

import lombok.extern.slf4j.Slf4j;
import com.example.springsecurityexample.Chat.ChatDTO;
import com.example.springsecurityexample.Chat.ChatRoom;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Slf4j
@Component
public class PostgreSQLConfig {
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public PostgreSQLConfig(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertData(ChatDTO chatDTO, ChatRoom chatRoom) {
        String query = "INSERT INTO chatcontents(roomid, message, notreadnumber, senddate, sender) VALUES (\'{" +
                chatDTO.getRoomId() + "}\', \'{" +
                chatDTO.getMessage() + "}\', " +
                chatDTO.getNotReadNumber() + ", \'{" +
                chatDTO.getSenddate() + "}\', \'{" +
                chatDTO.getSender() +"}\');";

        log.info("query = [{}]", query);

        jdbcTemplate.execute(query);
        log.info("insertData entered");
    }
}
