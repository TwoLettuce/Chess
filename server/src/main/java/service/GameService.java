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

    public int createGame(String authToken, String gameName) throws DataAccessException {
        return dataAccess.createGame(authToken, gameName);
    }

    public void joinGame(String authToken, JoinRequest joinRequest) throws DataAccessException {
        dataAccess.joinGame(authToken, joinRequest.playerColor(), joinRequest.gameID());
    }

}
