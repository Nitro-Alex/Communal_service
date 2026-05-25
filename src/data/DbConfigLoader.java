package data;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DbConfigLoader {

    public static DbConfig load(DbType dbType) throws IOException {
        String path;
        if (dbType == DbType.POSTGRESQL) {
            path = "config/postgresql.properties";
        } else {
            path = "config/sqlserver.properties";
        }

        Properties props = new Properties();
        try (InputStream in = new FileInputStream(path)) {
            props.load(in);
        }

        String driver = props.getProperty("driver");
        String url = props.getProperty("url");
        String user = props.getProperty("user");
        String password = props.getProperty("password");

        if (driver == null || url == null || user == null || password == null) {
            throw new IOException("Неполный набор параметров в файле: " + path);
        }

        return new DbConfig(driver, url, user, password);
    }
}