// Immediate load check
console.log('Debug: socket.js starting to load');

// Global variables
var stompClient = null;
const url = "http://localhost:8080"
// server.port = 8080;
let QuestCard = null;
let Question = null;
let totalPlayers;
let sponsorPlayerID;  //commented out in /topic/progress
let TookStoryCard = false;
let PlayerTookStory = null;
let StoryCard = null;
let currPlayer = 0;
let gameId;
let playersJoined;

// Current hand cards (kept in sync whenever the hand is re-rendered)
let currentHandCards = [];

// ===== Card Picker State =====
let pickerMode = null;          // 'quest-sponsor' | 'quest-player' | 'tournament' | 'discard' | 'ally-amour'
let pickerCurrentCards = [];    // snapshot of hand at time picker was opened
let pickerSelectedIndices = new Set();
let pickerUsedIndices = new Set();  // indices already committed to earlier stages (sponsor multi-stage)
let pickerNumStages = 0;
let pickerCurrentStage = 0;
let pickerStageSelections = [];
let pickerMaxSelect = -1;       // -1 = no limit
let pickerContextMsg = '';      // extra context message (e.g. BP requirements)
let pickerQuestData = null;     // holds game data while sponsor/player is choosing
let storyCardDrawerID = -1;    // ID of the player who drew the current story card (broadcast to all clients)

// Debug function with timestamp - console only
function debugLog(message) {
    const timestamp = new Date().toLocaleTimeString();
    const debugMessage = timestamp + ' - ' + message;
    
    // Log to console only
    console.log('Debug:', debugMessage);
}let playerturn = 0;


function setConnected(connected, gameId) {
    console.log("setConnected called - connected:", connected, "gameId:", gameId);

    $("#connect").prop("disabled", connected);
    $("#disconnect").prop("disabled", !connected);
    if (connected) {
        $("#conversation").show();
        $("#gameIdDisplay")
            .html("Game ID: " + gameId + "<br><small>Share this ID with other players to join</small>")
            .show();
        console.log("Updated game ID display");
    }
    else {
        $("#conversation").hide();
        $("#gameIdDisplay").empty();
    }
    $("#greetings").html(gameId);
}

