package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, String> validAuthTokens = new HashMap<>();
    private ArrayList<GameData> games = new ArrayList<>();

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return users.get(username);
    }

    public boolean findUsernameInAuthData(String username){
        return validAuthTokens.containsValue(username);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData game : games){
            if (game.getGameID() == gameID){
                return game;
            }
        }
        return null;
    }

    @Override
    public void addUser(UserData userData) throws DataAccessException {
        users.put(userData.username(), userData);
    }

    @Override
    public AuthData addAuthData(AuthData authData) throws DataAccessException {
        validAuthTokens.put(authData.authToken(), authData.username());
        return authData;
    }

    public void logout(String authToken) throws DataAccessException {
        validAuthTokens.remove(authToken);
    }

    public void clearDatabase() {
        validAuthTokens.clear();
        users.clear();
        games.clear();
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        return games;

    }

    @Override
    public int createGame(String authToken, int gameID, String gameName) throws DataAccessException {
        games.add(new GameData(gameID, null, null, gameName, new ChessGame()));
        return gameID;
    }

    @Override
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        validateFields(authToken, playerColor, gameID);
        int gameIndex = findGameIndex(gameID);
        String playerUsername = validAuthTokens.get(authToken);
        if (playerColor.equals("WHITE")) {
            games.get(gameIndex).setWhiteUsername(playerUsername);
        } else {
            games.get(gameIndex).setBlackUsername(playerUsername);
        }
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        if (validAuthTokens.containsKey(authToken)) {
            return new AuthData(validAuthTokens.get(authToken), authToken);
        } else {
            return null;
        }
    }

    private void validateFields(String authToken, String playerColor, int gameID) throws DataAccessException{
        int gameIndex = findGameIndex(gameID);
        validatePlayerColor(games.get(gameIndex), playerColor, validAuthTokens.get(authToken));
    }

    private int findGameIndex(int gameID) throws DataAccessException {
        for (int i = 0; i < games.size(); i++){
            if (games.get(i).getGameID() == gameID) {
                return i;
            }
        }
        throw new DataAccessException("Error: bad request");
    }

    private void validatePlayerColor(GameData game, String playerColor, String username) throws DataAccessException {
        var ex = new DataAccessException("Error: already taken");
        if (playerColor == null) {
            throw new DataAccessException("Error: bad request");
        }
        if (playerColor.equals("WHITE")){
            if (game.getWhiteUsername() != null) {throw ex;}
        } else if (playerColor.equals("BLACK")) {
            if (game.getBlackUsername() != null) {throw ex;}
        } else {
            throw new DataAccessException("Error: bad request");
        }
    }
}
