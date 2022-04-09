var stompClient = null;
const url = "http://localhost:8080"
// server.port = 8080;
let QuestCard= null;
let Question = null;
let totalPlayers;
let sponsorPlayerID;  //commented out in /topic/progress
let TookStoryCard = false;
let PlayerTookStory = null;
let StoryCard= null;
let currPlayer = 0;

let playerturn = 0;


function setConnected(connected,gameId) {
    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
    }
    else {
        $("#conversation").hide();
    }
    $("#greetings").html(gameId);
}

function connect(gameId, playersJoined, totalNumPlayers, playerName) {
//playerName is actually the playerID
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true,gameId);
        console.log('Connected: ' + frame);
        stompClient.subscribe("/topic/progress",function (response){
            let data = JSON.parse(response.body);
            console.log(data);


            if(data.test == true && data.playersCardDone == data.playersInStory.length){
                //show index for bidding to the first player in story
                stompClient.send("/topic/group/"+data.playersInStory[0].id.toString()+"/testBid", {}, JSON.stringify(data));
                return;
            }

            if(data.playWinner != null){
                $("#storyCard").empty();
                $("#storyCard").append("The Winner of the Story is: ");
                for (let i = 0; i < data.playWinner.length; i++){
                    $("#storyCard").append(data.playWinner[i].name);
                }
                //test bidder will be asked to choose cards
                console.log("data.test: "+data.test);
                if(data.test == true){
                    stompClient.send("/topic/group/"+data.playWinner[0].id.toString()+"/bidCards", {}, JSON.stringify(data));
                }

                sponsorPlayerID = null;

                TookStoryCard = false;
        // coming back

                /*currPlayer += 1;
                if(currPlayer==totalPlayers){
                                currPlayer = 0;
                            }
                                console.log(currPlayer);
                                console.log("First");
                                stompClient.send("/topic/group/showCard/" + currPlayer, {},  JSON.stringify(currPlayer));*/


                return;
            }


            if(data == "Nobody wants to sponsor the quest"){
                sponsorPlayerID = null;
                TookStoryCard = false;
                $("#storyCard").empty();
                //$("#getStoryCard").hide();
                /*currPlayer += 1;
                if(currPlayer==totalPlayers){
                                currPlayer = 0;
                            }
                                console.log(currPlayer);
                                console.log("Second");
                                stompClient.send("/topic/group/showCard/" + currPlayer, {},  JSON.stringify("Show Story Card"));*/
                return;
            }

            if(data == "We have enough players to start the game"){
                $("#info").hide();
                $("#gameID").hide();
                $("#greetings").hide();

                return;
            }

            if(data.cardType == "STORY_QUEST"){
                if(data.sponsor != null){
                    $("#storyCard").empty();
                    $("#storyCard").append("<p>Card: "+data.name+", Type: "+data.cardType+"</br>Sponsor: "+data.sponsor.name+"</br>No.of Stages: "+ data.noOfStages +"</br>SpecialFoe: "+data.specialFoe+"</p>");
                }else{
                    $("#storyCard").empty();
                    $("#storyCard").append("<p>Card: "+data.name+", Type: "+data.cardType+"</br>Sponsor: "+data.sponsor+"</br>No.of Stages: "+ data.noOfStages +"</br>SpecialFoe: "+data.specialFoe+"</p>");
                }
            } else if(data.cardType == "STORY_EVENT"){

                $("#storyCard").empty();
                $("#storyCard").append("<p>Card: "+data.name+", Type: "+data.cardType +"</br>Special Action: "+data.specialAction+"</p>");
                //Event(data.name);
                console.log(data.name);
                TookStoryCard = false;
                /*currPlayer += 1;
                if(currPlayer==totalPlayers){
                                currPlayer = 0;
                            }
                console.log(currPlayer);
                console.log("Event show story card");
                stompClient.send("/topic/group/showCard/" + currPlayer, {},  JSON.stringify("Show Story Card"));*/
            }else if(data.cardType == "STORY_TOURNAMENT"){
                StoryCard = data;
                $("#storyCard").empty();
                $("#storyCard").append("<p>Card: "+data.name+", Type: "+data.cardType +"</br>Bonus shield: "+data.bonusShields+"</p>");


                console.log(data.name);
                TookStoryCard = false;
            }
        });

        $("#player_id").append(playerName);

        stompClient.subscribe("/topic/group/" + playerName+"/tournamentAdCardUpdate",function (response){
            let data = JSON.parse(response.body);
            data = data.players[parseInt($("#player_id").text())].inHandCards;
            console.log(data);
            $("#deck").empty();
            for (let i = 0; i < data.length; i++){

                 if (data[i].cardType == "ADVENTURE_FOE"){
                                                    $("#deck").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + data[i].battleStrengthMin + "</td></tr>");
                                                } else if (data[i].cardType == "ADVENTURE_AMOUR"){
                                                    $("#deck").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + 10 + "</td></tr>");
                                                }
                                                else {
                                                    $("#deck").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + data[i].battlePoints + "</td></tr>");
                                                }

            }

        });

        stompClient.subscribe("/topic/group/" + playerName,function (response){
            let data = JSON.parse(response.body);
            console.log(data);
            $("#deck").empty();
            for (let i = 0; i < data.length; i++){

                if (data[i].cardType == "ADVENTURE_FOE"){
                                    $("#deck").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + data[i].battleStrengthMin + "</td></tr>");
                                } else if (data[i].cardType == "ADVENTURE_AMOUR"){
                                    $("#deck").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + 10 + "</td></tr>");
                                }
                                else {
                                    $("#deck").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + data[i].battlePoints + "</td></tr>");
                                }

            }



        });

        stompClient.subscribe("/topic/group/showCard/" + playerName, function (response){

        currPlayer = JSON.parse(response.body);

        $("#getStoryCard").show();


        })

        stompClient.subscribe("/topic/group/" + playerName + "/storyCard",function (response){
            if(TookStoryCard == true){
                TookStoryCard = false;
                stompClient.send("/topic/progress", {}, JSON.stringify("Nobody wants to sponsor the quest"));
            }else{
                TookStoryCard = true; //when winning message will be given to everyone set this to false;
                /*if (currPlayer == totalNumPlayers){
                    currPlayer = 0;
                } else {
                    currPlayer +=1;
                }*/

                currPlayer = parseInt($("#player_id").text());
                let data = JSON.parse(response.body);
                console.log("Got the story card: "+ data.name);

                stompClient.send("/topic/progress", {}, JSON.stringify(data));

                if(data.cardType == "STORY_QUEST"){
                    console.log("asking if wanna sponsor!!");
                    askQuestions(data, "who wants to be sponsor");
                }else if(data.cardType == "STORY_EVENT"){
                    console.log("story event card!!");
                    askQuestions(data, "event card");
                    Event(data.name);
                    $("#cards_index").hide();
                }
                //tournament card in play
                else if(data.cardType == "STORY_TOURNAMENT"){
                    console.log("tournament card in play");

                    console.log("asking if wanna play the tournament!!");
                    askQuestions(StoryCard, "Do you want to play the tournament?");
                }
            }
        });

        stompClient.subscribe("/topic/group/"+ playerName +"/bidCards", function (response){
            let data = JSON.parse(response.body);

            //ask test winner to choose cards to discard
            $("#cards_index4").show();

        });

        stompClient.subscribe("/topic/group/" + playerName+"/testBid",function (response){
            let data = JSON.parse(response.body);

            $("#Questions").empty();
            $("#Questions").show();
            $("#Questions").append(data.testBidMessage);

            $("#test_bid").show();
        });

        stompClient.subscribe("/topic/group/"+ playerName +"/playQuest",function (response){
            let data = JSON.parse(response.body);
            console.log(data);

            if(parseInt($("#player_id").text()) == sponsorPlayerID){
                //see if there is any player in quest
                if(data.playersInStory.length == 0){
                    stompClient.send("/topic/progress", {}, JSON.stringify("Nobody wants to sponsor the quest"));
                    return;
                }
                //ask sponsor to choose card for story
                $("#cards_index").show();
                return;

            }


            else{
                for(i=0; i<data.playersInStory.length; i++){
                    if(parseInt($("#player_id").text()) == data.playersInStory[i].id){
                        //ask players in story to choose their card

                        //if the player is not matched then asks the question

                        $("#Questions").empty();
                        $("#Questions").append(data.message);

                        $("#cards_index").show();
                        return;
                    }
                }
                askQuestions(QuestCard, "who wants to play the Quest");
            }
        });

        stompClient.subscribe("/topic/group/"+ playerName +"/quest",function (response){
            let nowAsking = parseInt($("#player_id").text())+1;
            let data = JSON.parse(response.body);
            /*$("#Messages").empty();
            $("#Messages").append("It is player " + data.currentPlayer + "'s turn");*/
            //data is a game

            //data is a game

            stompClient.send("/topic/progress", {}, JSON.stringify(data.currentStoryCard));

            if(nowAsking==totalPlayers){
                nowAsking = 0;
            }
            stompClient.send("/topic/group/"+nowAsking.toString()+"/playQuest", {}, JSON.stringify(data));

        });

        //send and subscribe for Tournament
        stompClient.subscribe("/topic/group/"+playerName+"/chooseTourCard", function (response){
            let data = JSON.parse(response.body);
            if(data.playWinner.length > 0){
                console.log("winner is ready to display!!!!")

                for (let j = 0; j < totalNumPlayers; j++){
                    console.log("calling player: "+j+"/rank");
                    stompClient.send("/topic/group/" + j + "/rank", {}, JSON.stringify(data.players));
                }
                stompClient.send("/topic/progress", {}, JSON.stringify(data));
            }else{
                $("#cards_index3").show();
            }
        });

        stompClient.subscribe("/topic/group/"+playerName+"/TournamentPlay?", function (response){
            let data = JSON.parse(response.body);

            if(PlayerTookStory == parseInt($("#player_id").text())){
                //every one was asked if they want to play

                //check if there is any player in playerInStory
                if(data.playersInStory.length == 0){
                    stompClient.send("/topic/progress", {}, JSON.stringify("Nobody wants to sponsor the quest"));//it works though string is out off the topic
                    return;
                }
                //now do next

                //will send to the first player in tournament(other players will send from function TournamentCards())
                stompClient.send("/topic/group/" + data.playersInStory[0].id.toString() + "/chooseTourCard", {}, JSON.stringify(data));
            }else{
                console.log("asking if wanna play the tournament!!");
                askQuestions(StoryCard, "Do you want to play the tournament?");
            }

        });


        stompClient.subscribe("/topic/group/discardPile",function (response){
            let data = JSON.parse(response.body);
            console.log(data);
            $("#discardPile").empty();
            for (let i = 0; i < data.length; i++){
                $("#discardPile").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " +data[i].battlePoints + "</td></tr>");
            }

        });

        stompClient.subscribe("/topic/group/" + playerName + "/rank",function (response){
            let data = JSON.parse(response.body);
            console.log(data);
            const winners = [];
            console.log("ready to update /rank");

            $("#ranks").empty();
            for (let i = 0; i < data.length; i++){
                $("#ranks").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].rankCard.name + "   Shields: " +data[i].shields +  "   In Play Cards: </td></tr>");
                if (data[i].shields >= 5){
                    winners.push(data[i].name);
                }
                for (let j=0; j < data[i].inPlayCards.length; j++){
                    $("#ranks").append( data[i].inPlayCards[j].name + ", " +  data[i].inPlayCards[j].cardType + " BattlePoints: " + data[i].inPlayCards[j].battlePoints + "</td></tr>") ;
                }
            }
            let victors = "";
            for (let p = 0; p < winners.length; p++){
                victors += winners[p] + " ";
            }
            if (winners.length > 0){
                alert("The Winner(s) of the game is: " + victors)

            }



        });

        stompClient.subscribe("/topic/group/event",function (response){
            let data = JSON.parse(response.body);
            console.log(data);
            //$("#discardPile").empty();
            $("#deck").empty();
            let players = data.players[playerName];

            /*for (let i = 0; i < players.inHandCards.length; i++){
                              $("#deck").append("<tr><td>" + i + ". " + players.inHandCards[i].name + ", " + players.inHandCards[i].cardType + players.inHandCards[i].battlePoints + "</td></tr>");
                              }*/
            for (let i = 0; i < players.inHandCards.length; i++){

                 if (players.inHandCards[i].cardType == "ADVENTURE_FOE"){
                                                    $("#deck").append("<tr><td>" + i + ". " + players.inHandCards[i].name + ", " + players.inHandCards[i].cardType + ", " + players.inHandCards[i].battleStrengthMin + "</td></tr>");
                                                } else if (players.inHandCards[i].cardType == "ADVENTURE_AMOUR"){
                                                    $("#deck").append("<tr><td>" + i + ". " + players.inHandCards[i].name + ", " + players.inHandCards[i].cardType + ", " + 10 + "</td></tr>");
                                                }
                                                else {
                                                    $("#deck").append("<tr><td>" + i + ". " + players.inHandCards[i].name + ", " + players.inHandCards[i].cardType + players.inHandCards[i].battlePoints + "</td></tr>");
                                                }

            }
            $("#ranks").empty();
            const winners = [];
            for (let i = 0; i < data.players.length; i++){
                            $("#ranks").append("<tr><td>" + i + ". " + data.players[i].name + ", " + data.players[i].rankCard.name + "   Shields: " +data.players[i].shields +  "   In Play Cards: </td></tr>");
                            if (data.players[i].shields >= 5){
                                winners.push(data.players[i].name);
                            }
                            for (let j=0; j < data.players[i].inPlayCards.length; j++){
                                $("#ranks").append( data.players[i].inPlayCards[j].name + ", " +  data.players[i].inPlayCards[j].cardType + " BattlePoints: " + data.players[i].inPlayCards[j].battlePoints + "</td></tr>") ;
                            }
                        }


                        if (winners.length > 0){
                            let victors = "";
                            for (let p = 0; p < winners.length; p++){
                                        victors += winners[p] + " ";
                                  }
                            alert("The Winner(s) of the game is: " + victors)


                        }



        });

        if (playersJoined==totalNumPlayers){
            totalPlayers= totalNumPlayers;
            alert("We have enough players to start the game!");

            stompClient.send("/topic/progress", {}, JSON.stringify("We have enough players to start the game"));
            $.ajax({
                url: url + "/game/begin",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "gameId": gameId
                }),
                success: function (data){

                    for (let i = 0; i < totalNumPlayers; i++){
                        stompClient.send("/topic/group/" + i, {}, JSON.stringify(data[i]));
                    }
                    $.ajax({
                        url: url + "/game/rank",
                        type: 'GET',
                        dataType: "json",
                        contentType: "application/json",

                        success: function (data){

                            for (let i = 0; i < totalNumPlayers; i++){
                                stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data));
                            }


                        },
                        error: function (error){
                            alert("its an error");
                            console.log(error);
                        }
                    })


                                    console.log(currPlayer);
                                    console.log("begin game");
                                    stompClient.send("/topic/group/showCard/" + currPlayer, {},  JSON.stringify(currPlayer));

                },
                error: function (error){
                    alert("its an error");
                    console.log(error);
                }
            })

        }
    })
}

