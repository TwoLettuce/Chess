package serverfacade;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import datamodels.JoinRequest;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverURL;
    public ServerFacade(String serverURL){
        this.serverURL = serverURL;
    }


    public void clear(String[] args) throws Exception{
        var request = buildRequest("DELETE", "/db", null,  "");
        sendRequest(request, null);
    }

    public datamodels.AuthData register(String[] args) throws Exception {
        datamodels.UserData userData = new datamodels.UserData(args[1], args[2], args[3]);
        var request = buildRequest("POST", "/user", userData,"");
        return sendRequest(request, datamodels.AuthData.class);
    }

    public datamodels.AuthData login(String[] args) throws Exception {
        datamodels.LoginData loginData = new datamodels.LoginData(args[1], args[2]);
        var request = buildRequest("POST", "/session", loginData, "");
        return sendRequest(request, datamodels.AuthData.class);
    }

    public void logout(String[] args, String authToken) throws Exception{
        var request = buildRequest("DELETE", "/session", null, authToken);
        sendRequest(request, null);
    }

    public ArrayList<datamodels.GameData> listGames(String[] args, String authToken) throws Exception {
        var request = buildRequest("GET", "/game", null, authToken);
        Object response = sendRequest(request, Object.class);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(datamodels.GameData.class, new GameDataTypeAdapter());
        Gson deserializer = builder.create();
        String gameDataAsJsonMap = deserializer.toJson(response);
        String gameDataAsJson = deserializer.toJson(deserializer.fromJson((String) gameDataAsJsonMap, HashMap.class).get("games"));
        return deserializer.fromJson((String) gameDataAsJson, GameDataList.class);
    }

    public int createGame(String[] args, String authToken) throws Exception {
        HashMap<String, String> gameName = new HashMap<>();
        gameName.put("gameName", args[1]);
        var request = buildRequest("POST", "/game", gameName, authToken);
        return ((Double) sendRequest(request, HashMap.class).get("gameID")).intValue();


    }

    public void joinGame(String color, int gameID, String authToken) throws Exception {
        datamodels.JoinRequest joinRequest = new JoinRequest(color, gameID);
        var request = buildRequest("PUT", "/game", joinRequest, authToken);
        sendRequest(request, null);
    }


    private HttpRequest buildRequest(String method, String path, Object requestBody, String authToken) throws Exception {
        URI uri = new URI(serverURL + path);
        var requestBuilder = HttpRequest.newBuilder(uri)
                .method(method, buildBody(requestBody))
                .header("authorization", authToken);
        return requestBuilder.build();
    }

    private <T> T sendRequest(HttpRequest request, Class<T> responseClass) throws Exception{
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return getResponseBody(response, responseClass);
    }

    private <T> T getResponseBody(HttpResponse<String> response, Class<T> responseClass) throws Exception {
        int status = response.statusCode();
        if (status / 100 != 2){
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
