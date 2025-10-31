package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.Collection;
import java.util.UUID;

public interface DataAccess {
    UserData getUser(String username) throws DataAccessException;
    void addUser(UserData userData) throws DataAccessException;
    AuthData addAuthData(AuthData authData) throws DataAccessException;
    void logout(String authToken) throws DataAccessException;
    void clearDatabase() throws DataAccessException;
    Collection<GameData> listGames(String authToken) throws DataAccessException;
    int createGame(String authToken, int gameID, String gameName) throws DataAccessException;
    void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException;
    AuthData getAuthData(String authToken) throws DataAccessException;
    boolean findUsernameInAuthData(String username) throws DataAccessException;
    GameData getGame(int gameID) throws DataAccessException;
    static String generateAuthToken(){
        return UUID.randomUUID().toString();
    }
}