function discardCard(){
    let card = document.getElementById("discardCard").value;
    let playerID = $("#player_id").text();
    console.log(playerID);
    if (card == null){
        return;
    }
    //stompClient.send("/topic/group/" + playerID.trim() + "/discard", {}, JSON.stringify(card));
    $.ajax({
        url: url + "/topic/group/discard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim()
            ,
            "card":card
        }),
        success: function (data){

            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })

    $.ajax({
        url: url + "/topic/group/discardPile",
        type: 'GET',
        dataType: "json",
        contentType: "application/json",

        success: function (data){

            stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data));

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })

}

function selectAlly(){

    let card = document.getElementById("discardCard").value;
    //console.log("response cards...."+card);
    let playerID = $("#player_id").text();
    if (card == null){
        return;
    }
    //stompClient.send("/topic/group/" + playerID.trim() + "/discard", {}, JSON.stringify(card));
    $.ajax({
        url: url + "/topic/group/select",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim()
            ,
            "card":card
        }),
        success: function (data){

            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));
            $.ajax({
                url: url + "/game/rank",
                type: 'GET',
                dataType: "json",
                contentType: "application/json",

                success: function (data){

                    for (let i = 0; i < totalNumPlayers; i++){
                        stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data));
                    }


                },
                error: function (error){
                    alert("its an error");
                    console.log(error);
                }
            })

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function TournamentCards(){
    $("#cards_index3").hide();
    let card = document.getElementById("cards_input3").value;
    console.log("response cards...."+card);
    let playerID = $("#player_id").text();
    console.log("passing player: "+playerID);


    $.ajax({
        url: url + "/topic/group/getTournamentCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim(),
            "card": card
        }),
        success: function (data){
            //data received will be game

            stompClient.send("/topic/group/" + playerID.trim()+"/tournamentAdCardUpdate", {}, JSON.stringify(data));

            for(let i=0;i<data.playersInStory.length;i++){
                console.log("data.playersInStory[i].id: "+data.playersInStory[i].id+" and parseInt($(#player_id).text()): "+parseInt($("#player_id").text()));
                if(data.playersInStory[i].id == parseInt($("#player_id").text())){
                    //when gets to the last player
                    console.log("i: "+i+" data.playersInStory.length: "+data.playersInStory.length);
                    if(i == data.playersInStory.length-1){
                        stompClient.send("/topic/group/" + data.playersInStory[0].id.toString() + "/chooseTourCard", {}, JSON.stringify(data));
                        stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data.discardedAdventureCardList));
                        return;
                    }
                    stompClient.send("/topic/group/" + data.playersInStory[i+1].id.toString() + "/chooseTourCard", {}, JSON.stringify(data));
                    return;
                }
            }

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function chooseCards(){
    $("#Questions").empty();
    $("#cards_index").hide();
    let card = document.getElementById("cards_input").value;
    console.log("response cards...."+card);
    let playerID = $("#player_id").text();
    console.log("passing player: "+playerID);

    $.ajax({
        url: url + "/topic/group/chooseCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim(),
            "card": card
        }),
        success: function (data){
            //data is game(not sure check)
            if(sponsorPlayerID == parseInt($("#player_id").text())){
                console.log("data.playersInStory.length "+data.playersInStory);
                for(i=0; i<data.playersInStory.length; i++){
                    stompClient.send("/topic/group/"+(data.playersInStory[i].id).toString()+"/playQuest", {}, JSON.stringify(data));
                }
            }else{
                stompClient.send("/topic/progress", {}, JSON.stringify(data));
            }


            //discarding the submitted cards
            let stages = card.split(" ");
            console.log("Array of cards to discard: " + stages);
            console.log(stages);
            const pile = [];
            for(i=0; i < stages.length; i++){
                let disCards = stages[i].split(",");
                for(j=0; j<disCards.length; j++){
                    pile.push(disCards[j]);
                }
                console.log(pile);


            }
            console.log(pile);
            console.log(playerID.trim());
            $.ajax({
                url: url + "/topic/group/discardCards",
                type: 'POST',
                dataType: "json",
                contentType: "application/json",
                data: JSON.stringify({
                    "player":playerID.trim()
                    ,
                    "card":pile
                }),
                success: function (data){

                    stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));

                },
                error: function (error){
                    alert("its an error");
                    console.log(error);
                }
            })

            $.ajax({
                url: url + "/topic/group/discardPile",
                type: 'GET',
                dataType: "json",
                contentType: "application/json",

                success: function (data){

                    stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data));

                },
                error: function (error){
                    alert("its an error");
                    console.log(error);
                }
            })

            $.ajax({
                url: url + "/game/rank",
                type: 'GET',
                dataType: "json",
                contentType: "application/json",

                success: function (data){

                    for (let i = 0; i < totalPlayers; i++){
                        stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data));
                    }


                },
                error: function (error){
                    alert("its an error");
                    console.log(error);
                }
            })

 //make ajax request for the deck

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
    $.ajax({
                            url: url + "/game/currentplayer",
                            type: 'GET',
                            dataType: "json",
                            contentType: "application/json",

                            success: function (data){

                                $("#Messages").empty();



                            },
                            error: function (error){
                                alert("its an error");
                                console.log(error);
                            }
                        })
}

