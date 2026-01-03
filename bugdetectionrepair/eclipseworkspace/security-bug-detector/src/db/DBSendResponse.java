package db;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import util.UtilConsole;

public class DBSendResponse extends DBInfo {
   public static void proc(String requestIdText, String reviewerNameText, String solutionText) {
      loadDBInfo();

      try {
         String sql = "INSERT INTO Responses (`requestId`, `reviewerName`, `reviewedAt`, " //
               + "`voteUp`, `voteDn`, `solution`) VALUES (?, ?, ?, ?, ?, ?)";
         PreparedStatement pstmt = null;

         pstmt = con.prepareStatement(sql);
         pstmt.setInt(1, Integer.parseInt(requestIdText));
         pstmt.setString(2, reviewerNameText);
         pstmt.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
         pstmt.setInt(4, 0);
         pstmt.setInt(5, 0);
         pstmt.setString(6, solutionText);

         System.out.println("[DEBUG] SQL query: " + pstmt.toString());

         pstmt.executeUpdate();
         pstmt.close();
         con.close();

         System.out.println("[DEBUG] Your response to [Request ID: " + requestIdText + //
               "] has been successfully submitted.");
         String out = "Your response to [Request ID: " + requestIdText + //
               "] has been successfully submitted.";
         UtilConsole.print(out + System.getProperty("line.separator"));
      } catch (SQLException e) {
         showQueryFail(e);
      }

      closeDB();
   }
}
