package com.kaidash.match.bot.handlers;

import com.kaidash.match.local.UserLocal;
import com.kaidash.match.repository.UserRepository;
import com.kaidash.match.service.ButtonsService;
import com.kaidash.match.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MessageHandler {
    private final UserService userService;
    private final UserLocal userLocal;
    private final ButtonsService buttonsService;
    private final SendMessage sendMessage = new SendMessage();
    private final UserRepository userRepository;

    @Autowired
    public MessageHandler(UserService userService, UserLocal userLocal, ButtonsService buttonsService, UserRepository userRepository) {
        this.userService = userService;
        this.userLocal = userLocal;
        this.buttonsService = buttonsService;
        this.userRepository = userRepository;
    }

    private static int count = 0;

    public List<SendMessage> handle(Update update){
        List<SendMessage> responses = new ArrayList<>();

        if (update.getMessage().getText().equals("/start")){
            count = 1;
        }

        switch (count){
            case 1:
                //нужно вывести анкету
                responses.add(createTextMessage(update, "Как мне тебя назвать?"));
                createButtons(List.of(update.getMessage().getFrom().getFirstName()));
                buttonsService.hideButtons();

                userLocal.setUserId(update.getMessage().getFrom().getId());

                count = 2;
                break;
            case 2:

                userLocal.setName(update.getMessage().getText());

                responses.add(createTextMessage(update, "Сколько тебе лет"));

                buttonsService.hideButtons();

                count = 3;
                break;
            case 3:

                userLocal.setAge(Integer.parseInt(update.getMessage().getText()));

                responses.add(createTextMessage(update, "Теперь определимся с полом"));
                createButtons(List.of("Я парень", "Я девушка"));

                count = 4;
                break;
            case 4:

                userLocal.setSex(update.getMessage().getText());

                responses.add(createTextMessage(update, "Кто тебе интересен?"));
                createButtons(List.of("Девушки", "Парни"));

                count = 5;
                break;
            case 5:

                userLocal.setOppositeSex(update.getMessage().getText());

                responses.add(createTextMessage(update, "Из какого ты города?"));

                buttonsService.hideButtons();

                count = 6;
                break;
            case 6:

                userLocal.setCity(update.getMessage().getText());

                responses.add(createTextMessage(update, "Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                                                                "Это поможет лучше подобрать тебе компанию."));
                createButtons(List.of("Пропустить"));

                count = 7;
                break;
            case 7:
                if (update.getMessage().getText().equals("Пропустить")){
                    userLocal.setDescription(null);
                }else {
                    userLocal.setDescription(update.getMessage().getText());
                }

                userService.saveLocalUsers(userLocal);


                String allData = "User Id - " + userLocal.getUserId() +"\n" +
                        "User name - " + userLocal.getName() + "\n" +
                        "User age - " + userLocal.getAge() +"\n" +
                        "User city - " + userLocal.getCity() +"\n" +
                        "User sex - " + userLocal.getSex() +"\n" +
                        "User opposite sex - " + userLocal.getOppositeSex() +"\n" +
                        "User description - " + userLocal.getDescription() +"\n";
                responses.add(createTextMessage(update, allData));
                System.out.println(allData);

                break;
        }

        return responses;
    }

    private void createButtons(List<String> list){
        ReplyKeyboardMarkup replyKeyboardMarkup = buttonsService.setButtons(buttonsService.createButtons(list));
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
    }

    private SendMessage createTextMessage(Update update, String message){
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText(message);
        return sendMessage;
    }
}
