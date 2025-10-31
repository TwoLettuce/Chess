package service;

import handler.ExceptionHandler;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;

import java.util.ArrayList;

public class UserService {
    private DataAccess dataAccess;
    public UserService(DataAccess dataAccess){
        this.dataAccess = dataAccess;
    }

    private void validateAuthToken(String authToken) throws DataAccessException {
        if (dataAccess.getAuthData(authToken) == null){
            throw new DataAccessException("Error: unauthorized");
        }
    }

    public AuthData register(UserData userData) throws DataAccessException {
        ExceptionHandler.verifyFieldsNotEmpty(userData);
        if (dataAccess.getUser(userData) == null){
            return dataAccess.registerUser(userData);
        } else {
            throw new DataAccessException("Error: Forbidden");
        }
    }


    public AuthData login(LoginData loginData) throws DataAccessException{
        ExceptionHandler.verifyFieldsNotEmpty(loginData);
        return dataAccess.login(loginData);
    }

    public void logout(String authToken) throws DataAccessException{
        validateAuthToken(authToken);
        dataAccess.logout(authToken);
    }
}
