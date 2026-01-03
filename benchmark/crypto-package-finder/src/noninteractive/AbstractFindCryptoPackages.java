/**
 */
package noninteractive;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;

import input.IGlobalProperty;
import util.UtilAST;
import visitor.FindCryptoPackagesVisitor;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public abstract class AbstractFindCryptoPackages implements IGlobalProperty {
   public int numJavaFileWithCryptoPackage;
   public int numJavaFileInRepo;

   public boolean doAnalysis(List<Path> listJavaFile, FindCryptoPackagesVisitor visitor) {
      numJavaFileInRepo = listJavaFile.size();
      numJavaFileWithCryptoPackage = 0;
      System.out.println("[DBG] # of files to analyze: " + numJavaFileInRepo);

      for (Path path : listJavaFile) {
         String javaFilePath = path.toAbsolutePath().toString();
         try {
            CompilationUnit comUnit = UtilAST.parse(javaFilePath, IS_FILE_CONTENTS_PRINT);
            if (comUnit == null) {
               return false;
            }
            visitor.setJavaFilePath(javaFilePath);
            comUnit.accept(visitor);
            if (!visitor.getListCryptoImportDecWithinFile().isEmpty()) {
               numJavaFileWithCryptoPackage++;
               System.out.println("[DBG] " + javaFilePath + ": " + numJavaFileWithCryptoPackage);
            }
         } catch (IOException e) {
            e.printStackTrace();
         }
      }
      return true;
   }

   public abstract void startAnalysis();
}
