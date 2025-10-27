package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.Collection;
import java.util.UUID;

public interface DataAccess {
    AuthData registerUser(UserData userData) throws DataAccessException;
    AuthData login(LoginData loginData) throws DataAccessException;
    void logout(String authToken) throws DataAccessException;
    UserData getUser(String username);
    void validateAuthToken(String authToken) throws DataAccessException;
    void clearDatabase();
    Collection<GameData> listGames(String authToken) throws DataAccessException;
    int createGame(String authToken, String gameName) throws DataAccessException;
    void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException;
    static String generateAuthToken(){
        return UUID.randomUUID().toString();
    }
}
