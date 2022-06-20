package com.kaidash.match.bot.handlers;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class MessageHandler {

    public SendMessage handle(Update update){

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId())).text(update.getMessage().getText()).build();
    }
}
