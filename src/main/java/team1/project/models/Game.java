package team1.project.models;

import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

import static java.util.Collections.reverse;
import static team1.project.models.CardType.*;

@Getter
@Setter
public class Game {
    private String gameId;
    private ArrayList<Player> players = new ArrayList<>(); //max 4, min 2, [0,1,2,3]
    private static ArrayList<Card> adventureCardList = new ArrayList<>(); //116 for teams of 3 (-8 test cards, -3 bid Allies)
    private static  ArrayList<Card> storyCardList = new ArrayList<>(); //28

    private ArrayList<Card> discardedAdventureCardList = new ArrayList<>();
    private ArrayList<Card> discardedStoryCardList = new ArrayList<>();
    private int currentPlayer = 0; // this is the current player of the game
    private int currentStoryPlayer; // within a story
    private static int kingsRecognitionEventShields = 0;
    private static int winners = 0;
    private static Scanner scanner;
    public static int totalNumPlayers;
    public static Card currentStoryCard;
    public static Tournament tournamentCard;
    public ArrayList<Player> playersInStory = new ArrayList<>();
    public ArrayList<Integer> sponsorStageBattlePoints = new ArrayList<>();
    private int playersJoined = 0;
    private GameStatus status;
    private int playersCardDone=0;
    private ArrayList<Player> PlayWinner = new ArrayList<>();
    private boolean chooseCard = false;
    private int totalPoints = 0;
    private int highest = 0;


    //for test
    private String message = "Battle points for the stages are:";
    private String testBidMessage = "Bid minimum: ";
    private boolean test = false;
    private int bid = 2;
    private int TestCardStageNo = -1;
    private int TestWinnerPlayerId = -1;
    private int askTestWinner = -1;
    private boolean skip = false;

    // 2D Array list for the sponsor cards in a quest
    private ArrayList<ArrayList<Card>> questTotalStagesSponsor = new ArrayList<>();

    // --- Champion Knight / Win condition ---
    private ArrayList<Player> gameWinners = new ArrayList<>();
    private boolean isGameOver = false;

    // --- Tournament tie-breaking (Round 2) ---
    private int tournamentRound = 1;
    private ArrayList<Player> tiedTournamentPlayers = new ArrayList<>();

    // --- Sponsor rotation (declining sponsorship) ---
    private int nextSponsorId = -1;
    private ArrayList<Integer> sponsorDeclinedIds = new ArrayList<>();

    // --- Mordred special foe ---
    private boolean mordredActive = false;

    // --- Free bids (for test stage bidding) ---
    private int effectiveBidAmount = 0; // bid amount minus free bids from Ally cards

    // --- Validation feedback for the frontend ---
    private String validationMessage = "";


    public void setTotalNumPlayers(int num){
        totalNumPlayers = num;
    }

    public int getTotalNumPlayers() {
        return totalNumPlayers;
    }

    public void createPlayer(Player player){
        Player p = new Player(player.getName(),playersJoined);
        players.add(p);
        playersJoined++;
    }

    /**
     * draw an adventure card for that player
     * @param ID
     */
    public void drawAdventureCard(String ID){
        Player p = null;
        // make sure it's not empty
        if(adventureCardList.size() == 0){
            //shuffle discarded pile and move it to adventure deck + give 1 card
            System.out.println("Adventure card list is empty, reshuffling the discard pile to use it !");
            Collections.shuffle(discardedAdventureCardList);
            adventureCardList.clear();
            adventureCardList.addAll(discardedAdventureCardList);
            discardedAdventureCardList.clear();
        }

        for (Player j : players){
            System.out.println(j.getId());
            if (j.getId() == Integer.parseInt(ID)){
                p = j;
                break;
            }
        }

        Card newCard  = adventureCardList.get(0);
        p.getInHandCards().add(newCard); // assign player the new adventure card from the adventure deck
        if(newCard.getCardType()==ADVENTURE_ALLY || newCard.getCardType()==ADVENTURE_AMOUR ){
            populateInPlayCardsFrontend(p);
        }
        adventureCardList.remove(0); // remove that card

    }

    //for frontend to set players to Current tournament
    public void settournamentPlayer(String playerID){
        playersInStory.add(players.get(Integer.parseInt(playerID)));

        drawAdventureCard(playerID);
    }
    public void settournamentCard(String playerID, String cardIndex){
        currentStoryPlayer = currentPlayer;
        String choice = "";
        int count = 0;
        int score = 0;
        int NumberOfPlayersInTournament;
        int player = -1;


        NumberOfPlayersInTournament = playersInStory.size();
        if (NumberOfPlayersInTournament==0){
            return;
        }

        for(int i=0;i<NumberOfPlayersInTournament;i++){
            if(playersInStory.get(i).getId() == Integer.parseInt(playerID)){
                player = i;
            }
        }

        String[] values = cardIndex.split(",");

        ArrayList<Integer> nums = new ArrayList<>();
        for (int c = 0; c < values.length; c++) {
            int l = Integer.parseInt(values[c]);
            nums.add(l);
        }

        Collections.sort(nums, Collections.reverseOrder());

        for (var k = 0; k < values.length; k++) {
            int cardRow = nums.get(k);

            // place each Ally into the in play list, and count amours and weapons before discarding them
            if (playersInStory.get(player).getInHandCards().get(cardRow).getCardType() == ADVENTURE_AMOUR) {
                totalPoints += 10;
                System.out.println("Player " + playersInStory.get(player).getName() + " has chosen : " + playersInStory.get(player).getInHandCards().get(cardRow).getName());
                discardedAdventureCardList.add(playersInStory.get(player).getInHandCards().get(cardRow));
                playersInStory.get(player).getInHandCards().remove(cardRow);
            } else if (playersInStory.get(player).getInHandCards().get(cardRow).getCardType() == ADVENTURE_ALLY) {
                Ally card2 = (Ally) playersInStory.get(player).getInHandCards().get(cardRow);
                totalPoints += card2.getBattlePoints(); //apply regular battle points
                playersInStory.get(player).getInPlayCards().add(playersInStory.get(player).getInHandCards().get(cardRow));
                System.out.println("Player " + playersInStory.get(player).getName() + " has chosen : " + playersInStory.get(player).getInHandCards().get(cardRow).getName());
                discardedAdventureCardList.add(playersInStory.get(player).getInHandCards().get(cardRow));
                playersInStory.get(player).getInHandCards().remove(cardRow);
            } else if (playersInStory.get(player).getInHandCards().get(cardRow).getCardType() == ADVENTURE_WEAPON) {
                Weapon card2 = (Weapon) playersInStory.get(player).getInHandCards().get(cardRow);
                totalPoints += card2.getBattlePoints();
                System.out.println("Player " + playersInStory.get(player).getName() + " has chosen : " + playersInStory.get(player).getInHandCards().get(cardRow).getName());
                discardedAdventureCardList.add(playersInStory.get(player).getInHandCards().get(cardRow));
                playersInStory.get(player).getInHandCards().remove(cardRow);
            }
            System.out.println("Player " + playersInStory.get(player).getName() + " tournament total points is : " + totalPoints);
            playersInStory.get(player).setTournamentScore(totalPoints);
            System.out.println("Player " + playersInStory.get(player).getName() + " now has : " + playersInStory.get(player).getShields() + " shields !");
            score = totalPoints;
            totalPoints=0;
        }
        if (score > highest) {
            highest = score;
        }

        if(playersInStory.get(NumberOfPlayersInTournament-1).getId() == Integer.parseInt(playerID)){
            Tournament card = (Tournament) currentStoryCard;

            // Collect tied players (those matching the highest score)
            ArrayList<Player> roundWinners = new ArrayList<>();
            for(Player p : playersInStory){
                if(p.getTournamentScore() >= highest && p.getTournamentScore() > 0){
                    roundWinners.add(p);
                }
            }

            if(roundWinners.size() > 1 && tournamentRound == 1){
                // TIE after Round 1 — initiate Round 2 with tied players only
                System.out.println("Tournament tie! Initiating Round 2 for " + roundWinners.size() + " tied players.");
                tournamentRound = 2;
                tiedTournamentPlayers.clear();
                tiedTournamentPlayers.addAll(roundWinners);
                for(Player p : players){ p.setTournamentScore(0); }
                playersInStory.clear();
                playersInStory.addAll(tiedTournamentPlayers);
                highest = 0;
                // Draw one new adventure card for each tied player
                for(Player p : tiedTournamentPlayers){
                    drawAdventureCard(Integer.toString(p.getId()));
                }
                PlayWinner.clear();
                return; // frontend will ask tied players to resubmit
            }

            // Round 2 (or no tie in Round 1) — determine final winners
            if(roundWinners.isEmpty()){
                // Fallback: anyone who joined gets 1 shield
                roundWinners.addAll(playersInStory);
            }
            for(Player p : roundWinners){
                PlayWinner.add(p);
                p.setShields(p.getShields() + NumberOfPlayersInTournament + card.getBonusShields());
                System.out.println("Player " + p.getName() + " won the tournament with "
                        + p.getShields() + " shields.");
                checkRankAdvancement(p);
            }
            for(Player p : players){ p.setTournamentScore(0); }
            tournamentRound = 1;
            tiedTournamentPlayers.clear();
        }
    }


    // This method asks a player to place an amour or ally card into their list of
    // inplay cards at anypoint in the game when it's called
    public void populateInPlayCards(Player p){
        boolean correctChoice = true;

        do{
            System.out.println("\n" + p.getName() + " you now have the choice to place an Ally or Amour card in play, please choose card by entering a number : \n ");
            System.out.println("Player : " + p.getName() + " list of cards to choose from : ");

            printCards(p.getInHandCards());
            int choice = scanner.nextInt();

            scanner.nextLine();

            if(p.getInHandCards().get(choice).getCardType() == ADVENTURE_ALLY){
                p.getInPlayCards().add(p.getInHandCards().get(choice));
                p.getInHandCards().remove(choice);
            }else if (p.getInHandCards().get(choice).getCardType() == ADVENTURE_AMOUR){
                p.getInPlayCards().add(p.getInHandCards().get(choice));
                p.getInHandCards().remove(choice);
            }else {
                System.out.println("You can only choose an amour or an Ally. please try again : ");
                correctChoice = false;
            }
        }while(!correctChoice);
    }

