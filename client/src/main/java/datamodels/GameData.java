package datamodels;

import chess.ChessGame;

import java.util.Objects;

public class GameData {
    private final int gameID;
    private String whiteUsername;
    private String blackUsername;
    private final String gameName;
    private final ChessGame game;

    public GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game){
        this.gameID = gameID;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;
        this.gameName = gameName;
        this.game = game;
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

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public String getGameName() {
        return gameName;
    }

    public ChessGame getGame() {
        return game;
    }

    public int getGameID() {
        return gameID;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        GameData data = (GameData) o;
        boolean sameID = gameID == data.gameID;
        boolean sameWhite = Objects.equals(whiteUsername, data.whiteUsername);
        boolean sameBlack = Objects.equals(blackUsername, data.blackUsername);
        boolean sameName = Objects.equals(gameName, data.gameName);
        boolean sameGame = Objects.equals(game, data.game);
        return sameID && sameWhite && sameBlack && sameName && sameGame;
    }

    public void setBlackUsername(String newBlackUsername){
        this.blackUsername = newBlackUsername;
    }

    public void setWhiteUsername(String newWhiteUsername) {
        this.whiteUsername = newWhiteUsername;
    }

    @Override
    public int hashCode() {
        return Objects.hash(gameID, whiteUsername, blackUsername, gameName, game);
    }


    public String getBlackUsername() {
        return blackUsername;
    }
}
