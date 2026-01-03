package db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.UtilConsole;

public class DBVoteUp extends DBInfo {
   public static void proc(String responseIdText) {
      loadDBInfo();

      try {
         String select_sql = "SELECT `voteUp` FROM Responses WHERE `responseId` = ?";
         String update_sql = "UPDATE Responses SET `voteUp` = ? WHERE `responseId` = ?";

         ResultSet rst = null;
         PreparedStatement pstmt = null;

         pstmt = con.prepareStatement(select_sql);
         pstmt.setString(1, responseIdText);
         System.out.println("[DEBUG] SQL query: " + pstmt.toString());

         // process the results
         rst = pstmt.executeQuery();
         rst.next();
         int voteUp = rst.getInt(1);
         System.out.println("[DEBUG] responseId: " + responseIdText + "\tvoteUp: " + voteUp);

         // increase vote
         voteUp += 1;
         pstmt = con.prepareStatement(update_sql);
         pstmt.setInt(1, voteUp);
         pstmt.setString(2, responseIdText);

         // process the results
         pstmt.executeUpdate();
         System.out.println("[DEBUG] Vote Up is successfully increased for [responseId: " + responseIdText + "].");
      } catch (SQLException e) {
         showQueryFail(e);
      }
      closeDB();

      String out = "Thank you for your feedback!";
      UtilConsole.print(out + System.getProperty("line.separator"));
   }
}
