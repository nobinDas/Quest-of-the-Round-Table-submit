package team1.project.models;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class Rank extends Card{

    private int battlePoints;

    public Rank(String name, CardType type, int battlePoints){
        super(name, type);
        this.battlePoints = battlePoints;
    }

    public String toString(){
        return "Rank: "+ super.getName();
    }
}
