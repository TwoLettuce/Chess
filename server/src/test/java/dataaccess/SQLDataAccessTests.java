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

public class SQLDataAccessTests {
    MySQLDataAccess mySQLDataAccess = new MySQLDataAccess();
    UserService SQLUserService = new UserService(mySQLDataAccess);
    DataService SQLDataService = new DataService(mySQLDataAccess);
    GameService SQLGameService = new GameService(mySQLDataAccess);
    UserData sampleSQLUserData = new UserData("myUsername", "myPassword", "myEmail@cs240.gov");
    UserData existingSQLUser = new UserData("I-exist!", "pa$$w0rd!", "iamironman@cia.gov");
    String SQLAuth;

    @BeforeEach
    void reset() throws DataAccessException {
        SQLDataService.clear();
        SQLAuth = SQLUserService.register(existingSQLUser).authToken();
        SQLUserService.logout(SQLAuth);
    }

    @Test
    public void registerNormalUser() throws DataAccessException {
        var response = SQLUserService.register(sampleSQLUserData);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
    }

    @Test
    public void registerInvalidUser() {
        Assertions.assertThrows(DataAccessException.class, () -> SQLUserService.register(new UserData("", " ", ",. . 09")));
    }

    @Test
    public void loginNormalUser() throws DataAccessException {
        var sampleLoginData  = new LoginData(existingSQLUser.username(), existingSQLUser.password());
        var authData = SQLUserService.login(sampleLoginData);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals(existingSQLUser.username(), authData.username());
    }

    @Test
    public void loginUserNotRegistered() throws DataAccessException{
        SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password()));
        Assertions.assertThrows(DataAccessException.class, () -> SQLUserService.login((new LoginData("non existent", "non existent"))));
    }



    @Test
    public void logoutNormalUsers() throws DataAccessException {
        UserData[] users = {new UserData("user1", "pass1", "email1@me.com"),
                new UserData("user2", "pass2", "email2@me.com"),
                new UserData("user3", "pass3", "email3@me.com")};
        ArrayList<String> authTokens = new ArrayList<>();
        for (var user : users) {
            var authData = SQLUserService.register(user);
            authTokens.add(authData.authToken());
        }
        for (var authToken : authTokens) {
            SQLUserService.logout(authToken);
        }
        for (var authToken : authTokens) {
            Assertions.assertThrows(DataAccessException.class, () -> SQLUserService.logout(authToken));
        }
    }

    @Test
    public void logoutUserNotLoggedIn() {
        Assertions.assertThrows(DataAccessException.class, () -> SQLUserService.logout(SQLAuth));
    }


    @Test
    public void createOneGameThenList() throws Exception{
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        Assertions.assertDoesNotThrow(() -> SQLGameService.createGame(SQLAuth, "Game1"));
        ArrayList<GameData> correctGameList = new ArrayList<>(List.of(new GameData(1, null, null, "Game1", new ChessGame())));
        Assertions.assertEquals(correctGameList, SQLGameService.listGames(SQLAuth));
    }

    @Test
    public void createManyGamesThenList () throws Exception {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        for (int i = 0; i < 5; i++){
            SQLGameService.createGame(SQLAuth, "Game" + i);
        }
        ArrayList<GameData> correctGameList = new ArrayList<>(List.of(
                new GameData(1, null, null, "Game0", new ChessGame()),
                new GameData(2, null, null, "Game1", new ChessGame()),
                new GameData(3, null, null, "Game2", new ChessGame()),
                new GameData(4, null, null, "Game3", new ChessGame()),
                new GameData(5, null, null, "Game4", new ChessGame())
        ));
        Assertions.assertEquals(correctGameList, SQLGameService.listGames(SQLAuth));
    }

    @Test
    public void testCreateGame() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        Assertions.assertDoesNotThrow(()-> SQLGameService.createGame(SQLAuth, "Game1"));
    }

    @Test
    public void testClear() throws DataAccessException {
        MemoryDataAccess emptyData = new MemoryDataAccess();
        DataService dataServiceForEmptyData = new DataService(emptyData);
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();

        SQLGameService.createGame(SQLAuth, "game1");
        SQLGameService.createGame(SQLAuth, "game2");
        Assertions.assertDoesNotThrow(SQLDataService::clear);
        Assertions.assertDoesNotThrow(dataServiceForEmptyData::clear);
    }

    @Test
    public void listOfZeroGames() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        Assertions.assertEquals(new ArrayList<GameData>(), SQLGameService.listGames(SQLAuth));
    }

    @Test
    public void testJoinGameWhite() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        SQLGameService.createGame(SQLAuth, "Game");
        Assertions.assertDoesNotThrow(() -> SQLGameService.joinGame(SQLAuth, new JoinRequest("WHITE", 1)));
    }

    @Test
    public void testJoinGameBlack() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        SQLGameService.createGame(SQLAuth, "Game");
        Assertions.assertDoesNotThrow(() -> SQLGameService.joinGame(SQLAuth, new JoinRequest("BLACK", 1)));
    }

    @Test
    public void testJoinGameBothPlayers() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        String auth2 = SQLUserService.register(new UserData("username", "pass", "email")).authToken();
        SQLGameService.createGame(SQLAuth, "Game");
        Assertions.assertDoesNotThrow(() -> SQLGameService.joinGame(SQLAuth, new JoinRequest("BLACK", 1)));
        Assertions.assertDoesNotThrow(() -> SQLGameService.joinGame(auth2, new JoinRequest("WHITE", 1)));
    }

    @Test
    public void testJoinGameBothPlayersAreSamePlayer() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        int gameID = SQLGameService.createGame(SQLAuth, "Game");
        SQLGameService.joinGame(SQLAuth, new JoinRequest("WHITE", gameID));
        Assertions.assertDoesNotThrow(() -> SQLGameService.joinGame(SQLAuth, new JoinRequest("BLACK", gameID)));
    }

    @Test
    public void testJoinGameColorTaken() throws DataAccessException {
        SQLAuth = SQLUserService.login(new LoginData(existingSQLUser.username(), existingSQLUser.password())).authToken();
        String auth2 = SQLUserService.register(new UserData("IAmYourFather", "NOOOOO!", "star.wars@george.lucas")).authToken();
        int gameID = SQLGameService.createGame(SQLAuth, "Game");
        SQLGameService.joinGame(SQLAuth, new JoinRequest("WHITE", gameID));
        Assertions.assertThrows(DataAccessException.class, () -> SQLGameService.joinGame(auth2, new JoinRequest("WHITE", gameID)));
    }
}
