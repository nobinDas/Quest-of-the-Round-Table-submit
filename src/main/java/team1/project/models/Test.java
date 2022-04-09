package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Test extends AdventureCard {

    private static final int TEST_NO_OF_BIDS = 3;


    public Test(String name, CardType type){
        super(name, type);

    }

    public String toString(){
        return super.toString()+ " NO OF BIDS: "+ TEST_NO_OF_BIDS;
    }
}