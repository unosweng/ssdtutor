/**
 */
package noninteractive;

import java.io.File; 
import java.io.IOException;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import input.IGlobalProperty;
import util.UTFile;
import util.UtilAST;
import visitor.DetectionASTVisitor;
import visitor.FindMisuseHashFunDetector;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class Find2MisuseHashFun extends FindAnomalyCrypto implements IGlobalProperty {

   public static void main(String[] args) throws IOException {
      Find2MisuseHashFun detector = new Find2MisuseHashFun();
      detector.startAnalysis();
      System.out.println("[DBG] Done.");
   }
   
   @Override
   public void startAnalysis() {
      DetectionASTVisitor visitor = new FindMisuseHashFunDetector();
      doAnalysis(UTFile.getFileListRecursive(TARGET_GIT_DIR, "*.java"), visitor);
   }
   
   @Test
   public void testDoAnalysis() throws IOException {
      String filenNme = "/runtime-bugdetectionexample/" //
            + "case2-misuse-hash-fun/src/HashWeak.java";
      String workDir = System.getProperty("user.dir");
      String parent1 = new File(workDir).getParent();
      String parent2 = new File(parent1).getParent();
      String javaFilePath = new File(parent2 + filenNme).getAbsolutePath().toString();

      CompilationUnit comUnit = UtilAST.parse(javaFilePath, IS_FILE_CONTENTS_PRINT);
      DetectionASTVisitor visitor = new FindMisuseHashFunDetector(javaFilePath, null);
      comUnit.accept(visitor);
   }
}
