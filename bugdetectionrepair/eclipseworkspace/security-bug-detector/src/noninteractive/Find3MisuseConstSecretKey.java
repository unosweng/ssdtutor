/**
 */
package noninteractive;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Ignore;
import org.junit.Test;

import input.IGlobalProperty;
import util.UTFile;
import util.UtilAST;
import visitor.DetectionASTVisitor;
import visitor.FindMisuseConstSecretKeyDetector;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class Find3MisuseConstSecretKey extends FindAnomalyCrypto implements IGlobalProperty {

   public static void main(String[] args) throws IOException {
      Find3MisuseConstSecretKey detector = new Find3MisuseConstSecretKey();
      detector.startAnalysis();
      System.out.println("[DBG] Done.");
   }

   @Override
   public void startAnalysis() {
      DetectionASTVisitor visitor = new FindMisuseConstSecretKeyDetector();
      doAnalysis(UTFile.getFileListRecursive(TARGET_GIT_DIR, "*.java"), visitor);
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
      DetectionASTVisitor visitor = new FindMisuseConstSecretKeyDetector(javaFilePath, null);
      comUnit.accept(visitor);
   }

   @Test
   public void test2() {
      String javaFilePath = "/Users/mksong/data/firebugs/git-repo/AndroidUtilCode/utilcode" //
            + "/lib/src/main/java/com/blankj/utilcode/util/EncryptUtils.java";
      try {
         CompilationUnit comUnit = UtilAST.parse(javaFilePath, IS_FILE_CONTENTS_PRINT);
         DetectionASTVisitor visitor = new FindMisuseConstSecretKeyDetector(javaFilePath, null);
         comUnit.accept(visitor);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
}
