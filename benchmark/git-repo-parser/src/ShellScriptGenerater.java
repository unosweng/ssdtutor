import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 */

/**
 * @since JDK1.8
 */
public class ShellScriptGenerater {
   public static String GIT_REPO = "../sum.csv";
   private static String GIT_CLONE_CMD_INFO = "git_clone_command_info.txt";

   public static void main(String[] args) {
      System.out.println("[DBG] Start ShellScriptGenerater...");
      if (!new File(GIT_REPO).exists()) {
         System.out.println("[ERR] File Not Found");
         return;
      } else {
         System.out.println("[DBG] Read an input file: " + new File(GIT_REPO).getAbsolutePath());
      }
      
      List<String> gitRepoList = UTFile.readFileToList(GIT_REPO);
      List<String> shellScriptList = new ArrayList<String>();

      for (int i = 0; i < gitRepoList.size(); i++) {
         String iLine = gitRepoList.get(i);
         String repoName = iLine.split(",")[0];
         String repoURL = iLine.split(",")[1];
         String[] tokenURL = repoURL.split("/");

         int secondLastToken = tokenURL.length - 2;
         String repoOwner = tokenURL[secondLastToken];

         String gitCloneCmd = "git clone " + repoURL + " " + (i+1001) + "_" + repoOwner + "_" + repoName;
         shellScriptList.add(gitCloneCmd.toLowerCase());
      }

      try {
         UTFile.writeFile(GIT_CLONE_CMD_INFO, shellScriptList);
         System.out.println("[DBG] Saved: " + new File(GIT_CLONE_CMD_INFO).getAbsolutePath());
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
