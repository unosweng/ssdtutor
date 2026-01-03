/**
 */
package noninteractive;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Ignore;
import org.junit.Test;

import input.IGlobalProperty;
import util.UTFile;
import util.UtilAST;
import visitor.AbstractFindCryptoPackagesVisitor;
import visitor.FindCryptoPackagesVisitor;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class FindCryptoPackages extends AbstractFindCryptoPackages implements IGlobalProperty {
   public static void main(String[] args) throws IOException {
      FindCryptoPackages detector = new FindCryptoPackages();
      detector.startAnalysis();
      System.out.println("[DBG] Done.");
   }

   String fileCryptopackage = "git_repo_cryptopackage_1001.csv";
   String repos = "git_clone_command_info_1001.txt";
   BufferedWriter out;

   @Override
   public void startAnalysis() {
      try {
         if (!new File(repos).exists()) {
            System.out.println("[ERR] Not found: " + new File(repos).getAbsolutePath());
            return;
         }
         if (new File(fileCryptopackage).exists()) {
            System.out.println("[DBG] Deleting an existing file: " + (new File(fileCryptopackage)).getAbsolutePath());
            new File(fileCryptopackage).delete();
         }

         List<String> contents = UTFile.readFileToList(repos);
         out = new BufferedWriter(new FileWriter(fileCryptopackage, true));

         for (int i = 0; i < contents.size(); i++) {
            String iRepo = contents.get(i);
            String repoDirName = iRepo.split("\\s+")[3];
            String dirName = GIT_REPO_DIR + repoDirName;
            if (new File(dirName).exists()) {
               FindCryptoPackagesVisitor visitor = new FindCryptoPackagesVisitor();
               boolean isParsed = doAnalysis(UTFile.getFileListRecursive(dirName, "*.java"), visitor);
               if (isParsed == false) {
                  continue;
               }
               String listImportDec = visitor.getListCryptoImportDecStr();
               int size = visitor.getSizeCryptoImportDec();
               String buf = repoDirName + "," + size + "," + numJavaFileWithCryptoPackage //
                     + "," + numJavaFileInRepo + "," + listImportDec;
               System.out.println("[DBG] " + buf);
               out.write(buf + "\n");
            }
         }
         out.close();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   @Test
   @Ignore
   public void testDoAnalysis1() throws IOException {
      String filenNme = "/runtime-bugdetectionexample/" //
            + "/case3-misuse-const-secret-key/src/KeyUseWrong.java";
      String workDir = System.getProperty("user.dir");
      String parent1 = new File(workDir).getParent();
      String parent2 = new File(parent1).getParent();
      String javaFilePath = new File(parent2 + filenNme).getAbsolutePath().toString();

      CompilationUnit comUnit = UtilAST.parse(javaFilePath, IS_FILE_CONTENTS_PRINT);
      AbstractFindCryptoPackagesVisitor visitor = new FindCryptoPackagesVisitor(javaFilePath);
      comUnit.accept(visitor);
   }

   @Test
   public void testFindCryptoPackages() {
      String javaFilePath = "/data/firebugs/git-repo/AndroidUtilCode/utilcode" //
            + "/lib/src/main/java/com/blankj/utilcode/util/EncryptUtils.java";
      try {
         CompilationUnit comUnit = UtilAST.parse(javaFilePath, IS_FILE_CONTENTS_PRINT);
         FindCryptoPackagesVisitor visitor = new FindCryptoPackagesVisitor(javaFilePath);
         comUnit.accept(visitor);
         String listImportDec = visitor.getListCryptoImportDecStr();
         String file = "git_repo_cryptopackage.csv";
         String gitRepoName = "1_androidutilcode";
         int size = visitor.getSizeCryptoImportDec();
         String buf = gitRepoName + "," + size + "," + listImportDec;
         UTFile.write(file, buf);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