    public void populateInPlayCardsFrontend(Player p) {
        chooseCard = true;
    }

    public void selectAlly(String ID, String index){
        Player p = null;
        for (Player j : players){
            System.out.println(j.getId());
            if (j.getId() == Integer.parseInt(ID)){
                p = j;
                break;
            }
        }
        assert p != null;
        Card card = p.getInHandCards().get(Integer.parseInt(index));
        p.getInPlayCards().add(card);

        p.getInHandCards().remove(Integer.parseInt(index));
        System.out.println("new deck: " + p.getInHandCards());
        System.out.println("new in play cards: " + p.getInPlayCards());
    }


    public void discardAdventureCard(String ID, String index){
        Player p = null;
        for (Player j : players){
            System.out.println(j.getId());
            if (j.getId() == Integer.parseInt(ID)){
                p = j;
                break;
            }
        }
        assert p != null;
        Card card = p.getInHandCards().get(Integer.parseInt(index));
        discardedAdventureCardList.add(card);

        p.getInHandCards().remove(Integer.parseInt(index));
        System.out.println("new deck: " + p.getInHandCards());
    }

    public void discardAdventureCards(String playerID, String[] cards){

        Player p = null;
        for (Player j : players){
            System.out.println(j.getId());
            if (j.getId() == Integer.parseInt(playerID)){
                p = j;
                break;
            }
        }
        assert p != null;
        ArrayList<Card> tobeDisc = new ArrayList<>();
        int counter=0;
        System.out.println("playerID: " + playerID);
        System.out.println(p);
        System.out.println(p.getInHandCards());

        for(Card pls : p.getInHandCards()){
            tobeDisc.add(pls);
        }

        for (String i : cards){
            int index = Integer.parseInt(i);
            Card card = p.getInHandCards().get(index);
            discardedAdventureCardList.add(card);
            tobeDisc.remove(card);
            System.out.println(p.getInHandCards());

        }
        p.setInHandCards(tobeDisc);



        System.out.println("new deck: " + p.getInHandCards());


    }

    public void findSponsor(Quest card) {
        String choice = "";

        int count = 0;

        // creating a circular array
        for (var i = currentPlayer; i <= totalNumPlayers; i++) {
            System.out.println(players.get(i-1).getName()+ ", Do you want to sponsor Quest "+card.getName()+" ? (y/n)");
            choice = scanner.next();
            if(choice.equals("y")){
                card.setSponsor(players.get(i-1));
                return;
            }
            count++;
        }
        if (count < totalNumPlayers) {
            for(var j = 1; j <= ( totalNumPlayers-count) ;j++){
                System.out.println(players.get(j-1).getName()+ ", Do you want to sponsor Quest "+card.getName()+" ? (y/n)");
                choice = scanner.next();
                if(choice.equals("y")){
                    card.setSponsor(players.get(j-1));
                    break;
                }
            }

        }
    }


    //assuming sponsor is found
    public void whoWantsToPlay(StoryCard currentStoryCard){

        String choice = "";
        int count = 0;

        if(currentStoryCard.getCardType() == STORY_QUEST){
            Quest card = (Quest) currentStoryCard;
            int sponsorId = card.getSponsor().getId();

            //ask every user if they want to play, except sponsor
            //starting from Sponsor's left
            for(int i = sponsorId+1; i <= totalNumPlayers; i++){
                System.out.println(players.get(i-1).getName()+", do you want to play in the quest"+card.getName()+" ? (y/n)\n");
                choice = scanner.next();
                if(choice.equals("y")){
                    playersInStory.add(players.get(i-1));
                }
                count++;
            }

            if (count < totalNumPlayers) {
                for(var j = 1; j <= ( totalNumPlayers-count-1) ;j++){
                    System.out.println(players.get(j-1).getName()+ ", do you want to play in the quest"+card.getName()+" ? (y/n)\n");
                    choice = scanner.next();
                    if(choice.equals("y")){
                        playersInStory.add(players.get(j-1));
                    }
                }
            }
        }

        if (playersInStory.size() >0) {
            // currentStoryPlayer = 0;
            System.out.println("*******************************\n");
            System.out.println("*****Players joining story*****\n");
            System.out.println("*******************************\n");
            for (int i = 0; i < (playersInStory.size()); i++) {
                System.out.println(playersInStory.get(i).getName() + ", has been added to the game\n");
            }
        } else {
            System.out.println("No one wants to play in the quest.\n");
            // need to discard story card, and move current player to sponsor + 1
            discardedStoryCardList.add(currentStoryCard);
        }
    }

    /**
     * draw the first story card available from the list (for now just use Quest***)
     * @return
     */
    public Card drawStoryCard(String ID){
        PlayWinner.clear();
        playersInStory.clear();
        sponsorDeclinedIds.clear();
        mordredActive = false;
        highest = 0;
        tournamentRound = 1;
        tiedTournamentPlayers.clear();

        if(storyCardList.size() == 0){
            System.out.println("Story card list is empty, reshuffling the discard pile.");
            Collections.shuffle(discardedStoryCardList);
            storyCardList.clear();
            storyCardList.addAll(discardedStoryCardList);
            discardedStoryCardList.clear();
        }

        // Draw the top card of the shuffled deck
        Card card = storyCardList.get(0);
        discardedStoryCardList.add(card);
        storyCardList.remove(0);

        currentStoryCard = card;
        currentPlayer = Integer.parseInt(ID);

        // For quest cards, initialise sponsor rotation starting with the drawing player
        if(card.getCardType() == STORY_QUEST){
            nextSponsorId = Integer.parseInt(ID);
        }

        return card;
    }

    public void printCards(ArrayList<Card> cards){
        for(var i =0 ; i < cards.size(); i++){
            System.out.println(i+" : "+ cards.get(i).getName()+", "+ cards.get(i).getCardType());
        }
    }

    public void discardAdventureCard(Player p){

        System.out.println(p.getName()+", you have more than 12 in hand cards Please discard the extra card ");
//        printCards(p.getInHandCards());
        System.out.print("Enter index from (0 - "+ (p.getInHandCards().size()-1)+" ):  ");
        int choice = scanner.nextInt();
        System.out.println("You have decided to discard card at index "+ choice+", name= "+ p.getInHandCards().get(choice).getName());
        Card card = p.getInHandCards().get(choice);
        discardedAdventureCardList.add(card);
        p.getInHandCards().remove(choice);
    }


    public void letTheTournamentsBegin(Tournament card) {

        //Tournament card = (Tournament) currentStoryCard;
        int totalPoints = 0;
        currentStoryPlayer = currentPlayer;
        String choice = "";
        int count = 0;
        int score = 0;
        int highest = 0;
        int NumberOfPlayersInTournament;
        playersInStory.clear();

        System.out.println("*************************  WELCOME TO THE TOURNAMENTS ! *************************");
        // creating a circular array
        for (var i = currentStoryPlayer; i <= totalNumPlayers; i++) {
            System.out.println(players.get(i-1).getName() + ", Do you want to play in the tournament : " + card.getName() + " ? (y/n)");
            choice = scanner.next();
            if (choice.equals("y")) {
                playersInStory.add(players.get(i-1));
            }
            count++;
        }
        if (count < totalNumPlayers) {
            for (var j = 1; j <= (totalNumPlayers - count); j++) {
                System.out.println(players.get(j - 1).getName() + ", Do you want to play in the tournament : " + card.getName() + " ? (y/n)");
                choice = scanner.next();
                if (choice.equals("y")) {
                    playersInStory.add(players.get(j - 1));
                }
            }
        }

        NumberOfPlayersInTournament = playersInStory.size();
        if (NumberOfPlayersInTournament==0){
            return;
        }
        System.out.println(" Tournament : " + card.getName() + " has started, and it has " + card.getBonusShields() + " shields for the winner(s) + total number of players who joined !");

        scanner.nextLine();
        for (var i = 0; i < playersInStory.size(); i++) {

            System.out.println(playersInStory.get(i).toString());
            System.out.println(playersInStory.get(i).getName() + " Please enter the  cards you would like to play for the tournament, seperated by a comma (ex. 3,7,10): ");
            String input = scanner.nextLine();
            String[] values = input.split(",");

            // creating an array to store parsed integers
            ArrayList<Integer> nums = new ArrayList<>();

            for (int c = 0; c < values.length; c++) {
                int l = Integer.parseInt(values[c]);
                nums.add(l);
            }
            // sorting entered choices in reverse order to remove cards correctly without affecting indexes
            Collections.sort(nums, Collections.reverseOrder());


            for (var k = 0; k < values.length; k++) {
                int cardRow = nums.get(k);

                // place each Ally into the in play list, and count amours and weapons before discarding them
                if (playersInStory.get(i).getInHandCards().get(cardRow).getCardType() == ADVENTURE_AMOUR) {
                    totalPoints += 10;
                    System.out.println("Player " + playersInStory.get(i).getName() + " has chosen : " + playersInStory.get(i).getInHandCards().get(cardRow).getName());
                    discardedAdventureCardList.add(playersInStory.get(i).getInHandCards().get(cardRow));
                    playersInStory.get(i).getInHandCards().remove(cardRow);
                } else if (playersInStory.get(i).getInHandCards().get(cardRow).getCardType() == ADVENTURE_ALLY) {
                    Ally card2 = (Ally) playersInStory.get(i).getInHandCards().get(cardRow);
                    totalPoints += card2.getBattlePoints(); //apply regular battle points
                    playersInStory.get(i).getInPlayCards().add(playersInStory.get(i).getInHandCards().get(cardRow));
                    System.out.println("Player " + playersInStory.get(i).getName() + " has chosen : " + playersInStory.get(i).getInHandCards().get(cardRow).getName());
                    discardedAdventureCardList.add(playersInStory.get(i).getInHandCards().get(cardRow));
                    playersInStory.get(i).getInHandCards().remove(cardRow);
                } else if (playersInStory.get(i).getInHandCards().get(cardRow).getCardType() == ADVENTURE_WEAPON) {
                    Weapon card2 = (Weapon) playersInStory.get(i).getInHandCards().get(cardRow);
                    totalPoints += card2.getBattlePoints();
                    System.out.println("Player " + playersInStory.get(i).getName() + " has chosen : " + playersInStory.get(i).getInHandCards().get(cardRow).getName());
                    discardedAdventureCardList.add(playersInStory.get(i).getInHandCards().get(cardRow));
                    playersInStory.get(i).getInHandCards().remove(cardRow);
                }
                System.out.println("Player " + playersInStory.get(i).getName() + " tournament total points is : " + totalPoints);
                playersInStory.get(i).setTournamentScore(totalPoints);
                System.out.println("Player " + playersInStory.get(i).getName() + " now has : " + playersInStory.get(i).getShields() + " shields !");
                score = totalPoints;
                totalPoints=0;
            }

            if (score > highest) {
                highest = score;
            }
        }

        // clear out the playersInStory list

        if (playersInStory.size()>0) {
            for(Player p: players){
                // declare the winners
                if(p.getTournamentScore() >= highest){
                    int playerShields = p.getShields();
                    System.out.println("Player " + p.getName() + " won the tournament, and will get shields equivalent to : ");
                    System.out.println("Num of people at the beginning of the tournament '" + NumberOfPlayersInTournament + "' + the card's number of bonus shields '" + card.getBonusShields() + "'" );
                    playerShields+= NumberOfPlayersInTournament + card.getBonusShields();
                    p.setShields(playerShields);
                    System.out.println("Player "+ p.getName()+ " has " + p.getShields() + " shields");

                }else{
                    System.out.println("Player "+ p.getName()+ " has " + p.getShields() + " shields");
                }
                //reset player's tournament score
                p.setTournamentScore(0);
            }
        }


        playersInStory.clear();
    }







