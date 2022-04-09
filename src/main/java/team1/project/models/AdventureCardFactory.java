package team1.project.models;

import java.util.ArrayList;

import static team1.project.models.CardType.*;
import static team1.project.models.CardType.ADVENTURE_AMOUR;

public class AdventureCardFactory extends CardFactory{
    @Override
    ArrayList<Card> createCard(String card) {
        ArrayList<Card> CardList = new ArrayList<>();
        if(card.equals("weapons")){
            for(int i = 0; i < 2 ; i++){
                CardList.add(new Weapon("Excalibur", CardType.ADVENTURE_WEAPON, 30));
            }
            for(int i = 0; i < 6 ; i++){
                CardList.add(new Weapon("Lance", CardType.ADVENTURE_WEAPON, 20));
            }
            for(int i = 0; i < 8 ; i++){
                CardList.add(new Weapon("Battle-ax", CardType.ADVENTURE_WEAPON, 15));
            }
            for(int i = 0; i < 16 ; i++){
                CardList.add(new Weapon("Sword", CardType.ADVENTURE_WEAPON, 10));
            }
            for(int i = 0; i < 11 ; i++){
                CardList.add(new Weapon("Horse", CardType.ADVENTURE_WEAPON, 10));
            }
            for(int i = 0; i < 6 ; i++){
                CardList.add(new Weapon("Dagger", CardType.ADVENTURE_WEAPON, 5));
            }
        }
        if(card.equals("foes")){
            CardList.add(new Foe("Dragon", CardType.ADVENTURE_FOE, 70, 50));
            for(int i = 0; i < 2 ; i++){
                CardList.add(new Foe("Giant", CardType.ADVENTURE_FOE, 40, 0));
            }
            for(int i = 0; i < 4 ; i++){
                CardList.add(new Foe("Mordred", CardType.ADVENTURE_FOE, 30, 0));
            }
            for(int i = 0; i < 2 ; i++){
                CardList.add(new Foe("Green Knight", CardType.ADVENTURE_FOE, 40, 25));
            }
            for(int i = 0; i < 3 ; i++){
                CardList.add(new Foe("Black Knight", CardType.ADVENTURE_FOE, 35, 25));
            }
            for(int i = 0; i < 6 ; i++){
                CardList.add(new Foe("Evil Knight", CardType.ADVENTURE_FOE, 30, 20));
            }
            for(int i = 0; i < 8 ; i++){
                CardList.add(new Foe("Saxon Knight", CardType.ADVENTURE_FOE, 25, 15));
            }
            for(int i = 0; i < 7 ; i++){
                CardList.add(new Foe("Robber Knight", CardType.ADVENTURE_FOE, 15, 0));
            }
            for(int i = 0; i < 5 ; i++){
                CardList.add(new Foe("Saxons", CardType.ADVENTURE_FOE, 20, 10));
            }
            for(int i = 0; i < 4 ; i++){
                CardList.add(new Foe("Boar", CardType.ADVENTURE_FOE, 15, 5));
            }

            for(int i = 0; i < 8 ; i++){
                CardList.add(new Foe("Thieves", CardType.ADVENTURE_FOE, 5, 0));
            }

        }
        if(card.equals("allies")){
            CardList.add(new Ally("Sir Galahad", CardType.ADVENTURE_ALLY, 0, 15));
            CardList.add(new Ally("Sir Lancelot", CardType.ADVENTURE_ALLY, 25, 15));
            CardList.add(new Ally("King Arthur", CardType.ADVENTURE_ALLY, 0, 10));
            CardList.add(new Ally("Sir Tristan", CardType.ADVENTURE_ALLY, 0, 10));
            CardList.add(new Ally("Sir Gawain", CardType.ADVENTURE_ALLY, 20, 10));
            CardList.add(new Ally("Sir Percival", CardType.ADVENTURE_ALLY, 20, 5));
            CardList.add(new Ally("King Pellinore", CardType.ADVENTURE_ALLY, 0, 10));
        }
        if(card.equals("amours")){
            for(int i = 0; i < 8 ; i++){
                CardList.add(new Amour("Amour", CardType.ADVENTURE_AMOUR));
            }
        }
        if(card.equals("test")){
            for(int i = 0; i < 2 ; i++){
                CardList.add(new Test("Test of Valor", CardType.ADVENTURE_TEST));
            }
        }
        return CardList;
    }
}