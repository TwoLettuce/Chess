package client;

import jdk.jshell.spi.ExecutionControlProvider;
import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;


public class ServerFacadeTests {

    private static Server server;
    private static final ServerFacade facade = new ServerFacade("http://localhost:8080");
    String[] register1 = new String[] {"register", "user1", "pass", "email1"};
    String[] register2 = new String[] {"register", "user2", "123o4iunfnpo2nnfne", "email2"};
    String[] register3 = new String[] {"register", "user3", "ilovemymommy", "email3"};

    String[] login1 = new String[] {"login", "user1", "pass"};
    String[] login2 = new String[] {"login", "user2", "123o4iunfnpo2nnfne"};
    String[] login3 = new String[] {"login", "user3", "ilovemymommy"};

    String[] game1 = new String[] {"create", "johnny"};
    String[] game2 = new String[] {"create", "larry"};
    String[] game3 = new String[] {"create", "stewy"};


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterEach
    public void clearDB() throws Exception {
        facade.clear(new String[]{"clear"});
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    /**
     * Tests needed: (total 14 tests)
     * -clear
     * -register
     * -login
     * -logout
     * -listGames
     * -createGame
     * -joinGame
     *
     */

    @Test
    public void testClearWhenEmpty() {
        Assertions.assertDoesNotThrow(()-> facade.clear(new String[] {"clear"}));
    }

    @Test
    public void testClearWithPopulation() throws Exception {
        facade.register(register1);
        facade.register(register2);
        facade.register(register3);
        Assertions.assertDoesNotThrow(() -> facade.clear(new String[] {"clear"}));
        Assertions.assertThrows(Exception.class, () -> facade.login(login1));
    }

    @Test
    public void testRegisterUsers(){
        Assertions.assertDoesNotThrow(() -> facade.register(register1));
        Assertions.assertDoesNotThrow(() -> facade.register(register2));
        Assertions.assertDoesNotThrow(() -> facade.register(register3));
    }

    @Test
    public void testRegisterExistingUsers() throws Exception {
        facade.register(register1);
        facade.register(register2);
        facade.register(register3);
        Assertions.assertThrows(Exception.class, () -> facade.register(register1));
        Assertions.assertThrows(Exception.class, () -> facade.register(register2));
        Assertions.assertThrows(Exception.class, () -> facade.register(register3));
    }

    @Test
    public void testLoginUser() throws Exception {
        facade.register(register1);
        Assertions.assertDoesNotThrow(() -> facade.login(login1));
        facade.register(register2);
        Assertions.assertDoesNotThrow(() -> facade.login(login2));
        facade.register(register3);
        Assertions.assertDoesNotThrow(() -> facade.login(login3));
    }

    @Test
    public void testLoginInvalidCredentials() throws Exception {
        facade.register(register1);
        Assertions.assertThrows(Exception.class, () -> facade.login(login2));
    }

    @Test
    public void testLogout() throws Exception {
        String authToken = facade.register(register1).authToken();
        Assertions.assertDoesNotThrow(() -> facade.logout(new String[] {"logout"}, authToken));
    }

    @Test
    public void testLogoutWithoutUser() throws Exception {
        Assertions.assertThrows(Exception.class, () -> facade.logout(null, ""));
    }

    @Test
    public void testCreateGame() throws Exception {
        String authToken = facade.register(register1).authToken();
        Assertions.assertDoesNotThrow(() -> facade.createGame(game1, authToken));
        Assertions.assertDoesNotThrow(() -> facade.createGame(game2, authToken));
    }

    @Test
    public void testCreateWithoutName() throws Exception {
        String authToken = facade.register(register1).authToken();
        Assertions.assertThrows(Exception.class, () -> facade.createGame(new String[] {"create"}, authToken));
    }

    @Test
    public void testListGames() throws Exception {
        String authToken = facade.register(register1).authToken();
        facade.createGame(game1, authToken);
        facade.createGame(game2, authToken);
        facade.createGame(game3, authToken);
        
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
