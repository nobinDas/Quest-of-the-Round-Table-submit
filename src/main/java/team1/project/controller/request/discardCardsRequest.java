package team1.project.controller.request;

import lombok.Data;

@Data
public class discardCardsRequest {
    private String player;
    private String[] card;


    public String getPlayer(){
        return player;
    }

    public String[] getCard(){
        return card;
    }


}
