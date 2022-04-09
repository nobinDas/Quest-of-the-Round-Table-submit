package team1.project.controller.request;

import lombok.Data;

@Data
public class PlayerRequest {
    private String player;

    public String getPlayer(){
        return player;
    }
}
