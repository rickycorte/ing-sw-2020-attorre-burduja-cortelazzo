# Protocol Design

This document explains how our client and server communicate.

Our protocol is designed to be simple and compact. We chose to use JSON to serialize data because compared to other text serialization methods it's clean, simple and has really good libraries ready to use.

Later in this document you will find a more detailed discussion over the main game phases and their communication flow:
- [Join](#join)
- [Left](#left)
- [Setup](#setup)
- [Turn](#turn)
- [End Game](#end-game)

The protocol we created is based on the idea that the server has full control over almost any communication. If fact the server sends requests to client to let them know what they should do.

This choice serves to have a light client with almost no logic that receives instructions (we call them commands) from the server on what to do. We could imagine the client as a fancy switch that let the user chose what to do.


## Serialization
Every command is send as a JSON. They all share a common structure:

| Field   | Type         | Description |
|---------|--------------|-----------|
| type    | CommandType  | Type of command used to know how handle the command and read correct data | 
| data    | String       | Payload containing the real command |

### Types of Commands
This are all possible values of `CommandType` and they represent all possible commands sent between server and client.

| Type                | Client | Server | 
|---------------------|---------------------|---------------------|
| JOIN                | Request to join a match | -- |
| ACK_JOIN            | Reply to a join request | -- |
| LEAVE               | Request to quit a match | -- | 
| START               |  Ask the server to start a match (setup phase) | Request made by the host to start a match
| FILTER_GODS         | Request the host to pick the gods for the match | List of gods that shoul be used in the match |  
| PICK_GOD            | Request to to pick a god from the provided list | God pick by a player
| SELECT_FIRST_PLAYER | Request the host to chose the first player from a list| First player chosen by the host | 
| PLACE_WORKERS       | Request to place the workers in the map | Positions where the player wants to place his workers
| ACTION_TIME         | Request to chose a valid move to execute in the turn | Move that the player want to do | 
| UPDATE              | Update on the new map status | --
| LOSER               | Lose notification | --
| WINNER              | Win notification | --

### Command Structure 

Every `Command` class is child of a `BaseCommand` that also serialized as JSON and passed as `data` field in the wrapper described above.
This double encapsulation was designed to keep deserialization clean and to help us convert back to a specific `BaseCommand` child classes based on `CommandType`.

| Field   | Type  | Description |
|---------|-------|-----------|
| request | bool  | True if the command requires a reply from the client | 
| sender  | int   | Sender ID |
| target  | int   | Target ID that should receive the command |
| payload | *     | Data stored in the command | 

Note that commands are not used only to request an action but are also used as responses or updates of the game state.

## Errors

If a wrong command is issued to the server there are two possible ways what it's handled:
- Ignored if the player is not the current one
- Resend request if the wrong command is issued by the current player

# Game Phases

## Join

## Left

## Setup

## Turn

## End Game