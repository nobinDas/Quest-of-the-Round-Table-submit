package team1.project.controller.request;

import lombok.Data;
import team1.project.models.Player;

@Data
public class ConnectRequest {
    private Player player;
    private String gameId;

    public Player getPlayer(){
        return player;
    }

    public String getGameId(){
        return gameId;
    }
}