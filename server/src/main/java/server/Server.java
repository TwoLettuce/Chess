package server;

import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.InvalidCredentialsException;
import dataaccess.MemoryDataAccess;
import datamodel.AuthData;
import datamodel.UserData;
import io.javalin.*;
import io.javalin.http.Context;
import service.UserService;

public class Server {

    private final Javalin server;
    private UserService userService;
    private DataAccess dataAccess;

    //TODO: assign the userService in the constructor and pass in a DataAccess

    public Server() {
        dataAccess = new MemoryDataAccess();
        userService = new UserService(dataAccess);
        server = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        server.delete("db", ctx -> ctx.result("{}"));

        server.post("user", this::register);

    }

    private void register(Context ctx) {
        var serializer = new Gson();
        var request = serializer.fromJson(ctx.body(), UserData.class);
        AuthData response;
        try {
            response = userService.register(request);
        } catch (InvalidCredentialsException e) {
            ctx.result(serializer.toJson("Username, password, or email is empty."));
            return;
        } 

        ctx.result(serializer.toJson(response));
    }


    public int run(int desiredPort) {
        server.start(desiredPort);
        return server.port();
    }

    public void stop() {
        server.stop();
    }
}
