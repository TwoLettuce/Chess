package serverfacade;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;
import ui.ChessBoardUI;

import java.net.http.HttpClient;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL;
    public ServerFacade(String serverURL){
        this.serverURL = serverURL;
    }


    public void clear(String[] args){
        var httpRequest = buildRequest("DELETE", "/db", null, null);
        System.out.println("clear");
    }



    public void register(String[] args) throws DataAccessException {

        UserData userData = new UserData(args[1], args[2], args[3]);
        var httpRequest = buildRequest("POST", "/user", userData, AuthData.class);

        System.out.println("register");
    }

    public void login(String[] args) throws DataAccessException {
        LoginData loginData = new LoginData(args[1], args[2]);
        var httpRequest = buildRequest("POST", "/session", AuthData.class, AuthData.class);
        System.out.println("login");
    }

    public void logout(String[] args){
        System.out.println("logout");
    }

    public void listGames(String[] args) {
        ChessBoardUI drawer = new ChessBoardUI();
        ChessGame game = new ChessGame();
        try {
            game.makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));
        } catch (Exception e){
            //this won't happen
        }
        drawer.draw(game.getBoard(), false);
    }

    public void createGame(String[] args){
        System.out.println("createGame");
    }

    public void joinGame(String[] args){
        System.out.println("joinGame");
    }


    private <T> T buildRequest(String method, String path, Object request, Class<T> responseClass) {

        return null;
    }
}
