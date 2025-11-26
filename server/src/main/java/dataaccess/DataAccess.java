package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.UserData;

import java.util.Collection;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;
    void addUser(UserData userData) throws DataAccessException;
    AuthData addAuthData(AuthData authData) throws DataAccessException;
    void deleteAuthData(String authToken) throws DataAccessException;
    void clearDatabase() throws DataAccessException;
    Collection<GameData> listGames(String authToken) throws DataAccessException;
    int createGame(String authToken, int gameID, String gameName) throws DataAccessException;
    void addUserToGame(String authToken, String playerColor, int gameID) throws DataAccessException;
    AuthData getAuthData(String authToken) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
}
