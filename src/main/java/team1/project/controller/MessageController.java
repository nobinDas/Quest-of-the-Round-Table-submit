package team1.project.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import team1.project.models.AdventureCard;
import team1.project.models.Player;

import java.util.ArrayList;

@Controller
public class MessageController {
    private SimpMessagingTemplate template;

    @MessageMapping("/progress")
    @SendTo("/topic/progress")
    public String broadcastNews(@Payload String message) {
        return message;
    }


//    public void yourDeck(Player player, @Payload ArrayList<AdventureCard> cards){
//        System.out.println("/topic/group thingy the player name is: " + player.getId());
//        this.template.convertAndSend("/topic/group/" + player.getId(), cards);
//    }
//
//    public void discardCard(Player player, @Payload ArrayList<AdventureCard> cards){
//        this.template.convertAndSend("/topic/group/" + player.getName() + "/discard", cards);
//    }

}
