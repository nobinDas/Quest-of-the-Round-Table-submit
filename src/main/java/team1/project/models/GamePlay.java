package team1.project.models;

import lombok.Data;

import java.util.ArrayList;

@Data
public class GamePlay {
    private String gameId;
    private int totalNumPlayers;
    private int playersJoined;
}
