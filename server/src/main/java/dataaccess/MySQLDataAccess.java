package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
    }

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {
        String authToken = DataAccess.generateAuthToken();
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES(?, ?, ?)")){
                preparedStatement.setString(1, userData.username());
                preparedStatement.setString(2, userData.password());
                preparedStatement.setString(3, userData.email());
                preparedStatement.executeUpdate();
            }

            return addAuthDataToDatabase(conn, authToken, userData.username());

        } catch (SQLException ex){
            throw new DataAccessException("Error: already taken");
        }
    }

    @Override
    public AuthData login(LoginData loginData) throws DataAccessException {
        //INSERT INTO validAuthData VALUES (<authToken>, <username>);
        AuthData authData;
        try (var conn = DatabaseManager.getConnection()) {
            try (var verifyLoginDataStatement = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?")){
                verifyLoginDataStatement.setString(1, loginData.username());
                verifyLoginDataStatement.setString(2, loginData.password());
                var resultSet = verifyLoginDataStatement.executeQuery();
                if (resultSet.next()) {
                    String authToken = DataAccess.generateAuthToken();
                    return addAuthDataToDatabase(conn, authToken, loginData.username());
                } else {
                    throw new DataAccessException("Error: unauthorized");
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    @Override
    public void logout(String authToken) throws DataAccessException {
        //DELETE FROM validAuthData WHERE authToken = <authToken>
        validateAuthToken(authToken);
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("DELETE FROM validAuthTokens WHERE authToken = ?")){
                preparedStatement.setString(1, authToken);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex){
            throw new DataAccessException("Error: unauthorized");
        }
    }

    @Override
    public UserData getUser(String username) {
        //SELECT * FROM userData WHERE username = <username>;
        return null;
    }

    private String validateAuthToken(String authToken) throws DataAccessException {
        //SELECT authToken FROM validAuthData WHERE authToken=<authToken>;
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM validauthtokens WHERE authToken = ?")){
                preparedStatement.setString(1, authToken);
                var result = preparedStatement.executeQuery();
                if (!result.next()){
                    throw new DataAccessException("Error: unauthorized");
                } else {
                    return result.getString(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    @Override
    public void clearDatabase() {
        //TRUNCATE TABLE <tableName>;
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE users")){
                preparedStatement.executeUpdate();
            }
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE validauthtokens")){
                preparedStatement.executeUpdate();
            }
            try (var preparedStatement = conn.prepareStatement("TRUNCATE TABLE games")){
                preparedStatement.executeUpdate();
            }
        } catch (SQLException | DataAccessException ex) {
            System.exit(-1);
        }
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        //SELECT * FROM gameData;
        return null;
    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        validateAuthToken(authToken);
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("INSERT INTO games (gameName, chessGame) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)){
                preparedStatement.setString(1, gameName);
                String chessGameAsJson = serializeChessGame(new ChessGame());
                preparedStatement.setString(2, chessGameAsJson);
                preparedStatement.executeUpdate();
                var result = preparedStatement.getGeneratedKeys();
                if (result.next()){
                    return result.getInt(1);
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: bad request");
        }
        return 0;
    }

    @Override
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        //UPDATE gameData SET whiteUsername/blackUsername = <username> WHERE gameID = <gameID>;
        String username = validateAuthToken(authToken);
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement("UPDATE games SET whiteUsername = ? WHERE gameID = ?")){
                preparedStatement.setString(1, username);
                preparedStatement.setInt(2, gameID);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error: bad request");
        }
    }

//    private String getUsername(String authToken) throws DataAccessException{
//        try (var conn = DatabaseManager.getConnection()) {
//            try (var preparedStatement = conn.prepareStatement("SELECT username FROM validAuthTokens WHERE authToken = ?")){
//                preparedStatement.setString(1, authToken);
//                var response = preparedStatement.executeQuery();
//                if (response.next()){
//                    return response.getString(1);
//                }
//            }
//        } catch (SQLException ex) {
//            throw new DataAccessException("Error: bad request");
//        }
//    }

    String[] tables = {
            """
            CREATE TABLE IF NOT EXISTS users (
              username varchar(256) NOT NULL,
              password varchar(256) NOT NULL,
              email varchar(256) NOT NULL,
              PRIMARY KEY (username)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS validAuthTokens (
              authToken varchar(256) NOT NULL,
              username varchar(256) NOT NULL,
              PRIMARY KEY (authToken)
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
              gameID int NOT NULL AUTO_INCREMENT,
              whiteUsername varchar(256),
              blackUsername varchar(256),
              gameName varchar(256) NOT NULL,
              chessGame text NOT NULL,
              PRIMARY KEY (gameID)
            );
            """
    };

    private String serializeChessGame(ChessGame game){
        Gson serializer = new Gson();
        return serializer.toJson(game);
    }

    private ChessGame deserializeChessGame(String chessGameAsJson){
        Gson serializer = new Gson();
        return serializer.fromJson(chessGameAsJson, ChessGame.class);
    }

    private AuthData addAuthDataToDatabase(Connection conn, String authToken, String username) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("INSERT INTO validauthtokens (authToken, username) VALUES(?, ?)")){
            preparedStatement.setString(1, authToken);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new DataAccessException("Error: ");
        }

        return new AuthData(username, authToken);
    }


    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var table : tables) {
                try (var preparedTables = conn.prepareStatement(table)) {
                    preparedTables.executeUpdate();
                }
            }
        } catch (DataAccessException | SQLException ex){
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
    }
}
