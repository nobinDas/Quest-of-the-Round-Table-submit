package team1.project.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import team1.project.controller.request.*;
import team1.project.models.*;
import team1.project.exception.InvalidGameException;
import team1.project.exception.InvalidParamException;

import java.util.ArrayList;

@RestController
@Slf4j
@AllArgsConstructor

public class ProjectApplicationController {
    private final GameService gameService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping("/game/start")
    public ResponseEntity<Game> start(@RequestBody GameRequest request) {
        log.info("start game request: {} {}", request.getPlayer().getName(), request.getNum_players());
        System.out.println("we here");
        ResponseEntity<Game> test = ResponseEntity.ok(gameService.createGame(request.getPlayer(), request.getNum_players()));
        System.out.println(test);
        return test;

//        return ResponseEntity.ok(gameService.createGame(player));
    }

    @PostMapping("/game/begin")
    public ResponseEntity<ArrayList<ArrayList<Card>>> begin(@RequestBody String gameId) throws InvalidParamException {
        log.info("begin game request: {} ",gameId);
        System.out.println("BEGIN GAME");

        Game gamer = gameService.beginGame(gameId);

        ArrayList<ArrayList<Card>> cards = new ArrayList<>();
        int num_players = gamer.getPlayers().size();
        for (int i = 0; i <num_players; i++){
            cards.add(gamer.getPlayers().get(i).getInHandCards());

        }

        ResponseEntity<ArrayList<ArrayList<Card>>> send = ResponseEntity.ok(cards);
        return send;

    }

    @GetMapping("/game/rank")
    public ResponseEntity<ArrayList<Player>> sendRanks(){
        log.info("Send rank cards request");
        Game gamer = gameService.returnGame();
        ArrayList<Player> ranks = new ArrayList<>();
        ArrayList<Integer> shields = new ArrayList<>();
        for (int i = 0; i <gamer.getPlayersJoined(); i++){

            ranks.add(gamer.getPlayers().get(i));
        }
        System.out.println("These are the rank cards: " +ranks);
        return ResponseEntity.ok(ranks);
    }

    @GetMapping("/topic/group/discardPile")
    public ResponseEntity<ArrayList<Card>> sendPile(){
        log.info("Send discard pile");
        Game gamer = gameService.returnGame();
        ArrayList<Card> discardPile = gamer.getDiscardedAdventureCardList();
        System.out.println(discardPile);

        return ResponseEntity.ok(discardPile);
    }

    @GetMapping("/topic/group/getGame")
    public ResponseEntity<Game> getGame(){
        log.info("Getting Game");

        Game gamer = gameService.returnGame();

        return ResponseEntity.ok(gamer);
    }

    @GetMapping("/game/currentplayer")
    public ResponseEntity<Game> getCurr(){
        log.info("Getting Current Player");

        Game gamer = gameService.returnGame();

        return ResponseEntity.ok(gamer);
    }

    @PostMapping("/topic/group/eventS")
    public ResponseEntity<Game> startEvent(@RequestBody String eventName){
        log.info("Starting event");
        Game gamer = gameService.returnGame();
        gamer.parseEventCardFrontend(eventName);

        return ResponseEntity.ok(gamer);
    }

    @PostMapping("/topic/group/getCard")
    public ResponseEntity<ArrayList<ArrayList<Card>>> sendAdventureCard(@RequestBody PlayerRequest player){
        log.info("send Adventure Card request{}", player);

        Game game = gameService.getAdventureCard(player.getPlayer());

        ArrayList<ArrayList<Card>> cards = new ArrayList<>();
        cards.add(game.getPlayers().get(Integer.parseInt(player.getPlayer())).getInHandCards());
        ResponseEntity<ArrayList<ArrayList<Card>>> send = ResponseEntity.ok(cards);
        return send;

    }

    @PostMapping("/topic/group/sumbitPlayerResForStory")
    public ResponseEntity<Game> playersInStory(@RequestBody PlayerRequest player){
        Game game = gameService.sendPlayerToStoryPlay(player.getPlayer());

//        StoryCard card = game.currentStoryCard;
//        ResponseEntity<StoryCard> send = ResponseEntity.ok(card);

        return ResponseEntity.ok(game);
    }

    @PostMapping("/topic/group/chooseCard")
    public ResponseEntity<Game> getCard(@RequestBody cardRequest player){
        log.info("send chooseCard request{}", player);

        Game game = gameService.storePlayCard(player.getPlayer(),player.getCard());

        System.out.println("game.getQuestTotalStagesSponsor() "+game.getQuestTotalStagesSponsor());

        return ResponseEntity.ok(game);
    }

    @PostMapping("/topic/group/getStoryCard")
    public ResponseEntity<Card> sendStoryCard(@RequestBody PlayerRequest player){
        log.info("send Story Card request{}", player);

        Game game = gameService.getStoryCard(player.getPlayer());

        Card card = game.currentStoryCard;
        ResponseEntity<Card> send = ResponseEntity.ok(card);
        return send;
    }


