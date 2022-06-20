package com.kaidash.match.bot;

import com.kaidash.match.bot.handlers.MessageHandler;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

@Component
@EnableScheduling
public class Bot extends TelegramWebhookBot {

    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.webHookPath}")
    private String path;

    private static final String PORT = System.getenv("PORT");

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

    @SneakyThrows
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {

        try (ServerSocket serverSocket = new ServerSocket(Integer.parseInt(PORT))) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return messageHandler.handle(update);
    }

    @SneakyThrows
    public void listSendMessage(List<SendMessage> messageList) {
        for(SendMessage message : messageList){
            execute(message);
        }
    }

}
