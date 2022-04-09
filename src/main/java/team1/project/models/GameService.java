package team1.project.models;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import team1.project.exception.InvalidGameException;
import team1.project.exception.InvalidParamException;

import java.util.Scanner;
import java.util.UUID;

import static team1.project.models.GameStatus.*;

@Service
@AllArgsConstructor
public class GameService {
    private static Scanner scanner;


    public Game createGame(Player player, int num_players){
        Game game = new Game(player, num_players);
        game.setGameId("New Game");
        System.out.println(game.getGameId());
        System.out.println("This is the game created in createGame: "+game);
        game.createPlayer(player);
        System.out.println("FIRST PLAYER IN THE GAME" +game.getPlayers().get(0));

        game.setStatus(NEW);
        GameStorage.getInstance().setGame(game);
        System.out.println("upto this clear");
        return game;
    }

    public Game returnGame(){

        return (Game) GameStorage.getInstance().getGames().values().toArray()[0];
    }

    public Game getAdventureCard(String player){
        Game game = returnGame();
        game.drawAdventureCard(player);
        return game;
    }

    //for the tournament
    public Game setTournamentPlayer(String player){
        Game game = returnGame();
        game.settournamentPlayer(player);
        return game;
    }
    public Game setTournamentCard(String player, String card){
        Game game = returnGame();
        game.settournamentCard(player,card);
        return game;
    }

    public Game bidCards(String player,String card){
        Game game = returnGame();
        game.bidCards(player,card);
        return game;
    }

    public Game sendPlayerToStoryPlay(String player){
        Game game = returnGame();
        game.appendPlayerInPlayersInStory(player);
        return game;
    }

    public Game storePlayCard(String player, String cardsIndex){
        Game game = returnGame();
        game.storePlayCard(player,cardsIndex);
        return game;
    }

    public Game getStoryCard(String player){
        Game game = returnGame();
        game.drawStoryCard(player);
        return game;
    }

    public Game DoTestBid(String player, String bid){
        Game game = returnGame();
        game.DoBid(player, bid);
        return game;
    }

    public Game quest(String player){
        Game game = returnGame();
        game.sponsorOfQuest(player);
        return game;
    }

    public Game beginGame(String gameId) throws InvalidParamException {
        if(!GameStorage.getInstance().getGames().containsKey(gameId)){
            System.out.println("WE ARE GETTING SLAPPED RIGHT HERE: " + GameStorage.getInstance().getGames());
            System.out.println("ALL KEYS " + GameStorage.getInstance().getGames().keySet());
            //throw new InvalidParamException("Game with provided id doesn't exists");
        }
        Game game = (Game) GameStorage.getInstance().getGames().values().toArray()[0];
        System.out.println("WE ARE IN beginGame FUNCTION "+ game.getGameId());
        game.handRankCard();
        game.handOutAdventureCards();
//        game.handRankCard();
        game.printCards(game.getPlayers().get(0).getInHandCards());
        return game;
    }

    //if someone choose from index
    public Game discardCard(String player, String index){
        Game game = (Game) GameStorage.getInstance().getGames().values().toArray()[0];
        game.discardAdventureCard(player, index);
        return game;
    }

    //choosen by play in a game
    public Game discardCards(String player, String[] cards){
        Game game = (Game) GameStorage.getInstance().getGames().values().toArray()[0];

        game.discardAdventureCards(player, cards);

        return game;
    }

    public Game selectCard(String player, String index){
        Game game = (Game) GameStorage.getInstance().getGames().values().toArray()[0];
        game.selectAlly(player, index);
        return game;
    }



    public Game connectToGame(Player player, String gameId) throws InvalidParamException, InvalidGameException {
        if(!GameStorage.getInstance().getGames().containsKey(gameId)){
            throw new InvalidParamException("Game with provided id doesn't exists");
        }
        Game game = GameStorage.getInstance().getGames().get(gameId);

        if(game.getTotalNumPlayers() == game.getPlayers().size()){
            throw new InvalidGameException("Game is full");
        }

        game.createPlayer(player);
        game.setStatus(IN_PROGRESS);
        GameStorage.getInstance().setGame(game);
        System.out.println("GAME JOINED: " +game);
        System.out.println("FIRST PLAYER IN THE GAME" +game.getPlayers().get(0));
        System.out.println("SECOND PLAYER IN THE GAME" +game.getPlayers().get(1));

        return game;

    }

}