    // Comparing battle points for each stage between sponsor vs players



    /**
     * calculate the number of cards a player p wants to bid for a test card
     */
    public Player testStageFight(Player p, Card testCard) {

        int bid = 0;

        if (playersInStory.size() == 1) {
            System.out.println(playersInStory.get(0).getName() + ", You are fighting against test card: " + testCard.getName());
            System.out.print("Enter the number of cards you want to bid (minimum 3), or a number below 3 to quit the stage and lose the quest : ");
            String input = scanner.nextLine();
            int bid1 = Integer.parseInt(input);
            bid = bid1;
            p=playersInStory.get(0);
            System.out.println("Player " + p.getName() + " will now lose " + bid1 + " cards for the test");
            for (int i=0; i<bid1; i++) {
                discardAdventureCard(p);
            }
            return p;
        } else if (playersInStory.size() == 2) {
            System.out.println(playersInStory.get(0).getName() + ", You are fighting against test card: " + testCard.getName());
            System.out.print("Enter the number of cards you want to bid (minimum 3), or a number below 3 to quit the stage and lose the quest : ");
            String input = scanner.nextLine();
            int bid1 = Integer.parseInt(input);
            System.out.println(playersInStory.get(1).getName() + ", You are fighting against test card: " + testCard.getName());
            System.out.print("Enter the number of cards you want to bid (minimum 3), or a number below 3 to quit the stage and lose the quest : ");
            String input2 = scanner.nextLine();
            int bid2 = Integer.parseInt(input2);
            if (bid1 > bid2) {
                bid = bid1;
                p=playersInStory.get(0);
                System.out.println("Player " + p.getName() + " will now lose " + bid1 + " cards for the test");
                for (int i=0; i<bid1; i++) {
                    discardAdventureCard(p);
                }
                return p;
            } else {
                bid = bid2;
                p=playersInStory.get(1);
                System.out.println("Player " + p.getName() + " will now lose " + bid2 + " cards for the test");
                for (int i=0; i<bid2; i++) {
                    discardAdventureCard(p);
                }
                return p;
            }
        } else if (playersInStory.size() == 3) {
            System.out.println(playersInStory.get(0).getName() + ", You are fighting against test card: " + testCard.getName());
            System.out.println("Enter the number of cards you want to bid (minimum 3), or a number below 3 to quit the stage and lose the quest : ");
            String input = scanner.nextLine();
            int bid1 = Integer.parseInt(input);
            System.out.println(playersInStory.get(1).getName() + ", You are fighting against test card: " + testCard.getName());
            System.out.println("Enter the number of cards you want to bid (minimum 3), or a number below 3 to quit the stage and lose the quest : ");
            String input2 = scanner.nextLine();
            int bid2 = Integer.parseInt(input2);
            System.out.println(playersInStory.get(2).getName() + ", You are fighting against test card: " + testCard.getName());
            System.out.println("Enter the number of cards you want to bid (minimum 3), or a number below 3 to quit the stage and lose the quest : ");
            String input3 = scanner.nextLine();
            int bid3 = Integer.parseInt(input3);

            if ((bid1 > bid2) && (bid1 > bid3)) {
                bid = bid1;
                p=playersInStory.get(0);
                System.out.println("Player " + p.getName() + " will now lose " + bid1 + " cards for the test");
                for (int i=0; i<bid1; i++){
                    discardAdventureCard(p);
                }
                return p;
            } else if (bid2 > bid3) {
                bid = bid2;
                p=playersInStory.get(1);
                System.out.println("Player " + p.getName() + " will now lose " + bid2 + " cards for the test");
                for (int i=0; i<bid2; i++){
                    discardAdventureCard(p);
                }
                return p;
            } else {
                bid = bid3;
                p=playersInStory.get(2);
                System.out.println("Player " + p.getName() + " will now lose " + bid3 + " cards for the test");
                for (int i=0; i<bid3; i++){
                    discardAdventureCard(p);
                }
                return p;
            }
        }
        return p;
    }
//        do{
//            System.out.println(p.getName() + ", You are fighting against test card: "+ testCard.getName());
//            System.out.print("Enter the number of cards you want to bid against the test card (minimum 3) : ");
//            bid = scanner.nextInt();
//        }while(bid < 3);


    // populating the questTotalStagesPlayers Array with weapon cards chosen to fight against sponsor's stages

    public void beginQuestForPlayers(){
        Quest card = (Quest) currentStoryCard;
        System.out.println(card.getName()+" - number of stages : \n"+card.getNoOfStages() + " stages");
        System.out.println("Special Foe : " + card.getSpecialFoe());
        currentStoryPlayer = currentPlayer;

        for (var m=0; m< card.getNoOfStages(); m++){
            aa:
            //for(var i = currentStoryPlayer-1; i < playersInStory.size(); i++){
            for(var i = 0; i < playersInStory.size(); i++){

                //populate player stage cards
                for(var j =0; j < card.getNoOfStages(); j++){
                    playersInStory.get(i).getQuestTotalStagesPlayers().add(new ArrayList<>());
                }
                //scanner.nextLine();



                //for the current stage go inside questTotalStagesSponsor arraylist and get the card type
                if (questTotalStagesSponsor.get(m).get(0).getCardType() == ADVENTURE_TEST) {
                    Player s = testStageFight(playersInStory.get(i),questTotalStagesSponsor.get(m).get(0));
                    System.out.println("******************* we are in the test phase********************");
                    System.out.println(" The winner of the test is " + s.getName());


                    int test = playersInStory.size();
                    for (int v=0; v<test; v++){
                        System.out.println("Current player is : " + playersInStory.get(v).getName());
                        if(!playersInStory.get(v).getName().equals(s.getName())){
                            // remove the losers
                            System.out.println("removing player : " + playersInStory.get(v).getName());
                            playersInStory.remove(v);
                            v--;
                            test--;
                        }
                        System.out.println("Current size is : " + playersInStory.size());
                    }

                    i--;
                    break aa;
                }


                System.out.println(playersInStory.get(i).getName()+" Please enter the weapon cards you would like to play for stage: " + (m+1)+": " + "seperated by a comma (ex. 3,7,10): ");
                System.out.println(playersInStory.get(i).toString());

                scanner.nextLine(); // added for the string"" bug, to clear the scanner buffer
                String input = scanner.nextLine();
                String[] values = input.split(",");

                // creating an array to store parsed integers
                ArrayList<Integer> nums = new ArrayList<>();

                for(int c=0; c < values.length; c++) {
                    int l = Integer.parseInt(values[c]);
                    nums.add(l);
                }
                // sorting entered choices in reverse order to remove cards correctly without affecting indexes
                Collections.sort(nums, Collections.reverseOrder());


                for(var k =0; k < values.length; k++){
                    int cardRow = Integer.parseInt(values[k]); //
                    playersInStory.get(i).getQuestTotalStagesPlayers().get(m).add((Weapon)playersInStory.get(i).getInHandCards().get(cardRow));
                }

//                removing chosen cards
                for(var k =0; k < nums.size(); k++){
                    int cardRow = nums.get(k);
                    discardedAdventureCardList.add(playersInStory.get(i).getInHandCards().get(cardRow));
                    playersInStory.get(i).getInHandCards().remove(cardRow);
                    /**
                     System.out.println("******** Chosen Weapon's Battle points **********");
                     System.out.println(((Weapon) playersInStory.get(i).getInHandCards().get(cardRow)).getBattlePoints());
                     System.out.println("*************************************************");
                     */
                }
//                printCards(playersInStory.get(i).getInHandCards());
            } // players
        } // stages
    }

    // Minimum 1 Foe or Test per stage
    // 1 Foe + any number of UNIQUE weapons
    // Battle points need to be incremental, ignoring a test
    // max 1 test per quest

    // beginQuest begins quest for the sponsor to populate questTotalStagesSponsor with the sponsor's choice of cards

