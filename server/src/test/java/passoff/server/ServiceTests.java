package passoff.server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.LoginData;
import datamodel.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.DataService;
import service.UserService;

import java.util.ArrayList;
import java.util.List;

public class ServiceTests {
    MemoryDataAccess dataAccess = new MemoryDataAccess();
    UserService userService = new UserService(dataAccess);
    DataService dataService = new DataService(dataAccess);
    UserData sampleUserData = new UserData("myUsername", "myPassword", "myEmail@cs240.gov");
    UserData existingUser = new UserData("I-exist!", "pa$$w0rd!", "iamironman@cia.gov");


    @BeforeEach
    void reset() throws DataAccessException {
        dataService.clear();
        String auth = userService.register(existingUser).authToken();
        userService.logout(auth);
    }

    @Test
    public void registerNormalUser() throws DataAccessException {
        var response = userService.register(sampleUserData);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
    }

    @Test
    public void registerInvalidUser() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(new UserData("", " ", ",. . 09")));
    }

    @Test
    public void loginNormalUser() throws DataAccessException {
        var sampleLoginData  = new LoginData(existingUser.username(), existingUser.password());
        var authData = userService.login(sampleLoginData);
        Assertions.assertNotNull(authData);
        Assertions.assertEquals(existingUser.username(), authData.username());
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(new LoginData("myUsername", "myPassword")));
    }



    @Test
    public void logoutNormalUsers() throws DataAccessException {
        UserData[] users = {new UserData("user1", "pass1", "email1@me.com"),
                            new UserData("user2", "pass2", "email2@me.com"),
                            new UserData("user3", "pass3", "email3@me.com")};
        ArrayList<String> authTokens = new ArrayList<>();
        for (var user : users) {
            var authData = userService.register(user);
            authTokens.add(authData.authToken());
        }
        for (var authToken : authTokens) {
            userService.logout(authToken);
        }
        for (var authToken : authTokens) {
            Assertions.assertThrows(DataAccessException.class, () -> userService.logout(authToken));
        }
    }
}
