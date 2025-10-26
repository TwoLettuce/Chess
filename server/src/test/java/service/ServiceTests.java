package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.GameData;
import datamodel.JoinRequest;
import datamodel.LoginData;
import datamodel.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

public class ServiceTests {
    MemoryDataAccess dataAccess = new MemoryDataAccess();
    UserService userService = new UserService(dataAccess);
    DataService dataService = new DataService(dataAccess);
    GameService gameService = new GameService(dataAccess);
    UserData sampleUserData = new UserData("myUsername", "myPassword", "myEmail@cs240.gov");
    UserData existingUser = new UserData("I-exist!", "pa$$w0rd!", "iamironman@cia.gov");
    String auth;

    @BeforeEach
    void reset() throws DataAccessException {
        dataService.clear();
        auth = userService.register(existingUser).authToken();
        userService.logout(auth);
    }

    @Test
    public void registerNormalUser() throws DataAccessException {
        var response = userService.register(sampleUserData);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
    }

    @Test
    public void registerInvalidUser() {
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(new UserData("", " ", ",. . 09")));
    }

    @Test
    public void loginNormalUser() throws DataAccessException {
        var sampleLoginData  = new LoginData(existingUser.username(), existingUser.password());
        var authData = userService.login(sampleLoginData);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals(existingUser.username(), authData.username());
    }

    @Test
    public void loginUserNotRegistered() throws DataAccessException{
        userService.login(new LoginData(existingUser.username(), existingUser.password()));
        Assertions.assertThrows(DataAccessException.class, () -> userService.login((new LoginData("non existent", "non existent"))));
    }



    @Test
    public void logoutNormalUsers() throws DataAccessException {
        UserData[] users = {new UserData("user1", "pass1", "email1@me.com"),
                            new UserData("user2", "pass2", "email2@me.com"),
                            new UserData("user3", "pass3", "email3@me.com")};
        ArrayList<String> authTokens = new ArrayList<>();
        for (var user : users) {
            var authData = userService.register(user);
            authTokens.add(authData.authToken());
        }
        for (var authToken : authTokens) {
            userService.logout(authToken);
        }
        for (var authToken : authTokens) {
            Assertions.assertThrows(DataAccessException.class, () -> userService.logout(authToken));
        }
    }

    @Test
    public void logoutUserNotLoggedIn() {
        Assertions.assertThrows(DataAccessException.class, () -> userService.logout(auth));
    }


    @Test
    public void createOneGameThenList() throws Exception{
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        Assertions.assertDoesNotThrow(() -> gameService.createGame(auth, "Game1"));
        ArrayList<GameData> correctGameList = new ArrayList<>(List.of(new GameData(1000, null, null, "Game1", new ChessGame())));
        Assertions.assertEquals(correctGameList, gameService.listGames(auth));
    }

    @Test
    public void createManyGamesThenList () throws Exception {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        for (int i = 0; i < 5; i++){
            gameService.createGame(auth, "Game" + i);
        }
        ArrayList<GameData> correctGameList = new ArrayList<>(List.of(
                new GameData(1000, null, null, "Game0", new ChessGame()),
                new GameData(1001, null, null, "Game1", new ChessGame()),
                new GameData(1002, null, null, "Game2", new ChessGame()),
                new GameData(1003, null, null, "Game3", new ChessGame()),
                new GameData(1004, null, null, "Game4", new ChessGame())
                ));
        Assertions.assertEquals(correctGameList, gameService.listGames(auth));
    }

    @Test
    public void testClear() throws DataAccessException {
        MemoryDataAccess emptyData = new MemoryDataAccess();
        DataService dataServiceForEmptyData = new DataService(emptyData);
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();

        gameService.createGame(auth, "game1");
        gameService.createGame(auth, "game2");
        Assertions.assertDoesNotThrow(dataService::clear);
        Assertions.assertDoesNotThrow(dataServiceForEmptyData::clear);
    }

    @Test
    public void listOfZeroGames() throws DataAccessException {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        Assertions.assertEquals(new ArrayList<GameData>(), gameService.listGames(auth));
    }

    @Test
    public void testJoinGameWhite() throws DataAccessException {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        gameService.createGame(auth, "Game");
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(auth, new JoinRequest("WHITE", 1000)));
    }

    @Test
    public void testJoinGameBlack() throws DataAccessException {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        gameService.createGame(auth, "Game");
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(auth, new JoinRequest("BLACK", 1000)));
    }

    @Test
    public void testJoinGameBothPlayers() throws DataAccessException {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        String auth2 = userService.register(new UserData("username", "pass", "email")).authToken();
        gameService.createGame(auth, "Game");
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(auth, new JoinRequest("BLACK", 1000)));
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(auth2, new JoinRequest("WHITE", 1000)));
    }

    @Test
    public void testJoinGameBothPlayersAreSamePlayer() throws DataAccessException {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        int gameID = gameService.createGame(auth, "Game");
        gameService.joinGame(auth, new JoinRequest("WHITE", gameID));
        Assertions.assertDoesNotThrow(() -> gameService.joinGame(auth, new JoinRequest("BLACK", gameID)));
    }

    @Test
    public void testJoinGameColorTaken() throws DataAccessException {
        auth = userService.login(new LoginData(existingUser.username(), existingUser.password())).authToken();
        String auth2 = userService.register(new UserData("IAmYourFather", "NOOOOO!", "star.wars@george.lucas")).authToken();
        int gameID = gameService.createGame(auth, "Game");
        gameService.joinGame(auth, new JoinRequest("WHITE", gameID));
        Assertions.assertThrows(DataAccessException.class, () -> gameService.joinGame(auth2, new JoinRequest("WHITE", gameID)));
    }
}