    public void beginQuest(){
        Quest card = (Quest) currentStoryCard;
        System.out.println(card.getName()+" has \n"+card.getNoOfStages() + " stages");
        System.out.println("Special Foe : " + card.getSpecialFoe());

        for(int i =0; i < card.getNoOfStages(); i++){
            questTotalStagesSponsor.add(new ArrayList<>());
        }
        scanner.nextLine();
        for (var i=0; i< card.getNoOfStages(); i++){
            System.out.print(card.getSponsor().getName()+" Please enter Foe/Weapon or (1 test card per quest) you'd like to play for stage: " + (i+1)+": " + "seperated by a comma (ex. 3,7,10): ");


            String input = scanner.nextLine();
            String[] values = input.split(",");

            // creating an array to store parsed integers
            ArrayList<Integer> nums = new ArrayList<>();

            for(int c=0; c < values.length; c++) {
                int l = Integer.parseInt(values[c]);
                nums.add(l);
            }
            // sorting entered choices in reverse order to remove cards correctly without affecting indexes
            Collections.sort(nums, Collections.reverseOrder());


            for(var k =0; k < values.length; k++){
                int cardRow = Integer.parseInt(values[k]);
                questTotalStagesSponsor.get(i).add(card.getSponsor().getInHandCards().get(cardRow));
            }

            // REMOVING CHOSEN CARDS
            for(var k =0; k < nums.size(); ++k){
                int cardRow = nums.get(k);
                discardedAdventureCardList.add(card.getSponsor().getInHandCards().get(cardRow));
                card.getSponsor().getInHandCards().remove(cardRow);
            }

            // PRINT IN-HAND CARDS
//            printCards(card.getSponsor().getInHandCards());

        }

        System.out.println("total quest size "+ questTotalStagesSponsor.size());
        for(int i = 0; i < questTotalStagesSponsor.size(); i++){
            System.out.println("stage "+(i+1));
            for(int j = 0; j < questTotalStagesSponsor.get(i).size(); j++){
                System.out.println(questTotalStagesSponsor.get(i).get(j).getName());
            }
        }

        //stages ready to play
        System.out.println("Stages ready for players to play**************");
        System.out.println("Players included in the quest: ");
        for (Player p: playersInStory
        ) {
            System.out.println("Name: "+ p.getName()+ ", id: "+ p.getId());
        }

    }
    //front end version — supports sponsor rotation when a player declines
    public void sponsorOfQuest(String player){
        // "null" or "-1" means the current candidate declined
        if(player == null || player.equals("null") || player.equals("-1")){
            int decliningId = nextSponsorId;
            sponsorDeclinedIds.add(decliningId);

            // Advance clockwise to next candidate
            int nextId = (decliningId + 1) % totalNumPlayers;

            // Skip players who already declined
            int attempts = 0;
            while(sponsorDeclinedIds.contains(nextId) && attempts < totalNumPlayers){
                nextId = (nextId + 1) % totalNumPlayers;
                attempts++;
            }

            if(attempts >= totalNumPlayers){
                // All players declined — discard quest card
                discardedStoryCardList.add(currentStoryCard);
                currentStoryCard = null;
                nextSponsorId = -1;
                System.out.println("All players declined sponsorship. Quest discarded.");
            } else {
                nextSponsorId = nextId;
                System.out.println("Sponsorship offered to player " + nextId);
            }
            return;
        }

        // Player accepted sponsorship
        currentPlayer = Integer.parseInt(player);
        currentStoryPlayer = currentPlayer;
        nextSponsorId = -1;
        sponsorDeclinedIds.clear();

        Quest card = (Quest) currentStoryCard;
        card.setSponsor(players.get(Integer.parseInt(player)));
        currentStoryCard = card;
    }

    public void bidCards(String playerID, String card){
        for (Player j : players){
            System.out.println(j.getId());
            if (j.getId() == Integer.parseInt(playerID)){
                String input = card;
                String[] cards = input.split(",");
                ArrayList<Card> newList = new ArrayList<>();
                for(Card pls : players.get(j.getId()).getInHandCards()){
                    newList.add(pls);
                }
                for (var k=0; k< cards.length; k++){
                        int cardRow = Integer.parseInt(cards[k]);
                        Card ac= players.get(j.getId()).getInHandCards().get(cardRow);
                        discardedAdventureCardList.add(ac);
                        newList.remove(ac);
                }
                players.get(j.getId()).setInHandCards(newList);
            }
        }
        message = null;
        message = "Battle points for the stages are:";
        testBidMessage = "Bid minimum: ";
        test = false;
        bid = 3;
        TestCardStageNo = -1;
        TestWinnerPlayerId = -1;
    }

    //test contest
    public void DoBid(String playerID, String Bid){
        Quest card= (Quest) currentStoryCard;

        if(playersInStory.size() == 1){
            if(Integer.parseInt(Bid) > 2){
                bid = Integer.parseInt(Bid);
//                testBidMessage = "Bid more than: " + bid;
            }else{
                playersInStory.remove(players.get(Integer.parseInt(playerID)));
            }
        }else {
            if (Integer.parseInt(Bid) <= bid) {
                playersInStory.remove(players.get(Integer.parseInt(playerID)));
            } else {
                bid = Integer.parseInt(Bid);
                testBidMessage = "Bid more than: " + bid;
            }
        }

        //when only sponser left in test declare winner
        if(playersInStory.size() == 0){
            TestWinnerPlayerId = card.sponsor.getId();

            System.out.println("TestWinnerPlayerId: "+TestWinnerPlayerId);

            PlayWinner.add(((Quest) currentStoryCard).sponsor);
            System.out.println(PlayWinner.get(0).getName()+" won the quest !");

            playersInStory.clear();

            //do all these when player select cards to discard fot bidding
//            message = "Battle points for the stages are:";
//            testBidMessage = "Bid minimum: ";
//            test = false;
//            bid = 3;
//            TestCardStageNo = -1;
//            TestWinnerPlayerId = -1;

            return;
        }
        //when only one player left and bid more than the sponsor("=" is for the 2 player game)
        else if(playersInStory.size() == 1 && bid >= 3){
            TestWinnerPlayerId = playersInStory.get(0).getId();
            System.out.println("TestWinnerPlayerId: "+TestWinnerPlayerId);

            // Apply free bids: reduce cards the winner needs to discard
            Player testWinner = playersInStory.get(0);
            int freeBids = calculatePlayerFreeBids(testWinner);
            effectiveBidAmount = Math.max(0, bid - freeBids);
            if(freeBids > 0){
                System.out.println(testWinner.getName() + " has " + freeBids
                    + " free bids — only needs to discard " + effectiveBidAmount + " cards.");
            }

            initializePlayerBattlePointsList();
            questStartFight();
            if(PlayWinner.size() == 0){
                PlayWinner.add(((Quest) currentStoryCard).sponsor);
                System.out.println(PlayWinner.get(0).getName()+" won the quest !");
            }

            //do all these when player select cards to discard fot bidding
//            message = "Battle points for the stages are:";
//            testBidMessage = "Bid minimum: ";
//            test = false;
//            bid = 3;
//            TestCardStageNo = -1;
//            TestWinnerPlayerId = -1;

            return;
        }
    }

    //front end version
    public void appendPlayerInPlayersInStory(String player){
        currentStoryPlayer = Integer.parseInt(player);
        playersInStory.add(players.get(Integer.parseInt(player)));
    }

    //front end version
    public void storePlayCard(String player, String choosenCards){
        Quest card= (Quest) currentStoryCard;
        if(players.get(Integer.parseInt(player)) == card.sponsor){
            for(int i =0; i < card.getNoOfStages(); i++) {
                questTotalStagesSponsor.add(new ArrayList<>());
            }
            String input = choosenCards;
            String[] stageValues = input.split(" ");
            boolean hasTestCard = false;
            for (var k=0; k< card.getNoOfStages(); k++){
                String[] Values = stageValues[k].split(",");
                for(var l =0; l < Values.length; l++){
                    int cardRow = Integer.parseInt(Values[l].trim());
                    Card chosen = card.getSponsor().getInHandCards().get(cardRow);

                    // Enforce: only 1 Test card per quest
                    if(chosen.getCardType() == ADVENTURE_TEST){
                        if(hasTestCard){
                            validationMessage = "Only 1 Test card allowed per quest. Extra test card ignored.";
                            System.out.println(validationMessage);
                            continue;
                        }
                        hasTestCard = true;
                        testBidMessage = "Bid minimum: " + 3;
                        test = true;
                    }

                    // Enforce: no duplicate weapon types in the same stage
                    if(chosen.getCardType() == ADVENTURE_WEAPON){
                        boolean duplicate = false;
                        for(Card existing : questTotalStagesSponsor.get(k)){
                            if(existing.getCardType() == ADVENTURE_WEAPON &&
                               existing.getName().equals(chosen.getName())){
                                duplicate = true;
                                break;
                            }
                        }
                        if(duplicate){
                            validationMessage = "Duplicate weapon '" + chosen.getName() + "' in stage " + (k+1) + " ignored.";
                            System.out.println(validationMessage);
                            continue;
                        }
                    }

                    questTotalStagesSponsor.get(k).add(chosen);
                }
            }
            for(var p =0; p < questTotalStagesSponsor.size(); p++){
                int points = calculateStageBattlePoints(questTotalStagesSponsor.get(p));
                if(points == 0){
                    TestCardStageNo = p;
                    System.out.println("TestCardStageNo: "+TestCardStageNo);
                }
                sponsorStageBattlePoints.add(points);
            }

            // Enforce: each non-test stage must have strictly more BP than previous non-test stage
            int prevPoints = 0;
            for(int p = 0; p < sponsorStageBattlePoints.size(); p++){
                int pts = sponsorStageBattlePoints.get(p);
                if(pts > 0){ // skip test stages (0 BP)
                    if(pts <= prevPoints){
                        validationMessage = "Stage " + (p+1) + " must have more battle points than stage " + p + ". Quest setup reset.";
                        System.out.println(validationMessage);
                        questTotalStagesSponsor.clear();
                        sponsorStageBattlePoints.clear();
                        test = false;
                        TestCardStageNo = -1;
                        return;
                    }
                    prevPoints = pts;
                }
            }
        }else{
            currentStoryPlayer = Integer.parseInt(player);

            for(var j=0; j<playersInStory.size(); j++){
                if(playersInStory.get(j).getId() == Integer.parseInt(player)){
                    for(var i =0; i < card.getNoOfStages(); i++){
                        playersInStory.get(j).getQuestTotalStagesPlayers().add(new ArrayList<>());
                    }

                    String input = choosenCards;
                    String[] stageValues = input.split(" ");
                    int stage = card.getNoOfStages();

                    //if there are 2 stages and also a test card then shows error cause i goes to position 1
                    if(test == true){
                        stage = card.getNoOfStages() -1 ;
                    }

                    for (var i=0; i< stage; i++){
                        String[] Values = stageValues[i].split(",");
                        System.out.println("stageValues "+stageValues[i]);
                        for(var l =0; l < Values.length; l++){
                            System.out.println("Values "+Values[l]);
                            int cardRow = Integer.parseInt(Values[l].trim());
                            if(test && i == TestCardStageNo){
                                System.out.println("TestCardStageNo: "+TestCardStageNo);
                                System.out.println("Got a test card in this stage so didn't add anything");
                                i++;
                            }
                            if (playersInStory.get(j).getInHandCards().get(cardRow).getCardType() == ADVENTURE_WEAPON){
                                playersInStory.get(j).getQuestTotalStagesPlayers().get(i).add((Weapon) playersInStory.get(j).getInHandCards().get(cardRow));
                            } else if (playersInStory.get(j).getInHandCards().get(cardRow).getCardType() == ADVENTURE_ALLY || playersInStory.get(j).getInHandCards().get(cardRow).getCardType() == ADVENTURE_AMOUR){
                                playersInStory.get(j).getInPlayCards().add(playersInStory.get(j).getInHandCards().get(cardRow));
                            }

                        }
                    }
                    playersCardDone++;

                    //for the test card
                    if(test){
                        System.out.println("we have test = true so return");
                        return;
                    }
                }
            }
            if(playersCardDone == playersInStory.size()){
                System.out.println("set to play the game");
                initializePlayerBattlePointsList();
                questStartFight();
                if(PlayWinner.size() == 0){
                    PlayWinner.add(((Quest) currentStoryCard).sponsor);
                    System.out.println(PlayWinner.get(0).getName()+" won the quest !");
                }
            }
        }
        System.out.println("total quest size "+ questTotalStagesSponsor.size());
    }

