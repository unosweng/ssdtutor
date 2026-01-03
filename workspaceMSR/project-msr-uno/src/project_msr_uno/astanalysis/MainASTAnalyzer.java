package project_msr_uno.astanalysis;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import util.UTFile;
import util.UtilAST;

public class MainASTAnalyzer {
   private static final String DIR_PATH = "/opt/workspaceMSR/input"; // Change to your system path.
   private static final boolean IS_FILE_CONTENTS_PRINT = false;

   public static void main(String[] args) throws Exception {
      MainASTAnalyzer analyzer = new MainASTAnalyzer();

      List<Path> listSource = UTFile.getFileListRecursive(DIR_PATH, "*.java");
      for (Path iPath : listSource) {
         String iSRCPath = iPath.toFile().getAbsolutePath();
         System.out.println("[DBG] Source: " + iSRCPath);
         analyzer.astAnalyzer(iSRCPath);
      }
   }

   void astAnalyzer(String sourcePath) throws Exception {
      CompilationUnit comUnit = UtilAST.parse(sourcePath, IS_FILE_CONTENTS_PRINT);
      comUnit.accept(new ASTVisitor() {
         public boolean visit(TypeDeclaration node) {
            System.out.println("[DBG] Class Name: " + node.getName());
            return true;
         }
      });
   }
}
