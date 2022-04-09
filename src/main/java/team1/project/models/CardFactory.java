package team1.project.models;

import java.util.ArrayList;

public abstract class CardFactory {
    abstract ArrayList<Card> createCard(String item);

    public ArrayList<Card> orderCard(String type) {
        ArrayList<Card> card = createCard(type);
        System.out.println("--- Making the card of type " + type + " ---");
        return card;
    }
}