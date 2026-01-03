package bugtracking;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import util.UTFile;
import util.UTStr;

public class BaseBugRepo {
   String bugRepoURL;
   String bugRepoCommentURL;
   String bugRepoQrURL;

   String pathOutput = "output" + File.separator;
   String pathBug = pathOutput + File.separator + "bug" + File.separator;
   String pathBugComment = pathOutput + File.separator + "comment" + File.separator;

   String errorFile = "error_download_%s.txt";
   int errorCounter = 0, errorFileCounter = 0;
   List<String> errorBuffer = new ArrayList<String>();

   // ArrayList<String> noPDFList = new ArrayList<String>();
   // ArrayList<String> PDFList = new ArrayList<String>();
   // ArrayList<String> webAddress = new ArrayList<String>();

   List<String> listBugID = new ArrayList<String>();

   int NUM_THREAD = 4;

   public BaseBugRepo() {
   }

   void analyzeBugzilla() throws IOException {
   }
   
   void readHtml(String fileTargetHtml, String projectName) throws IOException {
      File input = new File(fileTargetHtml);
      Document doc = Jsoup.parse(input, "UTF-8");
      Elements lines = doc.getAllElements();

      // Bug IDs
      for (Element line : lines) {
         if (line.attributes().hasKey("href") && line.attributes().get("href").contains("show_bug.cgi?id=")) {
            String relativeURL = line.attributes().get("href");
            String bugIDTokens[] = relativeURL.split("=");
            if (bugIDTokens.length == 2 && !listBugID.contains(bugIDTokens[1])) {
               listBugID.add(bugIDTokens[1]); // add bug id
            }
         }
      }

      System.out.println("[DBG] uniqueList.size: " + listBugID.size());
      UTFile.writeFile(pathOutput + projectName + "bugID_unique.txt", listBugID);
   }

   public void downloadFiles(String eachDirName) throws MalformedURLException, IOException, InterruptedException {
      List<DownloadFileThread> threads = new ArrayList<>();

      int increment = listBugID.size() / NUM_THREAD;
      int bgnIdx = 0, endIdx = increment;

      for (int i = 0; i < NUM_THREAD; i++) {
         System.out.println("[DBG] Creating thread_" + i + ", from " + bgnIdx + //
               " to " + endIdx + " for " + (endIdx - bgnIdx) + " files");

         // Step 1.
         DownloadFileThread thrx = new DownloadFileThread(i, bgnIdx, endIdx, eachDirName);
         threads.add(thrx);

         // Step 2.
         thrx.start();

         bgnIdx = endIdx;
         endIdx = (i + 2) * increment;
      }
      // Last thread for the remaining work.
      if (bgnIdx < listBugID.size()) {
         int theLastThread = NUM_THREAD;
         System.out.println("[DBG] Creating thread_" + theLastThread + ", from " + bgnIdx + //
               " to " + listBugID.size() + " for " + (listBugID.size() - bgnIdx) + " files");

         // Step 1.
         DownloadFileThread thrx = new DownloadFileThread(theLastThread, bgnIdx, listBugID.size(), eachDirName);
         threads.add(thrx);

         // Step 2.
         thrx.start();
      }

      for (DownloadFileThread iThread : threads) {
         iThread.join();
      }
   }

   class DownloadFileThread extends Thread {
      int threadID, bgnIdx, endIdx;
      String eachDirName;

      public DownloadFileThread(int threadID, int bgn, int end, String eachDirName) {
         this.threadID = threadID;
         this.bgnIdx = bgn;
         this.endIdx = end;
         this.eachDirName = eachDirName;
      }

      @Override
      public void run() {
         try {
            this.downloadFiles();
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      void downloadFiles() throws MalformedURLException, IOException {
         System.out.println("[DBG] Downloading thread_" + threadID + ", from " + bgnIdx + //
               " to " + endIdx + " for " + (endIdx - bgnIdx) + " files");
         String url = "", fileName = "";

         for (int i = bgnIdx; i < endIdx; i++) {
            try {
               url = bugRepoURL + listBugID.get(i);
               fileName = pathBug + eachDirName + File.separator + listBugID.get(i) + ".json";
               UTFile.writeURL(url, fileName);

               url = String.format(bugRepoCommentURL, listBugID.get(i));
               fileName = pathBugComment + eachDirName + File.separator + listBugID.get(i) + ".json";
               UTFile.writeURL(url, fileName);

               // System.out.print(i + " ");
               // if (i % 50 == 0)
               // System.out.println(" ");
            } catch (Exception e) {
               System.out.println("[DBG] Bug error at " + i);
               StringBuilder buffer = new StringBuilder();
               buffer.append("[DBG] Bug error at " + fileName + ", " + url + "\n");
               buffer.append(e.toString() + "\n");
               errorBuffer.add(buffer.toString());
               errorCounter++;

               if (errorCounter == 10) {
                  UTFile.writeFile(String.format(errorFile, String.valueOf(errorFileCounter++)), errorBuffer);
                  errorCounter = 0;
               }
               // e.printStackTrace();
            }
         }
      }
   }

   void saveTargetHtml(String fileVisitHtml, String output) throws IOException {
      File DestFile = new File(fileVisitHtml);
      FileWriter fileWriter = new FileWriter(DestFile);
      fileWriter.write(output);
      fileWriter.close();
   }

   void makeOutputDir() {
      UTFile.makeDir("output");
      for (int i = 1; i <= 12; i++) {
         UTFile.makeDir(pathBug + UTStr.getTwoDigits(i));
         UTFile.makeDir(pathBugComment + UTStr.getTwoDigits(i));
      }
   }
}
