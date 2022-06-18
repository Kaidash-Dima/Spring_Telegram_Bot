package com.kaidash.match.bot.handlers;

import com.kaidash.match.bot.Bot;
import com.kaidash.match.entity.ChatStatus;
import com.kaidash.match.entity.Language;
import com.kaidash.match.entity.Match;
import com.kaidash.match.entity.User;
import com.kaidash.match.service.ButtonsService;
import com.kaidash.match.service.MatchService;
import com.kaidash.match.service.UserService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class MenuHandler {

    private final Bot bot;
    private final MatchService matchService;
    private final UserService userService;
    private final ButtonsService buttonsService;

    public MenuHandler(@Lazy Bot bot, MatchService matchService, UserService userService, ButtonsService buttonsService) {
        this.bot = bot;
        this.matchService = matchService;
        this.userService = userService;
        this.buttonsService = buttonsService;
    }

    public void setLanguageProfile(Update update, User user){
        if (update.getMessage().getText().equals("\uD83C\uDDFA\uD83C\uDDE6")){
            user.setLanguage(Language.UKRAINIAN);
        }else if (update.getMessage().getText().equals("\uD83C\uDDF7\uD83C\uDDFA")){
            user.setLanguage(Language.RUSSIAN);
        }else if (update.getMessage().getText().equals("\uD83C\uDDFA\uD83C\uDDF8")){
            user.setLanguage(Language.ENGLISH);
        }
        userService.updateUser(user);
    }

    public SendMessage displayLanguageQuestion (Update update, User user){

        SendMessage sendMessage = new SendMessage();

        if (user.getLanguage() == Language.RUSSIAN || user.getLanguage() == null) {
            sendMessage = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("На каком языке будем общаться?")
                    .replyMarkup(createButtons(List.of("\uD83C\uDDFA\uD83C\uDDE6", "\uD83C\uDDF7\uD83C\uDDFA", "\uD83C\uDDFA\uD83C\uDDF8")))
                    .build();
        } else if (user.getLanguage() == Language.UKRAINIAN) {
            sendMessage = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("На якій мові будемо спілкуватись?")
                    .replyMarkup(createButtons(List.of("\uD83C\uDDFA\uD83C\uDDE6", "\uD83C\uDDF7\uD83C\uDDFA", "\uD83C\uDDFA\uD83C\uDDF8")))
                    .build();
        } else if (user.getLanguage() == Language.ENGLISH) {
            sendMessage = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("What language should we talk?")
                    .replyMarkup(createButtons(List.of("\uD83C\uDDFA\uD83C\uDDE6", "\uD83C\uDDF7\uD83C\uDDFA", "\uD83C\uDDFA\uD83C\uDDF8")))
                    .build();
        }
        user.setChatStatus(ChatStatus.LANGUAGE);
        userService.updateUser(user);

        return sendMessage;
    }

    public void determineStatus (Update update, User user){

        if (update.getMessage().getText().equals("1")){
            user.setChatStatus(ChatStatus.START);
        } else if (update.getMessage().getText().equals("2")) {
            user.setChatStatus(ChatStatus.DISLIKE);
        }else if(update.getMessage().getText().equals("❤")){
            user.setChatStatus(ChatStatus.LIKE);
        }else if(update.getMessage().getText().equals("\uD83D\uDC4E")) {
            user.setChatStatus(ChatStatus.DISLIKE);
        }else if(update.getMessage().getText().equals("\uD83D\uDCA4")){
            user.setChatStatus(ChatStatus.SLEEP);
        }else if(update.getMessage().getText().equals("3")){
            user.setChatStatus(ChatStatus.SHOW_MATCHES);
        }else if (update.getMessage().getText().equals("4")){
            user.setChatStatus(ChatStatus.REGISTRATION);
        }

        userService.updateUser(user);
    }

    public void setDescriptionProfile(Update update, User user){

        String message = update.getMessage().getText();

        switch (user.getLanguage()){

            case RUSSIAN:
                if (message.equals("Пропустить")) {
                    user.setDescription("");
                } else if (message.equals("Оставить")) {
                    user.setDescription(user.getDescription());
                } else {
                    user.setDescription(message);
                }
                break;

            case UKRAINIAN:

                if (message.equals("Пропустити")) {
                    user.setDescription("");
                } else if (message.equals("Залишити")) {
                    user.setDescription(user.getDescription());
                } else {
                    user.setDescription(message);
                }
                break;

            case ENGLISH:

                if (message.equals("Skip")) {
                    user.setDescription("");
                } else if (message.equals("Leave")) {
                    user.setDescription(user.getDescription());
                } else {
                    user.setDescription(message);
                }
                break;
        }

        userService.updateUser(user);
    }

    public SendMessage setCiteProfile(Update update, User user){

        SendMessage message = new SendMessage();

        user.setCity(update.getMessage().getText());

        user.setChatStatus(ChatStatus.ENTER_DESCRIPTION);
        userService.updateUser(user);

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (user.getDescription() == null) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                                    "Это поможет лучше подобрать тебе компанию.")
                            .replyMarkup(createButtons(List.of("Пропустить"))).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                                    "Это поможет лучше подобрать тебе компанию.")
                            .replyMarkup(createButtons(List.of("Пропустить", "Оставить"))).build();
                }
                break;

            case UKRAINIAN:

                if (user.getDescription() == null) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Розкажи про себе та кого хочеш знайти, чим пропонуєш зайнятися. " +
                                    "Це допоможе краще підібрати тобі компанію.")
                            .replyMarkup(createButtons(List.of("Пропустить"))).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Розкажи про себе та кого хочеш знайти, чим пропонуєш зайнятися. " +
                                    "Це допоможе краще підібрати тобі компанію.")
                            .replyMarkup(createButtons(List.of("Пропустити", "Залишити"))).build();
                }
                break;
            case ENGLISH:

                if (user.getDescription() == null) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Tell us about yourself and who you want to find, what you propose to do. " +
                                    "This will help you find the best company for you.")
                            .replyMarkup(createButtons(List.of("Skip"))).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Tell us about yourself and who you want to find, what you propose to do. " +
                                    "This will help you find the best company for you.")
                            .replyMarkup(createButtons(List.of("Skip", "Leave"))).build();
                }
                break;
        }
        return message;
    }

    public SendMessage setOppositeSexProfile(Update update, User user){
        SendMessage message = new SendMessage();

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (update.getMessage().getText().equals("Девушки") || update.getMessage().getText().equals("Парни")) {

                    if (update.getMessage().getText().equals("Девушки")) {
                        user.setOppositeSex(1);
                    } else if (update.getMessage().getText().equals("Парни")) {
                        user.setOppositeSex(0);
                    }

                    user.setChatStatus(ChatStatus.ENTER_CITY);
                    userService.updateUser(user);

                    if (user.getCity() == null) {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("Из какого ты города?").replyMarkup(new ReplyKeyboardRemove(true)).build();
                    } else {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("Из какого ты города?").replyMarkup(createButtons(List.of(user.getCity()))).build();
                    }

                } else {
                    return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Выбери из того что есть на кнопках \uD83D\uDC47").build();
                }
                break;

            case UKRAINIAN:

                if (update.getMessage().getText().equals("Дівчата") || update.getMessage().getText().equals("Хлопці")) {

                    if (update.getMessage().getText().equals("Дівчата")) {
                        user.setOppositeSex(1);
                    } else if (update.getMessage().getText().equals("Хлопці")) {
                        user.setOppositeSex(0);
                    }

                    user.setChatStatus(ChatStatus.ENTER_CITY);
                    userService.updateUser(user);

                    if (user.getCity() == null) {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("З якого ти міста?").replyMarkup(new ReplyKeyboardRemove(true)).build();
                    } else {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("З якого ти міста?").replyMarkup(createButtons(List.of(user.getCity()))).build();
                    }

                } else {
                    return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Вибери з того, що є на кнопках \uD83D\uDC47").build();
                }

                break;

            case ENGLISH:

                if (update.getMessage().getText().equals("Girls") || update.getMessage().getText().equals("Guys")) {

                    if (update.getMessage().getText().equals("Girls")) {
                        user.setOppositeSex(1);
                    } else if (update.getMessage().getText().equals("Guys")) {
                        user.setOppositeSex(0);
                    }

                    user.setChatStatus(ChatStatus.ENTER_CITY);
                    userService.updateUser(user);

                    if (user.getCity() == null) {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("What city are you from?").replyMarkup(new ReplyKeyboardRemove(true)).build();
                    } else {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("What city are you from?").replyMarkup(createButtons(List.of(user.getCity()))).build();
                    }

                } else {
                    return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Choose from what is on the buttons \uD83D\uDC47").build();
                }

                break;
        }
        return message;
    }

    public SendMessage setSexProfile(Update update, User user){

        SendMessage message = new SendMessage();

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (update.getMessage().getText().equals("Я парень") || update.getMessage().getText().equals("Я девушка")) {

                    if (update.getMessage().getText().equals("Я парень")) {
                        user.setSex(0);
                    } else if (update.getMessage().getText().equals("Я девушка")) {
                        user.setSex(1);
                    }

                    user.setChatStatus(ChatStatus.ENTER_OPPOSITE_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Кто тебе интересен?").replyMarkup(createButtons(List.of("Девушки", "Парни"))).build();

                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Выбери из того что есть на кнопках \uD83D\uDC47").build();
                }

                break;
            case UKRAINIAN:

                if (update.getMessage().getText().equals("Я хлопець") || update.getMessage().getText().equals("Я девушка")) {

                    if (update.getMessage().getText().equals("Я хлопець")) {
                        user.setSex(0);
                    } else if (update.getMessage().getText().equals("Я дівчина")) {
                        user.setSex(1);
                    }

                    user.setChatStatus(ChatStatus.ENTER_OPPOSITE_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Хто тобі цікавий?").replyMarkup(createButtons(List.of("Дівчата", "Хлопці"))).build();

                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Вибери з того, що є на кнопках \uD83D\uDC47").build();
                }

                break;

            case ENGLISH:

                if (update.getMessage().getText().equals("I'm a guy") || update.getMessage().getText().equals("I am a girl")) {

                    if (update.getMessage().getText().equals("I'm a guy")) {
                        user.setSex(0);
                    } else if (update.getMessage().getText().equals("I am a girl")) {
                        user.setSex(1);
                    }

                    user.setChatStatus(ChatStatus.ENTER_OPPOSITE_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Who are you interested in?").replyMarkup(createButtons(List.of("Girls", "Guys"))).build();

                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Choose from what is on the buttons \uD83D\uDC47").build();
                }

                break;
        }
        return message;
    }

    public SendMessage setAgeProfile(Update update, User user){

        SendMessage message = new SendMessage();

        switch (user.getLanguage()) {
            case RUSSIAN:

                try {
                    user.setAge(Integer.parseInt(update.getMessage().getText()));

                    user.setChatStatus(ChatStatus.ENTER_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Теперь определимся с полом").replyMarkup(createButtons(List.of("Я парень", "Я девушка"))).build();

                } catch (NumberFormatException e) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Я прийму только чило \uD83D\uDE21").build();
                }

                break;

            case UKRAINIAN:

                try {
                    user.setAge(Integer.parseInt(update.getMessage().getText()));

                    user.setChatStatus(ChatStatus.ENTER_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Тепер визначимося зі статтю").replyMarkup(createButtons(List.of("Я хлопець", "Я дівчина"))).build();

                } catch (NumberFormatException e) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Я прийму тільки число \uD83D\uDE21").build();
                }

                break;

            case ENGLISH:

                try {
                    user.setAge(Integer.parseInt(update.getMessage().getText()));

                    user.setChatStatus(ChatStatus.ENTER_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Now let's decide on the article").replyMarkup(createButtons(List.of("I'm a guy", "I am a girl"))).build();

                } catch (NumberFormatException e) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("I will only accept the number \uD83D\uDE21").build();
                }

                break;
        }
        return message;
    }

    public SendMessage setNameProfile(Update update, User user){

        SendMessage message = new SendMessage();

        user.setName(update.getMessage().getText());

        user.setChatStatus(ChatStatus.ENTER_AGE);
        userService.updateUser(user);

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (user.getAge() == 0) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Сколько тебе лет?").replyMarkup(new ReplyKeyboardRemove(true)).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Сколько тебе лет?").replyMarkup(createButtons(List.of(String.valueOf(user.getAge())))).build();
                }

                break;

            case UKRAINIAN:

                if (user.getAge() == 0) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Скільки тобі років?").replyMarkup(new ReplyKeyboardRemove(true)).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Скільки тобі років?").replyMarkup(createButtons(List.of(String.valueOf(user.getAge())))).build();
                }

                break;

            case ENGLISH:

                if (user.getAge() == 0) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("How old are you?").replyMarkup(new ReplyKeyboardRemove(true)).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("How old are you?").replyMarkup(createButtons(List.of(String.valueOf(user.getAge())))).build();
                }

                break;
        }
        return message;
    }

    public SendMessage startProfile(Update update, User user){

        SendMessage message = new SendMessage();

        user.setChatStatus(ChatStatus.ENTER_NAME);
        userService.updateUser(user);

        switch (user.getLanguage()) {
            case RUSSIAN:

                message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Как мне тебя назвать?").replyMarkup(createButtons(List.of(user.getName()))).build();

                break;

            case UKRAINIAN:

                message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Як мені тебе назвати?").replyMarkup(createButtons(List.of(user.getName()))).build();

                break;

            case ENGLISH:

                message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("What is your name?").replyMarkup(createButtons(List.of(user.getName()))).build();

                break;
        }
        return message;
    }

    public void likeProfile(Update update, User user){
        List<SendMessage> responses = new ArrayList<>();
        List<Match> matches = matchService.findAllByUserId(user.getId());
        List<Match> matchesOpposite;
        boolean matching = false;

        if(matches.size() != 0) {
            for (Match temp : matches){

                matchesOpposite = matchService.findAllByUserId(temp.getOppositeUserId().getId());

                if (matchesOpposite != null){
                    for (Match tempOpposite : matchesOpposite){
                        if(tempOpposite.getOppositeUserId().getId() == temp.getUserId()){

                            switch (user.getLanguage()){
                                case RUSSIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text("Есть взаимная симпатия! Начинай общаться\uD83D\uDC49" + "@" + temp.getOppositeUserId().getNickname())
                                            .build());

                                    break;

                                case UKRAINIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text("Є взаємна симпатія! Починай спілкуватися\uD83D\uDC49" + "@" + temp.getOppositeUserId().getNickname())
                                            .build());

                                    break;

                                case ENGLISH:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text("There is mutual sympathy! Start chatting\uD83D\uDC49" + "@" + temp.getOppositeUserId().getNickname())
                                            .build());

                                    break;
                            }

                            switch (userService.findById(tempOpposite.getUserId()).getLanguage()){
                                case RUSSIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text("Есть взаимная симпатия! Начинай общаться\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getNickname())
                                            .build());

                                    break;

                                case UKRAINIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text("Є взаємна симпатія! Починай спілкуватися\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getNickname())
                                            .build());

                                    break;

                                case ENGLISH:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text("There is mutual sympathy! Start chatting\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getNickname())
                                            .build());

                                    break;
                            }

                            matchService.deleteById(tempOpposite);
                            matchService.deleteById(temp);
                            matching = true;
                            break;
                        }
                    }
                }
            }
        }

        if (!matching){
            switch (user.getLanguage()) {
                case RUSSIAN:
                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Еще нет взаимных симпатий").build());
                    break;

                case UKRAINIAN:
                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("Еще нет взаимних симпатій").build());
                    break;

                case ENGLISH:
                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("There is no mutual sympathy yet").build());
                    break;
            }
        }

        bot.listSendMessage(responses);
    }

    public SendMessage waitingProfile(Update update, User user) {

        String message = null;

        switch (user.getLanguage()){
            case RUSSIAN:
                message = "1. Моя анкета.\n" +
                        "2. Смотреть анкеты.\n" +
                        "3. Показать взаимные симпатии.\n" +
                        "4. Изменить язык.";
                break;
            case UKRAINIAN:
                message = "1. Моя анкета.\n" +
                        "2. Дивитись анкети.\n" +
                        "3. Показати взаємні симпатії.\n" +
                        "4. Змінити мову.";
                break;
            case ENGLISH:
                message = "1. My profile.\n" +
                        "2. Look at profiles.\n" +
                        "3. Show mutual sympathy.\n" +
                        "4. Change language.";
                break;
        }

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(message).replyMarkup(createButtons(List.of("1", "2", "3", "4"))).build();
    }

    public void outputWaitingProfile(Update update, User user){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));

        if (user.getLanguage() == Language.RUSSIAN) {
            sendMessage.setText("Подождем пока кто-то увидит твою анкету");
        } else if (user.getLanguage() == Language.UKRAINIAN) {
            sendMessage.setText("Зачекаємо, поки хтось побачить твою анкету");
        } else if (user.getLanguage() == Language.ENGLISH) {
            sendMessage.setText("Wait for someone to see your profile");
        }

        bot.listSendMessage(Collections.singletonList(sendMessage));
    }

    public SendMessage nextProfile(Update update, User user){

        User oppositeUser = userService.findById(user.getOppositeSexId());

        if (user.getChatStatus() == ChatStatus.LIKE){
            Match match = new Match();
            List<Match> list = new ArrayList<>();
            match.setOppositeUserId(oppositeUser);
            list.add(match);
            user.setMatches(list);
            match.setUserId(user.getId());
            matchService.saveMatch(match);
            userService.saveUser(user);
            user.setChatStatus(ChatStatus.DISLIKE);
        }

        long nextId = 0;
        boolean temp = true;
        if (update.getMessage().getText().equals("2")){
            userService.resetId(user.getOppositeSexId() - 1);
        }

        do{
            if (user.getOppositeSexId() >= userService.getLastUser().getId() || nextId >= userService.getLastUser().getId()){
                oppositeUser = userService.getFirstUser();
                userService.resetId(oppositeUser.getId());
                nextId = oppositeUser.getId();
            }else {
                if (temp) {
                    userService.resetId(user.getOppositeSexId());
                    temp = false;
                }
                nextId = userService.nextId();
                oppositeUser = userService.findById(nextId);
            }
        }while (user.getOppositeSex() != oppositeUser.getSex());

        user.setOppositeSexId(nextId);
        userService.saveUser(user);

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(outputProfile(oppositeUser)).replyMarkup(createButtons(List.of("❤", "\uD83D\uDC4E", "\uD83D\uDCA4"))).build();
    }

    public String outputProfile(User user){
        return user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription();
    }

    public void myProfile(Update update, User user){
        List<SendMessage> responses = new ArrayList<>();

        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription()).build());

        switch (user.getLanguage()) {
            case RUSSIAN:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("1. Заново заполнить анкету\n2. Смотреть анкеты").replyMarkup(createButtons(List.of("1", "2"))).build());
                break;
            case UKRAINIAN:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("1. По-новому заповнити анкету\n2. Дивитись анкети").replyMarkup(createButtons(List.of("1", "2"))).build());
                break;
            case ENGLISH:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("1. Fill out the form again\n2. See questionnaires").replyMarkup(createButtons(List.of("1", "2"))).build());
                break;
        }

        bot.listSendMessage(responses);
    }

    public ReplyKeyboardMarkup createButtons(List<String> list){
        return buttonsService.setButtons(buttonsService.createButtons(list));
    }

}
