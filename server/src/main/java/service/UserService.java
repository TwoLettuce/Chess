package service;

import handler.ExceptionHandler;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import datamodel.AuthData;
import datamodel.LoginData;
import datamodel.UserData;
import org.mindrot.jbcrypt.BCrypt;

import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;
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
        userData = new UserData(userData.username(), obfuscatePassword(userData.password()), userData.email());
        if (dataAccess.getUser(userData.username()) == null){
            dataAccess.addUser(userData);
            return dataAccess.addAuthData(new AuthData(userData.username(), generateAuthToken()));
        } else {
            throw new DataAccessException("Error: already taken");
        }
    }


    public AuthData login(LoginData loginData) throws DataAccessException{
        ExceptionHandler.verifyFieldsNotEmpty(loginData);
        UserData userData = dataAccess.getUser(loginData.username());
        if (userData != null && validatePassword(loginData.password(), userData.password())){
            return dataAccess.addAuthData(new AuthData(loginData.username(), generateAuthToken()));
        } else {
            throw new DataAccessException("Error: unauthorized");
        }
    }

    private String generateAuthToken(){
        return UUID.randomUUID().toString();
    }

    private String obfuscatePassword(String password){
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    private boolean validatePassword(String password, String obfuscatedPassword){
        return BCrypt.checkpw(password, obfuscatedPassword);
    }

    public void logout(String authToken) throws DataAccessException{
        validateAuthToken(authToken);
        dataAccess.deleteAuthData(authToken);
    }
}
