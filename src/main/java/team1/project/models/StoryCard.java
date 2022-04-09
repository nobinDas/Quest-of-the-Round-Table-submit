package team1.project.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoryCard extends Card{


    public StoryCard(String name, CardType type){
        super(name, type);
    }

    public String toString(){
        return super.toString();
    }
}