function TestBid(){
    $("#Questions").empty();
    $("#test_bid").hide();
    let bid = document.getElementById("bidNo_input").value;
    console.log("response Bid...."+bid);
    let playerID = $("#player_id").text();
    console.log("passing player: "+playerID);

    $.ajax({
        url: url + "/topic/group/DoBid",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim(),
            "bid": bid
        }),
        success: function (data){
            //data received will be game with updated bid/updated playerInStoryList

            //check if there is a winner
            //if winner then call /topic/progress
            //don't forget to do return
            if(data.playWinner.length == 1){
                console.log("data.players: "+data.players);
                for(let i=0;i<totalPlayers; i++){
                    stompClient.send("/topic/group/" + i, {}, JSON.stringify(data.players[i].inHandCards));
                    stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data.players));
                }
                stompClient.send("/topic/progress", {}, JSON.stringify(data));
                return;
            }

            //else
            //now we have info about the highest bid
            //ask next player in story to bid
            for(let i=0; i<data.playersInStory.length;i++){
                if(data.playersInStory[i].id == parseInt($("#player_id").text())){
                    if(i == data.playersInStory.length-1){
                        stompClient.send("/topic/group/"+data.playersInStory[0].id.toString()+"/testBid", {}, JSON.stringify(data));
                    }else{
                        stompClient.send("/topic/group/"+data.playersInStory[i+1].id.toString()+"/testBid", {}, JSON.stringify(data));
                    }
                }
            }
        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function TestDiscard(){
    $("#cards_index4").hide()
    let card = document.getElementById("cards_input4").value;
    console.log("response cards...."+card);
    let playerID = $("#player_id").text();
    console.log("passing player: "+playerID);

    $.ajax({
        url: url + "/topic/group/bidCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim()
            ,
            "card":card
        }),
        success: function (data){
            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data.players[parseInt($("#player_id").text())].inHandCards));
            stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data.discardedAdventureCardList));
        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function getAdventureCard(){
    let playerID = $("#player_id").text();
    console.log(playerID);

    $.ajax({
        url: url + "/topic/group/getCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim()
        }),
        success: function (data){

            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function getStoryCard(){
    let playerID = $("#player_id").text();
    console.log(playerID);
    $("#getStoryCard").hide();

    $.ajax({
        url: url + "/topic/group/getStoryCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim()
        }),
        success: function (data){
            if(data.cardType == "STORY_TOURNAMENT"){
                PlayerTookStory = parseInt($("#player_id").text());
                console.log("current story player: "+PlayerTookStory);
            }
            stompClient.send("/topic/group/" + playerID.trim()+ "/storyCard", {}, JSON.stringify(data));
        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })

    currPlayer += 1;
    if(currPlayer==totalPlayers){
                                    currPlayer = 0;
                                }
                    console.log(currPlayer);
                    console.log("Event show story card");
                    stompClient.send("/topic/group/showCard/" + currPlayer, {},  JSON.stringify(currPlayer));


}

function Quest(){
    let playerID = $("#player_id").text();

    $.ajax({
        url: url + "/topic/group/quest",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim(),
        }),
        success: function (data){
            QuestCard = data.currentStoryCard;

            stompClient.send("/topic/group/"+playerID.trim()+"/quest", {}, JSON.stringify(data));

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function tournament(){
    let nowAsking = parseInt($("#player_id").text())+1;
    if(nowAsking==totalPlayers){
        nowAsking = 0;
    }
    let playerID = $("#player_id").text();

    $.ajax({
        url: url + "/topic/group/setTournamentPlayer",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim(),
        }),
        success: function (data){
            //this data is game
            //display adventure cards for player
            stompClient.send("/topic/group/" + playerID.trim()+"/tournamentAdCardUpdate", {}, JSON.stringify(data));

            //ask other players
            stompClient.send("/topic/group/"+nowAsking.toString()+"/TournamentPlay?", {}, JSON.stringify(data));
        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function Event(eventName){

    $.ajax({
        url: url + "/topic/group/eventS",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            eventName,
        }),
        success: function (data){
            //QuestCard = data;
            stompClient.send("/topic/group/event", {}, JSON.stringify(data));

        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
    //stompClient.send("/topic/group/" + currPlayer+1, {},  JSON.stringify("Show Story Card"));



}

function refreshCards(){

}


function submitStoryPlayer(){
    let nowAsking = parseInt($("#player_id").text())+1;
    if(nowAsking==totalPlayers){
        nowAsking = 0;
    }
    let playerID = $("#player_id").text();

    $.ajax({
        url: url + "/topic/group/sumbitPlayerResForStory",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player":playerID.trim()
        }),
        success: function (data){
            stompClient.send("/topic/group/"+nowAsking.toString()+"/playQuest", {}, JSON.stringify(data));
        },
        error: function (error){
            alert("its an error");
            console.log(error);
        }
    })
}

function MakeGame(){
    let playerName = document.getElementById("name").value;
    let playerNum = document.getElementById("num").value;
    thisPlayer = playerName;
    totalPlayers = playerNum;
    if(playerName == null || playerName ==='' || playerNum == null || playerNum ===''){
        alert("Please fill required fields");
    }else{

        $.ajax({
            url: url + "/game/start",
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "player":{
                    "name":playerName
                },
                "num_players":playerNum
            }),
            success: function (data){

                gameId = data.gameId;
                totalPlayers = data.totalNumPlayers;
                playersJoined = data.playersJoined;
                connect(gameId, playersJoined, totalPlayers, 0);
                //alert("You created a game, Game id is: " + data.gameId);
            },
            error: function (error){
                alert("its an error");
                console.log(error);
            }
        })
    }
}

function JoinGame(){
    let playerName = document.getElementById("name").value;
    if(playerName == null || playerName ===''){
        alert("Please enter a name!");
    }else{
        let gameId = document.getElementById("joincode").value;
        if(gameId == null ||gameId ===''){
            alert("Please Enter Game ID");
        }
        $.ajax({
            url: url + "/game/connect",
            type: 'POST',
            dataType: "json",
            contentType: "application/json",
            data: JSON.stringify({
                "player":{
                    "name":playerName
                },
                "gameId":gameId
            }),
            success: function (data){
                gameId = data.gameId;
                thisPlayer = playerName;
                totalPlayers = data.totalNumPlayers;
                playersJoined = data.playersJoined;
                connect(gameId, playersJoined, totalPlayers, playersJoined-1);

            },
            error: function (error){
                console.log(error);
            }
        })
    }
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function askQuestions(type, question){
    Question = question;
    if(Question == "who wants to be sponsor"){
        QuestCard = type;//remember to set it to null after the quest is done
        $("#Questions").empty();
        $("#Questions").append(`<p>Do you wanna be the sponsor?</p>
            </br><button id="yes" onclick="responseYes()">Yes</button>
            </br><button id="no" onclick="responseNo()">No</button>`);
    }
    if(Question == "who wants to play the Quest"){
        $("#Questions").empty();
        $("#Questions").append(`<p>Do you want to play the quest?</p>
            </br><button id="yes" onclick="responseYes()">Yes</button>
            </br><button id="no" onclick="responseNo()">No</button>`);
    } else if (Question  == "event card"){
        $("#Questions").empty();
        $("#Questions").append(`<p>Event is taking place</p>`);
        //Event();
    }else if (Question == "Do you want to play the tournament?"){
        $("#Questions").empty();
        $("#Questions").append(`<p>Do you want to play the Tournament?</p>
            </br><button id="yes" onclick="responseYes()">Yes</button>
            </br><button id="no" onclick="responseNo()">No</button>`);
    }
}

function responseYes(){
    let nowAsking = parseInt($("#player_id").text())+1;
    if(nowAsking==totalPlayers){
        nowAsking = 0;
    }
    $("#Questions").empty();
    if(Question == "who wants to be sponsor"){
        sponsorPlayerID = parseInt($("#player_id").text());
        console.log("Player wants to be the sponsor");
        Quest();
        return;
    }
    if(Question == "who wants to play the Quest"){
        console.log("Player wants to play");
        submitStoryPlayer();
    }
    if(Question == "Do you want to play the tournament?"){
        //save the player to game.playerinstory
        console.log("Player wants to play the tournament");
        tournament();

    }

}

function responseNo(){
    let nowAsking = parseInt($("#player_id").text())+1;
    if(nowAsking==totalPlayers){
        nowAsking = 0;
    }
    $("#Questions").empty();
    let data = QuestCard;
    let Card = StoryCard;//works only for tournament now
    let playerID = $("#player_id").text();
    let nextPlayerID = (parseInt(playerID)+1).toString();
    let num = 0;

    if(Question == "who wants to be sponsor"){
        console.log("Player doesn't wanna be the sponsor");
        if(parseInt(playerID) == (totalPlayers-1)){
            stompClient.send("/topic/group/" + num.toString() + "/storyCard", {}, JSON.stringify(data));
        }else{
            stompClient.send("/topic/group/" + nextPlayerID + "/storyCard", {}, JSON.stringify(data));
        }
    }
    if(Question == "who wants to play the Quest"){
        console.log("Player doesn't want to play");

        //get the game with get method and ask player if wanna play
        $.ajax({
            url: url + "/topic/group/getGame",
            type: 'GET',
            dataType: "json",
            contentType: "application/json",

            success: function (data){

                stompClient.send("/topic/group/"+nowAsking.toString()+"/playQuest", {}, JSON.stringify(data));

            },
            error: function (error){
                alert("its an error");
                console.log(error);
            }
        })
    }
    else if (Question == "Do you want to play the tournament?"){
        console.log("Player doesn't want to play the tournament");
        $.ajax({

            url: url + "/topic/group/getGame",
            type: 'GET',
            dataType: "json",
            contentType: "application/json",

            success: function (data){

                stompClient.send("/topic/group/"+nowAsking.toString()+"/TournamentPlay?", {}, JSON.stringify(data));
            },
            error: function (error){
                alert("its an error");
                console.log(error);
            }
        })

    }
}

function sendName() {
    stompClient.send("/start", {}, JSON.stringify({'name': $("#name").val()}));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

$(function () {
    $("form").on('submit', function (e) {
        e.preventDefault();
    });
    $( "#connect" ).click(function() { connect(); });
    $( "#disconnect" ).click(function() { disconnect(); });
    $( "#send" ).click(function() { MakeGame(); });
    $( "#code" ).click(function() { JoinGame(); });
    $( "#discard" ).unbind('click').click(function() { discardCard(); });
    $( "#cards_button" ).unbind('click').click(function() { chooseCards(); });
    $( "#cards_button3" ).unbind('click').click(function() { TournamentCards(); });
    $( "#Bid_button" ).unbind('click').click(function() { TestBid(); });
    $( "#cards_button4" ).unbind('click').click(function() { TestDiscard(); });
});
