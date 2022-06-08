package com.kaidash.match.bot.handlers;

import com.kaidash.match.entity.User;
import com.kaidash.match.local.UserLocal;
import com.kaidash.match.service.ButtonsService;
import com.kaidash.match.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageHandler {
    private final UserService userService;
    private final UserLocal userLocal;
    private final ButtonsService buttonsService;
    private final SendMessage sendMessage = new SendMessage();

    @Autowired
    public MessageHandler(UserService userService, UserLocal userLocal, ButtonsService buttonsService) {
        this.userService = userService;
        this.userLocal = userLocal;
        this.buttonsService = buttonsService;
    }

    private static int count = 0;
    private static int firstCount = 0;

    public List<SendMessage> handle(Update update){
        List<SendMessage> responses = new ArrayList<>();
        String menuChoice = update.getMessage().getText();

        if (userService.checkByUserId(update.getMessage().getFrom().getId()) && menuChoice.equals("/start")){
            return myProfile(update);
        }else if (!userService.checkByUserId(update.getMessage().getFrom().getId())){
            firstCount = 1;
        }

        switch (menuChoice){
            case "1":
            case "/start":
                count = 1;
                break;
            case "2":
                break;
        }

        if (count != 0){
            responses = changeMyProfile(update);
        }

        return responses;
    }

    private String findUser(Update update){
        User user = userService.findByUserId(update.getMessage().getFrom().getId());
        return user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription();
    }

    private List<SendMessage> myProfile(Update update){
        List<SendMessage> responses = new ArrayList<>();

        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId())).text(findUser(update)).build());

        responses.add(createTextMessage(update, "1. Заполнить анкету\n2. Смотреть анкеты"));

        createButtons(List.of("1", "2"));

        return responses;
    }

    private List<SendMessage> changeMyProfile(Update update){
        List<SendMessage> responses = new ArrayList<>();
        User user = new User();

        if (userService.checkByUserId(update.getMessage().getFrom().getId())){
            user = userService.findByUserId(update.getMessage().getFrom().getId());
        }

        switch (count){

            case 1:

                responses.add(createTextMessage(update, "Как мне тебя назвать?"));

                if (firstCount == 1){
                    createButtons(List.of(update.getMessage().getFrom().getFirstName()));
                }else{
                    createButtons(List.of(user.getName()));
                }

                userLocal.setUserId(update.getMessage().getFrom().getId());

                count = 2;
                break;
            case 2:

                userLocal.setName(update.getMessage().getText());

                responses.add(createTextMessage(update, "Сколько тебе лет"));

                if (firstCount == 1){
                    // скрыть или удалить кнопку
                    // или вывести клаву цифер
                }else{
                    createButtons(List.of(String.valueOf(user.getAge())));
                }

                count = 3;
                break;
            case 3:

                userLocal.setAge(Integer.parseInt(update.getMessage().getText()));

                responses.add(createTextMessage(update, "Теперь определимся с полом"));
                createButtons(List.of("Я парень", "Я девушка"));

                count = 4;
                break;
            case 4:

                if (update.getMessage().getText().equals("Я парень")) {
                    userLocal.setSex(false);
                }else if (update.getMessage().getText().equals("Я девушка")){
                    userLocal.setSex(true);
                }

                responses.add(createTextMessage(update, "Кто тебе интересен?"));
                createButtons(List.of("Девушки", "Парни"));

                count = 5;
                break;
            case 5:

                if (update.getMessage().getText().equals("Девушки")) {
                    userLocal.setOppositeSex(true);
                }else if (update.getMessage().getText().equals("Парни")){
                    userLocal.setOppositeSex(false);
                }

                responses.add(createTextMessage(update, "Из какого ты города?"));

                if (firstCount == 1){
                    // скрыть или удалить кнопку
                }else{
                    createButtons(List.of(user.getCity()));
                }

                count = 6;
                break;
            case 6:

                userLocal.setCity(update.getMessage().getText());

                responses.add(createTextMessage(update, "Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                        "Это поможет лучше подобрать тебе компанию."));

                if (firstCount == 1){
                    createButtons(List.of("Пропустить"));
                }else{
                    createButtons(List.of("Пропустить", "Оставить"));
                }


                count = 7;
                break;
            case 7:
                if (update.getMessage().getText().equals("Пропустить")){
                    userLocal.setDescription("");
                }else if (update.getMessage().getText().equals("Оставить")){
                    userLocal.setDescription(user.getDescription());
                }else{
                    userLocal.setDescription(update.getMessage().getText());
                }

                if (firstCount == 1) {
                    userService.saveLocalUsers(userLocal);
                }else{
                    userService.updateUser(userLocal);
                }

//------------------------------------------------------------------------------------------------------------------
                String allData = "User Id - " + userLocal.getUserId() +"\n" +
                        "User name - " + userLocal.getName() + "\n" +
                        "User age - " + userLocal.getAge() +"\n" +
                        "User city - " + userLocal.getCity() +"\n" +
                        "User sex - " + userLocal.isSex() +"\n" +
                        "User opposite sex - " + userLocal.isOppositeSex() +"\n" +
                        "User description - " + userLocal.getDescription() +"\n";
                System.out.println(allData);
//------------------------------------------------------------------------------------------------------------------

                responses = myProfile(update);

                count = 0;
                firstCount = 0;

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
