/**
 */
package visitor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class FindCryptoPackagesVisitor extends AbstractFindCryptoPackagesVisitor {
   private static final String JAVAX_CRYPTO = "javax.crypto.";

   public FindCryptoPackagesVisitor() {
      this.listCryptoImportDecWithinFile = new ArrayList<String>();
   }

   public FindCryptoPackagesVisitor(String javaFilePath) {
      this();
      this.javaFilePath = javaFilePath;
   }

   public boolean visit(ImportDeclaration n) {
      String pkgName = n.getName().getFullyQualifiedName();
      if (pkgName.contains(JAVAX_CRYPTO)) {
         this.listCryptoImportDec.add(pkgName);
         this.listCryptoImportDecWithinFile.add(pkgName);
      }
      return true;
   }

   public String getListCryptoImportDecStr() {
      StringBuilder buf = new StringBuilder();
      for (String iPkg : listCryptoImportDec) {
         buf.append(iPkg + "\n");
      }
      return "\"" + buf.toString().trim() + "\"";
   }

   public List<String> getListCryptoImportDec() {
      return listCryptoImportDec;
   }

   public int getSizeCryptoImportDec() {
      return listCryptoImportDec.size();
   }

   public List<String> getListCryptoImportDecWithinFile() {
      return listCryptoImportDecWithinFile;
   }
}
