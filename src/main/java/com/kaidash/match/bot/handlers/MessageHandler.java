package com.kaidash.match.bot.handlers;

import com.kaidash.match.entity.ChatStatus;
import com.kaidash.match.entity.Language;
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

    @Autowired
    public MessageHandler(UserService userService, ButtonsService buttonsService, MatchService matchService) {
        this.userService = userService;
        this.buttonsService = buttonsService;
        this.matchService = matchService;
    }

    public List<SendMessage> handle(Update update){
        List<SendMessage> responses = new ArrayList<>();
        User user = userService.findByUserId(update.getMessage().getFrom().getId());

        if (user != null) {
            determineStatus(update, user);

            switch (user.getChatStatus()) {
                case REGISTRATION:
                    responses.add(displayLanguageQuestion(update, user));
                    break;
                case LANGUAGE:
                    setLanguageProfile(update, user);
                    if (user.getAge() == 0){
                        responses.add(startProfile(update, user));
                    }else{
                        responses.add(waitingProfile(update, user));
                    }
                    userService.updateUser(user);
                    break;
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
                    setDescriptionProfile(update, user);
                    responses = myProfile(update, user);
                    break;
                case LIKE:
                case DISLIKE:
                    responses.add(nextProfile(update, user));
                    break;
                case SLEEP:

                    if (user.getLanguage() == Language.RUSSIAN) {
                        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("???????????????? ???????? ??????-???? ???????????? ???????? ????????????").build());
                    } else if (user.getLanguage() == Language.UKRAINIAN) {
                        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("??????????????????, ???????? ?????????? ???????????????? ???????? ????????????").build());
                    } else if (user.getLanguage() == Language.ENGLISH) {
                        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("Wait for someone to see your profile").build());
                    }

                    responses.add(waitingProfile(update, user));
                    break;
                case SHOW_MATCHES:
                    responses = likeProfile(update, user);
                    break;
            }
        }else {
            user = new User();
            user.setUserId(update.getMessage().getFrom().getId());
            user.setNickname(update.getMessage().getFrom().getUserName());
            user.setName(update.getMessage().getFrom().getFirstName());
            user.setOppositeSexId(userService.getFirstUser().getId());
            user.setLanguage(Language.RUSSIAN);
            user.setChatStatus(ChatStatus.REGISTRATION);
            userService.saveUser(user);
            responses.add(displayLanguageQuestion(update, user));
        }

        return responses;
    }

    private void setLanguageProfile(Update update, User user){
        if (update.getMessage().getText().equals("\uD83C\uDDFA\uD83C\uDDE6")){
            user.setLanguage(Language.UKRAINIAN);
        }else if (update.getMessage().getText().equals("\uD83C\uDDF7\uD83C\uDDFA")){
            user.setLanguage(Language.RUSSIAN);
        }else if (update.getMessage().getText().equals("\uD83C\uDDFA\uD83C\uDDF8")){
            user.setLanguage(Language.ENGLISH);
        }
        userService.updateUser(user);
    }

    private SendMessage displayLanguageQuestion (Update update, User user){

        SendMessage sendMessage = new SendMessage();

        if (user.getLanguage() == Language.RUSSIAN || user.getLanguage() == null) {
            sendMessage = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("???? ?????????? ?????????? ?????????? ?????????????????")
                    .replyMarkup(createButtons(List.of("\uD83C\uDDFA\uD83C\uDDE6", "\uD83C\uDDF7\uD83C\uDDFA", "\uD83C\uDDFA\uD83C\uDDF8")))
                    .build();
        } else if (user.getLanguage() == Language.UKRAINIAN) {
            sendMessage = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                    .text("???? ???????? ???????? ???????????? ?????????????????????????")
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

    private void determineStatus (Update update, User user){

        if (update.getMessage().getText().equals("1")){
            user.setChatStatus(ChatStatus.START);
        } else if (update.getMessage().getText().equals("2")) {
            user.setChatStatus(ChatStatus.DISLIKE);
        }else if(update.getMessage().getText().equals("???")){
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

    private void setDescriptionProfile(Update update, User user){

        String message = update.getMessage().getText();

        switch (user.getLanguage()){

            case RUSSIAN:
                if (message.equals("????????????????????")) {
                    user.setDescription("");
                } else if (message.equals("????????????????")) {
                    user.setDescription(user.getDescription());
                } else {
                    user.setDescription(message);
                }
                break;

            case UKRAINIAN:

                if (message.equals("????????????????????")) {
                    user.setDescription("");
                } else if (message.equals("????????????????")) {
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

    private SendMessage setCiteProfile(Update update, User user){

        SendMessage message = new SendMessage();

        user.setCity(update.getMessage().getText());

        user.setChatStatus(ChatStatus.ENTER_DESCRIPTION);
        userService.updateUser(user);

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (user.getDescription() == null) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????????? ?? ???????? ?? ???????? ???????????? ??????????, ?????? ?????????????????????? ????????????????. " +
                                    "?????? ?????????????? ?????????? ?????????????????? ???????? ????????????????.")
                            .replyMarkup(createButtons(List.of("????????????????????"))).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????????? ?? ???????? ?? ???????? ???????????? ??????????, ?????? ?????????????????????? ????????????????. " +
                                    "?????? ?????????????? ?????????? ?????????????????? ???????? ????????????????.")
                            .replyMarkup(createButtons(List.of("????????????????????", "????????????????"))).build();
                }
                break;

            case UKRAINIAN:

                if (user.getDescription() == null) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????????? ?????? ???????? ???? ???????? ?????????? ????????????, ?????? ?????????????????? ??????????????????. " +
                                    "???? ???????????????? ?????????? ?????????????????? ???????? ????????????????.")
                            .replyMarkup(createButtons(List.of("????????????????????"))).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????????? ?????? ???????? ???? ???????? ?????????? ????????????, ?????? ?????????????????? ??????????????????. " +
                                    "???? ???????????????? ?????????? ?????????????????? ???????? ????????????????.")
                            .replyMarkup(createButtons(List.of("????????????????????", "????????????????"))).build();
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

    private SendMessage setOppositeSexProfile(Update update, User user){
        SendMessage message = new SendMessage();

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (update.getMessage().getText().equals("??????????????") || update.getMessage().getText().equals("??????????")) {

                    if (update.getMessage().getText().equals("??????????????")) {
                        user.setOppositeSex(1);
                    } else if (update.getMessage().getText().equals("??????????")) {
                        user.setOppositeSex(0);
                    }

                    user.setChatStatus(ChatStatus.ENTER_CITY);
                    userService.updateUser(user);

                    if (user.getCity() == null) {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("???? ???????????? ???? ?????????????").replyMarkup(new ReplyKeyboardRemove(true)).build();
                    } else {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("???? ???????????? ???? ?????????????").replyMarkup(createButtons(List.of(user.getCity()))).build();
                    }

                } else {
                    return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????? ???? ???????? ?????? ???????? ???? ?????????????? \uD83D\uDC47").build();
                }
                break;

            case UKRAINIAN:

                if (update.getMessage().getText().equals("??????????????") || update.getMessage().getText().equals("????????????")) {

                    if (update.getMessage().getText().equals("??????????????")) {
                        user.setOppositeSex(1);
                    } else if (update.getMessage().getText().equals("????????????")) {
                        user.setOppositeSex(0);
                    }

                    user.setChatStatus(ChatStatus.ENTER_CITY);
                    userService.updateUser(user);

                    if (user.getCity() == null) {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("?? ?????????? ???? ???????????").replyMarkup(new ReplyKeyboardRemove(true)).build();
                    } else {
                        message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                .text("?? ?????????? ???? ???????????").replyMarkup(createButtons(List.of(user.getCity()))).build();
                    }

                } else {
                    return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????? ?? ????????, ???? ?? ???? ?????????????? \uD83D\uDC47").build();
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

    private SendMessage setSexProfile(Update update, User user){

        SendMessage message = new SendMessage();

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (update.getMessage().getText().equals("?? ????????????") || update.getMessage().getText().equals("?? ??????????????")) {

                    if (update.getMessage().getText().equals("?? ????????????")) {
                        user.setSex(0);
                    } else if (update.getMessage().getText().equals("?? ??????????????")) {
                        user.setSex(1);
                    }

                    user.setChatStatus(ChatStatus.ENTER_OPPOSITE_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????? ???????? ???????????????????").replyMarkup(createButtons(List.of("??????????????", "??????????"))).build();

                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????? ???? ???????? ?????? ???????? ???? ?????????????? \uD83D\uDC47").build();
                }

                break;
            case UKRAINIAN:

                if (update.getMessage().getText().equals("?? ??????????????") || update.getMessage().getText().equals("?? ??????????????")) {

                    if (update.getMessage().getText().equals("?? ??????????????")) {
                        user.setSex(0);
                    } else if (update.getMessage().getText().equals("?? ??????????????")) {
                        user.setSex(1);
                    }

                    user.setChatStatus(ChatStatus.ENTER_OPPOSITE_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????? ???????? ???????????????").replyMarkup(createButtons(List.of("??????????????", "????????????"))).build();

                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????? ?? ????????, ???? ?? ???? ?????????????? \uD83D\uDC47").build();
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

    private SendMessage setAgeProfile(Update update, User user){

        SendMessage message = new SendMessage();

        switch (user.getLanguage()) {
            case RUSSIAN:

                try {
                    user.setAge(Integer.parseInt(update.getMessage().getText()));

                    user.setChatStatus(ChatStatus.ENTER_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("???????????? ?????????????????????? ?? ??????????").replyMarkup(createButtons(List.of("?? ????????????", "?? ??????????????"))).build();

                } catch (NumberFormatException e) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?? ???????????? ???????????? ???????? \uD83D\uDE21").build();
                }

                break;

            case UKRAINIAN:

                try {
                    user.setAge(Integer.parseInt(update.getMessage().getText()));

                    user.setChatStatus(ChatStatus.ENTER_SEX);
                    userService.updateUser(user);

                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????? ?????????????????????? ???? ????????????").replyMarkup(createButtons(List.of("?? ??????????????", "?? ??????????????"))).build();

                } catch (NumberFormatException e) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?? ???????????? ???????????? ?????????? \uD83D\uDE21").build();
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

    private SendMessage setNameProfile(Update update, User user){

        SendMessage message = new SendMessage();

        user.setName(update.getMessage().getText());

        user.setChatStatus(ChatStatus.ENTER_AGE);
        userService.updateUser(user);

        switch (user.getLanguage()) {
            case RUSSIAN:

                if (user.getAge() == 0) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????????? ???????? ???????").replyMarkup(new ReplyKeyboardRemove(true)).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????????? ???????? ???????").replyMarkup(createButtons(List.of(String.valueOf(user.getAge())))).build();
                }

                break;

            case UKRAINIAN:

                if (user.getAge() == 0) {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????????? ???????? ???????????").replyMarkup(new ReplyKeyboardRemove(true)).build();
                } else {
                    message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????????????? ???????? ???????????").replyMarkup(createButtons(List.of(String.valueOf(user.getAge())))).build();
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

    private SendMessage startProfile(Update update, User user){

        SendMessage message = new SendMessage();

        user.setChatStatus(ChatStatus.ENTER_NAME);
        userService.updateUser(user);

        switch (user.getLanguage()) {
            case RUSSIAN:

                message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("?????? ?????? ???????? ???????????????").replyMarkup(createButtons(List.of(user.getName()))).build();

                break;

            case UKRAINIAN:

                message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("???? ???????? ???????? ???????????????").replyMarkup(createButtons(List.of(user.getName()))).build();

                break;

            case ENGLISH:

                message = SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("What is your name?").replyMarkup(createButtons(List.of(user.getName()))).build();

                break;
        }
        return message;
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

                            switch (user.getLanguage()){
                                case RUSSIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text("???????? ???????????????? ????????????????! ?????????????? ????????????????\uD83D\uDC49" + "@" + temp.getOppositeUserId().getNickname())
                                            .build());

                                    break;

                                case UKRAINIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                                            .text("?? ?????????????? ????????????????! ?????????????? ????????????????????????\uD83D\uDC49" + "@" + temp.getOppositeUserId().getNickname())
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
                                            .text("???????? ???????????????? ????????????????! ?????????????? ????????????????\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getNickname())
                                            .build());

                                    break;

                                case UKRAINIAN:

                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text(outputProfile(temp.getOppositeUserId())).build());
                                    responses.add(SendMessage.builder().chatId(String.valueOf(userService.findById(tempOpposite.getUserId()).getUserId()))
                                            .text("?? ?????????????? ????????????????! ?????????????? ????????????????????????\uD83D\uDC49" + "@" + tempOpposite.getOppositeUserId().getNickname())
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
                            .text("?????? ?????? ???????????????? ????????????????").build());
                    break;

                case UKRAINIAN:
                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("?????? ?????? ???????????????? ????????????????").build());
                    break;

                case ENGLISH:
                    responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                            .text("There is no mutual sympathy yet").build());
                    break;
            }
            responses.add(waitingProfile(update, user));
        }

        return responses;
    }

    private SendMessage waitingProfile(Update update, User user) {

        String message = null;

        switch (user.getLanguage()){
            case RUSSIAN:
                message = "1. ?????? ????????????.\n" +
                    "2. ???????????????? ????????????.\n" +
                    "3. ???????????????? ???????????????? ????????????????.\n" +
                    "4. ???????????????? ????????.";
                break;
            case UKRAINIAN:
                message = "1. ?????? ????????????.\n" +
                        "2. ???????????????? ????????????.\n" +
                        "3. ???????????????? ?????????????? ????????????????.\n" +
                        "4. ?????????????? ????????.";
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
        }while (user.getOppositeSex() != oppositeUser.getSex() || user.getId() == oppositeUser.getId());

        user.setOppositeSexId(nextId);
        userService.saveUser(user);

        return SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(outputProfile(oppositeUser)).replyMarkup(createButtons(List.of("???", "\uD83D\uDC4E", "\uD83D\uDCA4"))).build();
    }

    private String outputProfile(User user){
        return user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription();
    }

    private List<SendMessage> myProfile(Update update, User user){
        List<SendMessage> responses = new ArrayList<>();

        responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                .text(user.getName() + ", " + user.getAge() + ", " + user.getCity() + ", " + user.getDescription()).build());

        switch (user.getLanguage()) {
            case RUSSIAN:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("1. ???????????? ?????????????????? ????????????\n2. ???????????????? ????????????").replyMarkup(createButtons(List.of("1", "2"))).build());
                break;
            case UKRAINIAN:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("1. ????-???????????? ?????????????????? ????????????\n2. ???????????????? ????????????").replyMarkup(createButtons(List.of("1", "2"))).build());
                break;
            case ENGLISH:
                responses.add(SendMessage.builder().chatId(String.valueOf(update.getMessage().getChatId()))
                        .text("1. Fill out the form again\n2. See questionnaires").replyMarkup(createButtons(List.of("1", "2"))).build());
                break;
        }
        return responses;
    }

    private ReplyKeyboardMarkup createButtons(List<String> list){
        return buttonsService.setButtons(buttonsService.createButtons(list));
    }
}
