package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quest extends StoryCard {
    public  Player sponsor;
    private int noOfStages;
    private String specialFoe;


    public Quest(String name,CardType type, int noOfStages, String specialFoe){
        super(name, type);
        this.noOfStages = noOfStages;
        this.specialFoe = specialFoe;
    }

}
