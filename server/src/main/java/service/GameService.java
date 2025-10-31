package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.JoinRequest;
import handler.ExceptionHandler;

import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    private void validateAuthToken(String authToken) throws DataAccessException {
        if (dataAccess.getAuthData(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        validateAuthToken(authToken);
        return dataAccess.listGames(authToken);
    }

    public int createGame(String authToken, Object gameName) throws DataAccessException {
        validateAuthToken(authToken);
        if (!(gameName instanceof String)) {
            throw new DataAccessException("Error: bad request");
        }
        String gameNameString = gameName.toString();
        return dataAccess.createGame(authToken, gameNameString);
    }

    public void joinGame(String authToken, JoinRequest joinRequest) throws DataAccessException {
        validateAuthToken(authToken);
        ExceptionHandler.validateColor(joinRequest.playerColor());
        dataAccess.joinGame(authToken, joinRequest.playerColor(), joinRequest.gameID());
    }

}
