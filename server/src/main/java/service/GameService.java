package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.GameData;
import datamodel.JoinRequest;
import handler.ExceptionHandler;

import java.util.Collection;
import java.util.Objects;

public class GameService {
    private final DataAccess dataAccess;
    private int gameID = 1;
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

    public int createGame(String authToken, Object gameNameAsObject) throws DataAccessException {
        validateAuthToken(authToken);
        String gameName;
        if (gameNameAsObject != null){
            gameName = gameNameAsObject.toString();
        } else {
            throw new DataAccessException("Error: bad request");
        }
        int thisID = gameID;
        gameID++;
        return dataAccess.createGame(authToken, thisID, gameName);
    }

    public void joinGame(String authToken, JoinRequest joinRequest) throws DataAccessException {
        validateAuthToken(authToken);
        ExceptionHandler.validateColor(joinRequest.playerColor());
        GameData game = dataAccess.getGame(joinRequest.gameID());
        if (game == null){
            throw new DataAccessException("Error: bad request");
        }
        if (Objects.equals(joinRequest.playerColor(), "WHITE")){
            if (game.getWhiteUsername() != null){
                throw new DataAccessException("Error: already taken");
            }
        } else {
            if (game.getBlackUsername() != null){
                throw new DataAccessException("Error: already taken");
            }
        }
        dataAccess.addUserToGame(authToken, joinRequest.playerColor(), joinRequest.gameID());
    }

}
