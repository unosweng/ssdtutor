package bugtracking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import util.UTStr;

public class MainBugRepoLinuxKernel extends BaseBugRepo {
   static String projectName = "linuxkernel";
   static String fileTargetPeriodHtml = projectName + "_extracted_%s_%s.html";

   String YYYY = "2019", DD_FROM = "01", DD_TO = "31";

   String pathBug = pathOutput + File.separator + "bug" + File.separator;
   String pathBugComment = pathOutput + File.separator + "comment" + File.separator;

   String QR_PERIODS[] = { "%s-%s-%s", "%s-%s-%s" }; // They're equivalent but could be different for other purposes.

   static List<String> listExtractedHTML = new ArrayList<>();

   public static void main(String[] args) throws Exception {
      System.out.println("Start ...");
      Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
      Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);

      MainBugRepoLinuxKernel mainWebCrawler = new MainBugRepoLinuxKernel();
      mainWebCrawler.makeOutputDir();
      mainWebCrawler.analyzeBugzilla();

      for (int i = 0; i < listExtractedHTML.size(); i++) {
         String iExtractedHTML = listExtractedHTML.get(i);
         String iDirName = UTStr.getTwoDigits(i + 1);
         mainWebCrawler.readHtml(iExtractedHTML, projectName);

         System.out.println("[DBG] " + mainWebCrawler.bugRepoURL + ", " + iDirName + " ... ");
         System.out.println("[DBG] downloading ... ");
         mainWebCrawler.downloadFiles(iDirName);
      }

      System.out.println("Done.");
   }

   public MainBugRepoLinuxKernel() {
      bugRepoQrURL = "https://bugzilla.kernel.org";
      bugRepoURL = "https://bugzilla.kernel.org/rest/bug/";
      bugRepoCommentURL = "https://bugzilla.kernel.org/rest/bug/%s/comment";
   }

   void analyzeBugzilla() throws IOException {
      /* 
       * The period is updated by data collection strategy (monthly base)
       */
      for (int i = 1; i <= 12; i++) {
         String YYfrom = YYYY;
         String MMfrom = UTStr.getTwoDigits(i);
         String DDfrom = DD_FROM;
         String qrFrom = String.format(QR_PERIODS[0], YYfrom, MMfrom, DDfrom);

         String YYto = YYYY;
         String MMto = UTStr.getTwoDigits(i);
         String DDto = DD_TO;
         String qrTo = String.format(QR_PERIODS[1], YYto, MMto, DDto);

         String queryURL = bugRepoQrURL + "/buglist.cgi?" //
               + "chfieldfrom=" + qrFrom + "&" + "chfieldto=" + qrTo;
         System.out.println("[DBG] query URL: " + queryURL);

         String fileHTML = pathOutput + String.format(fileTargetPeriodHtml, qrFrom, qrTo);
         System.out.println("[DBG] \t file HTML: " + fileHTML);

         analyzeBugzilla(fileHTML, queryURL);
         listExtractedHTML.add(fileHTML);
      }
   }

   void analyzeBugzilla(String fileHTML, String queryURL) throws IOException {
      if (new File(fileHTML).exists()) {
         return;
      }

      try {
         /**
          * Step 1. visit bug repository.
          */
         WebDriver driver = new HtmlUnitDriver();
         driver.get(queryURL);
         String targetHtmlSource = driver.getPageSource();
         driver.close();
         /**
          * Step 2. save a target web page (html).
          */
         saveTargetHtml(fileHTML, targetHtmlSource);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
