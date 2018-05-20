# Omok
Project developed by Hunter College's Android Development Class.
Written by Jack Chen

## About
Omok is an Android application inspired by the board game Gomoku. The objective of the game is to place 5 pieces in a row in a horizontal, vertical or diagonal formation.  
Each player is assigned a white or black piece and alternates their turns while placing a piece of their respective color on the board.

## Design
The application works by finding and connecting with local players using Android's Wi-FI P2P framework.  
A thread is connected that is instantantiated . 
using the socket connection that is created. The thread is responsible for sending commands between the two users; for example, when 
the player is finished, a command is sent to notify a switch in controls to the other user.  
The state of the board is mapped onto a two-dimensional array where each spot on the array corresponds to the condition of it's respective spot.
