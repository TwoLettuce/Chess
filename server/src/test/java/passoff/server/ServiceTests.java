package passoff.server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.LoginData;
import datamodel.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.DataService;
import service.UserService;

public class ServiceTests {
    MemoryDataAccess dataAccess = new MemoryDataAccess();
    UserService userService = new UserService(dataAccess);
    DataService dataService = new DataService(dataAccess);
    UserData sampleUserData = new UserData("myUsername", "myPassword", "myEmail@cs240.gov");


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
        var res = userService.register(sampleUserData);
        String authToken = res.authToken();
        var sampleLoginData  = new LoginData(res.username(), sampleUserData.password());
        userService.logout(authToken);
        var response = userService.login(sampleLoginData);
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
        Assertions.assertThrows(DataAccessException.class,
                () -> userService.login(new LoginData("myUsername", "myPassword")));
        Assertions.assertDoesNotThrow(() -> dataService.clear());

    }
}
