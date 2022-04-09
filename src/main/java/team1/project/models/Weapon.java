package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Weapon extends AdventureCard {

    private int battlePoints;

    public Weapon(String name,CardType type,  int battlePoints){
        super(name, type);
        this.battlePoints = battlePoints;
    }

    public String toString(){
        return super.toString()+ " battle points : "+ battlePoints;
    }
}