function connect(gameId, playersJoined, totalNumPlayers, playerName) {
    //playerName is actually the playerID
    var socket = new SockJS('/gs-guide-websocket');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function (frame) {
        setConnected(true, gameId);
        console.log('Connected: ' + frame);
        stompClient.subscribe("/topic/progress", function (response) {
            let data = JSON.parse(response.body);
            console.log(data);


            if (data.test == true && data.playersCardDone == data.playersInStory.length) {
                //show index for bidding to the first player in story
                stompClient.send("/topic/group/" + data.playersInStory[0].id.toString() + "/testBid", {}, JSON.stringify(data));
                return;
            }

            if (data.playWinner != null) {
                $("#storyCard").empty();
                $("#storyCard").append("The Winner of the Story is: ");
                for (let i = 0; i < data.playWinner.length; i++) {
                    $("#storyCard").append(data.playWinner[i].name);
                }
                //test bidder will be asked to choose cards
                console.log("data.test: " + data.test);
                if (data.test == true) {
                    stompClient.send("/topic/group/" + data.playWinner[0].id.toString() + "/bidCards", {}, JSON.stringify(data));
                    // turn advances after TestDiscard completes — do NOT advance here
                } else {
                    // Quest or tournament fully resolved — advance turn
                    if (parseInt($("#player_id").text()) == storyCardDrawerID) {
                        advanceTurn();
                    }
                }

                sponsorPlayerID = null;
                TookStoryCard = false;
                return;
            }


            if (data == "Nobody wants to sponsor the quest") {
                sponsorPlayerID = null;
                TookStoryCard = false;
                $("#storyCard").empty();
                // Only the story card drawer advances the turn
                if (parseInt($("#player_id").text()) == storyCardDrawerID) {
                    advanceTurn();
                }
                return;
            }

            if (data == "We have enough players to start the game") {
                $("#info").hide();
                $("#gameID").hide();
                $("#greetings").hide();

                return;
            }

            if (data.cardType == "STORY_QUEST") {
                if (data.sponsor != null) {
                    $("#storyCard").empty();
                    $("#storyCard").append("<p>Card: " + data.name + ", Type: " + data.cardType + "</br>Sponsor: " + data.sponsor.name + "</br>No.of Stages: " + data.noOfStages + "</br>SpecialFoe: " + data.specialFoe + "</p>");
                } else {
                    $("#storyCard").empty();
                    $("#storyCard").append("<p>Card: " + data.name + ", Type: " + data.cardType + "</br>Sponsor: " + data.sponsor + "</br>No.of Stages: " + data.noOfStages + "</br>SpecialFoe: " + data.specialFoe + "</p>");
                }
            } else if (data.cardType == "STORY_EVENT") {

                $("#storyCard").empty();
                $("#storyCard").append("<p>Card: " + data.name + ", Type: " + data.cardType + "</br>Special Action: " + data.specialAction + "</p>");
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
            } else if (data.cardType == "STORY_TOURNAMENT") {
                StoryCard = data;
                $("#storyCard").empty();
                $("#storyCard").append("<p>Card: " + data.name + ", Type: " + data.cardType + "</br>Bonus shield: " + data.bonusShields + "</p>");


                console.log(data.name);
                TookStoryCard = false;
            }
        });

        $("#player_id").append(playerName);

        stompClient.subscribe("/topic/group/" + playerName + "/tournamentAdCardUpdate", function (response) {
            let data = JSON.parse(response.body);
            data = data.players[parseInt($("#player_id").text())].inHandCards;
            console.log(data);
            currentHandCards = data;
            renderDeck(data);
        });

        stompClient.subscribe("/topic/group/" + playerName, function (response) {
            let data = JSON.parse(response.body);
            console.log(data);
            currentHandCards = data;
            renderDeck(data);
        });

        stompClient.subscribe("/topic/group/showCard/" + playerName, function (response) {

            currPlayer = JSON.parse(response.body);

            $("#getStoryCard").show();


        })

        stompClient.subscribe("/topic/group/" + playerName + "/storyCard", function (response) {
            if (TookStoryCard == true) {
                TookStoryCard = false;
                stompClient.send("/topic/progress", {}, JSON.stringify("Nobody wants to sponsor the quest"));
            } else {
                TookStoryCard = true; //when winning message will be given to everyone set this to false;
                /*if (currPlayer == totalNumPlayers){
                    currPlayer = 0;
                } else {
                    currPlayer +=1;
                }*/

                currPlayer = parseInt($("#player_id").text());
                let data = JSON.parse(response.body);
                console.log("Got the story card: " + data.name);

                stompClient.send("/topic/progress", {}, JSON.stringify(data));

                if (data.cardType == "STORY_QUEST") {
                    console.log("asking if wanna sponsor!!");
                    askQuestions(data, "who wants to be sponsor");
                } else if (data.cardType == "STORY_EVENT") {
                    console.log("story event card!!");
                    askQuestions(data, "event card");
                    Event(data.name);
                    $("#card-picker").hide();
                }
                //tournament card in play
                else if (data.cardType == "STORY_TOURNAMENT") {
                    console.log("tournament card in play");

                    console.log("asking if wanna play the tournament!!");
                    askQuestions(StoryCard, "Do you want to play the tournament?");
                }
            }
        });

        // Sync storyCardDrawerID across all clients so any client can coordinate turn end
        stompClient.subscribe("/topic/storyCardDrawer", function (response) {
            storyCardDrawerID = JSON.parse(response.body);
        });

        stompClient.subscribe("/topic/group/" + playerName + "/bidCards", function (response) {
            // ask test winner to choose cards to discard as their bid payment
            openCardPicker('discard', currentHandCards, { contextMsg: 'Select the cards you are discarding as your bid.' });
        });

        stompClient.subscribe("/topic/group/" + playerName + "/testBid", function (response) {
            let data = JSON.parse(response.body);

            $("#Questions").empty();
            $("#Questions").show();
            $("#Questions").append(data.testBidMessage);

            $("#test_bid").show();
        });

        stompClient.subscribe("/topic/group/" + playerName + "/playQuest", function (response) {
            let data = JSON.parse(response.body);
            console.log(data);

            if (parseInt($("#player_id").text()) == sponsorPlayerID) {
                //see if there is any player in quest
                if (data.playersInStory.length == 0) {
                    stompClient.send("/topic/progress", {}, JSON.stringify("Nobody wants to sponsor the quest"));
                    return;
                }
                // ask sponsor to pick cards for each quest stage
                pickerQuestData = data;
                let numStages = (QuestCard && QuestCard.noOfStages) ? QuestCard.noOfStages : 1;
                openCardPicker('quest-sponsor', currentHandCards, {
                    numStages: numStages,
                    contextMsg: 'Build each quest stage: pick a Foe (required) and optional Weapon cards per stage.'
                });
                return;
            }

            else {
                for (i = 0; i < data.playersInStory.length; i++) {
                    if (parseInt($("#player_id").text()) == data.playersInStory[i].id) {
                        pickerQuestData = data;
                        // Total quest stages minus any test stage
                        let totalStages = (data.currentStoryCard && data.currentStoryCard.noOfStages)
                            ? data.currentStoryCard.noOfStages : 1;
                        let hasTest = data.test || false;
                        let numPlayerStages = totalStages - (hasTest ? 1 : 0);

                        // Show stage BP targets in the Questions area
                        let stageBPs = data.sponsorStageBattlePoints;
                        if (stageBPs && stageBPs.length > 0) {
                            let bpStr = stageBPs.map(function(bp, idx) {
                                return 'Stage ' + (idx + 1) + ': <b>' + bp + ' BP</b>';
                            }).join(' &nbsp;|&nbsp; ');
                            $("#Messages").html('Stage targets to beat: ' + bpStr);
                        }

                        openCardPicker('quest-player', currentHandCards, {
                            numStages: numPlayerStages,
                            contextMsg: 'Choose Weapons, Allies, Amours for each quest stage. Foes and Tests not allowed.'
                        });
                        return;
                    }
                }
                askQuestions(QuestCard, "who wants to play the Quest");
            }
        });

        stompClient.subscribe("/topic/group/" + playerName + "/quest", function (response) {
            let nowAsking = parseInt($("#player_id").text()) + 1;
            let data = JSON.parse(response.body);
            /*$("#Messages").empty();
            $("#Messages").append("It is player " + data.currentPlayer + "'s turn");*/
            //data is a game

            //data is a game

            stompClient.send("/topic/progress", {}, JSON.stringify(data.currentStoryCard));

            if (nowAsking == totalPlayers) {
                nowAsking = 0;
            }
            stompClient.send("/topic/group/" + nowAsking.toString() + "/playQuest", {}, JSON.stringify(data));

        });

        //send and subscribe for Tournament
        stompClient.subscribe("/topic/group/" + playerName + "/chooseTourCard", function (response) {
            let data = JSON.parse(response.body);
            if (data.playWinner.length > 0) {
                console.log("winner is ready to display!!!!")

                for (let j = 0; j < totalNumPlayers; j++) {
                    console.log("calling player: " + j + "/rank");
                    stompClient.send("/topic/group/" + j + "/rank", {}, JSON.stringify(data.players));
                }
                stompClient.send("/topic/progress", {}, JSON.stringify(data));
            } else {
                pickerQuestData = data;
                openCardPicker('tournament', currentHandCards, {
                    contextMsg: 'Choose the cards you will play in the tournament. Higher total BP wins.'
                });
            }
        });

        stompClient.subscribe("/topic/group/" + playerName + "/TournamentPlay?", function (response) {
            let data = JSON.parse(response.body);

            if (PlayerTookStory == parseInt($("#player_id").text())) {
                //every one was asked if they want to play

                //check if there is any player in playerInStory
                if (data.playersInStory.length == 0) {
                    stompClient.send("/topic/progress", {}, JSON.stringify("Nobody wants to sponsor the quest"));//it works though string is out off the topic
                    return;
                }
                //now do next

                //will send to the first player in tournament(other players will send from function TournamentCards())
                stompClient.send("/topic/group/" + data.playersInStory[0].id.toString() + "/chooseTourCard", {}, JSON.stringify(data));
            } else {
                console.log("asking if wanna play the tournament!!");
                askQuestions(StoryCard, "Do you want to play the tournament?");
            }

        });


        stompClient.subscribe("/topic/group/discardPile", function (response) {
            let data = JSON.parse(response.body);
            console.log(data);
            $("#discardPile").empty();
            for (let i = 0; i < data.length; i++) {
                $("#discardPile").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].cardType + ", " + data[i].battlePoints + "</td></tr>");
            }

        });

        stompClient.subscribe("/topic/group/" + playerName + "/rank", function (response) {
            let data = JSON.parse(response.body);
            console.log(data);
            const winners = [];
            console.log("ready to update /rank");

            $("#ranks").empty();
            for (let i = 0; i < data.length; i++) {
                $("#ranks").append("<tr><td>" + i + ". " + data[i].name + ", " + data[i].rankCard.name + "   Shields: " + data[i].shields + "   In Play Cards: </td></tr>");
                if (data[i].shields >= 5) {
                    winners.push(data[i].name);
                }
                for (let j = 0; j < data[i].inPlayCards.length; j++) {
                    $("#ranks").append(data[i].inPlayCards[j].name + ", " + data[i].inPlayCards[j].cardType + " BattlePoints: " + data[i].inPlayCards[j].battlePoints + "</td></tr>");
                }
            }
            let victors = "";
            for (let p = 0; p < winners.length; p++) {
                victors += winners[p] + " ";
            }
            if (winners.length > 0) {
                alert("The Winner(s) of the game is: " + victors)

            }



        });

        stompClient.subscribe("/topic/group/event", function (response) {
            let data = JSON.parse(response.body);
            console.log(data);
            //$("#discardPile").empty();
            let players = data.players[playerName];
            currentHandCards = players.inHandCards;
            renderDeck(players.inHandCards);
            $("#ranks").empty();
            const winners = [];
            for (let i = 0; i < data.players.length; i++) {
                $("#ranks").append("<tr><td>" + i + ". " + data.players[i].name + ", " + data.players[i].rankCard.name + "   Shields: " + data.players[i].shields + "   In Play Cards: </td></tr>");
                if (data.players[i].shields >= 5) {
                    winners.push(data.players[i].name);
                }
                for (let j = 0; j < data.players[i].inPlayCards.length; j++) {
                    $("#ranks").append(data.players[i].inPlayCards[j].name + ", " + data.players[i].inPlayCards[j].cardType + " BattlePoints: " + data.players[i].inPlayCards[j].battlePoints + "</td></tr>");
                }
            }

            if (winners.length > 0) {
                let victors = "";
                for (let p = 0; p < winners.length; p++) {
                    victors += winners[p] + " ";
                }
                alert("The Winner(s) of the game is: " + victors)
            }

            // Event cards resolve instantly — advance turn after all clients update
            if (parseInt($("#player_id").text()) == storyCardDrawerID) {
                advanceTurn();
            }
        });

        if (playersJoined == totalNumPlayers) {
            totalPlayers = totalNumPlayers;
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
                success: function (data) {

                    for (let i = 0; i < totalNumPlayers; i++) {
                        stompClient.send("/topic/group/" + i, {}, JSON.stringify(data[i]));
                    }
                    $.ajax({
                        url: url + "/game/rank",
                        type: 'GET',
                        dataType: "json",
                        contentType: "application/json",

                        success: function (data) {

                            for (let i = 0; i < totalNumPlayers; i++) {
                                stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data));
                            }


                        },
                        error: function (error) {
                            alert("its an error");
                            console.log(error);
                        }
                    })


                    console.log(currPlayer);
                    console.log("begin game");
                    stompClient.send("/topic/group/showCard/" + currPlayer, {}, JSON.stringify(currPlayer));

                },
                error: function (error) {
                    alert("its an error");
                    console.log(error);
                }
            })

        }
    })
}

