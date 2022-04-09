// const joinForm = document.getElementById('join-form');
// const removeCard = document.getElementById('remove_cards');
// let playerName;
//
// let player2 = {
//     "name":"PLayer2",
//     "AdventureCard":{"0":"Squire","1":"Squire","2":"Squire","3":"Squire","4":"Squire","5":"Squire","6":"Squire","7":"Squire","8":"Squire","9":"Squire","10":"Squire","11":"Squire"},
//     "DiscardCard": {"0":"Squire","1":"Squire"}
// };
// let player3 = {
//     "name":"PLayer3",
//     "AdventureCard":{"0":"Squire","1":"Squire","2":"Squire","3":"Squire","4":"Squire","5":"Squire","6":"Squire","7":"Squire","8":"Squire","9":"Squire","10":"Squire","11":"Squire"},
//     "DiscardCard": {"0":"Squire","1":"Squire"}
// };
//
// let allPlayers = [player2, player3];
//
// let player = {
//     "name":"PLayer1",
//     "AdventureCard":{"0":"Squire","1":"Squire","2":"Squire","3":"Squire","4":"Squire","5":"Squire","6":"Squire","7":"Squire","8":"Squire","9":"Squire","10":"Squire","11":"Squire"},
//     "DiscardCard": {"0":"Squire","1":"Squire"}
// };
//
//
// // function join_game(){
// //     console.log("here");
// //     player1.name = document.getElementById("player_name").value;
// // }
//
// const socket = io();
//
// socket.on('makeDisplay', data =>{
//     Object.keys(data).forEach(key =>{
//         if(key.name = player.name){
//             document.getElementById('player').innerHTML = displayPlayer(key);
//         }else{
//             document.getElementById('other_players').innerHTML = displayOtherPlayers(key);
//         }
//     });
// });
//
// socket.on('otherPlayersDiscardedCard', data =>{
//     // let playerName = data.name;
//
//     let playerName = player2.name;
//
//     document.getElementById(playerName).innerHTML = displayForEach(data.DiscardCard);
// });
//
// socket.on('waiting_message', data =>{
//     if(player.name != data.name){
//         allPlayers += data;
//     }
//     else{
//         console.log('waiting for other players');
//     }
//     console.log(allPlayers);
//     //collect the players that joined and display their info
//     //in DOM manipulation assign the div id for particular player with
//     //name from the data
// });
//
// socket.on('changes', data =>{
//     let player = data.name;
//     if(player != player.name){
//         //change info for other player
//         document.getElementById(player).innerHTML = displayOtherPlayers(data);
//     }else{
//         //insert data in to the info of player
//     }
// });
//
//
// //DOM manipulation
// // function outputCardData(card){
// //     console.log(player.name);
// //     let result = `<b>Adventure Cards of player: ${playerName}<b><br>`;
// //     i=1;
// //     Object.values(card).forEach(val =>{
// //         result += `<input type="checkbox" id="card${i}">
// //         <label for="card${i}"> ${val}</label><br>`;
// //         i++;
// //     });
// //     return result;
// // }
//
// function sendNamePlayers(){
//     let name = document.getElementById("player_name").value;
//     let players = document.getElementById("num_players").value;
//
//     console.log(name);
//     console.log(players);
//     location.href = 'main.html';
//
// }
//
// function displayPlayer(key){
//     //makes display for the player
//     console.log(player.name);
//
//     let result = `<b>Player name: ${key.name}<b><br>`;
//
//     result += `<b>Adventure Cards<b><br>`;
//     i=1;
//     card = key.AdventureCard;
//     Object.values(card).forEach(val =>{
//         result += `<input type="checkbox" id="card${i}">
//         <label for="card${i}"> ${val}</label><br>`;
//         i++;
//     });
//     return result;
// }
// function displayOtherPlayers(key){
//     // makes display for other players
//     let result = `<div id = "${key.name}">
//     <b>Player name: ${key.name}<b><br>
//     </div>`;
//     return result;
// }
//
// // function displayForEach(data){
// //     let result = "<b>Discarded Card<b><br>";
// // 	Object.keys(data).forEach(key =>{
// // 		result += `<p>${key}</p><br>`;
// // 	});
// // }
//
// removeCard.addEventListener('button', e =>{
//     socket.emit('removeCard', cardRemoved);
// })
//
// joinForm.addEventListener('submit', e =>{
//     player.name = document.getElementById('player_name').value;
// })