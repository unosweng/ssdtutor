package visitor;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import util.UtilAST;

public class CFGVisitor extends ASTVisitor {
   private IPackageFragment[] packages;

   public CFGVisitor() {
   }

   public CFGVisitor(IPackageFragment[] packages) {
      this.packages = packages;
   }

   public boolean visit(MethodDeclaration metDec) {
      IJavaElement methodDecl = metDec.resolveBinding().getJavaElement();
      String className = metDec.resolveBinding().getDeclaringClass().getQualifiedName();

      if (methodDecl instanceof IMethod) {
         IMethod callee = (IMethod) methodDecl;
         System.out.println("[DBG] Build CFG for " + className + "." + UtilAST.getSignature(callee));
         CallGraphVisitor callGraphVisitor = new CallGraphVisitor();
         callGraphVisitor.buildCallGraph(callee, 0, this.packages);
         callGraphVisitor.displayCFG();
      }
      return true;
   }
}
