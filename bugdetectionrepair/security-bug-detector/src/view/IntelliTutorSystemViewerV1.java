/**
 */
package view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import util.UtilConsole;

/**
 * @author
 * @date
 * @since J2SE-11
 */
public class IntelliTutorSystemViewerV1 {
   private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
   public static final String VIEW_ID = "security-bug-detector.partdescriptor.intelligenttutorsystemforprogramming";

   @Inject
   public IntelliTutorSystemViewerV1() {
   }

   @PostConstruct
   public void postConstruct(Composite parent, EMenuService menuService) {
      Composite outer = new Composite(parent, SWT.NONE);
      outer.setLayout(new FillLayout(SWT.HORIZONTAL));

      //////////////////
      // REQUEST FORM //
      //////////////////
      Composite tutorContainer = new Composite(outer, SWT.BORDER);
      tutorContainer.setLayout(new FillLayout(SWT.H_SCROLL | SWT.V_SCROLL));

      GridLayout gridLayout = new GridLayout();
      gridLayout.numColumns = 2;
      gridLayout.makeColumnsEqualWidth = true;
      tutorContainer.setLayout(gridLayout);

      new Label(tutorContainer, SWT.NULL).setText("[REQUEST FORM]");
      new Label(tutorContainer, SWT.NULL).setText("");

      new Label(tutorContainer, SWT.NULL).setText("Student ID: ");
      Text studentId = new Text(tutorContainer, SWT.BORDER);
      new Label(tutorContainer, SWT.NULL).setText("Student Name: ");
      Text studentName = new Text(tutorContainer, SWT.BORDER);
      new Label(tutorContainer, SWT.NULL).setText("Student VCS: ");
      Text studentVCS = new Text(tutorContainer, SWT.BORDER);
      new Label(tutorContainer, SWT.NULL).setText("Code Fragments: ");
      Text codeFragment = new Text(tutorContainer, SWT.BORDER);
      new Label(tutorContainer, SWT.NULL).setText("Error Type: ");
      Text errType = new Text(tutorContainer, SWT.BORDER);
      new Label(tutorContainer, SWT.NULL).setText("Description: ");
      Text desc = new Text(tutorContainer, SWT.BORDER);

      Button btnSearch = new Button(tutorContainer, SWT.BORDER);
      btnSearch.setText("Search Answered Questions");
      btnSearch.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[INFO] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String sql = "SELECT `studentName`, `requestedAt`, `code`, `errType`, `desc`, `reviewerName`,`reviewedAt`, `responseId`, `voteUp`, `voteDn`, `solution` FROM Requests Q INNER JOIN Responses A ON Q.requestId = A.requestId WHERE (Q.studentId LIKE ? AND Q.code LIKE ?) ORDER BY A.voteUp DESC";

                  ResultSet rst = null;
                  PreparedStatement pstmt = null;
                  pstmt = con.prepareStatement(sql);
                  pstmt.setString(1, "%" + studentId.getText() + "%");
                  pstmt.setString(2, "%" + codeFragment.getText() + "%");

                  System.out.println("[DEBUG] SQL query: " + pstmt.toString());

                  rst = pstmt.executeQuery();

                  System.out.format("\n%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s\n", "Student Name", "Requested At", "Code", "Error Type", "Description", "Reviewer Name", "Reviewed At", "Response ID", "Vote Up", "Vote Down", "Solution");

                  // Clear view and show output
                  // styledText.setText("");
                  String out = String.format("%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s", "Student Name", "Requested At", "Code", "Error Type", "Description", "Reviewer Name", "Reviewed At", "Response ID", "Vote Up", "Vote Down", "Solution");
                  UtilConsole.print(out + System.getProperty("line.separator"));

                  // Add listener
                  while (rst.next()) {
                     System.out.format("%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s\n", rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8), rst.getString(9), rst.getString(10), rst.getString(11));
                     out = String.format("%-15s%-25s%-50s%-20s%-30s%-15s%-25s%-15s%-15s%-15s%-50s", rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8), rst.getString(9), rst.getString(10), rst.getString(11));
                     UtilConsole.print(out + System.getProperty("line.separator"));
                  }

                  System.out.println();
                  pstmt.close();
                  con.close();
                  System.out.println("[INFO] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      Button btnSend = new Button(tutorContainer, SWT.BORDER);
      btnSend.setText("Send Question");
      btnSend.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:

               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[INFO] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String sql = "INSERT INTO Requests (`studentId`, `studentName`, `vcs`, `requestedAt`, `code`, `errType`, `desc`) VALUES (?, ?, ?, ?, ?, ?, ?)";
                  PreparedStatement pstmt = null;

                  pstmt = con.prepareStatement(sql);
                  pstmt.setInt(1, Integer.parseInt(studentId.getText()));
                  pstmt.setString(2, studentName.getText());
                  pstmt.setString(3, studentVCS.getText());
                  pstmt.setTimestamp(4, new java.sql.Timestamp(new java.util.Date().getTime()));
                  pstmt.setString(5, codeFragment.getText());
                  pstmt.setString(6, errType.getText());
                  pstmt.setString(7, desc.getText());
                  System.out.println("[DEBUG] SQL query: " + pstmt.toString());

                  pstmt.executeUpdate();
                  pstmt.close();
                  con.close();

                  // Clear view and show output
                  String out = String.format("%s", "Your question is successfully placed in queue.");
                  UtilConsole.print(out + System.getProperty("line.separator"));

                  System.out.println("[INFO] Student's request is successfully inserted.");
                  System.out.println("[INFO] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      Button btnShowAll = new Button(tutorContainer, SWT.BORDER);
      btnShowAll.setText("Show Recent 50 Questions");
      btnShowAll.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));

      btnShowAll.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:

               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[INFO] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String sql = "select `requestId`, `studentId`, `studentName`, `vcs`, `requestedAt`, `code`, `errType`, `desc` from Requests ORDER BY requestedAt DESC LIMIT 50";
                  ResultSet rst = null;
                  PreparedStatement pstmt = null;
                  pstmt = con.prepareStatement(sql);
                  rst = pstmt.executeQuery();

                  System.out.format("\n%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s\n", "Request ID", "Student ID", "Student Name", "Student VCS", "Requested At", "Code", "Error Type", "Description");

                  // Clear view and show output
                  String out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s", "Request ID", "Student ID", "Student Name", "Student VCS", "Requested At", "Code", "Error Type", "Description");
                  UtilConsole.print(out + System.getProperty("line.separator"));

                  while (rst.next()) {
                     System.out.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s\n", rst.getInt(1), rst.getInt(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
                     out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-30s", rst.getInt(1), rst.getInt(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
                     UtilConsole.print(out + System.getProperty("line.separator"));
                  }
                  System.out.println();
                  pstmt.close();
                  con.close();
                  System.out.println("[INFO] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      /////////////////
      // Voting Form //
      /////////////////
      new Label(tutorContainer, SWT.NULL).setText("");
      new Label(tutorContainer, SWT.NULL).setText("[VOTING]");
      new Label(tutorContainer, SWT.NULL).setText("");
      new Label(tutorContainer, SWT.NULL).setText("Response ID: ");
      Text responseId = new Text(tutorContainer, SWT.BORDER);
      responseId.setLayoutData(new GridData(SWT.FILL));

      Button btnVoteUp = new Button(tutorContainer, SWT.BORDER);
      btnVoteUp.setText("Vote Up");
      btnVoteUp.setLayoutData(new GridData(SWT.FILL));
      btnVoteUp.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[DEBUG] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String select_sql = "SELECT `voteUp` FROM Responses WHERE `responseId` = ?";
                  String update_sql = "UPDATE Responses SET `voteUp` = ? WHERE `responseId` = ?";

                  ResultSet rst = null;
                  PreparedStatement pstmt = null;

                  // create the preparedstatement and add the criteria
                  pstmt = con.prepareStatement(select_sql);
                  pstmt.setString(1, responseId.getText());
                  System.out.println("[DEBUG] SQL query: " + pstmt.toString());

                  // process the results
                  rst = pstmt.executeQuery();
                  rst.next();
                  int voteUp = rst.getInt(1);
                  System.out.println("[DEBUG] responseId: " + responseId.getText() + "\tvoteUp: " + voteUp);

                  // increase vote
                  voteUp += 1;
                  pstmt = con.prepareStatement(update_sql);
                  pstmt.setInt(1, voteUp);
                  pstmt.setString(2, responseId.getText());

                  // process the results
                  pstmt.executeUpdate();
                  System.out.println("[DEBUG] Vote Up is successfully increased for [responseId: " + responseId.getText() + "].");
                  pstmt.close();
                  con.close();

                  // Clear view and show output
                  String out = "Thank you for your feedback!";
                  UtilConsole.print(out + System.getProperty("line.separator"));
                  System.out.println("[DEBUG] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      Button btnVoteDn = new Button(tutorContainer, SWT.BORDER);
      btnVoteDn.setText("Vote Down");
      btnVoteDn.setLayoutData(new GridData(SWT.FILL));
      btnVoteDn.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[DEBUG] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String select_sql = "SELECT `voteDn` FROM Responses WHERE `responseId` = ?";
                  String update_sql = "UPDATE Responses SET `voteDn` = ? WHERE `responseId` = ?";

                  ResultSet rst = null;
                  PreparedStatement pstmt = null;

                  // create the preparedstatement and add the criteria
                  pstmt = con.prepareStatement(select_sql);
                  pstmt.setString(1, responseId.getText());
                  System.out.println("[DEBUG] SQL query: " + pstmt.toString());

                  // process the results
                  rst = pstmt.executeQuery();
                  rst.next();
                  int voteDn = rst.getInt(1);
                  System.out.println("[DEBUG] responseId: " + responseId.getText() + "\tvoteDn: " + voteDn);

                  // increase vote
                  voteDn += 1;
                  pstmt = con.prepareStatement(update_sql);
                  pstmt.setInt(1, voteDn);
                  pstmt.setString(2, responseId.getText());

                  // process the results
                  pstmt.executeUpdate();
                  System.out.println("[DEBUG] Vote Down is successfully increased for [responseId: " + responseId.getText() + "].");
                  pstmt.close();
                  con.close();

                  // Clear view and show output
                  String out = "Thank you for your feedback!";
                  UtilConsole.print(out + System.getProperty("line.separator"));
                  System.out.println("[DEBUG] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      ///////////////////
      // Response Form //
      ///////////////////
      new Label(tutorContainer, SWT.NULL).setText("[RESPONSE FORM]");
      new Label(tutorContainer, SWT.NULL).setText("");

      new Label(tutorContainer, SWT.NULL).setText("Request ID: ");
      Text requestId = new Text(tutorContainer, SWT.BORDER);

      new Label(tutorContainer, SWT.NULL).setText("Reviewer Name: ");
      Text reviewerName = new Text(tutorContainer, SWT.BORDER);

      new Label(tutorContainer, SWT.NULL).setText("Solution: ");
      Text solution = new Text(tutorContainer, SWT.BORDER);

      Button btnShowUnansweredQuestions = new Button(tutorContainer, SWT.BORDER);
      btnShowUnansweredQuestions.setText("Show 50 Outdated Unanswered Questions");
      btnShowUnansweredQuestions.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  // Class.forName("com.mysql.jdbc.Driver");
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[INFO] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String sql = "SELECT * FROM Requests Q WHERE NOT EXISTS (SELECT 1 FROM Responses A WHERE Q.requestId = A.requestId) LIMIT 50";

                  ResultSet rst = null;
                  PreparedStatement pstmt = null;

                  // prepare statement
                  pstmt = con.prepareStatement(sql);
                  System.out.println("[DEBUG] SQL query: " + pstmt.toString());

                  // process results
                  rst = pstmt.executeQuery();

                  System.out.format("\n%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s\n", "Request ID", "Student ID", "Student Name", "Student VCS", "Requested At", "Code", "Error Type", "Description");

                  // Clear view and show output
                  String out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s", "Request ID", "Student ID", "Student Name", "Student VCS", "Requested At", "Code", "Error Type", "Description");
                  UtilConsole.print(out + System.getProperty("line.separator"));

                  // Add listener
                  while (rst.next()) {
                     System.out.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s\n", rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
                     out = String.format("%-15s%-15s%-20s%-25s%-25s%-50s%-20s%-50s", rst.getString(1), rst.getString(2), rst.getString(3), rst.getString(4), rst.getString(5), rst.getString(6), rst.getString(7), rst.getString(8));
                     UtilConsole.print(out + System.getProperty("line.separator"));
                  }

                  System.out.println();
                  pstmt.close();
                  con.close();
                  System.out.println("[INFO] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      Button btnResponse = new Button(tutorContainer, SWT.BORDER);
      btnResponse.setText("Send Response");
      btnResponse.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_END));
      btnResponse.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
               // Connect DB
               Connection con = null;
               PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
               String url = dbbundle.getString("DB_URL");
               String username = dbbundle.getString("DB_USERNAME");
               String password = dbbundle.getString("DB_PASSWORD");

               try {
                  // load and register JDBC driver for MySQL
                  Class.forName(JDBC_DRIVER);
                  con = DriverManager.getConnection(url, username, password);
                  System.out.println("[DEBUG] DB connection success!");
                  // Construct sql using PreparedStatement which is more secure way than prepare
                  String sql = "INSERT INTO Responses (`requestId`, `reviewerName`, `reviewedAt`, `voteUp`, `voteDn`, `solution`) VALUES (?, ?, ?, ?, ?, ?)";
                  PreparedStatement pstmt = null;

                  pstmt = con.prepareStatement(sql);
                  pstmt.setInt(1, Integer.parseInt(requestId.getText()));
                  pstmt.setString(2, reviewerName.getText());
                  pstmt.setTimestamp(3, new java.sql.Timestamp(new java.util.Date().getTime()));
                  pstmt.setInt(4, 0);
                  pstmt.setInt(5, 0);
                  pstmt.setString(6, solution.getText());

                  System.out.println("[DEBUG] SQL query: " + pstmt.toString());

                  pstmt.executeUpdate();
                  pstmt.close();
                  con.close();

                  // Clear view and show output
                  System.out.println("[DEBUG] Your response to [Request ID: " + requestId.getText() + "] has been successfully submitted.");
                  String out = "Your response to [Request ID: " + requestId.getText() + "] has been successfully submitted.";
                  UtilConsole.print(out + System.getProperty("line.separator"));

                  System.out.println();
                  pstmt.close();
                  con.close();
                  System.out.println("[DEBUG] DB connection is successfully closed.");
               } catch (SQLException | ClassNotFoundException e) {
                  String errmsg = "[ERROR] DB load fail: " + e.toString();
                  System.out.println(errmsg);
                  UtilConsole.print(errmsg);
               }
               break;
            }
         }
      });

      ////////////////////////
      // Periodical Pooling //
      ////////////////////////
      ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();

      int param_period = 30; // Check every 15 second

      exec.scheduleAtFixedRate(new Runnable() {
         @Override
         public void run() {

            // Connect DB
            Connection con = null;
            PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
            String url = dbbundle.getString("DB_URL");
            String username = dbbundle.getString("DB_USERNAME");
            String password = dbbundle.getString("DB_PASSWORD");

            try {
               // load and register JDBC driver for MySQL
               Class.forName(JDBC_DRIVER);
               con = DriverManager.getConnection(url, username, password);
               System.out.println("[DEBUG] DB connection success!");

               String sql = "SELECT notiId,Q.requestId,responseId FROM Notifications N INNER JOIN Requests Q ON N.requestId = Q.requestId WHERE studentId=?;";

               ResultSet rst = null;
               PreparedStatement pstmt = null;

               // prepare statement
               pstmt = con.prepareStatement(sql);

               //////////
               // TODO //
               //////////
               // Now, this "periodical pooling" is pooling the info for the HARDCODED student (i.e., 99999999) by the below line.
               // In future, one should make this to be automatic by replacing below line based on one's design.
               // For example, simply one might think of designing a user to enter student him/herself id into the textbox, then the below line should get a string from the corresponding textbox.
               pstmt.setString(1, "99999999");

               System.out.println("[DEBUG] SQL query: " + pstmt.toString());
               // process results
               rst = pstmt.executeQuery();
               // rst.next();
               ArrayList<String> notiIds = new ArrayList<String>();
               ArrayList<String> requestIds = new ArrayList<String>();
               ArrayList<String> responseIds = new ArrayList<String>();
               int count = 0;
               while (rst.next()) {
                  count++;
                  notiIds.add(rst.getString(1));
                  requestIds.add(rst.getString(2));
                  responseIds.add(rst.getString(3));
               }

               if (count > 0) {
                  pstmt.close();
                  con.close();
                  System.out.println("[INFO] DB connection is successfully closed.");
               }

               if (count > 0) {
                  System.out.println("[DEBUG] You've got " + count + " new answer(s). Please check that out.");

                  ////////////////////////
                  // Notification frame //
                  ////////////////////////
                  //////////
                  // TODO //
                  //////////
                  // Need to make the "check-in" link to update Notification table
                  String message = "You got " + count + " new answer(s). Please check them out: <br><br>";
                  // for (int i=0 ; i<count ; i++) {
                  // message += "[" + String.valueOf(i+1) + "/" + String.valueOf(count) + "] answer: <a href=\"https://www.google.com\">1Check-in</a><br>" ;

                  // }

                  String header = "New answer notification";
                  JFrame frame = new JFrame();

                  //////////
                  // TODO //
                  //////////
                  // Note that the frame size here need to be updated to be adaptive to the length of textual contents.
                  // Now the frame size is fixed to show only 3 lines of answers by the below line.
                  frame.setSize(300, 200);
                  frame.setUndecorated(true);
                  frame.setLayout(new GridBagLayout());
                  GridBagConstraints constraints = new GridBagConstraints();
                  constraints.gridx = 0;
                  constraints.gridy = 0;
                  constraints.weightx = 1.0f;
                  constraints.weighty = 1.0f;
                  constraints.insets = new Insets(5, 5, 5, 5);
                  constraints.fill = GridBagConstraints.BOTH;
                  JLabel headingLabel = new JLabel(header);
                  // headingLabel.setIcon(headingIcon); // --- use image icon you want to be as heading image.
                  headingLabel.setOpaque(false);
                  frame.add(headingLabel, constraints);
                  constraints.gridx++;
                  constraints.weightx = 0f;
                  constraints.weighty = 0f;
                  constraints.fill = GridBagConstraints.NONE;
                  constraints.anchor = GridBagConstraints.NORTH;
                  JButton cloesButton = new JButton(new AbstractAction("x") {
                     private static final long serialVersionUID = 1L;

                     @Override
                     public void actionPerformed(final ActionEvent e) {
                        frame.dispose();
                     }
                  });
                  cloesButton.setMargin(new Insets(1, 4, 1, 4));
                  cloesButton.setFocusable(false);
                  frame.add(cloesButton, constraints);

                  constraints.gridx = 0;
                  constraints.gridy++;
                  constraints.weightx = 1.0f;
                  constraints.weighty = 1.0f;
                  constraints.insets = new Insets(15, 5, 5, 5);
                  constraints.fill = GridBagConstraints.BOTH;
                  JLabel messageLabel = new JLabel("<HtMl>" + message + "</HTML");
                  frame.add(messageLabel, constraints);
                  frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                  frame.setVisible(true);

                  constraints.gridx = 0;
                  constraints.gridy++;
                  constraints.weightx = 1.0f;
                  constraints.weighty = 1.0f;
                  constraints.insets = new Insets(5, 5, 5, 5);
                  constraints.fill = GridBagConstraints.BOTH;
                  // JLabel _deletelater = new JLabel("<html> Button Location </html>");
                  // frame.add(_deletelater, constraints);
                  for (int i = 0; i < count; i++) {
                     JButton checkinButton = new JButton("Check-in [" + String.valueOf(i + 1) + "/" + String.valueOf(count) + "] answer");
                     final int buttonIndex = i;
                     checkinButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                           System.out.println("[DEBUG] notiId:" + String.valueOf(notiIds.get(buttonIndex)));
                           System.out.println("[DEBUG] requestId:" + String.valueOf(requestIds.get(buttonIndex)));
                           System.out.println("[DEBUG] responsdId:" + String.valueOf(responseIds.get(buttonIndex)));

                           // Connect DB
                           Connection con = null;
                           PropertyResourceBundle dbbundle = (PropertyResourceBundle) ResourceBundle.getBundle("database");
                           String url = dbbundle.getString("DB_URL");
                           String username = dbbundle.getString("DB_USERNAME");
                           String password = dbbundle.getString("DB_PASSWORD");

                           try {
                              // load and register JDBC driver for MySQL
                              // Class.forName("com.mysql.jdbc.Driver");
                              Class.forName(JDBC_DRIVER);
                              con = DriverManager.getConnection(url, username, password);
                              System.out.println("[INFO] DB connection success!");
                              // Construct sql using PreparedStatement which is more secure way than prepare
                              String delete_sql = "DELETE FROM Notifications WHERE notiId=?";

                              PreparedStatement pstmt = null;
                              pstmt = con.prepareStatement(delete_sql);
                              pstmt.setInt(1, Integer.parseInt(notiIds.get(buttonIndex)));

                              // process the results
                              pstmt.executeUpdate();
                              System.out.println("[DEBUG] An entry (notiId:" + String.valueOf(notiIds.get(buttonIndex)) + ") is successfully deleted from the Notifications table.");
                              pstmt.close();
                              con.close();
                              System.out.println("[INFO] DB connection is successfully closed.");
                              frame.dispose();
                           } catch (SQLException | ClassNotFoundException ae) {
                              String errmsg = "[ERROR] DB load fail: " + e.toString();
                              System.out.println(errmsg);
                              UtilConsole.print(errmsg);
                           }
                        }
                     });

                     frame.add(checkinButton, constraints);
                     constraints.gridy++;
                  }

                  // for (int i=0 ; i<count ; i++) {
                  // JButton checkinButton = new JButton("Check-in [" + String.valueOf(i+1) + "/" + String.valueOf(count) + "] answer");
                  // frame.add(checkinButton, constraints);
                  // }

                  Dimension scrSize = Toolkit.getDefaultToolkit().getScreenSize();// size of the screen
                  Insets toolHeight = Toolkit.getDefaultToolkit().getScreenInsets(frame.getGraphicsConfiguration());// height of the task bar
                  frame.setLocation(scrSize.width - frame.getWidth(), scrSize.height - toolHeight.bottom - frame.getHeight());

                  new Thread() {
                     @Override
                     public void run() {
                        try {
                           Thread.sleep(14000); // time after which pop up will be disappeared.
                           frame.dispose();
                        } catch (InterruptedException e) {
                           e.printStackTrace();
                        }
                     };
                  }.start();
                  ///////////////
                  // Frame end //
                  ///////////////
               }
            } catch (SQLException | ClassNotFoundException e) {
               System.out.println("[ERROR] DB load fail: " + e.toString());
            }

            // use connection
         }
      }, 0, param_period, TimeUnit.SECONDS);
   }
}