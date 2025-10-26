package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MySQLDataAccess implements DataAccess {
    Map<String, String> users;

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {


        if (users.containsKey(userData.username())) {
            throw new DataAccessException("Error: already taken");
        }
        users.put(userData.username(), userData);
        String authToken = generateAuthToken();
        validAuthTokens.put(authToken, userData.username());
        return new AuthData(userData.username(), authToken);
    }

    @Override
    public AuthData login(LoginData loginData) throws DataAccessException {
        return null;
    }

    @Override
    public void logout(String authToken) throws DataAccessException {

    }

    @Override
    public UserData getUser(String username) {
        return null;
    }

    @Override
    public void validateAuthToken(String authToken) throws DataAccessException {

    }

    @Override
    public void clearDatabase() {

    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        return List.of();
    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {

    }

    String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS users (
              'username' varchar(256) NOT NULL,
              'password' varchar(256) NOT NULL,
              'email' varchar(256) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS validAuthTokens (
            
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games
            """
    };

    private void configureDatabase() {
        try (var conn = DatabaseManager.getConnection()){
            DatabaseManager.createDatabase();
        } catch (DataAccessException | SQLException ex){
            System.exit(-1);
        }
    }
}
