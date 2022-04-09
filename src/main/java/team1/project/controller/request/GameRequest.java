package team1.project.controller.request;

import lombok.Data;
import team1.project.models.Player;

@Data
public class GameRequest {
    private Player player;
    private int num_players;

    public Player getPlayer(){
        return player;
    }

    public int getGameId(){
        return num_players;
    }
}
