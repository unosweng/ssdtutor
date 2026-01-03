package visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.tree.TreeNode;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import controlflowgraph.backward.CFGNode;
import util.UtilAST;

public class CallGraphVisitor {
   protected Map<String, CFGNode> mapCFGNodes = new HashMap<String, CFGNode>();
   protected Set<IMethod> masterMethodSet = new HashSet<>();
   private TypeDeclaration typeDecl;
   protected int nodeCount = 0;
   final int DEPTH = 5;

   protected void buildCallGraph(IMethod javaMethodElem, int depth, IPackageFragment[] packages) {
      if (depth > DEPTH) {
         System.out.println("Recursion Depth Exceeded Theadshold (" + DEPTH + ")");
         return;
      }

      CFGNode calleeNode = getCFGNode(javaMethodElem, null);
      if (calleeNode == null) {
         return;
      }

      this.nodeCount++;
      List<IMethod> callerMethods = UtilAST.getCallerMethods(javaMethodElem, packages);
      List<IMethod> newCallerMethods = new ArrayList<>();

      // filter callerMethods down to only those that are not already in the set
      for (IMethod iMethod : callerMethods) {
         if (!masterMethodSet.contains(iMethod)) {
            masterMethodSet.add(iMethod);
            newCallerMethods.add(iMethod);
         }
      }

      for (IMethod iMethod : newCallerMethods) {
         CFGNode caller = getCFGNode(iMethod, calleeNode.getID());

         if (caller == null) { // Added a checker since JDT caller-search API does not work correctly.
            continue;
         }

         try {
            if (isAncestor(calleeNode, caller)) {
               continue;
            }
            calleeNode.add(caller);
         } catch (java.lang.IllegalArgumentException e) {
            e.printStackTrace();
         }
      }

      // Call recursively.
      for (IMethod iMethod : newCallerMethods) {
         buildCallGraph(iMethod, ++depth, packages);
      }
   }

   boolean isAncestor(CFGNode curNode, CFGNode nodeToBeAdded) {
      TreeNode[] path = curNode.getPath();
      for (TreeNode treeNode : path) {
         if (treeNode instanceof CFGNode) {
            CFGNode iNode = (CFGNode) treeNode;
            String idNode = iNode.getID();
            String idNodeToBeAdded = nodeToBeAdded.getID();

            if (idNode.equals(idNodeToBeAdded)) {
               return true;
            }
         }
      }
      return false;
   }

   public CFGNode getCFGNode(IMethod javaElemIMethod, String calleeID) {
      String id = null, pkgName = null, typeName = null;
      IPackageDeclaration[] pkgDecl = null;

      try {
         ICompilationUnit compilationUnit = javaElemIMethod.getCompilationUnit();
         if (compilationUnit == null) {
            return null;
         }

         pkgDecl = javaElemIMethod.getCompilationUnit().getPackageDeclarations();
         if (pkgDecl.length != 1) {
            System.out.println("\tPackage Declaration not found for: " + javaElemIMethod.getElementName());
            // throw new RuntimeException("[WRN] SEE HERE (FindMisuse.getCGNode)");
         }
         else {
            pkgName = pkgDecl[0].getElementName();
            typeName = javaElemIMethod.getDeclaringType().getElementName();
         }
         CompilationUnit iCUnit = UtilAST.parse(compilationUnit);
         iCUnit.accept(new ASTVisitor() {
            public boolean visit(TypeDeclaration node) {
               typeDecl = node;
               return true;
            }
         });
         iCUnit = null;
         System.gc();
      } catch (Exception e) {
         e.printStackTrace();
      }

      String sig = UtilAST.getSignature(javaElemIMethod);
      id = pkgName + "." + typeName + "." + sig;

      // Added a checker for false positive results by JDT caller-search API.
      if (calleeID != null && calleeID.equals(id)) {
         return null;
      }

      CFGNode cfgNode = this.mapCFGNodes.get(id);
      if (cfgNode == null) {
         cfgNode = new CFGNode(id, javaElemIMethod, typeDecl);
         mapCFGNodes.put(id, cfgNode);
      }
      return cfgNode;
   }

   public void displayCFG() {
      Collection<CFGNode> nodes = mapCFGNodes.values();
      System.out.println("CFG Map contains [" + nodes.size() + "] nodes.");

      for (CFGNode node : nodes) {
         if (!node.isRoot()) {
            continue;
         }
         String nodeMsg = "\tRoot: " + node.getID();
         nodeMsg += "\t (# of child nodes: " + node.getChildCount() + ")";
         if (node.getListMethodInv().size() > 0) {
            nodeMsg += "\t (methodInv: " + node.getListMethodInv().size() + ")";
         }
         System.out.println(nodeMsg);

         printChildNodes(node, "\t");
      }
   }

   public void printChildNodes(CFGNode node, String msg) {
      if (node.getChildCount() == 0 || node.isLeaf()) {
         return;
      }

      for (int i = 0; i < node.getChildCount(); i++) {
         TreeNode tnChild = node.getChildAt(i);
         if (tnChild instanceof CFGNode) {
            CFGNode child = (CFGNode) tnChild;
            String nodeMsg = msg + "\t-> " + child.getID();
            nodeMsg += "\t (# of child nodes: " + child.getChildCount() + ")";
            if (child.getListMethodInv().size() > 0) {
               nodeMsg += "\t (methodInv: " + child.getListMethodInv().size() + ")";
            }
            System.out.println(nodeMsg);
            if (child.getChildCount() > 0) {
               printChildNodes(child, msg + "\t");
            }
         }
      }
   }
}
