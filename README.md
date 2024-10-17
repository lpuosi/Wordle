# Wordle Client-Server Application

A Java-based implementation of the Wordle game, featuring multiplayer support with user registration, login, and statistics sharing.

## Features
- **Multiplayer Support**: Users can register and log in to play with others.
- **Gameplay**: Players attempt to guess a secret word with hints provided.
- **Statistics Tracking**: Users can view and share their game statistics.
- **Data Persistence**: User data and game statistics are saved in JSON format.
- **Communication Protocols**:
  - **TCP**: Used for gameplay and user login.
  - **UDP**: Used for sharing statistics among users.

## Usage Instructions

**Important**: You must run the server first before starting the client.

## How to Compile and Run

### Server:
```bash
javac -cp ".:Server/lib/gson-2.10.1.jar" Server/*.java
java -cp ".:Server/lib/gson-2.10.1.jar" Server.ServerMain
```
### Client:
```bash
javac Client/*.java
java Client.ClientMain
```

