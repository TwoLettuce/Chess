package serverfacade;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;
import ui.ChessBoardUI;

import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL;
    public ServerFacade(String serverURL){
        this.serverURL = serverURL;
    }


    public void clear(String[] args) throws Exception{
        var httpRequest = buildRequest("DELETE", "/db", null, null, "");
        System.out.println("clear");
    }

    public AuthData register(String[] args) throws Exception {
        UserData userData = new UserData(args[1], args[2], args[3]);
        return buildRequest("POST", "/user", userData, AuthData.class, "");
    }

    public AuthData login(String[] args) throws Exception {
        LoginData loginData = new LoginData(args[1], args[2]);
        return buildRequest("POST", "/session", loginData, AuthData.class, "");
    }

    public void logout(String[] args, String authToken){
        System.out.println("logout");
    }

    public void listGames(String[] args, String authToken) {
        ChessBoardUI drawer = new ChessBoardUI();
        ChessGame game = new ChessGame();
        try {
            game.makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));
        } catch (Exception e){
            //this won't happen
        }
        drawer.draw(game.getBoard(), false);
    }

    public void createGame(String[] args, String authToken){
        System.out.println("createGame");
    }

    public void joinGame(String[] args, String authToken){
        System.out.println("joinGame");
    }


    private <T> T buildRequest(String method, String path, Object requestBody, Class<T> responseClass, String authToken) throws Exception {
        URI uri = new URI(serverURL + path);
        var requestBuilder = HttpRequest.newBuilder(uri)
                .method(method, buildBody(requestBody))
                .header("authorization", authToken);
        var request = requestBuilder.build();
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());

        return getResponseBody(response, responseClass);
    }

    private <T> T getResponseBody(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        int status = response.statusCode();
        if (status / 200 != 2){
            throw new Exception("unexpected status: " + status);
        }
        if (responseClass != null){
            Gson serializer = new Gson();
            return serializer.fromJson(response.body(), responseClass);
        }

        return null;
    }

    private HttpRequest.BodyPublisher buildBody(Object requestBody) {
        Gson serializer = new Gson();
        if (requestBody != null){
            return HttpRequest.BodyPublishers.ofString(serializer.toJson(requestBody));
        } else {
            return HttpRequest.BodyPublishers.noBody();
        }
    }


}
