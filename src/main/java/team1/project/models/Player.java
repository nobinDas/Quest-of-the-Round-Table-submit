package team1.project.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Player {
    private String name;
    private int battlePoints;// Sum of: Rank points + off hand card battle points from Allies or Amours + chosen weapon (in hand) and/or Ally card points
    private int shields;
    private ArrayList<Card> inHandCards = new ArrayList<>(); // 12 adventure cards
    private ArrayList<Card> inPlayCards = new ArrayList<>(); // Cards in play, Amours + Allies + Rank
    private ArrayList<ArrayList<Weapon>> questTotalStagesPlayers = new ArrayList<>(); // holds weapons in a quest for players playing the quest
    private int tournamentScore;
    private int id; // range from 1-4
    private Rank rankCard;
    private ArrayList<Integer> storyBattlePoints = new ArrayList<>(); // store the player total battle points for each stages

    public Player(String name, int id){
        this.name = name;
        this.id = id;
    }





    public String toString(){
        String result = "Name: "+ name+"\n"+rankCard+"\nPlayer Battle points: "+battlePoints+"\n";
        for(Card ac: inHandCards){
            result += ac.toString()+"\n";
        }
        return result;
    }
}