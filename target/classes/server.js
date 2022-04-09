// const path = require('path');
// const http = require('http');
// const express = require('express');
// const socketio = require('socket.io');
//
// const app = express();
// const server = http.createServer(app);
// const io = socketio(server);
//
// app.use(express.static(path.join(__dirname, 'public')));
//
// let player1 = {
//     "name":"PLayer1",
//     "AdventureCard":{"0":"Squire","1":"Squire","2":"Squire","3":"Squire","4":"Squire","5":"Squire","6":"Squire","7":"Squire","8":"Squire","9":"Squire","10":"Squire","11":"Squire"},
//     "DiscardCard": {"0":"Squire","1":"Squire"}
//     };
// let player2 = {
//     "name":"PLayer2",
//     "AdventureCard":{"0":"Squire","1":"Squire","2":"Squire","3":"Squire","4":"Squire","5":"Squire","6":"Squire","7":"Squire","8":"Squire","9":"Squire","10":"Squire","11":"Squire"},
//     "DiscardCard": {"0":"Squire","1":"Squire"}
//     };
// let player3 = {
//     "name":"PLayer3",
//     "AdventureCard":{"0":"Squire","1":"Squire","2":"Squire","3":"Squire","4":"Squire","5":"Squire","6":"Squire","7":"Squire","8":"Squire","9":"Squire","10":"Squire","11":"Squire"},
//     "DiscardCard": {"0":"Squire","1":"Squire"}
//     };
//
// let total = 0;
// let requiredPlayerNUm = 4;
// newPlayer = player3;
//
// io.on('connection', socket => {
//     total++;
//     if(total < requiredPlayerNUm){
//         socket.broadcast.emit('waiting_message', newPlayer);
//     }
//
//     //when all players enters the game
//     if(total == requiredPlayerNUm){
//         handOverPlayers(playerList);
//     }
//
//     //broadcast to others when a user connects
//     // socket.broadcast.emit();
//
//     //broadcast to all
//     // io.emit()
//
//
// });
//
// function handOverPlayers(playerList){
//     socket.broadcast.emit('makeDisplay', playerList);
// }
//
// io.on('removeCard', data => {
//     //if a player discards card
//     //make necessary changes and send back player's whole info
//
//     // whenever something changes about a certain player
//     // Do the calculation and send this with the whole info about that player
//     playerData = player2; //example
//     socket.broadcast.emit('changes', playerData);
// })
//
//
// const PORT = 3000 || process.env.PORT;
//
// server.listen(PORT, () => console.log(`Server running on port ${PORT}`));
//
