package controlflowgraph.backward;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import util.UtilAST;

/**
 * @since JDK1.8
 */
public class CFGNode extends DefaultMutableTreeNode {
   private static final long serialVersionUID = 1L;
   private String IDCallee;
   public IMethod iMethodCallee;
   private MethodDeclaration methodDecCallee;
   public int parmPositionToBackTrack = -1;
   public Map<String, VariableDeclarationFragment> variableDeclFragment;
   private List<MethodInvocation> listMethodInv = new ArrayList<MethodInvocation>();
   private List<ConstructorInvocation> listConstructorInv = new ArrayList<ConstructorInvocation>();

   LinkedHashSet<SimpleName> listSlices = new LinkedHashSet<SimpleName>();

   public CFGNode(String ID) {
      super();
      this.IDCallee = ID;
   }

   public CFGNode(String id, IMethod iMethod, TypeDeclaration typeDec) {
      super();
      this.IDCallee = id;
      this.iMethodCallee = iMethod;

      try {
         this.methodDecCallee = UtilAST.findMethodDec(iMethod, typeDec);

         if (this.methodDecCallee == null) {

            // check the parent first
            this.methodDecCallee = getMethodDeclParent(iMethod, typeDec);
            if (this.methodDecCallee == null) {
               return;
            }
         }

         this.methodDecCallee.accept(new ASTVisitor() {
            @Override
            public boolean visit(ConstructorInvocation n) {
               listConstructorInv.add(n);
               return true;
            }

            @Override
            public boolean visit(MethodInvocation n) {
               listMethodInv.add(n);
               return true;
            }
         });
      } catch (NullPointerException e) {
         e.printStackTrace();
      }
   }

   public MethodDeclaration getMethodDeclParent(IMethod iMethod, TypeDeclaration typeDec) {
      if (typeDec == null) {
         return null;
      }

      ASTNode parent = typeDec.getParent();
      if (parent != null && parent instanceof TypeDeclaration) {
         TypeDeclaration ptd = (TypeDeclaration) parent;
         return UtilAST.findMethodDeclaration(iMethod, ptd);
      }

      return null;
   }

   public String getID() {
      return IDCallee;
   }

   public List<MethodInvocation> getListMethodInv() {
      return listMethodInv;
   }

   public void setListMethodInv(List<MethodInvocation> listMethodInv) {
      this.listMethodInv = listMethodInv;
   }
}
