package dataaccess;

import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

public class MySQLDataAccess implements DataAccess {
    Map<String, String> users;

    public MySQLDataAccess() throws DataAccessException {
        configureDatabase();
    }

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("")){

            }
        } catch (SQLException ex){
            throw new DataAccessException("Error:");
        }
        return null;
//        if (users.containsKey(userData.username())) {
//            throw new DataAccessException("Error: already taken");
//        }
//        users.put(userData.username(), userData.password());
//        String authToken = DataAccess.generateAuthToken();
//        validAuthTokens.put(authToken, userData.username());
//        return new AuthData(userData.username(), authToken);
    }

    @Override
    public AuthData login(LoginData loginData) throws DataAccessException {
        //INSERT INTO userData VALUES (<username>, <password>, <email>);
        //INSERT INTO validAuthData VALUES (<authToken>, <username>);
        return null;
    }

    @Override
    public void logout(String authToken) throws DataAccessException {
        //DELETE FROM validAuthData WHERE authToken = <authToken>
    }

    @Override
    public UserData getUser(String username) {
        //SELECT * FROM userData WHERE username = <username>;
        return null;
    }

    @Override
    public void validateAuthToken(String authToken) throws DataAccessException {
        //SELECT authToken FROM validAuthData WHERE authToken=<authToken>;
    }

    @Override
    public void clearDatabase() {
        //TRUNCATE TABLE <tableName>;
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        //SELECT * FROM gameData;
        return null;
    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        //INSERT INTO gameData (gameID, gameName, chessGame) VALUES (<gameID>, <gameName>, <chessGameAsJson> );
        //TODO: maybe just have gameID auto-increment itself within the MySQL table structure?
        return 0;
    }

    @Override
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        //UPDATE gameData SET whiteUsername/blackUsername = <username> WHERE gameID = <gameID>;
    }

    String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS users (
              username varchar(256) NOT NULL,
              password varchar(256) NOT NULL,
              email varchar(256) NOT NULL,
              PRIMARY KEY (username)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS validAuthTokens (
              authToken varchar(256) NOT NULL,
              username varchar(256) NOT NULL,
              PRIMARY KEY (authToken)
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
              gameID int NOT NULL AUTO_INCREMENT,
              whiteUsername varchar(256),
              blackUsername varchar(256),
              gameName varchar(256) NOT NULL,
              chessGame text NOT NULL,
              PRIMARY KEY (gameID)
            )
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()){
            for (var table : tables){
                try (var preparedTables = conn.prepareStatement(table)){
                    preparedTables.executeQuery();
                }
            }
        } catch (DataAccessException | SQLException ex){
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
    }
}
