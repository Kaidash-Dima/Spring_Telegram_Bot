package com.kaidash.match.bot.handlers;

import com.kaidash.match.entity.ChatStatus;
import com.kaidash.match.entity.Match;
import com.kaidash.match.entity.User;
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
    private final ButtonsService buttonsService;
    private final MatchService matchService;
//    private SendMessage sendMessage = new SendMessage();

    @Autowired
    public MessageHandler(UserService userService, ButtonsService buttonsService, MatchService matchService) {
        this.userService = userService;
        this.buttonsService = buttonsService;
        this.matchService = matchService;
    }

    public List<SendMessage> handle(Update update){
        List<SendMessage> responses = new ArrayList<>();
        User user = userService.findByUserId(update.getMessage().getFrom().getId());

        if (user == null) {
            user = new User();
            user.setUserId(update.getMessage().getFrom().getId());
            user.setNickname(update.getMessage().getFrom().getUserName());
            user.setName(update.getMessage().getFrom().getFirstName());
            user.setChatStatus(ChatStatus.START);
            userService.saveUser(user);
        }

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
        }

        userService.updateUser(user);

        switch (user.getChatStatus()){
            case START:
                responses.add(startProfile(update, user));
                break;
            case ENTER_NAME:
                responses.add(setNameProfile(update, user));
                break;
            case ENTER_AGE:
                responses.add(setAgeProfile(update, user));
                break;
            case ENTER_SEX:
                responses.add(setSexProfile(update, user));
                break;
            case ENTER_OPPOSITE_SEX:
                responses.add(setOppositeSexProfile(update, user));
                break;
            case ENTER_CITY:
                responses.add(setCiteProfile(update, user));
                break;
            case ENTER_DESCRIPTION:
                setDescriptionProfile(update,user);
                responses = myProfile(update, user);
                break;
//            case NEXT_PROFILE:
//                responses.add(nextProfile(update, user));
//                user.setChatStatus(ChatStatus.DISLIKE);
//                userService.updateUser(user);
//                break;
            case LIKE:
            case DISLIKE:
                responses.add(nextProfile(update, user));
                break;
            case SLEEP:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Подождем пока кто-то увидит твою анкету").build());
                responses.add(waitingProfile(update));
                break;
            case SHOW_MATCHES:
                responses = likeProfile(update, user);
                responses.add(waitingProfile(update));
                break;
        }

        return responses;
    }

    private void setDescriptionProfile(Update update, User user){

        if (update.getMessage().getText().equals("Пропустить")) {
            user.setDescription("");
        } else if (update.getMessage().getText().equals("Оставить")) {
            user.setDescription(user.getDescription());
        } else {
            user.setDescription(update.getMessage().getText());
        }

        userService.updateUser(user);
    }

    private SendMessage setCiteProfile(Update update, User user){

        user.setCity(update.getMessage().getText());

        user.setChatStatus(ChatStatus.ENTER_DESCRIPTION);
        userService.updateUser(user);

        if (user.getDescription() == null) {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                            "Это поможет лучше подобрать тебе компанию.")
                    .replyMarkup(createButtons(List.of("Пропустить"))).build();
        } else {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Расскажи о себе и кого хочешь найти, чем предлагаешь заняться. " +
                            "Это поможет лучше подобрать тебе компанию.")
                    .replyMarkup(createButtons(List.of("Пропустить", "Оставить"))).build();
        }
    }

    private SendMessage setOppositeSexProfile(Update update, User user){

        if (update.getMessage().getText().equals("Девушки") || update.getMessage().getText().equals("Парни")) {

            if (update.getMessage().getText().equals("Девушки")) {
                user.setOppositeSex(1);
            } else if (update.getMessage().getText().equals("Парни")) {
                user.setOppositeSex(0);
            }

            user.setChatStatus(ChatStatus.ENTER_CITY);
            userService.updateUser(user);

            if (user.getCity() == null) {
                return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Из какого ты города?").replyMarkup(new ReplyKeyboardRemove(true)).build();
            } else {
                return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Из какого ты города?").replyMarkup(createButtons(List.of(user.getCity()))).build();
            }

        } else {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Выбери из того что есть на кнопках \uD83D\uDC47").build();
        }
    }

    private SendMessage setSexProfile(Update update, User user){

        if (update.getMessage().getText().equals("Я парень") || update.getMessage().getText().equals("Я девушка")) {

            if (update.getMessage().getText().equals("Я парень")) {
                user.setSex(0);
            } else if (update.getMessage().getText().equals("Я девушка")) {
                user.setSex(1);
            }

            user.setChatStatus(ChatStatus.ENTER_OPPOSITE_SEX);
            userService.updateUser(user);

            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Кто тебе интересен?").replyMarkup(createButtons(List.of("Девушки", "Парни"))).build();

        } else {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Выбери из того что есть на кнопках \uD83D\uDC47").build();
        }
    }

    private SendMessage setAgeProfile(Update update, User user){

        try {
            user.setAge(Integer.parseInt(update.getMessage().getText()));

            user.setChatStatus(ChatStatus.ENTER_SEX);
            userService.updateUser(user);

            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Теперь определимся с полом").replyMarkup(createButtons(List.of("Я парень", "Я девушка"))).build();

        } catch (NumberFormatException e) {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Я прийму только чило \uD83D\uDE21").build();
        }
    }

    private SendMessage setNameProfile(Update update, User user){

        user.setName(update.getMessage().getText());

        user.setChatStatus(ChatStatus.ENTER_AGE);
        userService.updateUser(user);

        if (user.getAge() == 0) {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Сколько тебе лет?").replyMarkup(new ReplyKeyboardRemove(true)).build();
        } else {
            return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("Сколько тебе лет?").replyMarkup(createButtons(List.of(String.valueOf(user.getAge())))).build();
        }
    }

    private SendMessage startProfile(Update update, User user){

        user.setChatStatus(ChatStatus.ENTER_NAME);
        userService.updateUser(user);

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text("Как мне тебя назвать?").replyMarkup(createButtons(List.of(user.getName()))).build();
    }

    private List<SendMessage> likeProfile(Update update, User user){
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
                            responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                    .text(outputProfile(temp.getOppositeUserId())).build());
                            responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                    .text("Есть взаимная симпатия! Начинай общаться\uD83D\uDC49" + "@" + temp.getOppositeUserId().getNickname())
                                    .build());
                            responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                    .text(outputProfile(temp.getOppositeUserId())).build());
                            responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                    .text("Есть взаимная симпатия! Начинай общаться\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getNickname())
                                    .build());
                            matchService.deleteById(tempOpposite);
                            matchService.deleteById(temp);
                            matching = true;
                            break;
                        }
                    }
                }
            }
            if (!matching) {
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("Еще не кто не лайкнул твою анкету").build());
                responses.add(waitingProfile(update));
            }
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

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(message).replyMarkup(createButtons(List.of("1", "2", "3"))).build();
    }

    private SendMessage nextProfile(Update update, User user){

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
        if (update.getMessage().getText().equals("2")){
            userService.resetId(user.getOppositeSexId() - 1);
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

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(outputProfile(oppositeUser)).replyMarkup(createButtons(List.of("❤", "\uD83D\uDC4E", "\uD83D\uDCA4"))).build();
    }

    private String outputProfile(User user){
        return user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription();
    }

    private List<SendMessage> myProfile(Update update, User user){
        List<SendMessage> responses = new ArrayList<>();

        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription()).build());

        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text("1. Заного заполнить анкету\n2. Смотреть анкеты").replyMarkup(createButtons(List.of("1", "2"))).build());

        return responses;
    }

    private ReplyKeyboardMarkup createButtons(List<String> list){
        return buttonsService.setButtons(buttonsService.createButtons(list));
    }
}
