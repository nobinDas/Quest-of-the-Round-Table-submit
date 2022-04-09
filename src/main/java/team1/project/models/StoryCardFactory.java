package team1.project.models;

import java.util.ArrayList;

import static team1.project.models.CardType.*;

public class StoryCardFactory extends CardFactory{
    @Override
    ArrayList<Card> createCard(String card) {
        ArrayList<Card> CardList = new ArrayList<>();
        if(card.equals("quest")){
            for(int i = 0; i <2; i++){
                CardList.add(new Quest("Vanquish King Arthur's Enemies", STORY_QUEST, 3, null));
            }
            for(int i = 0; i <2; i++){
                CardList.add(new Quest("Boar Hunt", STORY_QUEST, 2, "Boar"));
            }
            for(int i = 0; i <2; i++){
                CardList.add(new Quest("Repel the Saxon Raiders", STORY_QUEST, 2, "All Saxons"));
            }
            CardList.add(new Quest("Search for the Holy Grail", STORY_QUEST, 5, "All"));
            CardList.add(new Quest("Test of the Green Knight", STORY_QUEST, 4, "Green Knight"));
            CardList.add(new Quest("Search for the Questing Beast", STORY_QUEST, 4, null));
            CardList.add(new Quest("Defend the Queen's Honor", STORY_QUEST, 4, "All"));
            CardList.add(new Quest("Rescue the Fair Maiden", STORY_QUEST, 3, "Black Knight"));
            CardList.add(new Quest("Journey Through the Enchanted Forest ", STORY_QUEST, 3, "Evil Saxons"));
            CardList.add(new Quest("Slay the Dragon", STORY_QUEST, 3, "Dragon"));
        }
        if(card.equals("event")){
            for(int i = 0; i <2; i++){
                CardList.add(new Event("King's Recognition", STORY_EVENT, "The next player(s) to complete a Quest will receive 2 extra shields", 1));
            }
            for(int i = 0; i <2; i++){
                CardList.add(new Event("Queen's Favor", STORY_EVENT, "The lowest ranked player(s) immediately receives 2 Adventure Cards", 2));
            }
            for(int i = 0; i <2; i++){
                CardList.add(new Event("Court Called to Camelot", STORY_EVENT, "All Allies in play must be discarded", 3));
            }
            CardList.add(new Event("Pox", STORY_EVENT, "All players except the player drawing this card lose 1 shield", 4));
            CardList.add(new Event("Plague", STORY_EVENT, "Drawer loses 2 shields if possible", 5));
            CardList.add(new Event("Chivalrous Deed", STORY_EVENT, "Player(s) with both lowest rank and least amount of shields, receives 3 shields", 6));
            CardList.add(new Event("Prosperity Throughout the Realm", STORY_EVENT, "All players may immediately draw 2 Adventure Cards", 7));
            CardList.add(new Event("King's Call to Arms", STORY_EVENT, "The highest ranked player(s) must place 1 weapon in the discard pile. If unable to do so, 2 Foe Cards must be discarded", 8));
        }
        if(card.equals("tournament")){
            CardList.add(new Tournament("Tournament at Camelot", STORY_TOURNAMENT, 3));
            CardList.add(new Tournament("Tournament at Orkney", STORY_TOURNAMENT, 2));
            CardList.add(new Tournament("Tournament at Tintagel", STORY_TOURNAMENT, 1));
            CardList.add(new Tournament("Tournament at York", STORY_TOURNAMENT, 0));
        }
        return CardList;
    }
}