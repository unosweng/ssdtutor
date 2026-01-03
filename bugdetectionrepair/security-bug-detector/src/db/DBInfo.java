package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import util.UtilConsole;

public class DBInfo {
   protected final static String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
   static Connection con = null;
   static PreparedStatement pstmt = null;
   static PropertyResourceBundle dbbundle = null;
   static String url;
   static String username;
   static String password;

   public static void loadDBInfo() {
      dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
      url = dbbundle.getString("DB_URL");
      username = dbbundle.getString("DB_USERNAME");
      password = dbbundle.getString("DB_PASSWORD");

      try {
         Class.forName(JDBC_DRIVER);
         con = DriverManager.getConnection(url, username, password);
         System.out.println("[INFO] DB connection success!");
      } catch (Exception e) {
         String errmsg = "[ERROR] DB load fail: " + e.toString();
         System.out.println(errmsg);
         UtilConsole.print(errmsg);
      }
   }

   public static void closeDB() {
      try {
         pstmt.close();
         con.close();
         System.out.println("[INFO] DB connection is successfully closed.");
      } catch (SQLException e) {
         String errmsg = "[ERROR] DB close fail: " + e.toString();
         System.out.println(errmsg);
         UtilConsole.print(errmsg);
      }
   }

   public static void showQueryFail(Exception e) {
      String errmsg = "[ERROR] Query Execution Fail: " + e.toString();
      System.out.println(errmsg);
      UtilConsole.print(errmsg);
   }
}
