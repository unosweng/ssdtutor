package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.UtilConsole;

public class DBShowOutdatedUnansweredQuestions extends DBInfo {
   public static void proc() {
      loadDBInfo();

      try {
         String sql = "SELECT * FROM Requests Q "//
               + "WHERE NOT EXISTS (SELECT 1 FROM Responses A WHERE Q.requestId = A.requestId) LIMIT 50";

         ResultSet rst = null;
         PreparedStatement pstmt = null;

         pstmt = con.prepareStatement(sql);
         System.out.println("[DEBUG] SQL query: " + pstmt.toString());

         // process results
         rst = pstmt.executeQuery();

         System.out.format("\n%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s\n", //
               "Request ID", "Student ID", "Student Name", "Student VCS", //
               "Requested At", "Code", "Error Type", "Description");

         String out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s", //
               "Request ID", "Student ID", "Student Name", "Student VCS", //
               "Requested At", "Code", "Error Type", "Description");
         UtilConsole.print(out + System.getProperty("line.separator"));

         while (rst.next()) {
            System.out.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s\n", //
                  rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), //
                  rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
            out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s", //
                  rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), //
                  rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
            UtilConsole.print(out + System.getProperty("line.separator"));
         }

      } catch (SQLException e) {
         showQueryFail(e);
      }

      closeDB();
   }
}