    public void questStartFight(){

        Quest card = (Quest) currentStoryCard;
        Player s = card.getSponsor();
        int totalStages = ((Quest) currentStoryCard).getNoOfStages();


        for (int i=0; i<playersInStory.size(); i++) {// loop over each player in playersInStory
            for (var stage = 0; stage < totalStages; stage++) {// for each of those players loop over each stage

                if(test && stage == TestCardStageNo){
                    stage++;
                    if(stage == totalStages){
                        stage--;
                        skip = true;
                    }
                }
                if(!skip){
                    System.out.println("stage: "+stage);

                    // Check if any card in this stage is a Mordred foe — mark it active
                    for(Card sc : questTotalStagesSponsor.get(stage)){
                        if(sc.getCardType() == ADVENTURE_FOE && sc.getName().equals("Mordred")){
                            mordredActive = true;
                            System.out.println("Mordred foe encountered! Sponsor may remove an ally from a player.");
                        }
                    }

                    System.out.println(" Drawing an Adventure card for stage " + (stage + 1) + " for " + playersInStory.get(i).getName());
                    drawAdventureCard(Integer.toString(playersInStory.get(i).getId())); // handing out an adventure card before each stage is fought

                    System.out.println(" Current status for IN HAND cards for " + playersInStory.get(i).getName());
//                    printCards(playersInStory.get(i).getInHandCards());
                    System.out.println(" Current status for IN PLAY cards for " + playersInStory.get(i).getName());
//                    printCards(playersInStory.get(i).getInPlayCards());


                    // calculating total in play BPs for a player
                    int totalInPlayPoints = calculateBattlePointsPlayerStagesAmoursAndAllies(playersInStory.get(i));
                    System.out.println(" Total IN PLAY points for Player " + playersInStory.get(i).getName() + "  " + totalInPlayPoints + " points");
                    System.out.println(" Total WEAPON points " + (playersInStory.get(i).getStoryBattlePoints().get(stage)) + " points");
                    System.out.println(" Total RANK " + (playersInStory.get(i).getRankCard().getBattlePoints()) + " points");
                    System.out.println(" Total in play points for Player " + (totalInPlayPoints) + " points");

                    int sponsorStagePoints = sponsorStageBattlePoints.get(stage);
                    int playerStagePoints = ((playersInStory.get(i).getStoryBattlePoints().get(stage))+  // adding points from weapons
                            (playersInStory.get(i).getRankCard().getBattlePoints()) + // adding points from ranks card
                            (totalInPlayPoints)); // adding points from the in play card array list
                    System.out.println(" Total playerStagePoints " + (playerStagePoints) + " points");

                    if (playerStagePoints < sponsorStagePoints) {

                        System.out.println(playersInStory.get(i).getName() + " ===== lost in stage ===== " + (stage + 1));
                        System.out.println("Sponsor points = " + sponsorStagePoints);
                        System.out.println(playersInStory.get(i).getName() + "'s points = " + playerStagePoints);
                        System.out.println("=> " + playersInStory.get(i).getName() + " lost in stage " + (stage + 1));

                        // Amour is NOT discarded on stage loss — it stays until quest end (official rules)
                        // Only discard weapons of the losing player
                        for (int x=0; x<playersInStory.size();x++){
                            for (int k=0; k< playersInStory.get(x).getQuestTotalStagesPlayers().size(); k++){
                                ArrayList<Weapon> aw = (playersInStory.get(x).getQuestTotalStagesPlayers().get(k));
                                discardedAdventureCardList.addAll(aw);
                                aw.clear();
                            }
                        }

                        // need to remove the player who lost from playersInStory
                        playersInStory.remove(playersInStory.get(i));
                        System.out.println(" Players still in Story " + playersInStory);

                        // i-- for the removed player
                        i--;
                        // if (stage+1)!=totalStages in the if condition below => player didn't win the quest
                        stage--;

                        break;
                    }
                }

//                System.out.println("stage: "+stage);
//                //draw an ad card for the current player p
//
//                System.out.println(" Drawing an Adventure card for stage " + (stage + 1) + " for " + playersInStory.get(i).getName());
//                drawAdventureCard(Integer.toString(playersInStory.get(i).getId())); // handing out an adventure card before each stage is fought
//
//                System.out.println(" Current status for IN HAND cards for " + playersInStory.get(i).getName());
//                printCards(playersInStory.get(i).getInHandCards());
//                System.out.println(" Current status for IN PLAY cards for " + playersInStory.get(i).getName());
//                printCards(playersInStory.get(i).getInPlayCards());
//
//
//                // calculating total in play BPs for a player
//                int totalInPlayPoints = calculateBattlePointsPlayerStagesAmoursAndAllies(playersInStory.get(i));
//                System.out.println(" Total IN PLAY points for Player " + playersInStory.get(i).getName() + "  " + totalInPlayPoints + " points");
//                System.out.println(" Total WEAPON points " + (playersInStory.get(i).getStoryBattlePoints().get(stage)) + " points");
//                System.out.println(" Total RANK " + (playersInStory.get(i).getRankCard().getBattlePoints()) + " points");
//                System.out.println(" Total in play points for Player " + (totalInPlayPoints) + " points");
//
//                int sponsorStagePoints = sponsorStageBattlePoints.get(stage);
//                int playerStagePoints = ((playersInStory.get(i).getStoryBattlePoints().get(stage))+  // adding points from weapons
//                        (playersInStory.get(i).getRankCard().getBattlePoints()) + // adding points from ranks card
//                        (totalInPlayPoints)); // adding points from the in play card array list
//                System.out.println(" Total playerStagePoints " + (playerStagePoints) + " points");
//
//                if (playerStagePoints < sponsorStagePoints) {
//
//                    System.out.println(playersInStory.get(i).getName() + " ===== lost in stage ===== " + (stage + 1));
//                    System.out.println("Sponsor points = " + sponsorStagePoints);
//                    System.out.println(playersInStory.get(i).getName() + "'s points = " + playerStagePoints);
//                    System.out.println("=> " + playersInStory.get(i).getName() + " lost in stage " + (stage + 1));
//
//                    //removing amour when the player loses
//                    for (int x=0; x<playersInStory.size();x++){
//                        for (int j=0; j<playersInStory.get(x).getInPlayCards().size(); j++){
//                            AdventureCard ac= playersInStory.get(x).getInPlayCards().get(j);
//                            if(ac.getCardType() == ADVENTURE_AMOUR){
//                                discardedAdventureCardList.add(ac);
//                                playersInStory.get(x).getInPlayCards().remove(ac);
//                            }
//                        }
//                        //remove weapons when the player loses
//                        for (int k=0; k< playersInStory.get(x).getQuestTotalStagesPlayers().size(); k++){
//                            ArrayList<Weapon> aw = (playersInStory.get(x).getQuestTotalStagesPlayers().get(k));
//                            discardedAdventureCardList.addAll(aw);
//                            aw.clear();
//                        }
//                    }
//
//                    // need to remove the player who lost from playersInStory
//                    playersInStory.remove(playersInStory.get(i));
//                    System.out.println(" Players still in Story " + playersInStory);
//
//                    // i-- for the removed player
//                    i--;
//                    // if (stage+1)!=totalStages in the if condition below => player didn't win the quest
//                    stage--;
//
//                    break;
//                }

                if ((stage+1)==totalStages){
                    skip = false;
                    System.out.println(" Player " + playersInStory.get(i).getName() + " won the quest !");
                    PlayWinner.add(playersInStory.get(i));
                    int earned = totalStages + kingsRecognitionEventShields;
                    if(kingsRecognitionEventShields > 0){
                        System.out.println("Adding " + kingsRecognitionEventShields + " bonus shields from King's Recognition!");
                    }
                    playersInStory.get(i).setShields(playersInStory.get(i).getShields() + earned);
                    kingsRecognitionEventShields = 0;
                    System.out.println(playersInStory.get(i).getName() + " now has " + playersInStory.get(i).getShields() + " shields.");
                    // Check for rank promotion / win condition
                    checkRankAdvancement(playersInStory.get(i));
                }

            }
        }

        System.out.println(" The Sponsor is " + s.getName());
        System.out.println(s.getName() + " has " + s.getInHandCards().size() + "cards in hand");
        System.out.println(s.getName() + " gets to add adventure cards = Number of cards used + # of stages = " +
                sponsorStageBattlePoints.size() + " cards used + " + totalStages + " stages played = " + (sponsorStageBattlePoints.size() + totalStages));

        System.out.println(s.getName() + "'s current # of in hand cards = " + (s.getInHandCards().size()));
        // Sponsor draws cards = # of cards played + # of stages, regardless of player wins or losses
        for (int k=0; k<(sponsorStageBattlePoints.size() + totalStages); k++){
            drawAdventureCard(Integer.toString(s.getId()));
        }
        System.out.println(s.getName() + " has " + s.getInHandCards().size() + " cards in HAND after drawing cards");
        System.out.println(s.getName() + " has " + s.getInPlayCards().size() + " cards in PLAY after drawing cards");

        /**
         * this is for the remaining players in the playersInStory who won and stayed in this list
         * player(s) who lost already were taken off this list
         */

        // Discard Amour from ALL players at quest end (official rules: amour lasts full quest)
        for (Player p : players) {
            p.getInPlayCards().removeIf(ac -> {
                if (ac.getCardType() == ADVENTURE_AMOUR) {
                    discardedAdventureCardList.add(ac);
                    return true;
                }
                return false;
            });
        }

        // Discard remaining weapon selections for winning players still in list
        for (int i=0; i<playersInStory.size();i++){
            for (int k=0; k< playersInStory.get(i).getQuestTotalStagesPlayers().size(); k++){
                ArrayList<Weapon> aw = (playersInStory.get(i).getQuestTotalStagesPlayers().get(k));
                discardedAdventureCardList.addAll(aw);
                aw.clear();
            }
        }

        playersInStory.clear();

    }
    // Method used to calculate total battle points and store them in StoryBattlePoints Array
    public void initializePlayerBattlePointsList(){

        for(var i =0; i < playersInStory.size(); i++){

            for(var j =0; j < playersInStory.get(i).getQuestTotalStagesPlayers().size(); j++){
                int totalForStage = calculateBattlePointsPlayerStages(playersInStory.get(i).getQuestTotalStagesPlayers().get(j));
                playersInStory.get(i).getStoryBattlePoints().add(totalForStage);
            }
        }
    }

