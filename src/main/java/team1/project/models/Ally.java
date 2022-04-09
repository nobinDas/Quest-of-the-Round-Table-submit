package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Ally extends AdventureCard {

    private int battlePoints;
    private int specialBattlePoints;

    public Ally(String name, CardType type, int battlePoints, int specialBattlePoints){
        super(name, type);
        this.battlePoints = battlePoints;
        this.specialBattlePoints = specialBattlePoints;
    }

    public String toString(){
        return super.toString()+ " battle points: "+ battlePoints+" special battle points: "+ specialBattlePoints;
    }
}