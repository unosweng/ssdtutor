package bugtracking;

import org.junit.Test;

public class TestBaseBugRepo {

   @Test
   public void testDownloadFiles1() throws Exception {
      BaseBugRepo repo = new BaseBugRepo();

      repo.NUM_THREAD = 3;

      int NUM_BugReport = 47;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");

      NUM_BugReport = 48;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      repo = new BaseBugRepo();
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");

      NUM_BugReport = 49;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      repo = new BaseBugRepo();
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");

      NUM_BugReport = 50;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      repo = new BaseBugRepo();
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");
   }

   @Test
   public void testDownloadFiles2() throws Exception {
      BaseBugRepo repo = new BaseBugRepo();
      repo.NUM_THREAD = 4;

      int NUM_BugReport = 47;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");

      NUM_BugReport = 48;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      repo = new BaseBugRepo();
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");

      NUM_BugReport = 49;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      repo = new BaseBugRepo();
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");

      NUM_BugReport = 50;
      System.out.println("[DBG] # threads: " + repo.NUM_THREAD + ", # bug reports: " + NUM_BugReport);
      repo = new BaseBugRepo();
      for (int i = 0; i < NUM_BugReport; i++) {
         repo.listBugID.add("dummy data");
      }
      repo.downloadFiles("");
      System.out.println("----------------------------------------------------");
   }
}
