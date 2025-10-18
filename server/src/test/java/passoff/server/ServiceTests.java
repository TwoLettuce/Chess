package passoff.server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import datamodel.LoginData;
import datamodel.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.UserService;

public class ServiceTests {
    MemoryDataAccess dataAccess = new MemoryDataAccess();
    UserService userService = new UserService(dataAccess);


    @Test
    public void registerNormalUser() throws DataAccessException {
        var response = userService.register(new UserData("myUsername", "myPassword", "myEmail@cs240.gov"));
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
    }

    @Test
    public void registerInvalidUser() throws DataAccessException {
        Assertions.assertThrows(DataAccessException.class, () -> userService.register(new UserData("", " ", ",. . 09")));
    }

    @Test
    public void loginNormalUser() throws DataAccessException {
        var response = userService.login(new LoginData("myUsername", "myPassword"));
        Assertions.assertNotNull(response);
        Assertions.assertEquals("myUsername", response.username());
        Assertions.assertThrows(DataAccessException.class, () -> userService.login(new LoginData("myUsername", "myPassword")));
    }
}
