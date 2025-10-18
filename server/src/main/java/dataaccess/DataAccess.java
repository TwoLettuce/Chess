package dataaccess;

import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;

import javax.xml.crypto.Data;
import java.util.UUID;

public interface DataAccess {
    AuthData registerUser(UserData userData) throws DataAccessException;
    AuthData login(LoginData loginData) throws DataAccessException;
    void logout(String authToken) throws DataAccessException;
    UserData getUser(String username);
    boolean validateAuthToken(String authToken);
    void clearDatabase();
}
