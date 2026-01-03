package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.UtilConsole;

public class DBShowRecentQuestions extends DBInfo {
   public static void proc() {
      loadDBInfo();

      try {
         String sql = "select `requestId`, `studentId`, `studentName`, `vcs`, " //
               + "`requestedAt`, `code`, `errType`, `desc` " //
               + "from Requests ORDER BY requestedAt DESC LIMIT 50";
         ResultSet rst = null;
         PreparedStatement pstmt = null;
         pstmt = con.prepareStatement(sql);
         rst = pstmt.executeQuery();

         System.out.format("\n%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s\n", //
               "Request ID", "Student ID", "Student Name", "Student VCS", //
               "Requested At", "Code", "Error Type", "Description");

         String out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s", //
               "Request ID", "Student ID", "Student Name", "Student VCS", //
               "Requested At", "Code", "Error Type", "Description");
         UtilConsole.print(out + System.getProperty("line.separator"));

         while (rst.next()) {
            System.out.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s\n", //
                  rst.getInt(1), rst.getInt(2), rst.getString(3), rst.getString(4), //
                  rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
            out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s", //
                  rst.getInt(1), rst.getInt(2), rst.getString(3), rst.getString(4), //
                  rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
            UtilConsole.print(out + System.getProperty("line.separator"));
         }
      } catch (SQLException e) {
         showQueryFail(e);
      }
      closeDB();
   }
}