    // calculates and returns total battle points for weapons for a stage in a quest
    public int calculateBattlePointsPlayerStages(ArrayList<Weapon> stage){

        int totalBattlePoints = 0;
        for(var i=0; i<stage.size(); i++){
            Weapon card = stage.get(i);
            totalBattlePoints += card.getBattlePoints();
        }
        return totalBattlePoints;
    }

    //TESTING CODE to add BPs and Allies to inPlayCards list >
    //TESTING CODE to add BPs and Allies to inPlayCards list >
    //TESTING CODE to add BPs and Allies to inPlayCards list >


    public int calculateBattlePointsPlayerStagesAmoursAndAllies(Player p){

        int totalBattlePoints = 0;

        for(var i=0; i<p.getInPlayCards().size(); i++) {
            if (p.getInPlayCards().get(i).getCardType() == ADVENTURE_AMOUR) {
                totalBattlePoints += 10;
            }else if(p.getInPlayCards().get(i).getCardType() == ADVENTURE_ALLY) {
                Ally card = (Ally) p.getInPlayCards().get(i);
                if (card.getSpecialBattlePoints() == 0){
                    totalBattlePoints += card.getBattlePoints(); //apply regular battle points
                }else {
                    /**
                     * check quest name and check whether to apply special or regular battle points
                     */
                    Quest questCard = (Quest) currentStoryCard;
                    if ((card.getName().contains("Gawain") && questCard.getName().contains("Green Knight")) ||
                            (card.getName().contains("Pellinore") && questCard.getName().contains("Questing Beast")) ||
                            (card.getName().contains("Percival") && questCard.getName().contains("Holy Grail")) ||
                            (card.getName().contains("Lancelot") && questCard.getName().contains("Defend the Queen's Honor"))) {
                        totalBattlePoints+= card.getSpecialBattlePoints();
                    }else {
                        totalBattlePoints+= card.getBattlePoints();
                    }
                }
            }
        }
        return totalBattlePoints;
    }


    //TESTING CODE DONE <
    //TESTING CODE DONE <
    //TESTING CODE DONE <


    // calculates battle points for all stages of a quest for a sponsor
    public int calculateStageBattlePoints(ArrayList<Card> stage){

        int totalBattlePoints = 0;

        for(var i=0; i<stage.size(); i++){
            if(stage.get(i).getCardType() == ADVENTURE_TEST){
                totalBattlePoints += 0;
            }else if(stage.get(i).getCardType() == ADVENTURE_WEAPON){
                Weapon card = (Weapon) stage.get(i);
                totalBattlePoints += card.getBattlePoints();
            }else if(stage.get(i).getCardType() == ADVENTURE_FOE){
                Foe card = (Foe) stage.get(i);
                if (card.getBattleStrengthMin() == 0){
                    totalBattlePoints += card.getBattleStrengthMax();
                }else{
                    /**
                     * check quest name and special foe to assign max or min battle strengths
                     */
                    Quest questCard = (Quest) currentStoryCard;
                    if (questCard.getSpecialFoe() == null){
                        totalBattlePoints += card.getBattleStrengthMin();
                    }else{
                        //special foe is not null

                        if(questCard.getSpecialFoe().equals("ALL") || questCard.getSpecialFoe().equals(card.getName())) {
                            totalBattlePoints+= card.getBattleStrengthMax();
                        }else if (questCard.getSpecialFoe().contains("Saxon") && card.getName().contains("Saxon")){
                            totalBattlePoints+= card.getBattleStrengthMax();
                        }else {
                            totalBattlePoints+= card.getBattleStrengthMin();
                        }
                    }
                }
            }
        }

        //for the test card
        if(totalBattlePoints > 0){
            System.out.println("totalBattlePoints > 0");
            message = message +" "+totalBattlePoints;
        }

        return totalBattlePoints;
    }

    public Game(Player player, int num_players){
        scanner = new Scanner(System.in);
        //startGame(player, num_players);
//        createAdventureDeck();
        setTotalNumPlayers(num_players);
//        createStoryDeck();
        createDecks();
        //Collections.shuffle(storyCardList);
    }

    public void handRankCard(){
        for(Player p: players){
            Rank r = new Rank("Squire", RANK_SQUIRE,5);
            p.setRankCard(r);
            p.setBattlePoints(r.getBattlePoints());
        }
    }
    /**
     * move the current player up
     */
    public void moveCurrentPlayerTurn(){
        if((currentPlayer+1) > totalNumPlayers){
            currentPlayer = 1;
        }else{
            currentPlayer++;
        }
    }



    public void movePlayersInStory(){
        if(currentStoryPlayer >= playersInStory.size()-1){
            currentStoryPlayer = 0;
        }else{
            currentStoryPlayer++;
        }
    }

    /**
     * add players to the playersInStory list
     */

    public void promptNoOfPlayers(){
        int num =0;
        do{
            System.out.print("Enter number of players that wants to play game (2-4): ");
            num = scanner.nextInt();
            setTotalNumPlayers(num);

            if(num <2 || num > 4){
                System.out.println("Input entered not in Range, please enter a valid number (2-4)");
            }
        }while(num <2 || num > 4);
        //totalNumPlayers = num;
    }

    public void createPlayers(){
        scanner.nextLine();
        for(var i = 0; i < totalNumPlayers; i++){
            System.out.print("Enter name for Player "+ (i+1)+" : ");
            String name = scanner.nextLine();
            Player p = new Player(name,(i+1));
            players.add(p); //add the player to the list
        }
    }

    /**
     * hand out 12 adventure cards to each player
     */
    public void handOutAdventureCards(){
        Collections.shuffle(adventureCardList);
        // Always remove from index 0 so every player gets exactly 12 unique cards
        // in order from the top of the shuffled deck — no skipping.
        for(int j = 0; j < totalNumPlayers; j++){
            for(int i = 0; i < 12; i++){
                Card ac = adventureCardList.get(0);
                players.get(j).getInHandCards().add(ac);
                adventureCardList.remove(0);
            }
        }
    }
    public void initializePlayers(Player p1, Player p2){
        p1.setId(1);
        p1.setId(2);
        players.add(p1);
        players.add(p2);
    }

    public void initializePlayers(Player p1, Player p2, Player p3){
        p1.setId(1);
        p1.setId(2);
        p1.setId(3);
        players.add(p1);
        players.add(p2);
        players.add(p3);
    }


    public void initializePlayers(Player p1, Player p2, Player p3, Player p4){
        p1.setId(1);
        p1.setId(2);
        p1.setId(3);
        p1.setId(4);
        players.add(p1);
        players.add(p2);
        players.add(p3);
        players.add(p4);
    }

    public void createDecks(){
        // Clear static lists first so a fresh game never inherits cards from a previous one
        adventureCardList.clear();
        storyCardList.clear();

        CardFactory adventureCardDeck = new AdventureCardFactory();
        CardFactory storyCardDeck = new StoryCardFactory();

        adventureCardList.addAll(adventureCardDeck.orderCard("weapons"));
        adventureCardList.addAll(adventureCardDeck.orderCard("foes"));
        adventureCardList.addAll(adventureCardDeck.orderCard("allies"));
        adventureCardList.addAll(adventureCardDeck.orderCard("amours"));
        adventureCardList.addAll(adventureCardDeck.orderCard("test"));

        // Full 28-card story deck, shuffled
        storyCardList.addAll(storyCardDeck.orderCard("quest"));
        storyCardList.addAll(storyCardDeck.orderCard("event"));
        storyCardList.addAll(storyCardDeck.orderCard("tournament"));
        Collections.shuffle(storyCardList);
    }

