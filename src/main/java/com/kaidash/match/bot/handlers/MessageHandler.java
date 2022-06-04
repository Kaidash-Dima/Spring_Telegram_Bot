package com.kaidash.match.bot.handlers;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class MessageHandler {
    private static int count = 0;
    public List<SendMessage> handle(Update update){
        List<SendMessage> responses = new ArrayList<>();

        if (update.getMessage().getText().equals("/start")){
            count = 1;
        }

        switch (count){
            case 1:
                //выводим анкету
                System.out.println("Enter case 1");
                count = 2;
                break;
            case 2:
                System.out.println("Enter case 2");
                break;
        }

        return responses;
    }
}
