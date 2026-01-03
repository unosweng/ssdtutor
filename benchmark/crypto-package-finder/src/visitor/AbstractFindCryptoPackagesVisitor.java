/**
 */
package visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import util.UTFile;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public abstract class AbstractFindCryptoPackagesVisitor extends ASTVisitor {
   IDocument doc;
   String javaFilePath;
   IPackageFragment[] iPackages;
   List<String> listCryptoImportDec = new ArrayList<String>();
   List<String> listCryptoImportDecWithinFile;

   public String getQualifiedName(MethodInvocation metInv) {
      if (metInv.resolveMethodBinding() == null //
            || metInv.resolveMethodBinding().getDeclaringClass() == null) {
         return null;
      }

      return metInv.resolveMethodBinding().getDeclaringClass().getQualifiedName();
   }

   public String getQualifiedName(ClassInstanceCreation ciCre) {

      if (ciCre.resolveConstructorBinding() == null //
            || ciCre.resolveConstructorBinding().getDeclaringClass() == null) {
         return null;
      }

      String quaName = ciCre.resolveConstructorBinding().getDeclaringClass().getQualifiedName();
      return quaName;
   }

   public void setJavaFilePath(String p) {
      this.javaFilePath = p;
      this.listCryptoImportDecWithinFile.clear();
   }

   public int getLineNumber(int offset) {
      int lineNumber = 0;
      try {
         if (doc == null) {
            String source = UTFile.readEntireFile(javaFilePath);
            doc = new Document(source);
         }
         lineNumber = doc.getLineOfOffset(offset) + 1;

      } catch (Exception e) {
         e.printStackTrace();
      }
      return lineNumber;
   }
}
