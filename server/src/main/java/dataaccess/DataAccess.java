package dataaccess;

import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;

import javax.xml.crypto.Data;

public interface DataAccess {
    AuthData registerUser(UserData userData) throws DataAccessException;
    AuthData login(LoginData loginData) throws DataAccessException;
    UserData getUser(String username);
}
