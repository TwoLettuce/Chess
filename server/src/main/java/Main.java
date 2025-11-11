import server.Server;

public class Main {
    public static void main(String[] args) {
        Server server = new Server();
        server.run(8080);
        System.out.println("♕ Welcome to 240 Chess! Type Help to get started. ♕");
        System.out.println("♕ Thanks for playing! ♕");
    }
}