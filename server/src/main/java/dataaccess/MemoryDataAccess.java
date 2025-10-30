package dataaccess;

import chess.ChessGame;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private int nextGameID = 1000;
    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, String> validAuthTokens = new HashMap<>();
    private ArrayList<GameData> games = new ArrayList<>();

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())) {
            throw new DataAccessException("Error: already taken");
        }
        users.put(userData.username(), userData);
        String authToken = DataAccess.generateAuthToken();
        validAuthTokens.put(authToken, userData.username());
        return new AuthData(userData.username(), authToken);
    }

    public AuthData login(LoginData loginData) throws DataAccessException {
        if (users.containsKey(loginData.username())
                && getUser(loginData.username()).password().equals(loginData.password())) {
            String authToken = DataAccess.generateAuthToken();
            validAuthTokens.put(authToken, loginData.username());
            return new AuthData(loginData.username(), authToken);
        } else {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public void logout(String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        validAuthTokens.remove(authToken);
    }

    public void clearDatabase() {
        validAuthTokens.clear();
        users.clear();
        games.clear();
        nextGameID = 1;
    }

    private void validateAuthToken(String authToken) throws DataAccessException {
        if (!validAuthTokens.containsKey(authToken)) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        return games;

    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        validateAuthToken(authToken);
        int thisGameID = nextGameID;
        nextGameID++;
        games.add(new GameData(thisGameID, null, null, gameName, new ChessGame()));
        return thisGameID;
    }

    @Override
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        validateAuthToken(authToken);
        validateFields(authToken, playerColor, gameID);
        int gameIndex = findGameIndex(gameID);
        String playerUsername = validAuthTokens.get(authToken);
        if (playerColor.equals("WHITE")) {
            games.get(gameIndex).setWhiteUsername(playerUsername);
        } else {
            games.get(gameIndex).setBlackUsername(playerUsername);
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