    //for the tournament
    @PostMapping("/topic/group/setTournamentPlayer")
    public ResponseEntity<Game> playerSetForTournament(@RequestBody PlayerRequest player){
        log.info("set tournament player request{}", player);

        Game game = gameService.setTournamentPlayer(player.getPlayer());

        return ResponseEntity.ok(game);

    }


    @PostMapping("/topic/group/getTournamentCard")
    public ResponseEntity<Game> cardSetForTournament(@RequestBody cardRequest player){
        log.info("set tournament cards request{}", player);

        Game game = gameService.setTournamentCard(player.getPlayer(),player.getCard());

        return ResponseEntity.ok(game);
    }

    @PostMapping("/topic/group/bidCard")
    public ResponseEntity<Game> bidCards(@RequestBody cardRequest player){
        log.info("set bid card request{}", player);

        Game game = gameService.bidCards(player.getPlayer(),player.getCard());

        return ResponseEntity.ok(game);
    }

    @PostMapping("/topic/group/DoBid")
    public ResponseEntity<Game> Dobid(@RequestBody bidRequest player){
        log.info("set tournament cards request{}", player);

        Game game = gameService.DoTestBid(player.getPlayer(),player.getBid());

        return ResponseEntity.ok(game);
    }

    @PostMapping("/topic/group/quest")
    public ResponseEntity<Game> quest(@RequestBody PlayerRequest player){
        log.info("send quest request{}", player);

        Game game = gameService.quest(player.getPlayer());

//        StoryCard card = game.currentStoryCard;
//        ResponseEntity<StoryCard> send = ResponseEntity.ok(card);

        return ResponseEntity.ok(game);
    }


    @PostMapping("/topic/group/discard")
    public ResponseEntity<ArrayList<ArrayList<Card>>> discard(@RequestBody cardRequest request) throws InvalidParamException {
        log.info("discard card request: {} ",request);
        System.out.println("DISCARDING CARD");
        Game gamer = gameService.discardCard( request.getPlayer(), request.getCard());

        ArrayList<ArrayList<Card>> cards = new ArrayList<>();


        cards.add(gamer.getPlayers().get(Integer.parseInt(request.getPlayer())).getInHandCards());
        ResponseEntity<ArrayList<ArrayList<Card>>> send = ResponseEntity.ok(cards);
        return send;

//        return ResponseEntity.ok(gameService.createGame(player));
    }

    @PostMapping("/topic/group/select")
    public ResponseEntity<ArrayList<ArrayList<Card>>> selectAlly(@RequestBody cardRequest request) throws InvalidParamException {
        log.info("Select Ally/Amour card request: {} ",request);
        System.out.println("SELECT ALLY/AMOUR");
        Game gamer = gameService.selectCard( request.getPlayer(), request.getCard());

        ArrayList<ArrayList<Card>> cards = new ArrayList<>();


        cards.add(gamer.getPlayers().get(Integer.parseInt(request.getPlayer())).getInHandCards());

        ResponseEntity<ArrayList<ArrayList<Card>>> send = ResponseEntity.ok(cards);
        System.out.println("CARDS ARRAYLIST: " + cards);

        return send;

    }

    @PostMapping("/topic/group/discardCards")
    public ResponseEntity<ArrayList<ArrayList<Card>>> discardCards(@RequestBody discardCardsRequest request) throws InvalidParamException {
        log.info("discard card request: {} ",request);
        System.out.println("DISCARDING CARD");
        Game gamer = gameService.discardCards( request.getPlayer(), request.getCard());

        ArrayList<ArrayList<Card>> cards = new ArrayList<>();


        cards.add(gamer.getPlayers().get(Integer.parseInt(request.getPlayer())).getInHandCards());
        ResponseEntity<ArrayList<ArrayList<Card>>> send = ResponseEntity.ok(cards);
        return send;

    }


    @PostMapping("/game/connect")
    public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws InvalidParamException, InvalidGameException {
        log.info("connect request: {}", request);
        ResponseEntity<Game> test = ResponseEntity.ok(gameService.connectToGame(request.getPlayer(), request.getGameId()));
        System.out.println(test);
        return test;
    }

    /*@MessageMapping("/progress")
    @SendTo("/topic/progress")
    public String broadcastNews(@Payload String message) {
        return message;
    }*/
    /*@PostMapping("/game/join")
    public ResponseEntity<Game> join(@RequestBody Player player, @RequestBody String GameID) throws InvalidParamException, InvalidGameException {
        log.info("join game request: {}", player.getName());
        System.out.println("we here");
        ResponseEntity<Game> test = ResponseEntity.ok(gameService.connectToGame(player, GameID));
        System.out.println(test);
        return test;

//        return ResponseEntity.ok(gameService.createGame(player));
    }*/




    //@PostMapping("/connect")
    //public ResponseEntity<Game> connect(@RequestBody ConnectRequest request) throws InvalidParamException, InvalidGameException {
    //    log.info("connect request: {}", request);
    //    return ResponseEntity.ok(gameService.connectToGame(request.getPlayer(), request.getGameId()));
    //}
}