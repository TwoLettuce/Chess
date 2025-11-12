import server.Server;

public class ServerMain {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);
        System.out.println("♕ Welcome to 240 Chess! Type Help to get started. ♕");
    }
}