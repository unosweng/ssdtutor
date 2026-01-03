package concurrency.scheduler;

import java.util.ArrayList;

import db.DBPeriodicalPoolingNotifications;
import view.NotificationPopup;

public class PeriodicalPoolingExecutor implements Runnable {

   @Override
   public void run() {
      int count = 0;
      ArrayList<String> notiIds = new ArrayList<String>();
      ArrayList<String> requestIds = new ArrayList<String>();
      ArrayList<String> responseIds = new ArrayList<String>();

      count = DBPeriodicalPoolingNotifications.get(count, notiIds, requestIds, responseIds);

      if (count > 0) {
         System.out.println("[DEBUG] You've got " + count + " new answer(s). Please check that out.");

         NotificationPopup.show(count, notiIds, requestIds, responseIds);
      }
   }
}
