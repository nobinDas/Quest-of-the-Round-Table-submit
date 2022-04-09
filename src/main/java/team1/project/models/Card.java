package team1.project.models;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Card {

    private String name;
    private CardType cardType;

    public String toString(){
        return "Card = "+name+", Card type = "+cardType;
    }
}
