//package com.kaidash.match.controller;
//
//
//import com.kaidash.match.bot.Bot;
//import org.springframework.web.bind.annotation.*;
//import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
//import org.telegram.telegrambots.meta.api.objects.Update;
//
//@RestController
//public class WebHookController {
//    private final Bot bot;
//
//    public WebHookController(Bot bot) {
//        this.bot = bot;
//    }
//
//    @RequestMapping(value = "/", method = RequestMethod.POST)
//    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update){
//        return bot.onWebhookUpdateReceived(update);
//    }
//}
