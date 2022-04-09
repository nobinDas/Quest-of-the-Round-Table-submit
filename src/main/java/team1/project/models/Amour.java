package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Amour extends AdventureCard {

    private static final int AMOUR_BATTLE_POINTS = 10;


    public Amour(String name, CardType type){
        super(name, type);

    }

    public String toString(){
        return super.toString()+ "BATTLE POINTS : "+ AMOUR_BATTLE_POINTS;
    }
}