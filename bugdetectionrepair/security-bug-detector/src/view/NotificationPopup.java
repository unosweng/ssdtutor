package view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

import db.DBPeriodicalPoolingNotifications;

public class NotificationPopup {

   public static void show(int count, List<String> notiIds, List<String> requestIds, List<String> responseIds) {
      //////////
      // TODO //
      //////////
      // Need to make the "check-in" link to update Notification table
      String message = "You got " + count + " new answer(s). Please check them out: <br><br>";
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

      for (int i = 0; i < count; i++) {
         JButton checkinButton = new JButton("Check-in [" + String.valueOf(i + 1) + "/" + String.valueOf(count) + "] answer");
         final int buttonIndex = i;
         checkinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               System.out.println("[DEBUG] notiId:" + String.valueOf(notiIds.get(buttonIndex)));
               System.out.println("[DEBUG] requestId:" + String.valueOf(requestIds.get(buttonIndex)));
               System.out.println("[DEBUG] responsdId:" + String.valueOf(responseIds.get(buttonIndex)));

               DBPeriodicalPoolingNotifications.delete(notiIds, buttonIndex);
               frame.dispose();
            }
         });

         frame.add(checkinButton, constraints);
         constraints.gridy++;
      }

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
   }
}
