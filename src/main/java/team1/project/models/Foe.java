package team1.project.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Foe extends AdventureCard {

    /**
     * if Foe has only one battleStrength,
     * we consider it as the max strength,
     * assigned to battleStrengthMax
     */
    private int battleStrengthMax;
    private int battleStrengthMin;

    public Foe(String name, CardType type, int battleStrengthMax, int battleStrengthMin){
        super(name, type);
        this.battleStrengthMax = battleStrengthMax;
        this.battleStrengthMin = battleStrengthMin;
    }

    public String toString(){
        return super.toString() + " max battle Strength: "+ battleStrengthMax+ " min battle strength: "+ battleStrengthMin;
    }
}
