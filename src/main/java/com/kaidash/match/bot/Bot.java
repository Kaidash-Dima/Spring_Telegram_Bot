package com.kaidash.match.bot;

import com.kaidash.match.bot.handlers.MessageHandler;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
public class Bot extends TelegramWebhookBot {

    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.webHookPath}")
    private String path;

    private final MessageHandler messageHandler;

    @Autowired
    public Bot(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotPath() {
        return path;
    }
//    @SneakyThrows
//    @Override
//    public void onUpdateReceived(Update update) {
//        List<SendMessage> responces = new ArrayList<>();
//
//        if (update.hasMessage() && update.getMessage().hasText()){
//            responces = messageHandler.handle(update);
//        }
//
//        for (SendMessage res : responces) execute(res);

//    }

    @SneakyThrows
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
//        List<SendMessage> responces = new ArrayList<>();
        SendMessage message = new SendMessage();

//        if (update.hasMessage() && update.getMessage().hasText()){
//            responces = messageHandler.handle(update);
//        }
//        SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
//                .text("Подождем пока кто-то увидит твою анкету").build();

        message.setChatId(String.valueOf(update.getMessage().getChatId()));
        message.setText("Hello");

        return message;
//        for (SendMessage res : responces) execute(res);
//        return null;
    }
}
