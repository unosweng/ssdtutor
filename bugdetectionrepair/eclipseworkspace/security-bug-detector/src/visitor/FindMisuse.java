/**
 */
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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import controlflowgraph.backward.CFGNode;
import input.IGlobalProperty;
import util.PrintHelper;
import util.UtilAST;

/**
 * @since JDK1.8
 */
public class FindMisuse {
   private static final String[] BUILTIN_API_CALLS = { "run" };
   protected int nodeCount = 0;
   protected Map<String, CFGNode> mapCFGNodes = new HashMap<String, CFGNode>();
   protected Set<IMethod> masterMethodSet = new HashSet<>();
   protected DetectionASTVisitor detector;
   private TypeDeclaration typeDecl;

   /**
    * buildCallGraph - builds a control flow graph of nodes
    * 
    * @param javaMethodElem
    * @param depth
    */
   protected void buildCallGraph(IMethod javaMethodElem, int depth) {

      if (depth > IGlobalProperty.MAX_RECURSION_DEPTH) {
         // PrintHelper.printErrorMsg("Recursion Depth Exceeded: " + IGlobalProperty.MAX_RECURSION_DEPTH);
         return;
      }

      if (skipSearchMethodCallers(javaMethodElem)) {
         return;
      }

      CFGNode calleeNode = getCFGNode(javaMethodElem, null);
      if (calleeNode == null) {
         return;
      }

      this.nodeCount++;
      List<IMethod> callerMethods = UtilAST.getCallerMethods(javaMethodElem, detector.iPackages);
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
            __DBG__(javaMethodElem, newCallerMethods);
            e.printStackTrace();
         }
      }

      // process list of method invocations
      List<MethodInvocation> listMethodInv = calleeNode.getListMethodInv();
      for (MethodInvocation mi : listMethodInv) {
         IMethodBinding miBinding = mi.resolveMethodBinding();
         if (miBinding == null) {
            continue;
         }
         IJavaElement javaElement = miBinding.getJavaElement();
         if (javaElement instanceof IMethod) {
            IMethod calleeMethod = (IMethod) javaElement;
            CFGNode callee = getCFGNode(calleeMethod, calleeNode.getID());
            if (callee == null) {
               continue;
            }

            try {
               if (isAncestor(calleeNode, callee)) {
                  continue;
               }
               calleeNode.add(callee);
            } catch (IllegalArgumentException e) {
               e.printStackTrace();
            }
         }
      }
      // Call recursively.
      for (IMethod iMethod : newCallerMethods) {
         // System.out.println(depth + ":" + masterMethodSet.size() + ":" +
         // callerMethods.size() + ":" + newCallerMethods.size()
         // + " - " + iMethod.getKey());
         buildCallGraph(iMethod, ++depth);
      }
   }

   boolean skipSearchMethodCallers(IMethod javaMethodElem) {
      // skip searching callers of built-in APIs (e.g., java.lang.run)
      for (String iCall : BUILTIN_API_CALLS) {
         if (javaMethodElem.getElementName().equals(iCall)) {
            return true;
         }
      }
      return false;
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

   void __DBG__(IMethod javaMethodElem, List<IMethod> callerMethods) {
      String _callee = UtilAST.getFullSignature(javaMethodElem);

      for (IMethod iMethod : callerMethods) {

         String caller = UtilAST.getFullSignature(iMethod);
      }
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
         } else {
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

   protected void findCallSites() {
      Collection<CFGNode> listNode = mapCFGNodes.values();
      for (CFGNode iNode : listNode) {
         if (iNode.isLeaf()) {
            TreeNode[] nodesInPath = iNode.getPath();

            for (TreeNode iTreeNode : nodesInPath) {
               if (iTreeNode instanceof CFGNode) {
                  CFGNode curNode = (CFGNode) iTreeNode;
                  findCallSiteWithinMethod(curNode);
               }
            }
         }
      }
   }

   void findCallSiteWithinMethod(CFGNode curNode) {
      CFGNode parCallee = (CFGNode) curNode.getParent();
      if (parCallee != null) {

         // First check the list of Method Invocations
         List<MethodInvocation> listMethodInv = curNode.getListMethodInv();
         for (MethodInvocation methInvc : listMethodInv) {
            IMethodBinding methInvcResolveMethodBinding = methInvc.resolveMethodBinding();
            MethodDeclaration parCalleeMethodDec = parCallee.getMethodDec();
            if (methInvcResolveMethodBinding != null && parCalleeMethodDec != null) {
               IMethodBinding parCalleeResolveBinding = parCalleeMethodDec.resolveBinding();

               if (methInvcResolveMethodBinding.toString().equals(parCalleeResolveBinding.toString())) {
                  curNode.setCallSiteMethodInvc(methInvc);
               }
            }
         }

         // Then check the list of Constructor Invocations
         List<ConstructorInvocation> listConstructorInv = curNode.getListConstructorInv();
         for (ConstructorInvocation constrInvc : listConstructorInv) {
            IMethodBinding resolveConstructorBinding = constrInvc.resolveConstructorBinding();
            MethodDeclaration parCalleeMethodDec = parCallee.getMethodDec();

            if (resolveConstructorBinding != null && parCalleeMethodDec != null) {
               IMethodBinding parCalleeResolveBinding = parCalleeMethodDec.resolveBinding();

               if (resolveConstructorBinding.equals(parCalleeResolveBinding)) {
                  curNode.setCallSiteConstructorInvc(constrInvc);
               }
            }

         }
      }
   }

   protected void markCallSiteRootCallee(ASTNode node) {
      Collection<CFGNode> listCallers = mapCFGNodes.values();
      for (CFGNode iCaller : listCallers) {
         if (iCaller.isLeaf()) {
            TreeNode rootTreeNode = iCaller.getPath()[0];
            if (rootTreeNode instanceof CFGNode) {
               CFGNode rootNode = (CFGNode) rootTreeNode;
               rootNode.setRoot(true);
               rootNode.setRootCallSite(node);
               return;
            }
         }
      }
   }

   public void displayCFG() {
      Collection<CFGNode> nodes = mapCFGNodes.values();
      PrintHelper.printDebugMsg("CFG Map contains [" + nodes.size() + "] nodes.");

      for (CFGNode node : nodes) {
         // only drill down from root node
         if (!node.isRoot()) {
            continue;
         }
         String nodeMsg = "\tRoot: " + node.getID();
         nodeMsg += "\t (children: " + node.getChildCount() + ")";
         if (node.getListMethodInv().size() > 0) {
            nodeMsg += "\t (methodInv: " + node.getListMethodInv().size() + ")";
         }
         PrintHelper.printDebugMsg(nodeMsg);

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

            String nodeMsg = msg + "\t-> " + child;
            nodeMsg += "\t (children: " + child.getChildCount() + ")";
            if (child.getListMethodInv().size() > 0) {
               nodeMsg += "\t (methodInv: " + child.getListMethodInv().size() + ")";
            }
            PrintHelper.printDebugMsg(nodeMsg);
            if (child.getChildCount() > 0) {
               printChildNodes(child, msg + "\t");
            }
         }
      }
   }
}
