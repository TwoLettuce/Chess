package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.JoinRequest;

import java.util.Collection;

public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        return dataAccess.listGames(authToken);
    }

    public int createGame(String authToken, Object gameName) throws DataAccessException {
        if (!(gameName instanceof String))
            throw new DataAccessException("Error: bad request");
        String gameNameString = gameName.toString();
        return dataAccess.createGame(authToken, gameNameString);
    }

    public void joinGame(String authToken, JoinRequest joinRequest) throws DataAccessException {
        dataAccess.joinGame(authToken, joinRequest.playerColor(), joinRequest.gameID());
    }

}
