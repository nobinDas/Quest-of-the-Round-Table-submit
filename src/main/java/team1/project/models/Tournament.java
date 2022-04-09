package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Tournament extends StoryCard {

    private int bonusShields;

    public Tournament(String name,CardType type, int bonusShields){
        super(name, type);
        this.bonusShields = bonusShields;
    }

    public String toString(){
        return super.toString()+ " bonus shields: "+ bonusShields;
    }
}