function discardCard() {
    let card = document.getElementById("discardCard").value;
    let playerID = $("#player_id").text();
    console.log(playerID);
    if (card == null) {
        return;
    }
    //stompClient.send("/topic/group/" + playerID.trim() + "/discard", {}, JSON.stringify(card));
    $.ajax({
        url: url + "/topic/group/discard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim()
            ,
            "card": card
        }),
        success: function (data) {

            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })

    $.ajax({
        url: url + "/topic/group/discardPile",
        type: 'GET',
        dataType: "json",
        contentType: "application/json",

        success: function (data) {

            stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data));

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })

}

function selectAlly(cardIndexStr) {
    // If called with no arg, open the picker instead
    if (cardIndexStr === undefined) {
        openCardPicker('ally-amour', currentHandCards, {
            contextMsg: 'Select one Ally or Amour card to play.'
        });
        return;
    }

    let card = cardIndexStr;
    let playerID = $("#player_id").text();
    $.ajax({
        url: url + "/topic/group/select",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim()
            ,
            "card": card
        }),
        success: function (data) {

            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));
            $.ajax({
                url: url + "/game/rank",
                type: 'GET',
                dataType: "json",
                contentType: "application/json",

                success: function (data) {

                    for (let i = 0; i < totalNumPlayers; i++) {
                        stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data));
                    }


                },
                error: function (error) {
                    alert("its an error");
                    console.log(error);
                }
            })

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function TournamentCards(cardStr) {
    $("#card-picker").hide();
    let card = cardStr;
    console.log("response cards...." + card);
    let playerID = $("#player_id").text();
    console.log("passing player: " + playerID);


    $.ajax({
        url: url + "/topic/group/getTournamentCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim(),
            "card": card
        }),
        success: function (data) {
            //data received will be game

            stompClient.send("/topic/group/" + playerID.trim() + "/tournamentAdCardUpdate", {}, JSON.stringify(data));

            for (let i = 0; i < data.playersInStory.length; i++) {
                console.log("data.playersInStory[i].id: " + data.playersInStory[i].id + " and parseInt($(#player_id).text()): " + parseInt($("#player_id").text()));
                if (data.playersInStory[i].id == parseInt($("#player_id").text())) {
                    //when gets to the last player
                    console.log("i: " + i + " data.playersInStory.length: " + data.playersInStory.length);
                    if (i == data.playersInStory.length - 1) {
                        stompClient.send("/topic/group/" + data.playersInStory[0].id.toString() + "/chooseTourCard", {}, JSON.stringify(data));
                        stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data.discardedAdventureCardList));
                        return;
                    }
                    stompClient.send("/topic/group/" + data.playersInStory[i + 1].id.toString() + "/chooseTourCard", {}, JSON.stringify(data));
                    return;
                }
            }

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function chooseCards(cardStr) {
    $("#Questions").empty();
    $("#card-picker").hide();
    $("#Messages").empty();  // Clear messages div right away

    let card = cardStr;
    console.log("response cards...." + card);
    let playerID = $("#player_id").text();
    console.log("passing player: " + playerID);

    $.ajax({
        url: url + "/topic/group/chooseCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim(),
            "card": card
        }),
        success: function (data) {
            //data is game(not sure check)
            if (sponsorPlayerID == parseInt($("#player_id").text())) {
                console.log("data.playersInStory.length " + data.playersInStory);
                for (i = 0; i < data.playersInStory.length; i++) {
                    stompClient.send("/topic/group/" + (data.playersInStory[i].id).toString() + "/playQuest", {}, JSON.stringify(data));
                }
            } else {
                stompClient.send("/topic/progress", {}, JSON.stringify(data));
            }

            //discarding the submitted cards
            let stages = card.split(" ");
            console.log("Array of cards to discard: " + stages);
            console.log(stages);
            const pile = [];
            for (i = 0; i < stages.length; i++) {
                let disCards = stages[i].split(",");
                for (j = 0; j < disCards.length; j++) {
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
                    "player": playerID.trim(),
                    "card": pile
                }),
                success: function (data) {
                    stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));
                },
                error: function (error) {
                    alert("its an error");
                    console.log(error);
                }
            })

            $.ajax({
                url: url + "/topic/group/discardPile",
                type: 'GET',
                dataType: "json",
                contentType: "application/json",
                success: function (data) {
                    stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data));
                },
                error: function (error) {
                    alert("its an error");
                    console.log(error);
                }
            })

            $.ajax({
                url: url + "/game/rank",
                type: 'GET',
                dataType: "json",
                contentType: "application/json",
                success: function (data) {
                    for (let i = 0; i < totalPlayers; i++) {
                        stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data));
                    }
                },
                error: function (error) {
                    alert("its an error");
                    console.log(error);
                }
            })
        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function TestBid() {
    $("#Questions").empty();
    $("#test_bid").hide();
    let bid = document.getElementById("bidNo_input").value;
    console.log("response Bid...." + bid);
    let playerID = $("#player_id").text();
    console.log("passing player: " + playerID);

    $.ajax({
        url: url + "/topic/group/DoBid",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim(),
            "bid": bid
        }),
        success: function (data) {
            //data received will be game with updated bid/updated playerInStoryList

            //check if there is a winner
            //if winner then call /topic/progress
            //don't forget to do return
            if (data.playWinner.length == 1) {
                console.log("data.players: " + data.players);
                for (let i = 0; i < totalPlayers; i++) {
                    stompClient.send("/topic/group/" + i, {}, JSON.stringify(data.players[i].inHandCards));
                    stompClient.send("/topic/group/" + i + "/rank", {}, JSON.stringify(data.players));
                }
                stompClient.send("/topic/progress", {}, JSON.stringify(data));
                return;
            }

            //else
            //now we have info about the highest bid
            //ask next player in story to bid
            for (let i = 0; i < data.playersInStory.length; i++) {
                if (data.playersInStory[i].id == parseInt($("#player_id").text())) {
                    if (i == data.playersInStory.length - 1) {
                        stompClient.send("/topic/group/" + data.playersInStory[0].id.toString() + "/testBid", {}, JSON.stringify(data));
                    } else {
                        stompClient.send("/topic/group/" + data.playersInStory[i + 1].id.toString() + "/testBid", {}, JSON.stringify(data));
                    }
                }
            }
        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function TestDiscard(cardStr) {
    $("#card-picker").hide();
    let card = cardStr;
    console.log("response cards...." + card);
    let playerID = $("#player_id").text();
    console.log("passing player: " + playerID);

    $.ajax({
        url: url + "/topic/group/bidCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim()
            ,
            "card": card
        }),
        success: function (data) {
            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data.players[parseInt($("#player_id").text())].inHandCards));
            stompClient.send("/topic/group/discardPile", {}, JSON.stringify(data.discardedAdventureCardList));
            // Test bid payment is the final step of a test-quest turn — advance to next player
            advanceTurn();
        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function getAdventureCard() {
    let playerID = $("#player_id").text();
    console.log(playerID);

    $.ajax({
        url: url + "/topic/group/getCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim()
        }),
        success: function (data) {

            stompClient.send("/topic/group/" + playerID.trim(), {}, JSON.stringify(data[0]));

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

// Advance turn to the player after the story card drawer and show their story card button.
// Called only once per turn, at the true end of each card resolution.
function advanceTurn() {
    let nextPlayer = (storyCardDrawerID + 1) % totalPlayers;
    currPlayer = nextPlayer;
    stompClient.send("/topic/group/showCard/" + nextPlayer, {}, JSON.stringify(nextPlayer));
}

function getStoryCard() {
    let playerID = $("#player_id").text();
    console.log(playerID);
    $("#getStoryCard").hide();

    // Broadcast who drew this story card so all clients can coordinate turn advancement
    storyCardDrawerID = parseInt(playerID);
    stompClient.send("/topic/storyCardDrawer", {}, JSON.stringify(storyCardDrawerID));

    $.ajax({
        url: url + "/topic/group/getStoryCard",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim()
        }),
        success: function (data) {
            if (data.cardType == "STORY_TOURNAMENT") {
                PlayerTookStory = parseInt($("#player_id").text());
                console.log("current story player: " + PlayerTookStory);
            }
            stompClient.send("/topic/group/" + playerID.trim() + "/storyCard", {}, JSON.stringify(data));
        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
    // NOTE: advanceTurn() is NOT called here. It is called at each card-resolution endpoint below.
}

function Quest() {
    let playerID = $("#player_id").text();

    $.ajax({
        url: url + "/topic/group/quest",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim(),
        }),
        success: function (data) {
            QuestCard = data.currentStoryCard;

            stompClient.send("/topic/group/" + playerID.trim() + "/quest", {}, JSON.stringify(data));

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function tournament() {
    let nowAsking = parseInt($("#player_id").text()) + 1;
    if (nowAsking == totalPlayers) {
        nowAsking = 0;
    }
    let playerID = $("#player_id").text();

    $.ajax({
        url: url + "/topic/group/setTournamentPlayer",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim(),
        }),
        success: function (data) {
            //this data is game
            //display adventure cards for player
            stompClient.send("/topic/group/" + playerID.trim() + "/tournamentAdCardUpdate", {}, JSON.stringify(data));

            //ask other players
            stompClient.send("/topic/group/" + nowAsking.toString() + "/TournamentPlay?", {}, JSON.stringify(data));
        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function Event(eventName) {

    $.ajax({
        url: url + "/topic/group/eventS",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            eventName,
        }),
        success: function (data) {
            //QuestCard = data;
            stompClient.send("/topic/group/event", {}, JSON.stringify(data));

        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
    //stompClient.send("/topic/group/" + currPlayer+1, {},  JSON.stringify("Show Story Card"));



}

function refreshCards() {

}

// ===== Deck Renderer (shared by all subscriptions) =====
function renderDeck(cards) {
    $("#deck").empty();
    for (let i = 0; i < cards.length; i++) {
        let bp;
        if (cards[i].cardType === "ADVENTURE_FOE") {
            bp = cards[i].battleStrengthMin;
        } else if (cards[i].cardType === "ADVENTURE_AMOUR") {
            bp = 10;
        } else {
            bp = cards[i].battlePoints;
        }
        $("#deck").append("<tr><td>" + i + ". " + cards[i].name + ", " + cards[i].cardType + ", " + bp + "</td></tr>");
    }
}

// ===== Card Picker Functions =====

function getCardBP(card) {
    if (card.cardType === "ADVENTURE_FOE") return card.battleStrengthMin || 0;
    if (card.cardType === "ADVENTURE_AMOUR") return 10;
    return card.battlePoints || 0;
}

function getPickerSelectedBP() {
    let total = 0;
    pickerSelectedIndices.forEach(function(i) { total += getCardBP(pickerCurrentCards[i]); });
    return total;
}

function openCardPicker(mode, cards, options) {
    options = options || {};
    pickerMode = mode;
    pickerCurrentCards = cards.slice();
    pickerSelectedIndices = new Set();
    pickerUsedIndices = new Set();
    pickerCurrentStage = 0;
    pickerStageSelections = [];
    pickerNumStages = options.numStages || 1;
    pickerMaxSelect = (options.maxSelect !== undefined) ? options.maxSelect : -1;
    pickerContextMsg = options.contextMsg || '';

    $("#Questions").empty();
    if (pickerContextMsg) {
        $("#Questions").append("<p>" + pickerContextMsg + "</p>");
    }
    $("#card-picker").show();
    renderPickerCards();
    updatePickerStatus();
}

function renderPickerCards() {
    let html = '';
    for (let i = 0; i < pickerCurrentCards.length; i++) {
        let card = pickerCurrentCards[i];
        let bp = getCardBP(card);
        let selected = pickerSelectedIndices.has(i);
        let used = pickerUsedIndices.has(i);
        let disabled = used || isPickerCardDisabled(i, card);

        let cls = 'card-item';
        if (selected) cls += ' card-selected';
        if (disabled) cls += ' card-disabled';

        let bpLabel = bp > 0 ? bp + ' BP' : '';
        let usedLabel = used ? ' <em style="color:#aaa;font-size:0.8em">(stage ' + getUsedStageLabel(i) + ')</em>' : '';

        html += '<div class="' + cls + '" tabindex="' + (disabled ? '-1' : '0') + '" data-idx="' + i + '"'
              + ' onclick="togglePickerCard(' + i + ')"'
              + ' onkeydown="handlePickerKey(event,' + i + ')">'
              + '<span class="card-idx">' + i + '.</span>'
              + '<span class="card-name">' + card.name + usedLabel + '</span>'
              + '<span class="card-type-label">' + card.cardType + '</span>'
              + '<span class="card-bp-val">' + bpLabel + '</span>'
              + '</div>';
    }
    $("#picker-card-list").html(html);
}

function getUsedStageLabel(idx) {
    for (let s = 0; s < pickerStageSelections.length; s++) {
        if (pickerStageSelections[s].indexOf(idx) !== -1) return (s + 1);
    }
    return '?';
}

function isPickerCardDisabled(idx, card) {
    // Already-selected cards can always be toggled off
    if (pickerSelectedIndices.has(idx)) return false;

    if (pickerMode === 'discard') return false;

    if (pickerMode === 'ally-amour') {
        return card.cardType !== 'ADVENTURE_ALLY' && card.cardType !== 'ADVENTURE_AMOUR';
    }

    if (pickerMode === 'quest-sponsor') {
        // Allies and Amours are never placed by the sponsor
        if (card.cardType === 'ADVENTURE_ALLY' || card.cardType === 'ADVENTURE_AMOUR') return true;

        let hasFoe = false, hasTest = false;
        let usedWeaponNames = new Set();
        pickerSelectedIndices.forEach(function(i) {
            let c = pickerCurrentCards[i];
            if (c.cardType === 'ADVENTURE_FOE') hasFoe = true;
            if (c.cardType === 'ADVENTURE_TEST') hasTest = true;
            if (c.cardType === 'ADVENTURE_WEAPON') usedWeaponNames.add(c.name);
        });

        if (hasTest) return true;  // test stage: nothing else allowed
        if (hasFoe && card.cardType === 'ADVENTURE_FOE') return true;  // already have a foe
        if (hasFoe && card.cardType === 'ADVENTURE_TEST') return true;  // foe chosen, no test
        if (card.cardType === 'ADVENTURE_WEAPON' && usedWeaponNames.has(card.name)) return true;  // duplicate weapon

        return false;
    }

    if (pickerMode === 'quest-player' || pickerMode === 'tournament') {
        // Foes and Tests are never played by adventuring players
        if (card.cardType === 'ADVENTURE_FOE' || card.cardType === 'ADVENTURE_TEST') return true;

        // No duplicate weapon names per stage
        if (card.cardType === 'ADVENTURE_WEAPON') {
            let usedWeaponNames = new Set();
            pickerSelectedIndices.forEach(function(i) {
                let c = pickerCurrentCards[i];
                if (c.cardType === 'ADVENTURE_WEAPON') usedWeaponNames.add(c.name);
            });
            return usedWeaponNames.has(card.name);
        }

        return false;
    }

    return false;
}

function togglePickerCard(idx) {
    let card = pickerCurrentCards[idx];
    // used-in-prior-stage cards and truly-disabled cards cannot be toggled
    // (selected cards are never disabled so they can always be deselected)
    if (pickerUsedIndices.has(idx)) return;
    if (!pickerSelectedIndices.has(idx) && isPickerCardDisabled(idx, card)) return;

    if (pickerSelectedIndices.has(idx)) {
        pickerSelectedIndices.delete(idx);
    } else {
        if (pickerMode === 'ally-amour') {
            pickerSelectedIndices.clear();
        }
        if (pickerMaxSelect > 0 && pickerSelectedIndices.size >= pickerMaxSelect) return;
        pickerSelectedIndices.add(idx);
    }
    // Re-render fully so disabled states update (e.g. selecting a Foe grays other Foes)
    renderPickerCards();
    updatePickerStatus();
}

function handlePickerKey(event, idx) {
    if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        togglePickerCard(idx);
    } else if (event.key === 'ArrowDown') {
        event.preventDefault();
        let next = idx + 1;
        while (next < pickerCurrentCards.length && pickerUsedIndices.has(next)) next++;
        if (next < pickerCurrentCards.length) $('[data-idx="' + next + '"]').focus();
    } else if (event.key === 'ArrowUp') {
        event.preventDefault();
        let prev = idx - 1;
        while (prev >= 0 && pickerUsedIndices.has(prev)) prev--;
        if (prev >= 0) $('[data-idx="' + prev + '"]').focus();
    }
}

function updatePickerStatus() {
    let bp = getPickerSelectedBP();
    let count = pickerSelectedIndices.size;
    let msg = '';

    if (pickerMode === 'quest-sponsor') {
        let stageStr = 'Stage ' + (pickerCurrentStage + 1) + ' of ' + pickerNumStages;

        // Analyse what's already selected this stage
        let hasFoe = false, hasTest = false, weaponNames = [];
        pickerSelectedIndices.forEach(function(i) {
            let c = pickerCurrentCards[i];
            if (c.cardType === 'ADVENTURE_FOE') { hasFoe = true; }
            if (c.cardType === 'ADVENTURE_TEST') { hasTest = true; }
            if (c.cardType === 'ADVENTURE_WEAPON') weaponNames.push(c.name);
        });

        if (hasTest) {
            msg = '<b>' + stageStr + '</b> — '
                + '<span style="color:#8e44ad">TEST stage</span>: players will bid instead of fight. '
                + 'No weapons needed. Click <b>Next Stage</b> (or <b>Confirm</b> on last stage) to continue.';
        } else if (hasFoe) {
            msg = '<b>' + stageStr + '</b> — Foe selected. <b>' + bp + ' BP</b>. '
                + (weaponNames.length > 0
                    ? 'Weapons: ' + weaponNames.join(', ') + '. '
                    : '')
                + 'Add more Weapons to raise stage BP, or advance to next stage. '
                + '<em style="color:#777">Duplicate weapon types are disabled.</em>';
        } else {
            msg = '<b>' + stageStr + '</b> — '
                + '<span style="color:#c0392b">No Foe yet.</span> '
                + 'Select <b>1 Foe</b> (required) OR <b>1 Test card</b>. '
                + 'Then optionally add Weapons. Allies &amp; Amours not allowed in stages.';
        }

        let hasNextStage = pickerCurrentStage < pickerNumStages - 1;
        $("#picker-next-btn").toggle(hasNextStage);
        $("#picker-confirm-btn").toggle(!hasNextStage);

    } else if (pickerMode === 'quest-player') {
        let stageStr = pickerNumStages > 1
            ? 'Stage ' + (pickerCurrentStage + 1) + ' of ' + pickerNumStages
            : 'Quest Attack';

        // Collect stage BP targets if available
        let targetBP = 0;
        if (pickerQuestData && pickerQuestData.sponsorStageBattlePoints
                && pickerQuestData.sponsorStageBattlePoints[pickerCurrentStage]) {
            targetBP = pickerQuestData.sponsorStageBattlePoints[pickerCurrentStage];
        }

        let weaponNames = [];
        pickerSelectedIndices.forEach(function(i) {
            let c = pickerCurrentCards[i];
            if (c.cardType === 'ADVENTURE_WEAPON') weaponNames.push(c.name);
        });

        if (count === 0) {
            msg = '<b>' + stageStr + '</b> — '
                + 'Pick <b>Weapons</b>, <b>Allies</b>, or <b>Amours</b>. '
                + 'Foes and Tests are disabled. No duplicate weapon types per stage.'
                + (targetBP > 0 ? ' <em>Stage target: ' + targetBP + ' BP</em>' : '');
        } else {
            let diff = targetBP > 0 ? targetBP - bp : null;
            msg = '<b>' + stageStr + '</b> — <b>' + bp + ' BP</b> selected'
                + (weaponNames.length > 0 ? ' (Weapons: ' + weaponNames.join(', ') + ')' : '')
                + (diff !== null
                    ? (diff > 0
                        ? ' — need <b style="color:#c0392b">' + diff + ' more BP</b> to beat this stage'
                        : ' — <b style="color:#27ae60">stage target met ✓</b>')
                    : '');
        }

        let hasNextStage = pickerNumStages > 1 && pickerCurrentStage < pickerNumStages - 1;
        $("#picker-next-btn").toggle(hasNextStage);
        $("#picker-confirm-btn").toggle(!hasNextStage);

    } else if (pickerMode === 'tournament') {
        if (count === 0) {
            msg = '<b>Tournament</b> — Pick <b>Weapons</b>, <b>Allies</b>, or <b>Amours</b>. '
                + 'No Foes or Tests. No duplicate weapon types. Highest total BP wins.';
        } else {
            let weaponNames = [];
            pickerSelectedIndices.forEach(function(i) {
                let c = pickerCurrentCards[i];
                if (c.cardType === 'ADVENTURE_WEAPON') weaponNames.push(c.name);
            });
            msg = '<b>Tournament</b> — <b>' + bp + ' BP</b> selected'
                + (weaponNames.length > 0 ? ' (Weapons: ' + weaponNames.join(', ') + ')' : '')
                + '. Duplicate weapon types are disabled.';
        }
        $("#picker-next-btn").hide();
        $("#picker-confirm-btn").show();

    } else if (pickerMode === 'discard') {
        msg = '<b>Discard (bid payment)</b> — '
            + (count === 0
                ? 'Select the cards you are discarding as your bid cost. Any card type allowed.'
                : count + ' card(s) selected. Click <b>Confirm</b> when done.');
        $("#picker-next-btn").hide();
        $("#picker-confirm-btn").show();

    } else if (pickerMode === 'ally-amour') {
        msg = '<b>Play Ally / Amour</b> — '
            + (count === 1
                ? 'Ready. Click <b>Confirm</b> to move this card to In-Play.'
                : 'Select <b>one</b> Ally or Amour card. Other card types are disabled.');
        $("#picker-next-btn").hide();
        $("#picker-confirm-btn").show();
    }

    $("#picker-status").html(msg);
}

function pickerClear() {
    pickerSelectedIndices.clear();
    renderPickerCards();
    updatePickerStatus();
}

function pickerNextStage() {
    if (pickerMode !== 'quest-sponsor') return;
    let stage = [...pickerSelectedIndices];
    pickerStageSelections.push(stage);
    stage.forEach(function(i) { pickerUsedIndices.add(i); });
    pickerCurrentStage++;
    pickerSelectedIndices.clear();
    renderPickerCards();
    updatePickerStatus();
}

function pickerConfirm() {
    let mode = pickerMode;
    pickerMode = null;

    if (mode === 'quest-sponsor') {
        pickerStageSelections.push([...pickerSelectedIndices]);
        let cardStr = pickerStageSelections
            .map(function(stage) { return stage.join(','); })
            .join(' ');
        chooseCards(cardStr);

    } else if (mode === 'quest-player') {
        let cardStr = [...pickerSelectedIndices].join(',');
        chooseCards(cardStr);

    } else if (mode === 'tournament') {
        let cardStr = [...pickerSelectedIndices].join(',');
        TournamentCards(cardStr);

    } else if (mode === 'discard') {
        let cardStr = [...pickerSelectedIndices].join(',');
        TestDiscard(cardStr);

    } else if (mode === 'ally-amour') {
        let idx = [...pickerSelectedIndices][0];
        if (idx !== undefined) {
            selectAlly(idx.toString());
        }
    }

    $("#card-picker").hide();
    $("#Questions").empty();
}


function submitStoryPlayer() {
    let nowAsking = parseInt($("#player_id").text()) + 1;
    if (nowAsking == totalPlayers) {
        nowAsking = 0;
    }
    let playerID = $("#player_id").text();

    $.ajax({
        url: url + "/topic/group/sumbitPlayerResForStory",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": playerID.trim()
        }),
        success: function (data) {
            stompClient.send("/topic/group/" + nowAsking.toString() + "/playQuest", {}, JSON.stringify(data));
        },
        error: function (error) {
            alert("its an error");
            console.log(error);
        }
    })
}

function MakeGame() {
    debugLog("MakeGame function called");

    let playerName = document.getElementById("name").value;
    let playerNum = document.getElementById("num").value;

    debugLog("Raw values - name: " + playerName + ", players: " + playerNum);

    // Clear whitespace and validate
    playerName = playerName ? playerName.trim() : '';
    playerNum = playerNum ? playerNum.trim() : '';

    debugLog("Trimmed values - name: " + playerName + ", players: " + playerNum);

    if (playerName === '' || playerNum === '') {
        alert("Please fill in both your name and the number of players");
        return;
    }

    // Validate number of players
    playerNum = parseInt(playerNum);
    debugLog("Parsed player number: " + playerNum);

    if (isNaN(playerNum) || playerNum < 2 || playerNum > 4) {
        alert("Please enter a valid number of players (2-4)");
        return;
    }

    debugLog("Validation passed, making AJAX request");

    $.ajax({
        url: url + "/game/start",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": {
                "name": playerName
            },
            "num_players": playerNum
        }),
        success: function (data) {
            debugLog("Game creation success, response: " + JSON.stringify(data));

            gameId = data.gameId;
            totalPlayers = data.totalNumPlayers;
            playersJoined = data.playersJoined;
            thisPlayer = playerName;

            debugLog("Before connecting - gameId: " + gameId + ", players: " + playersJoined + ", total: " + totalPlayers);

            // Show game ID immediately
            $("#gameIdDisplay")
                .html("Game ID: " + gameId + "<br><small>Share this ID with other players to join</small>")
                .show();

            // Connect to WebSocket
            connect(gameId, playersJoined, totalPlayers, 0);

            // Show alert last
            alert("Game created successfully! Your Game ID is: " + gameId + "\nShare this ID with other players so they can join.");
        },
        error: function (error) {
            debugLog("Error creating game: " + JSON.stringify(error));
            if (error.responseJSON && error.responseJSON.message) {
                alert("Error: " + error.responseJSON.message);
            } else {
                alert("Error creating game. Please try again.");
            }
        }
    });
}

function JoinGame() {
    let playerName = document.getElementById("name").value;
    let gameId = document.getElementById("joincode").value;

    if (playerName == null || playerName.trim() === '') {
        alert("Please enter a name!");
        return;
    }
    if (gameId == null || gameId.trim() === '') {
        alert("Please enter a Game ID!");
        return;
    }
    // Ensure gameId is exactly 3 digits
    if (!/^\d{3}$/.test(gameId)) {
        alert("Please enter a valid 3-digit Game ID!");
        return;
    }
    $.ajax({
        url: url + "/game/connect",
        type: 'POST',
        dataType: "json",
        contentType: "application/json",
        data: JSON.stringify({
            "player": {
                "name": playerName
            },
            "gameId": gameId
        }),
        success: function (data) {
            gameId = data.gameId;
            thisPlayer = playerName;
            totalPlayers = data.totalNumPlayers;
            playersJoined = data.playersJoined;
            connect(gameId, playersJoined, totalPlayers, playersJoined - 1);

        },
        error: function (error) {
            console.log(error);
            if (error.responseJSON && error.responseJSON.message) {
                alert("Error: " + error.responseJSON.message);
            } else {
                alert("Error joining game. Please check if the Game ID is correct.");
            }
        }
    })
}

function disconnect() {
    if (stompClient !== null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function askQuestions(type, question) {
    Question = question;
    if (Question == "who wants to be sponsor") {
        QuestCard = type;//remember to set it to null after the quest is done
        $("#Questions").empty();
        $("#Questions").append(`<p>Do you wanna be the sponsor?</p>
            </br><button id="yes" onclick="responseYes()">Yes</button>
            </br><button id="no" onclick="responseNo()">No</button>`);
    }
    if (Question == "who wants to play the Quest") {
        $("#Questions").empty();
        $("#Questions").append(`<p>Do you want to play the quest?</p>
            </br><button id="yes" onclick="responseYes()">Yes</button>
            </br><button id="no" onclick="responseNo()">No</button>`);
    } else if (Question == "event card") {
        $("#Questions").empty();
        $("#Questions").append(`<p>Event is taking place</p>`);
        //Event();
    } else if (Question == "Do you want to play the tournament?") {
        $("#Questions").empty();
        $("#Questions").append(`<p>Do you want to play the Tournament?</p>
            </br><button id="yes" onclick="responseYes()">Yes</button>
            </br><button id="no" onclick="responseNo()">No</button>`);
    }
}

function responseYes() {
    let nowAsking = parseInt($("#player_id").text()) + 1;
    if (nowAsking == totalPlayers) {
        nowAsking = 0;
    }
    $("#Questions").empty();
    if (Question == "who wants to be sponsor") {
        sponsorPlayerID = parseInt($("#player_id").text());
        console.log("Player wants to be the sponsor");
        Quest();
        return;
    }
    if (Question == "who wants to play the Quest") {
        console.log("Player wants to play");
        submitStoryPlayer();
    }
    if (Question == "Do you want to play the tournament?") {
        //save the player to game.playerinstory
        console.log("Player wants to play the tournament");
        tournament();

    }

}

function responseNo() {
    let nowAsking = parseInt($("#player_id").text()) + 1;
    if (nowAsking == totalPlayers) {
        nowAsking = 0;
    }
    $("#Questions").empty();
    let data = QuestCard;
    let Card = StoryCard;//works only for tournament now
    let playerID = $("#player_id").text();
    let nextPlayerID = (parseInt(playerID) + 1).toString();
    let num = 0;

    if (Question == "who wants to be sponsor") {
        console.log("Player doesn't wanna be the sponsor");
        if (parseInt(playerID) == (totalPlayers - 1)) {
            stompClient.send("/topic/group/" + num.toString() + "/storyCard", {}, JSON.stringify(data));
        } else {
            stompClient.send("/topic/group/" + nextPlayerID + "/storyCard", {}, JSON.stringify(data));
        }
    }
    if (Question == "who wants to play the Quest") {
        console.log("Player doesn't want to play");

        //get the game with get method and ask player if wanna play
        $.ajax({
            url: url + "/topic/group/getGame",
            type: 'GET',
            dataType: "json",
            contentType: "application/json",

            success: function (data) {

                stompClient.send("/topic/group/" + nowAsking.toString() + "/playQuest", {}, JSON.stringify(data));

            },
            error: function (error) {
                alert("its an error");
                console.log(error);
            }
        })
    }
    else if (Question == "Do you want to play the tournament?") {
        console.log("Player doesn't want to play the tournament");
        $.ajax({

            url: url + "/topic/group/getGame",
            type: 'GET',
            dataType: "json",
            contentType: "application/json",

            success: function (data) {

                stompClient.send("/topic/group/" + nowAsking.toString() + "/TournamentPlay?", {}, JSON.stringify(data));
            },
            error: function (error) {
                alert("its an error");
                console.log(error);
            }
        })

    }
}

function sendName() {
    stompClient.send("/start", {}, JSON.stringify({ 'name': $("#name").val() }));
}

function showGreeting(message) {
    $("#greetings").append("<tr><td>" + message + "</td></tr>");
}

// Main initialization when document is ready
$(function () {
    // Clear messages div on startup
    $("#Messages").empty();
    
    // Verify elements exist silently
    if (!document.getElementById('send')) {
        console.log("ERROR: Make Game button not found!");
    }
    if (!document.getElementById('name')) {
        console.log("ERROR: Name input not found!");
    }
    if (!document.getElementById('num')) {
        console.log("ERROR: Number input not found!");
    }

    $("#send").on('click', function (e) {
        e.preventDefault();
        $("#Messages").empty();  // Clear messages before making game
        MakeGame();
    });

    $("#code").on('click', function (e) {
        e.preventDefault();
        $("#Messages").empty();  // Clear messages before joining game
        JoinGame();
    });

    $("#discard").on('click', function (e) {
        e.preventDefault();
        discardCard();
    });

    $("#cards_button").on('click', function (e) {
        e.preventDefault();
        chooseCards();
    });

    $("#cards_button3").on('click', function (e) {
        e.preventDefault();
        TournamentCards();
    });

    $("#Bid_button").on('click', function (e) {
        e.preventDefault();
        TestBid();
    });

    $("#cards_button4").on('click', function (e) {
        e.preventDefault();
        TestDiscard();
    });

    $("#connect").on('click', connect);
    $("#disconnect").on('click', disconnect);
});
