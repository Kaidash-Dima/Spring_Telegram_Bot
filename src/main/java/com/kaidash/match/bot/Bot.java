package com.kaidash.match.bot;

//import com.kaidash.match.bot.handlers.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@EnableScheduling
public class Bot extends TelegramWebhookBot {

    @Value("${bot.name}")
    private String name;

    @Value("${bot.token}")
    private String token;

    @Value("${bot.webHookPath}")
    private String path;

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


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId())).text(update.getMessage().getText()).build();
    }

}
