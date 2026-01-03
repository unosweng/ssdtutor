package db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBPeriodicalPoolingNotifications extends DBInfo {

   public static int get(int count, List<String> notiIds, List<String> requestIds, List<String> responseIds) {
      loadDBInfo();

      try {
         String sql = "SELECT notiId,Q.requestId,responseId FROM " //
               + "Notifications N INNER JOIN Requests Q ON N.requestId = Q.requestId WHERE studentId=?;";
         pstmt = con.prepareStatement(sql);

         //////////
         // TODO //
         //////////
         // Now, this "periodical pooling" is pooling the info for the HARDCODED student (i.e., 99999999) by the below line.
         // In future, one should make this to be automatic by replacing below line based on one's design.
         // For example, simply one might think of designing a user to enter student him/herself id into the textbox, then the below line should get a string from the corresponding textbox.
         pstmt.setString(1, "99999999");

         System.out.println("[DEBUG] SQL query: " + pstmt.toString());
         ResultSet rst = pstmt.executeQuery();
         while (rst.next()) {
            count++;
            notiIds.add(rst.getString(1));
            requestIds.add(rst.getString(2));
            responseIds.add(rst.getString(3));
         }
      } catch (SQLException e) {
         showQueryFail(e);
      }

      if (count > 0) {
         closeDB();
      }
      return count;
   }

   public static void delete(List<String> notiIds, final int buttonIndex) {
      loadDBInfo();

      try {
         String sql = "DELETE FROM Notifications WHERE notiId=?";
         pstmt = con.prepareStatement(sql);
         pstmt.setInt(1, Integer.parseInt(notiIds.get(buttonIndex)));

         pstmt.executeUpdate();

         System.out.println("[DEBUG] An entry (notiId:" + String.valueOf(notiIds.get(buttonIndex)) + //
               ") is successfully deleted from the Notifications table.");
      } catch (SQLException e) {
         showQueryFail(e);
      }

      closeDB();
   }
}
