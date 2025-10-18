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
    private ArrayList<String> validAuthTokens = new ArrayList<>();

    @Override
    public AuthData registerUser(UserData userData) throws DataAccessException {
        if (users.containsKey(userData.username())){
            throw new DataAccessException("Username already exists.");
        }
        users.put(userData.username(), userData);
        String authToken = generateAuthToken();
        validAuthTokens.add(authToken);
        return new AuthData(userData.username(), authToken);
    }

    public AuthData login(LoginData loginData) throws DataAccessException {
        if (getUser(loginData.username()).password().equals(loginData.password())){
            String authToken = generateAuthToken();
            validAuthTokens.add(authToken);
            return new AuthData(loginData.username(), authToken);
        } else {
            throw new DataAccessException("Error: bad request");
        }
    }

    public void validateAuthToken(String authToken) throws DataAccessException {
        if (!validAuthTokens.contains(authToken)){
            throw new DataAccessException("Error: Not logged in");
        }
    }

    private String generateAuthToken() {
        return UUID.randomUUID().toString();
    }

    @Override
    public UserData getUser(String username) {
        return users.get(username);
    }
}
