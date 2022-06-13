package com.kaidash.match.bot.handlers;

import com.kaidash.match.entity.Match;
import com.kaidash.match.entity.User;
import com.kaidash.match.local.UserLocal;
import com.kaidash.match.service.ButtonsService;
import com.kaidash.match.service.MatchService;
import com.kaidash.match.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import java.util.ArrayList;
import java.util.List;

@Component
public class MessageHandler {
    private final UserService userService;
    private final UserLocal userLocal;
    private final ButtonsService buttonsService;
    private final MatchService matchService;
    private SendMessage sendMessage = new SendMessage();

    @Autowired
    public MessageHandler(UserService userService, UserLocal userLocal, ButtonsService buttonsService, MatchService matchService) {
        this.userService = userService;
        this.userLocal = userLocal;
        this.buttonsService = buttonsService;
        this.matchService = matchService;
    }

    private static int count = 0;
    private static int firstCount = 0;
    private static int likeUser = 0;

    public List<SendMessage> handle(Update update){
        List<SendMessage> responses = new ArrayList<>();
        String menuChoice = update.getMessage().getText();
        User user = userService.findByUserId(update.getMessage().getFrom().getId());

        if (menuChoice.equals("/start") && user != null){
            return myProfile(update);
        }else if (user != null){
            firstCount = 1;
        }
        switch (menuChoice){
            case "1":
            case "/start":
                count = 1;
                break;
            case "2":
                responses.add(nextProfile(update));
                break;
            case "❤️":
                likeUser = 1;
                responses.add(nextProfile(update));
                break;
            case "\uD83D\uDC4E":
                likeUser = 0;
                responses.add(nextProfile(update));
                break;
            case "\uD83D\uDCA4":
                likeUser = 3;
                assert user != null;
                user.setOppositeSexId(user.getOppositeSexId() - 1);
                userService.saveUser(user);
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Подождем пока кто-то увидит твою анкету").build());
                responses.add(waitingProfile(update));
                break;
            case "3":
                responses = likeProfile(update);
                break;
        }

        if (count != 0){
            responses = changeMyProfile(update);
        }

        return responses;
    }

