package visitor;

import java.util.HashMap;
import java.util.Map;


import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class FindRootCause extends ASTVisitor{
   String indicatorClass;
   String packageName, className, methodName;
   boolean hasClass;
   Map<String, String> docFile= new HashMap<String, String>();
   
   public FindRootCause(String indicatorClass) {
      this.indicatorClass = indicatorClass;
      this.hasClass = false;
   }
   
   /**********************************************************************************/
   @Override
   public boolean visit(PackageDeclaration pkgDecl) {
      this.packageName = pkgDecl.getName().getFullyQualifiedName();
      return true;
   }
   
   @Override
   public boolean visit(TypeDeclaration typeDecl) {
      this.className = typeDecl.getName().getFullyQualifiedName();
      return true;
   }
   
   @Override
   public boolean visit(MethodDeclaration metDec) {
      this.methodName = metDec.getName().getFullyQualifiedName();
      metDec.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodInvocation metInv) {
            if (!(metInv.resolveMethodBinding() == null) //
                  && !(metInv.resolveMethodBinding().getDeclaringClass() == null)) {
               if (metInv.resolveMethodBinding().getDeclaringClass().getQualifiedName().equals(indicatorClass)) {
                  saveRootCause();
                  hasClass = true;
               }
            }
            return true;
         }
         
         @Override
         public boolean visit(ClassInstanceCreation ciCre) {
            if (!(ciCre.resolveConstructorBinding() == null) //
                  && !(ciCre.resolveConstructorBinding().getDeclaringClass() == null)) {
               if (ciCre.resolveConstructorBinding().getDeclaringClass().getQualifiedName().equals(indicatorClass)) {
                  saveRootCause();
                  hasClass = true;
               }
            }
            return true;
         }
      });
      return true;
   }
   /**********************************************************************************/
   
   public boolean getClassReturn(){
      this.indicatorClass = null;
      System.gc();
      return hasClass;
   }
   
   public Map<String, String> getValue(){
      return docFile;
   }
   
   public void clearValue(){
      docFile.clear();
      System.gc();
   }
   
   public void saveRootCause() {
      docFile.put("Package", this.packageName);
      docFile.put("Class", this.className);
      docFile.put("Method", this.methodName);
      docFile.put("Field", this.indicatorClass);
   }
}
