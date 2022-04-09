package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Event extends StoryCard {

    private String specialAction;
    /**
     * this is done to keep track of the event card instead of comparing string names
     */
    private int id;

    public Event(String name,CardType type, String specialAction, int id){
        super(name, type);
        this.specialAction = specialAction;
        this.id = id;
    }

    public String toString(){
        return super.toString()+ " event special action: "+ specialAction+ " event id : "+ id;
    }
}