    private List<SendMessage> likeProfile(Update update){
        List<SendMessage> responses = new ArrayList<>();
        User user = userService.findByUserId(update.getMessage().getFrom().getId());
        List<Match> matches = matchService.findAllByUserId(user.getId());
        List<Match> matchesOpposite = new ArrayList<>();

        if(matches.size() != 0) {
            for (Match temp : matches){

                matchesOpposite = matchService.findAllByUserId(temp.getOppositeUserId().getId());

                if (matchesOpposite != null){
                    for (Match tempOpposite : matchesOpposite){
                        if(tempOpposite.getOppositeUserId().getId() == temp.getUserId()){
                            responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                    .text(outputProfile(temp.getOppositeUserId())).build());
                            responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                    .text("Есть взаимная симпатия! Начинай общаться\uD83D\uDC49" + "@" + temp.getOppositeUserId().getUserName())
                                    .build());
                            responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                    .text(outputProfile(temp.getOppositeUserId())).build());
                            responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                    .text("Есть взаимная симпатия! Начинай общаться\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getUserName())
                                    .build());
                            matchService.deleteById(tempOpposite);
                            matchService.deleteById(temp);
                            break;
                        }
                    }
                }
            }
            responses.add(waitingProfile(update));
        }else{
            responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Еще не кто не лайкнул твою анкету").build());
            responses.add(waitingProfile(update));
        }
        return responses;
    }

    private SendMessage waitingProfile(Update update){
        String message = "1. Моя анкета.\n" +
                            "2. Смотреть анкеты.\n" +
                            "3. Показать кому понравилась твоя анкета.";
        createButtons(List.of("1", "2", "3"));

        return createTextMessage(update, message);
    }

    private SendMessage nextProfile(Update update){
        User user = userService.findByUserId(update.getMessage().getFrom().getId());
        User oppositeUser = userService.findById(user.getOppositeSexId());

        if (likeUser == 1){
            Match match = new Match();
            List<Match> list = new ArrayList<>();
            match.setOppositeUserId(oppositeUser);
            list.add(match);
            user.setMatches(list);
            match.setUserId(user.getId());
            matchService.saveMatch(match);
            userService.saveUser(user);
            likeUser = 0;
        }

        long nextId = 0;
        if (likeUser == 2){
            userService.resetId(user.getOppositeSexId() - 1);
            likeUser = 0;
        }

        do{
            if (user.getOppositeSexId() >= userService.getLastUser().getId() || nextId >= userService.getLastUser().getId()){
                oppositeUser = userService.getFirstUser();
                userService.resetId(oppositeUser.getId());
                nextId = oppositeUser.getId();
            }else {
                nextId = userService.nextId();
                oppositeUser = userService.findById(nextId);
            }
        }while (user.getOppositeSex() != oppositeUser.getSex());

        user.setOppositeSexId(nextId);
        userService.saveUser(user);

        sendMessage = createTextMessage(update, outputProfile(oppositeUser));

        createButtons(List.of("❤️", "\uD83D\uDC4E", "\uD83D\uDCA4"));

        return sendMessage;
    }

    private String outputProfile(User user){
        return user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription();
    }

    private String outputFindProfile(long userId){
        User user = userService.findByUserId(userId);
        return user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription();
    }

    private List<SendMessage> myProfile(Update update){
        List<SendMessage> responses = new ArrayList<>();

        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(outputFindProfile(update.getMessage().getFrom().getId())).build());

        responses.add(createTextMessage(update, "1. Заного заполнить анкету\n2. Смотреть анкеты"));

        createButtons(List.of("1", "2"));

        return responses;
    }

    private List<SendMessage> changeMyProfile(Update update){
        List<SendMessage> responses = new ArrayList<>();
        User user = userService.findByUserId(update.getMessage().getFrom().getId());

        if (user != null) {

            switch (count) {

                case 1:

                    responses.add(createTextMessage(update, "Как мне тебя назвать?"));

                    if (firstCount == 1) {
                        createButtons(List.of(update.getMessage().getFrom().getFirstName()));
                    } else {
                        createButtons(List.of(user.getName()));
                    }

                    userLocal.setUserId(update.getMessage().getFrom().getId());
                    userLocal.setUserName(update.getMessage().getFrom().getUserName());

                    count = 2;
                    break;
                case 2:
                    userLocal.setName(update.getMessage().getText());

                    responses.add(createTextMessage(update, "Сколько тебе лет"));

                    if (firstCount == 1) {
                        sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                    } else {
                        createButtons(List.of(String.valueOf(user.getAge())));
                    }

                    count = 3;
                    break;
                case 3:

                    try {
                        userLocal.setAge(Integer.parseInt(update.getMessage().getText()));

                        responses.add(createTextMessage(update, "Теперь определимся с полом"));
                        createButtons(List.of("Я парень", "Я девушка"));

                        count = 4;
                    } catch (NumberFormatException e) {
                        responses.add(createTextMessage(update, "Я прийму только чило \uD83D\uDE21"));
                    }

                    break;
                case 4:

                    if (update.getMessage().getText().equals("Я парень") || update.getMessage().getText().equals("Я девушка")) {

                        if (update.getMessage().getText().equals("Я парень")) {
                            userLocal.setSex(0);
                        } else if (update.getMessage().getText().equals("Я девушка")) {
                            userLocal.setSex(1);
                        }

                        responses.add(createTextMessage(update, "Кто тебе интересен?"));
                        createButtons(List.of("Девушки", "Парни"));

                        count = 5;
                    } else {
                        responses.add(createTextMessage(update, "Выбери из того что есть на кнопках \uD83D\uDC47"));
                    }

                    break;
                case 5:

                    if (update.getMessage().getText().equals("Девушки") || update.getMessage().getText().equals("Парни")) {

                        if (update.getMessage().getText().equals("Девушки")) {
                            userLocal.setOppositeSex(1);
                        } else if (update.getMessage().getText().equals("Парни")) {
                            userLocal.setOppositeSex(0);
                        }

                        responses.add(createTextMessage(update, "Из какого ты города?"));

                        if (firstCount == 1) {
                            sendMessage.setReplyMarkup(new ReplyKeyboardRemove(true));
                        } else {
                            createButtons(List.of(user.getCity()));
                        }

                        count = 6;
                    } else {
                        responses.add(createTextMessage(update, "Выбери из того что есть на кнопках \uD83D\uDC47"));
                    }
                    break;
                case 6:

                    userLocal.setCity(update.getMessage().getText());

                    responses.add(createTextMessage(update, "Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                            "Это поможет лучше подобрать тебе компанию."));

                    if (firstCount == 1) {
                        createButtons(List.of("Пропустить"));
                    } else {
                        createButtons(List.of("Пропустить", "Оставить"));
                    }

                    count = 7;
                    break;
                case 7:
                    if (update.getMessage().getText().equals("Пропустить")) {
                        userLocal.setDescription("");
                    } else if (update.getMessage().getText().equals("Оставить")) {
                        userLocal.setDescription(user.getDescription());
                    } else {
                        userLocal.setDescription(update.getMessage().getText());
                    }

                    if (firstCount == 1) {
                        userService.saveLocalUsers(userLocal);
                        user = userService.findByUserId(userLocal.getUserId());
                        userLocal.setOppositeSexId(user.getId());
                        userService.updateUser(userLocal);
                    } else {
                        userService.updateUser(userLocal);
                    }

                    responses = myProfile(update);

                    count = 0;
                    firstCount = 0;

                    break;
            }
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
