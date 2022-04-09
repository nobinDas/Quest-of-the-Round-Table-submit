package team1.project.controller.request;

import lombok.Data;

@Data
public class bidRequest {
    private String player;
    private String bid;


    public String getPlayer(){
        return player;
    }

    public String getBid() { return bid; }
}