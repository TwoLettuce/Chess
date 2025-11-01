package dataaccess;

import chess.ChessGame;
import datamodel.GameData;
import datamodel.JoinRequest;
import datamodel.LoginData;
import datamodel.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DataService;
import service.GameService;
import service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SQLDataAccessTests {
    MySQLDataAccess mySQLDataAccess = new MySQLDataAccess();
    UserService sqlUserService = new UserService(mySQLDataAccess);
    DataService sqlDataService = new DataService(mySQLDataAccess);
    GameService sqlGameService = new GameService(mySQLDataAccess);
    UserData sampleSQLUserData = new UserData("myUsername", "myPassword", "myEmail@cs240.gov");
    UserData existingSQLUser = new UserData("I-exist!", "pa$$w0rd!", "iamironman@cia.gov");
    String authSQL;

    @BeforeEach
    void reset() throws DataAccessException {
        sqlDataService.clear();
        authSQL = sqlUserService.register(existingSQLUser).authToken();
        sqlUserService.logout(authSQL);
    }

    @Test
    public void registerNormalUserSQL() throws DataAccessException {
        var response = sqlUserService.register(sampleSQLUserData);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
    }

    @Test
    public void registerInvalidUserSQL() {
        Assertions.assertThrows(DataAccessException.class, () -> sqlUserService.register(new UserData("", " ", ",. . 09")));
    }

    @Test
    public void loginNormalUserSQL() throws DataAccessException {
        var sampleLoginData  = new LoginData(existingSQLUser.username(), existingSQLUser.password());
        var authData = sqlUserService.login(sampleLoginData);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals(existingSQLUser.username(), authData.username());
    }

    @Test
    public void loginUserNotRegisteredSQL() throws DataAccessException{
        sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password()));
        Assertions.assertThrows(DataAccessException.class, () -> sqlUserService.login((new LoginData("non existent", "non existent"))));
    }



    @Test
    public void logoutNormalUsersSQL() throws DataAccessException {
        UserData[] users = {new UserData("user1", "pass1", "email1@me.com"),
                new UserData("user2", "pass2", "email2@me.com"),
                new UserData("user3", "pass3", "email3@me.com")};
        ArrayList<String> authTokens = new ArrayList<>();
        for (var user : users) {
            var authData = sqlUserService.register(user);
            authTokens.add(authData.authToken());
        }
        for (var authToken : authTokens) {
            sqlUserService.logout(authToken);
        }
        for (var authToken : authTokens) {
            Assertions.assertThrows(DataAccessException.class, () -> sqlUserService.logout(authToken));
        }
    }

    @Test
    public void logoutUserNotLoggedInSQL() {
        Assertions.assertThrows(DataAccessException.class, () -> sqlUserService.logout(authSQL));
    }


    @Test
    public void createOneGameThenListSQL() throws Exception{
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        Assertions.assertDoesNotThrow(() -> sqlGameService.createGame(authSQL, "Game1"));
        ArrayList<GameData> correctGameList = new ArrayList<>(List.of(new GameData(1, null, null, "Game1", new ChessGame())));
        Assertions.assertEquals(correctGameList, sqlGameService.listGames(authSQL));
    }

    @Test
    public void createManyGamesThenListSQL() throws Exception {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        for (int i = 0; i < 5; i++){
            sqlGameService.createGame(authSQL, "Game" + i);
        }
        ArrayList<GameData> correctGameList = new ArrayList<>(List.of(
                new GameData(1, null, null, "Game0", new ChessGame()),
                new GameData(2, null, null, "Game1", new ChessGame()),
                new GameData(3, null, null, "Game2", new ChessGame()),
                new GameData(4, null, null, "Game3", new ChessGame()),
                new GameData(5, null, null, "Game4", new ChessGame())
        ));
        Assertions.assertEquals(correctGameList, sqlGameService.listGames(authSQL));
    }

    @Test
    public void testCreateGameSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        Assertions.assertDoesNotThrow(()-> sqlGameService.createGame(authSQL, "Game1"));
    }

    @Test
    public void testClearSQL() throws DataAccessException {
        MemoryDataAccess emptyData = new MemoryDataAccess();
        DataService dataServiceForEmptyData = new DataService(emptyData);
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();

        sqlGameService.createGame(authSQL, "game1");
        sqlGameService.createGame(authSQL, "game2");
        Assertions.assertDoesNotThrow(sqlDataService::clear);
        Assertions.assertDoesNotThrow(dataServiceForEmptyData::clear);
    }

    @Test
    public void listOfZeroGamesSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        Assertions.assertEquals(new ArrayList<GameData>(), sqlGameService.listGames(authSQL));
    }

    @Test
    public void testJoinGameWhiteSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        sqlGameService.createGame(authSQL, "Game");
        Assertions.assertDoesNotThrow(() -> sqlGameService.joinGame(authSQL, new JoinRequest("WHITE", 1)));
    }

    @Test
    public void testJoinGameBlackSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        sqlGameService.createGame(authSQL, "Game");
        Assertions.assertDoesNotThrow(() -> sqlGameService.joinGame(authSQL, new JoinRequest("BLACK", 1)));
    }

    @Test
    public void testJoinGameBothPlayersSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        String auth2 = sqlUserService.register(new UserData("username", "pass", "email")).authToken();
        sqlGameService.createGame(authSQL, "Game");
        Assertions.assertDoesNotThrow(() -> sqlGameService.joinGame(authSQL, new JoinRequest("BLACK", 1)));
        Assertions.assertDoesNotThrow(() -> sqlGameService.joinGame(auth2, new JoinRequest("WHITE", 1)));
    }

    @Test
    public void testJoinGameBothPlayersAreSamePlayerSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        int gameID = sqlGameService.createGame(authSQL, "Game");
        sqlGameService.joinGame(authSQL, new JoinRequest("WHITE", gameID));
        Assertions.assertDoesNotThrow(() -> sqlGameService.joinGame(authSQL, new JoinRequest("BLACK", gameID)));
    }

    @Test
    public void testGetUser() throws DataAccessException{
        UserData res = mySQLDataAccess.getUser(existingSQLUser.username());
        Assertions.assertTrue(Objects.equals(existingSQLUser.username(), res.username()) && Objects.equals(existingSQLUser.email(), res.email()));
    }

    @Test
    public void assertHashedPasswordStoredInUserData() throws DataAccessException{
        UserData result = mySQLDataAccess.getUser(existingSQLUser.username());
        Assertions.assertNotSame(existingSQLUser.password(), result.password());
    }

    @Test
    public void testDeleteAuthData() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        mySQLDataAccess.deleteAuthData(authSQL);
        Assertions.assertNull(mySQLDataAccess.getAuthData(authSQL));
    }

    @Test
    public void testJoinGameColorTakenSQL() throws DataAccessException {
        authSQL = sqlUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        String auth2 = sqlUserService.register(new UserData("IAmYourFather", "NOOOOO!", "star.wars@george.lucas")).authToken();
        int gameID = sqlGameService.createGame(authSQL, "Game");
        sqlGameService.joinGame(authSQL, new JoinRequest("WHITE", gameID));
        Assertions.assertThrows(DataAccessException.class, () -> sqlGameService.joinGame(auth2, new JoinRequest("WHITE", gameID)));
    }
}
