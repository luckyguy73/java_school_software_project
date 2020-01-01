package gci.database;

import java.io.*;
import java.sql.*;
import java.util.*;

public class ConnectionManager {

    private static Connection con;
    private static String url;
    private static String user;
    private static String password;

    public static void setProperties() throws FileNotFoundException, IOException {
        //try with resources statement
        try (FileInputStream stream = new FileInputStream("properties/db_props.xml")) {
            Properties props = new Properties();
            props.loadFromXML(stream);
            url = props.getProperty("url");
            user = props.getProperty("user");
            password = props.getProperty("password");
        }
    }

    public static Connection getConnection() throws SQLException {
        try {
            con = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            showErrorMessage(e);
        }
        return con;
    }

    public static void closeConnection(PreparedStatement stmt) throws SQLException {
        stmt.close();
        con.close();
    }
    
    public static void showErrorMessage(SQLException e) {
        System.err.println("Error Code: " + e.getErrorCode() + " " + e.getMessage());
    }
}
