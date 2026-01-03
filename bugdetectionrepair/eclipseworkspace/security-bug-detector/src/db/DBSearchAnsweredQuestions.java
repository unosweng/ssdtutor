package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.UtilConsole;

public class DBSearchAnsweredQuestions extends DBInfo {
   public static void proc(String studentIdText, String codeFragmentText) {
      loadDBInfo();

      try {
         String sql = "SELECT `studentName`, `requestedAt`, `code`, `errType`, `desc`, " //
               + "`reviewerName`,`reviewedAt`, `responseId`, `voteUp`, `voteDn`, `solution` " //
               + "FROM Requests Q INNER JOIN Responses A ON Q.requestId = A.requestId " //
               + "WHERE (Q.studentId LIKE ? AND Q.code LIKE ?) ORDER BY A.voteUp DESC";

         ResultSet rst = null;
         PreparedStatement pstmt = null;
         pstmt = con.prepareStatement(sql);
         pstmt.setString(1, "%" + studentIdText + "%");
         pstmt.setString(2, "%" + codeFragmentText + "%");

         System.out.println("[DEBUG] SQL query: " + pstmt.toString());

         rst = pstmt.executeQuery();

         System.out.format("\n%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s\n", //
               "Student Name", "Requested At", "Code", "Error Type", "Description", "Reviewer Name", //
               "Reviewed At", "Response ID", "Vote Up", "Vote Down", "Solution");

         String out = String.format("%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s", //
               "Student Name", "Requested At", "Code", "Error Type", "Description", //
               "Reviewer Name", "Reviewed At", "Response ID", "Vote Up", "Vote Down", "Solution");
         UtilConsole.print(out + System.getProperty("line.separator"));

         while (rst.next()) {
            System.out.format("%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s\n", //
                  rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), //
                  rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8), //
                  rst.getString(9), rst.getString(10), rst.getString(11));
            out = String.format("%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s", //
                  rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), //
                  rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8), //
                  rst.getString(9), rst.getString(10), rst.getString(11));
            UtilConsole.print(out + System.getProperty("line.separator"));
         }
      } catch (SQLException e) {
         showQueryFail(e);
      }

      closeDB();
   }
}
