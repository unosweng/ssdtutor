/**
 */
package noninteractive;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

import input.IGlobalProperty;
import util.UtilAST;
import visitor.DetectionASTVisitor;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public abstract class FindAnomalyCrypto implements IGlobalProperty {
   public void doAnalysis(List<Path> listJavaFile, DetectionASTVisitor visitor) {
      System.out.println("[DBG] # of files to analyze: " + listJavaFile.size());
      for (Path path : listJavaFile) {
         String javaFilePath = path.toAbsolutePath().toString();
         System.out.println("[DBG] " + javaFilePath);
         try {
            CompilationUnit comUnit = UtilAST.parse(javaFilePath, IS_FILE_CONTENTS_PRINT);
            visitor.setJavaFilePath(javaFilePath);
            comUnit.accept(visitor);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
   }
   
   public abstract void startAnalysis();
}