    /**
     * populate the adventureDeck list
     * @param type - is from enum CardType
     * @param name - name of the card
     * @param max - for FOE: it is the max battle points; for Weapon: it is battlepoints; for Amour: ignore it; for Ally: it is specialBattlePoints
     * @param min - for FOE: it is min battle points; for weapon: ignore it; for Amour: ignore it; for Ally: it is battlePoints
     */
    public AdventureCard buildAdventureCard(CardType type, String name, int max, int min){
        //return new AdventureCard(name, type);
        if(type == ADVENTURE_FOE){
            return new Foe(name,ADVENTURE_FOE, max, min);
        }else if(type == ADVENTURE_WEAPON) {
            return new Weapon(name, ADVENTURE_WEAPON, max);
        }else if(type == ADVENTURE_TEST){
            return new Test(name, ADVENTURE_TEST);
        }else if(type == ADVENTURE_ALLY){
            return new Ally(name, ADVENTURE_ALLY, min, max);
        }else if(type == ADVENTURE_AMOUR){
            return new Amour(name, ADVENTURE_AMOUR);
        }
        return null;
    }



//    public void createAdventureDeck(){
//
//        int count = 0;
//
//        //add all the Foes- total 50
//
//        //add Dragon FOE * 1
//        adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Dragon", 70, 50));
//        count++;
//
//        //add Giant FOE * 2
//
//        for(int i = count; i <3 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Giant", 40, 0));
//            count++;
//        }
//
//        //add Mordred FOE *4
//        for(int i = count; i <7 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Mordred", 30, 0));
//            count++;
//        }
//
//        //add Green Knight FOE * 2
//        for(int i = count; i < 9 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Green Knight", 40, 25));
//            count++;
//        }
//
//        //add Black Knight FOE * 3
//        for(int i = count; i < 12 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Black Knight", 35, 25));
//            count++;
//        }
//
//        //add Evil Knight FOE * 6
//        for(int i = count; i < 18 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Evil Knight", 30, 20));
//            count++;
//        }
//
//        //add Saxon Knight FOE * 8
//        for(int i = count; i < 26 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Saxon Knight", 25, 15));
//            count++;
//        }
//
//        //add Robber Knight FOE * 7
//        for(int i = count; i < 33 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Robber Knight", 15, 0));
//            count++;
//        }
//
//        //add Saxons FOE * 5
//        for(int i = count; i < 38 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Saxons", 20, 10));
//            count++;
//        }
//
//        //add Boar FOE * 4
//        for(int i = count; i < 42 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Boar", 15, 5));
//            count++;
//        }
//
//        //add Thieves FOE * 8
//        for(int i = count; i < 50 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_FOE, "Thieves", 5, 0));
//            count++;
//        }
//
//        //add all the Weapons - total 49
//
//
//        //add Excalibur Weapon * 2
//        for(int i = count; i < 52 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_WEAPON, "Excalibur", 30, 0));
//            count++;
//        }
//
//        //add Lance Weapon * 6
//        for(int i = count; i < 58 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_WEAPON, "Lance", 20, 0));
//            count++;
//        }
//
//        //add Battle-ax Weapon * 8
//        for(int i = count; i < 66 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_WEAPON, "Battle-ax", 15, 0));
//            count++;
//        }
//
//        //add Sword Weapon * 16
//        for(int i = count; i < 82 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_WEAPON, "Sword", 10, 0));
//            count++;
//        }
//
//        //add Horse Weapon * 11
//        for(int i = count; i < 93 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_WEAPON, "Horse", 10, 0));
//            count++;
//        }
//
//        //add Dagger Weapon * 6
//        for(int i = count; i < 99 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_WEAPON, "Dagger", 5, 0));
//            count++;
//        }
//
//        //add all the Allies - total 7
//        //min is default battle points, max is if they have special battle points
//
//        //add Sir Galahad Ally * 1
//        for(int i = count; i < 100 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "Sir Galahad", 0, 15));
//            count++;
//        }
//
//        //add Sir Lancelot Ally * 1
//        for(int i = count; i < 101 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "Sir Lancelot", 25, 15));
//            count++;
//        }
//
//        //add King Arthur Ally * 1
//        for(int i = count; i < 102 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "King Arthur", 0, 10));
//            count++;
//        }
//
//
//        //add Sir Tristan Ally * 1 // Queen Iseult is removed from teams of 3 -> no max
//        for(int i = count; i < 103 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "Sir Tristan", 0, 10));
//            count++;
//        }
//
//        //add Sir Gawain Ally * 1
//        for(int i = count; i < 104 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "Sir Gawain", 20, 10));
//            count++;
//        }
//
//        //add Sir Percival Ally * 1
//        for(int i = count; i < 105 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "Sir Percival", 20, 5));
//            count++;
//        }
//
//        //add King Pellinore Ally * 1
//        for(int i = count; i < 106 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_ALLY, "King Pellinore", 0, 10));
//            count++;
//        }
//
//
//        //add all the Amours - total 8
//
//        for(int i = count; i <114 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_AMOUR, "Amour", -1, -1));
//            count++;
//        }
//
//        //add all the Tests - 1 type only for teams of 3, total 2 cards
//        //add Test of Valor Test * 2
//
//        //i needs to be set to 116 after testing
//        for(int i = count; i <116 ; i++){
//            adventureCardList.add(buildAdventureCard(CardType.ADVENTURE_TEST, "Test of Valor", -1, -1));
//            count++;
//        }
//
//
//
//
//
//    }
    //Need to assign id to each event card !!
    public StoryCard buildStoryCard(CardType type, String name, int noStages, String specialFoe, String specialAction, int bonusShields, int eventId){
        StoryCard card = null;
        //only perform action on Quest type card for now!

        if(type == STORY_QUEST){
            card = new Quest(name, type, noStages, specialFoe);
        }else if(type == STORY_TOURNAMENT){
            card = new Tournament(name, type, bonusShields);
        }else if(type == STORY_EVENT){
            card = new Event(name, type, specialAction, eventId);
        }
        return card;

    }


//    public void createStoryDeck(){
//        int count = 0;

        //add all the Story cards - total 28

        //add all the Quests - total 13

        //add Search for the Holy Grail Quest * 1

//        storyCardList.add(buildStoryCard(STORY_QUEST, "Search for the Holy Grail", 5, "ALL", null, -1, -1));
//        count++;
//
//        //add Test of the Green Knight Quest * 1
//        storyCardList.add(buildStoryCard(STORY_QUEST, "Test of the Green Knight", 4, "Green Knight", null, -1, -1));
//        count++;
//
//        //add Search for the Questing Beast Quest * 1
//        storyCardList.add(buildStoryCard(STORY_QUEST, "Search for the Questing Beast", 4, null, null, -1, -1));
//        count++;
//
//        //add Defend the Queen's Honor Quest * 1
//        storyCardList.add(buildStoryCard(STORY_QUEST, "Defend the Queen's Honor", 4, "ALL", null, -1, -1));
//        count++;
//
//        //add Rescue the Fair Maiden Quest * 1
//        storyCardList.add(buildStoryCard(STORY_QUEST, "Rescue the Fair Maiden", 3, "Black Knight", null, -1, -1));
//        count++;
//
//        //add Journey Through the Enchanted Forest Quest * 1
//        storyCardList.add(buildStoryCard(STORY_QUEST, "Journey Through the Enchanted Forest ", 3, "Evil Knight", null, -1, -1));
//        count++;
//
//        //add Vanquish King Arthur's Enemies Quest * 2
//        for(int i = count; i <8 ; i++){
//            storyCardList.add(buildStoryCard(STORY_QUEST, "Vanquish King Arthur's Enemies", 3, null, null, -1, -1));
//            count++;
//        }
//
//        //add Slay the Dragon Quest * 1
//        storyCardList.add(buildStoryCard(STORY_QUEST, "Slay the Dragon", 3, "Dragon", null, -1, -1));
//        count++;

//        storyCardList.add(buildStoryCard(STORY_EVENT, "Chivalrous Deed", -1, null, "Player(s) with both lowest rank and least amount of shields, receives 3 shields", -1, 6));
//        count++;
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        count++;
//
//        //add Boar Hunt Quest * 2
//        for(int i = 0; i <1 ; i++){
//            storyCardList.add(buildStoryCard(STORY_QUEST, "Boar Hunt", 2, "Boar", null, -1, -1));
//            count++;
//        }
//
//        reverse(storyCardList);

        //reverse(storyCardList);
        //add Repel the Saxon Raiders  Quest * 2
//        for(int i = count; i <13 ; i++){
//            storyCardList.add(buildStoryCard(STORY_QUEST, "Repel the Saxon Raiders ", 2, "All Saxons", null, -1, -1));
//            count++;
//        }

        //add all the tournaments - total 4

        //add Tournament at Camelot * 1
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        count++;
//
//        //add Tournament at Orkney * 1

//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Orkney", -1, null, null, 2, -1));
//        count++;
//
//        //add Tournament at Tintagel * 1
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Tintagel", -1, null, null, 1, -1));
//        count++;
//
//        //add Tournament at York * 1
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at York", -1, null, null, 0, -1));
//        count++;


        //add all the Events - total 11

        //add King's Recognition Event * 2


 /*       for(int i = count; i <19 ; i++){
            storyCardList.add(buildStoryCard(STORY_EVENT, "King's Recognition", -1, null, "The next player(s) to complete a Quest will receive 2 extra shields", -1, 1));
            count++;
        }

        //add Queen's Favor Event * 2
        for(int i = count; i <21 ; i++){
            storyCardList.add(buildStoryCard(STORY_EVENT, "Queen's Favor", -1, null, "The lowest ranked player(s) immediately receives 2 Adventure Cards", -1, 2));
            count++;
        }

        //add Court Called to Camelot Event * 2
        for(int i = count; i <23 ; i++){
            storyCardList.add(buildStoryCard(STORY_EVENT, "Court Called to Camelot", -1, null, "All Allies in play must be discarded", -1, 3));
            count++;
        }*/





        //add Chivalrous Deed Event * 1
        /*storyCardList.add(buildStoryCard(STORY_EVENT, "Chivalrous Deed", -1, null, "Player(s) with both lowest rank and least amount of shields, receives 3 shields", -1, 6));
        count++;*/

        //reverse(storyCardList);

        //add Prosperity Throughout the Realm Event * 1

//
//        //add Chivalrous Deed Event * 1
//        storyCardList.add(buildStoryCard(STORY_EVENT, "Chivalrous Deed", -1, null, "Player(s) with both lowest rank and least amount of shields, receives 3 shields", -1, 6));
//        count++;
//
//        //add Prosperity Throughout the Realm Event * 1

//        storyCardList.add(buildStoryCard(STORY_EVENT, "Prosperity Throughout the Realm", -1, null, "All players may immediately draw 2 Adventure Cards", -1, 7));
//        count++;
//
//        //add King's Call to Arms Event * 1
//        storyCardList.add(buildStoryCard(STORY_EVENT, "King's Call to Arms", -1, null, "The highest ranked player(s) must place 1 weapon in the discard pile. If unable to do so, 2 Foe Cards must be discarded", -1, 8));
//        count++;



//        // ADDING EXTRA CARDS FOR TESTING :
//         // NEED TO DELETE ALL BELOW AFTER
//
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//        storyCardList.add(buildStoryCard(STORY_TOURNAMENT, "Tournament at Camelot", -1, null, null, 3, -1));
//


//    }



