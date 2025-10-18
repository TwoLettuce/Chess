package dataaccess;

import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess{

    private HashMap<String, UserData> users = new HashMap<>();
    private HashMap<String, String> validAuthTokens = new HashMap<>();

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())){
            throw new DataAccessException("Username already exists.");
        }
        users.put(userData.username(), userData);
        String authToken = generateAuthToken();
        validAuthTokens.put(authToken, userData.username());
        return new AuthData(userData.username(), authToken);
    }

    public AuthData login(LoginData loginData) throws DataAccessException {
        if (!validAuthTokens.containsValue(loginData.username())
                && users.containsKey(loginData.username())
                && getUser(loginData.username()).password().equals(loginData.password())) {
            String authToken = generateAuthToken();
            validAuthTokens.put(authToken, loginData.username());
            return new AuthData(loginData.username(), authToken);
        } else {
            throw new DataAccessException("Error: bad request");
        }
    }

    public void logout(String authToken) throws DataAccessException {
        if (validateAuthToken(authToken)) {
            validAuthTokens.remove(authToken);
        } else {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public void clearDatabase(){
        validAuthTokens.clear();
        users.clear();
    }

    public boolean validateAuthToken(String authToken) {
        return validAuthTokens.containsKey(authToken);
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
