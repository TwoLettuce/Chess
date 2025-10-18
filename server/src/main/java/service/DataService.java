package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class DataService {

    private final DataAccess dataAccess;

    public DataService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clear()  {
        dataAccess.clearDatabase();
    }
}