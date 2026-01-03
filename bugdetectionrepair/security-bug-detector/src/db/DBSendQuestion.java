package db;

import java.sql.SQLException;

import util.UtilConsole;

public class DBSendQuestion extends DBInfo {
   public static void proc(String studentIdText, String studentNameText, String studentVCSText, //
         String codeFragmentText, String errTypeText, String descText) {

      loadDBInfo();

      try {
         String sql = "INSERT INTO Requests (`studentId`, `studentName`, `vcs`, " //
               + "`requestedAt`, `code`, `errType`, `desc`) VALUES (?, ?, ?, ?, ?, ?, ?)";

         pstmt = con.prepareStatement(sql);
         pstmt.setInt(1, Integer.parseInt(studentIdText));
         pstmt.setString(2, studentNameText);
         pstmt.setString(3, studentVCSText);
         pstmt.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
         pstmt.setString(5, codeFragmentText);
         pstmt.setString(6, errTypeText);
         pstmt.setString(7, descText);
         System.out.println("[DEBUG] SQL query: " + pstmt.toString());

         pstmt.executeUpdate();
      } catch (SQLException e) {
         showQueryFail(e);
      }
      closeDB();

      String out = String.format("%s", "Your question is successfully placed in queue.");
      UtilConsole.print(out + System.getProperty("line.separator"));
      System.out.println("[INFO] Student's request is successfully inserted.");
   }
}
