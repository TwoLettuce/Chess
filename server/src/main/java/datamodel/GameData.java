package datamodel;

import chess.ChessGame;

import java.util.Objects;

public class GameData {
    private int gameID;
    private String whiteUsername;
    private String blackUsername;
    private String gameName;
    private ChessGame game;

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game){
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
    }

    public void setWhiteUsername(String newWhiteUsername) {
        this.whiteUsername = newWhiteUsername;
    }

    public void setBlackUsername(String newBlackUsername){
        this.blackUsername = newBlackUsername;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public ChessGame getGame() {
        return game;
    }

    public int getGameID() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameData gameData = (GameData) o;
        boolean hasIdenticalGameID = gameID == gameData.gameID;
        boolean hasIdenticalWhiteUsername = Objects.equals(whiteUsername, gameData.whiteUsername);
        boolean hasIdenticalBlackUsername = Objects.equals(blackUsername, gameData.blackUsername);
        boolean hasIdenticalGameName = Objects.equals(gameName, gameData.gameName);
        boolean hasIdenticalGame = Objects.equals(game, gameData.game);
        return hasIdenticalGameID && hasIdenticalWhiteUsername && hasIdenticalBlackUsername && hasIdenticalGameName && hasIdenticalGame;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, whiteUsername, blackUsername, gameName, game);
    }

    @Override
    public String toString() {
        return "{" +
                 gameID +
                ", '"+ whiteUsername + '\'' +
                ", '" + blackUsername + '\'' +
                ", '" + gameName + '\'' +
                ", " + game +
                '}';
    }
}
