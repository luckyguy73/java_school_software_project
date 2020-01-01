package gci.utilities;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DatabaseQueryHelper {

    private static Connection con;
    private static PreparedStatement ps;
    private static ResultSet rs;

    public static ResultSet getResultSetUpdate(String... param) throws SQLException {
        System.out.println("getResultSetUpdate connection status: " + con.isClosed());
        ps = con.prepareStatement(param[0], Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, param[1]);
        ps.executeUpdate();
        rs = ps.getGeneratedKeys();
        rs.next();
        return rs;
    }

    public static ResultSet getResultSetQuery(String... param) throws SQLException {
        ps = con.prepareStatement(param[0]);
        ps.setString(1, param[1]);
        rs = ps.executeQuery();
        rs.next();
        return rs;
    }
    
    public static String toTitleCase(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return Arrays.stream(str.split(" ")).map(w -> w.substring(0, 1).toUpperCase()
            + w.substring(1).toLowerCase()).collect(Collectors.joining(" "));
    }
}
