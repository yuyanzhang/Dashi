
package db;

public class DBUtil {
    private static final String HOSTNAME = "localhost";
    private static final String PORT_NUM = "3306";
    private static final String DB_NAME = "laiproject";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";
    public static final String URL;
    static {
   	 URL = "jdbc:mysql://" + HOSTNAME + ":" + PORT_NUM + "/" + DB_NAME
   			 + "?user=" + USERNAME + "&password=" + PASSWORD;
    }
}
