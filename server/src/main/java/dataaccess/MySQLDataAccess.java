package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import datamodel.AuthData;
import datamodel.GameData;
import datamodel.LoginData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MySQLDataAccess implements DataAccess {

    public MySQLDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException ex) {
            System.out.println("Something's wrong");
            //throw new DataAccessException("");
        }
    }

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("SELECT username FROM users WHERE username = ?")){
                preparedStatement.setString(1, userData.username());
                var result = preparedStatement.executeQuery();
                if (result.next()){
                    throw new DataAccessException("Error: Forbidden");
                }
            }
            try (var preparedStatement = conn.prepareStatement("INSERT INTO users (username, password, email) VALUES(?, ?, ?)")){
                preparedStatement.setString(1, userData.username());
                preparedStatement.setString(2, obfuscatePassword(userData.password()));
                preparedStatement.setString(3, userData.email());
                preparedStatement.executeUpdate();
            }

            String authToken = DataAccess.generateAuthToken();
            return addAuthDataToDatabase(conn, authToken, userData.username());

        } catch (SQLException ex){
            throw new ServerConnectionInterruptException("Error: connection interrupted");
        }
    }

    @Override
    public AuthData login(LoginData loginData) throws DataAccessException {
        //INSERT INTO validAuthData VALUES (<authToken>, <username>);
        try (var conn = DatabaseManager.getConnection()) {
            try (var verifyLoginDataStatement = conn.prepareStatement("SELECT * FROM users WHERE username = ?")){
                verifyLoginDataStatement.setString(1, loginData.username());
                var resultSet = verifyLoginDataStatement.executeQuery();
                if (resultSet.next()) {
                    if (BCrypt.checkpw(loginData.password(), resultSet.getString(2))) {
                        String authToken = DataAccess.generateAuthToken();
                        return addAuthDataToDatabase(conn, authToken, loginData.username());
                    }
                }
                    throw new DataAccessException("Error: unauthorized");

            }
        } catch (SQLException ex) {
            throw new ServerConnectionInterruptException("Error: connection interrupted");

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
            throw new ServerConnectionInterruptException("Error: connection interrupted");

        }
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
                    return result.getString(2);
                }
            }
        } catch (SQLException ex) {
            throw new ServerConnectionInterruptException("Error: connection interrupted");

        }
    }

    private Map<String, String> getUsersInGame(int gameID) throws DataAccessException {
        HashMap<String, String> users = new HashMap<>();
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM games WHERE gameID = ?")){
                preparedStatement.setInt(1, gameID);
                var response = preparedStatement.executeQuery();
                if (response.next()){
                    users.put("whiteUsername", response.getString(2));
                    users.put("blackUsername", response.getString(3));
                } else {
                    throw new DataAccessException("Error: bad request");
                }
            }
        } catch (SQLException ex) {
            throw new ServerConnectionInterruptException("Error: connection interrupted");
        }
        return users;
    }

    @Override
    public void clearDatabase() throws DataAccessException {
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
            throw new DataAccessException("Error: server error");
        }
    }

    @Override
    public Collection<GameData> listGames(String authToken) throws DataAccessException {
        GameData thisGameData;
        ArrayList<GameData> listOfGames = new ArrayList<>();
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM games")){
                var response = preparedStatement.executeQuery();
                while (response.next()){
                    thisGameData = buildGameFromResultSet(response);
                    listOfGames.add(thisGameData);
                }
            }
        } catch (SQLException ex) {
            throw new ServerConnectionInterruptException("Error: connection interrupted");

        }
        return listOfGames;
    }

    @Override
    public int createGame(String authToken, String gameName) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var preparedStatement = conn.prepareStatement(
                    "INSERT INTO games (gameName, chessGame) VALUES(?, ?)", Statement.RETURN_GENERATED_KEYS)){
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
            throw new ServerConnectionInterruptException("Error: connection interrupted");

        }
        return 0;
    }

    @Override
    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        var users = getUsersInGame(gameID);
        //UPDATE gameData SET whiteUsername/blackUsername = <username> WHERE gameID = <gameID>;
        String username = validateAuthToken(authToken);
        try (var conn = DatabaseManager.getConnection()) {
            String userColor;
            if (playerColor.equals("WHITE") && users.get("whiteUsername") == null) {
                userColor = "whiteUsername";
            } else if (playerColor.equals("BLACK") && users.get("blackUsername") == null) {
                userColor = "blackUsername";
            } else {
                throw new DataAccessException("Error: already taken");
            }
            try (var preparedStatement = conn.prepareStatement("UPDATE games SET " + userColor + " = ? WHERE gameID = ?")){
                preparedStatement.setString(1, username);
                preparedStatement.setInt(2, gameID);
                preparedStatement.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new ServerConnectionInterruptException("Error: connection interrupted");

        }
    }

    @Override
    public AuthData getAuthData(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()){
            try (var preparedStatement = conn.prepareStatement("SELECT * FROM validAuthTokens WHERE authToken = ?")){
                preparedStatement.setString(1, authToken);
                var result = preparedStatement.executeQuery();
                if (result.next()){
                    return new AuthData(result.getString("username"), result.getString("authToken"));
                } else {
                    return null;
                }
            }
        } catch (SQLException ex){
            throw new ServerConnectionInterruptException("Error: couldn't connect");
        }
    }

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

    private String obfuscatePassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private GameData buildGameFromResultSet(ResultSet resultSet) throws DataAccessException {
        try {
            int gameID = resultSet.getInt(1);
            String whiteUsername = resultSet.getString(2);
            String blackUsername = resultSet.getString(3);
            String gameName = resultSet.getString(4);
            String chessGameAsJson = resultSet.getString(5);
            ChessGame chessGame = deserializeChessGame(chessGameAsJson);
            return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
        } catch (SQLException ex){
            throw new ServerConnectionInterruptException("Error: connection interrupted");

        }
    }

    private AuthData addAuthDataToDatabase(Connection conn, String authToken, String username) throws DataAccessException {
        try (var preparedStatement = conn.prepareStatement("INSERT INTO validauthtokens (authToken, username) VALUES(?, ?)")){
            preparedStatement.setString(1, authToken);
            preparedStatement.setString(2, username);
            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw new ServerConnectionInterruptException("Error: connection interrupted");

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
