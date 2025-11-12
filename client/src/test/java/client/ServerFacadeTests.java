package client;

import org.junit.jupiter.api.*;
import server.Server;
import serverfacade.ServerFacade;


public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }



    @Test
    public void printNormalGame(){
        ServerFacade facade = new ServerFacade("http://localhost:8080");
        Assertions.assertDoesNotThrow(() -> facade.listGames(new String[]{"listgames", "urmom"}));
    }

    @Test
    public void sampleTest() {
        Assertions.assertTrue(true);
    }

}
