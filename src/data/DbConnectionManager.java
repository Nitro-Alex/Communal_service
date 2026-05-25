package data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnectionManager {

    public static Connection openConnection(DbType dbType) throws Exception {
        DbConfig config = DbConfigLoader.load(dbType);
        Class.forName(config.getDriverClassName());
        return DriverManager.getConnection(
                config.getUrl(),
                config.getUser(),
                config.getPassword()
        );
    }
}