    public Rank buildRankCard(String name, int battlePoints){
        return null;
    }

    public void createRankDeck(){
        int count = 0;
    }


    /**
     * Check if a player's shield count qualifies them for a rank promotion.
     * Handles overflow shields carrying forward to the next rank.
     * Win condition: Champion Knight with 10+ shields → Knight of the Round Table.
     */
    public void checkRankAdvancement(Player p){
        Rank rank = p.getRankCard();
        int shields = p.getShields();

        if(rank.getCardType() == RANK_SQUIRE && shields >= 5){
            int overflow = shields - 5;
            p.setShields(overflow);
            p.setRankCard(new Rank("Knight", RANK_KNIGHT, 10));
            p.setBattlePoints(10);
            System.out.println(p.getName() + " promoted to Knight!");
            checkRankAdvancement(p); // overflow might trigger further promotion
        } else if(rank.getCardType() == RANK_KNIGHT && shields >= 7){
            int overflow = shields - 7;
            p.setShields(overflow);
            p.setRankCard(new Rank("Champion Knight", RANK_CHAMPION_KNIGHT, 20));
            p.setBattlePoints(20);
            System.out.println(p.getName() + " promoted to Champion Knight!");
            checkRankAdvancement(p);
        } else if(rank.getCardType() == RANK_CHAMPION_KNIGHT && shields >= 10){
            // Player has won the game!
            if(!gameWinners.contains(p)){
                gameWinners.add(p);
                System.out.println(p.getName() + " is now a Knight of the Round Table! GAME OVER!");
            }
            isGameOver = true;
            status = GameStatus.FINISHED;
        }
    }

    /**
     * Sum all free bids provided by Ally cards currently in a player's in-play cards.
     */
    public int calculatePlayerFreeBids(Player p){
        int total = 0;
        for(Card c : p.getInPlayCards()){
            if(c.getCardType() == ADVENTURE_ALLY){
                Ally ally = (Ally) c;
                total += ally.getFreeBids();
            }
        }
        return total;
    }

    /**
     * Mordred effect: remove a named ally from a target player's in-play cards.
     * Called when a Mordred foe is used as a stage card and the sponsor names an ally.
     */
    public void removeMordredAlly(String targetPlayerId, String allyIndex){
        Player target = null;
        for(Player p : players){
            if(p.getId() == Integer.parseInt(targetPlayerId)){
                target = p;
                break;
            }
        }
        if(target == null) return;

        int idx = Integer.parseInt(allyIndex);
        if(idx < 0 || idx >= target.getInPlayCards().size()) return;

        Card ally = target.getInPlayCards().get(idx);
        if(ally.getCardType() == ADVENTURE_ALLY){
            discardedAdventureCardList.add(ally);
            target.getInPlayCards().remove(idx);
            System.out.println("Mordred removed " + ally.getName() + " from " + target.getName());
        }
        mordredActive = false;
    }

    /**
     * Returns rank level for comparison: Squire=1, Knight=2, Champion Knight=3.
     */
    private int getRankLevel(Player p){
        CardType rt = p.getRankCard().getCardType();
        if(rt == RANK_CHAMPION_KNIGHT) return 3;
        if(rt == RANK_KNIGHT) return 2;
        return 1;
    }

    public void parseEventCardFrontend(String eventsName){

        System.out.println("* * * Parsing an event card * * *");
        System.out.println(((eventsName.split(":")[1]).split("}")[0]).substring(1,((eventsName.split(":")[1]).split("}")[0]).length() - 1));
        String eventName = ((eventsName.split(":")[1]).split("}")[0]).substring(1,((eventsName.split(":")[1]).split("}")[0]).length() - 1);

        if (eventName.equals("King's Recognition")){
            System.out.println("We are currently in event King's Recognition");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            performKingsRecognition();

        }else if(eventName.equals("Queen's Favor")){
            System.out.println("We are currently in event Queen's Favor");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            performQueensFavor();

        }else if(eventName.equals("Court Called to Camelot")){
            System.out.println("We are currently in event Court Called to Camelot");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            performCourtCalledToCamelot();

        }else if(eventName.equals("Pox")){
            System.out.println("We are currently in event Pox");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            System.out.println("current player in event: "+currentPlayer);
            int a = currentPlayer-1;
            if (a == -1){
                a = players.size()-1;

            }
            System.out.println("Current player is :" + players.get(currentPlayer).getName());
            performPox(players.get(currentPlayer));

        }else if(eventName.equals("Plague")){
            System.out.println("We are currently in event Plague");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            int a = currentPlayer-1;
            if (a == -1){
                a = players.size()-1;
            }
            System.out.println("Current player is :" + players.get(currentPlayer).getName());
            performPlague(players.get(currentPlayer));

        }else if(eventName.equals("Chivalrous Deed")){
            System.out.println("We are currently in event Chivalrous Deed");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            performChivalrousDeed();

        }else if(eventName.equals("Prosperity Throughout the Realm")){
            System.out.println("We are currently in event Prosperity Throughout the Realm");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            performProsperityThroughoutTheRealm();

        }else if ((eventName.equals("King's Call to Arms"))){

            System.out.println("We are currently in event Prosperity King's Call to Arms");
            System.out.println("The Event's special action is :");
            //System.out.println(eventCard.getSpecialAction());
            performKingsCallToArms();
        }

    }

    /**
     * perform plague event action
     * Drawer loses 2 shields if possible (assumption is loses 2 or nothing!!!)
     */

    public void performPlague(Player drawingPlayer){
        // Official rule: drawer loses 2 shields, minimum 0
        int newShields = Math.max(0, drawingPlayer.getShields() - 2);
        drawingPlayer.setShields(newShields);
        System.out.println(drawingPlayer.getName() + " loses 2 shields from Plague. Now has " + newShields);
    }

    public void performPox(Player drawingPlayer){
        System.out.println("Performing the POX Event");

        for(Player p: players){

            //skip the current player
            if(p.getId() !=drawingPlayer.getId()){
                int playerShields = p.getShields();

                if(playerShields > 0){
                    playerShields--;
                    p.setShields(playerShields);
                }else{
                    System.out.println("Player "+ p.getName()+ " does not enough shields to loose ! ");
                }
            }
        }
    }

    public void performChivalrousDeed(){
        // Official rule: player(s) with BOTH lowest rank AND fewest shields gain 3 shields
        System.out.println("Performing the Chivalrous Deed Event");

        // Step 1: find the lowest rank level
        int minRank = Integer.MAX_VALUE;
        for(Player p : players){
            int lvl = getRankLevel(p);
            if(lvl < minRank) minRank = lvl;
        }

        // Step 2: among lowest-ranked players, find fewest shields
        int minShields = Integer.MAX_VALUE;
        for(Player p : players){
            if(getRankLevel(p) == minRank && p.getShields() < minShields){
                minShields = p.getShields();
            }
        }

        // Step 3: give 3 shields to those who match both conditions
        for(Player p : players){
            if(getRankLevel(p) == minRank && p.getShields() == minShields){
                p.setShields(p.getShields() + 3);
                System.out.println(p.getName() + " received 3 shields from Chivalrous Deed. Now has " + p.getShields());
                checkRankAdvancement(p);
            }
        }
    }

    public void performCourtCalledToCamelot() {
        // Official rule: all Allies currently in play for every player are discarded
        for (Player p : players) {
            p.getInPlayCards().removeIf(ac -> {
                if (ac.getCardType() == ADVENTURE_ALLY) {
                    discardedAdventureCardList.add(ac);
                    return true;
                }
                return false;
            });
        }
    }


    public void performKingsCallToArms() {
        // Find the highest rank level among all players
        int maxRankLevel = 0;
        for(Player p : players){
            int lvl = getRankLevel(p);
            if(lvl > maxRankLevel) maxRankLevel = lvl;
        }

        // Only the highest-ranked player(s) must discard
        for(Player p : players){
            if(getRankLevel(p) != maxRankLevel) continue;

            boolean discardedWeapon = false;
            ArrayList<Card> hand = p.getInHandCards();
            for(int i = 0; i < hand.size(); i++){
                if(hand.get(i).getCardType() == ADVENTURE_WEAPON){
                    discardedAdventureCardList.add(hand.get(i));
                    hand.remove(i);
                    System.out.println(p.getName() + " discarded a weapon for King's Call to Arms.");
                    discardedWeapon = true;
                    break;
                }
            }
            if(!discardedWeapon){
                int foeCount = 0;
                for(int i = 0; i < hand.size() && foeCount < 2; i++){
                    if(hand.get(i).getCardType() == ADVENTURE_FOE){
                        discardedAdventureCardList.add(hand.get(i));
                        hand.remove(i);
                        i--;
                        foeCount++;
                        System.out.println(p.getName() + " discarded a foe for King's Call to Arms.");
                    }
                }
            }
        }
    }


    public void performQueensFavor() {
        // Official rule: lowest-ranked player(s) immediately draw 2 Adventure Cards
        int minRank = Integer.MAX_VALUE;
        for(Player p : players){
            int lvl = getRankLevel(p);
            if(lvl < minRank) minRank = lvl;
        }
        for (Player p : players) {
            if(getRankLevel(p) == minRank){
                for (int i = 0; i < 2; i++) {
                    drawAdventureCard(Integer.toString(p.getId()));
                }
                System.out.println(p.getName() + " drew 2 cards from Queen's Favor.");
            }
        }
    }




    public void performProsperityThroughoutTheRealm() {
        // Each player shall receive 2 cards
        for (Player p : players) {
            for (int i = 0; i < 2; i++) {
                drawAdventureCard(Integer.toString(p.getId()));
            }
        }
    }

    public void performKingsRecognition() {
        kingsRecognitionEventShields += 2;
        System.out.println("Currently this card has " + kingsRecognitionEventShields + " shields for the next quest winner(s).");
    }


    /*
     * check if the game has finished.
     */

}