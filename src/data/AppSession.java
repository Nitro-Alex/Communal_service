package data;

import data.dto.ResidentSearchResult;

import java.sql.Connection;
import java.sql.SQLException;

public class AppSession {

    private static final AppSession INSTANCE = new AppSession();

    private UserRole role;
    private DbType dbType;
    private Connection connection;
    private Integer residentId;
    private ResidentSearchResult residentSearchResult;

    private AppSession() {
    }

    public static AppSession getInstance() {
        return INSTANCE;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public DbType getDbType() {
        return dbType;
    }

    public void setDbType(DbType dbType) {
        this.dbType = dbType;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Integer getResidentId() {
        return residentId;
    }

    public void setResidentId(Integer residentId) {
        this.residentId = residentId;
    }

    public ResidentSearchResult getResidentSearchResult() {
        return residentSearchResult;
    }

    public void setResidentSearchResult(ResidentSearchResult residentSearchResult) {
        this.residentSearchResult = residentSearchResult;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }
    }
}