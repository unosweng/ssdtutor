/**
 */
package visitor.findmisuse.constsecretkeycases;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import controlflowgraph.backward.CFGNode;
import input.IRuleInfo;
import model.MethodElement.Indicator;
import util.PrintHelper;
import util.UtilAST;
import visitor.DetectionASTVisitor;
import visitor.FindMisuse;

/**
 * @since JDK1.8
 */
public class FindMisuseConstSecretKeyCase2MethodParm extends FindMisuse {
   final String FOUND_VULNERABILITY = "Traced backward ARG1 and found";
   public int POSITION_TO_TRACK = 0;
   private String ideMetInv;

   public FindMisuseConstSecretKeyCase2MethodParm(DetectionASTVisitor detector) {
      this.detector = detector;
   }

   public boolean checkCase2_MethodParm(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
      List<SimpleName> listArgument = new ArrayList<SimpleName>();
      List<?> arguments = ciCre.arguments();
      for (Object iObj : arguments) {
         if (iObj instanceof SimpleName) {
            SimpleName iSpName = (SimpleName) iObj;
            listArgument.add(iSpName);
         } else if (iObj instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) iObj;
            Expression expr = mi.getExpression();
            if (expr instanceof SimpleName) {
               SimpleName sName = (SimpleName) expr;
               listArgument.add(sName);
            }
         }
      }
      boolean isFound = check_MethodParm_MethodInvocArgument(metDec, listArgument);
      if (isFound) {
         // System.out.println("[DBG] All parameters in " + SECRET_KEY_SPEC);
         return true;
      }
      return false;
   }

   boolean check_MethodParm_MethodInvocArgument(MethodDeclaration metDec, List<SimpleName> arguments) {
      if (arguments.isEmpty()) {
         return false;
      }

      List<?> parameters = metDec.parameters();

      for (Object jObj : parameters) {
         if (jObj instanceof SingleVariableDeclaration) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) jObj;
            SimpleName varName = svd.getName();

            if ((POSITION_TO_TRACK + 1) > arguments.size()) {
               continue;
            }

            SimpleName iArg = arguments.get(POSITION_TO_TRACK);
            IBinding rb1 = iArg.resolveBinding();
            IBinding rb2 = varName.resolveBinding();
            
            if (rb1 == null || rb2 == null) {
               continue;
            } else if (rb1.equals(rb2)) {
               return true;
            }
         }
      }
      return false;
   }
   
   public static String getQualifiedName(ClassInstanceCreation ciCre) {
      if (ciCre.resolveConstructorBinding() == null //
            || ciCre.resolveConstructorBinding().getDeclaringClass() == null) {
         return null;
      }

      String quaName = ciCre.resolveConstructorBinding().getDeclaringClass().getQualifiedName();
      return quaName;
   }


   public void traceBackwardFromVulnerability(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
      IMethodBinding Mthbinding = metDec.resolveBinding();
      this.ideMetInv = getQualifiedName(ciCre);
      IJavaElement javaElem = Mthbinding.getJavaElement();
      if (javaElem instanceof IMethod) {
         IMethod vulneralbleCalleeMethod = (IMethod) javaElem;
         nodeCount = 0;
         mapCFGNodes.clear();
         masterMethodSet.clear();
         buildCallGraph(vulneralbleCalleeMethod, 0);
         findCallSites();
         markCallSiteRootCallee(ciCre);
         findBackwardSlices();
         findVulnerabilityInSlices();
      }
   }

   void findBackwardSlices() {
      for (CFGNode iCaller : mapCFGNodes.values()) {
         if (iCaller.isLeaf()) { // the last caller of vulnerable callee
            TreeNode[] pathFromRootToLeaf = iCaller.getPath();
            for (TreeNode iNodeInPath : pathFromRootToLeaf) {
               defUseAnalysisBackwardSlice((CFGNode) iNodeInPath);
            }
         }
      }
   }

   void defUseAnalysisBackwardSlice(CFGNode n) {
      if (n.isRoot() && -1 == n.getParmPositionToBackTrack()) {
         // # Target exists in the first parameter.
         ClassInstanceCreation rootCallSite = (ClassInstanceCreation) n.getRootCallSite();
         Object object = rootCallSite.arguments().get(this.POSITION_TO_TRACK);
         if (n.getMethodDec() != null && object instanceof SimpleName) {
            SimpleName varObj = (SimpleName) object;
            // # Find which parameter should be tracked backward.
            List<?> parameters = n.getMethodDec().parameters();
            int matchedPos = findMatchedParameter(parameters, varObj);
            n.setParmPositionToBackTrack(matchedPos);
         }
      } else if (n.isRoot() == false) {
         // # Trace backward from a call site with an affecting parameter.
         n.findBackwardSlices(detector, ideMetInv, null);
      }
   }

   void findVulnerabilityInSlices() {
      // System.out.println("[DBG]\t" + "nodeCount: " + nodeCount);

      for (CFGNode iCaller : mapCFGNodes.values()) {
         if (iCaller.isLeaf()) { // the last caller of vulnerable callee

            List<String> info2 = findVulnerabilityInSlice(iCaller);
            for (String iMsg : info2) {
               if (iMsg.contains(FOUND_VULNERABILITY)) {
//                  detector.appendMsgToView(iMsg);
                  PrintHelper.printDebugMsg(iMsg);
               }
            }
         }
      }
   }

   List<String> findVulnerabilityInSlice(DefaultMutableTreeNode leafNode) {
      List<String> listResult = new ArrayList<String>();
      // String path = "";
      TreeNode[] pathFromRootToLeaf = leafNode.getPath();
      for (TreeNode iNodeInPath : pathFromRootToLeaf) {
         // path += ("\t" + iNodeInPath + "\n");
         listResult.add("\t" + iNodeInPath);
         CFGNode n = (CFGNode) iNodeInPath;
         MethodInvocation callSiteMethInvc = n.getCallSiteMethodInvc();

         if (callSiteMethInvc != null) {
            int offset = callSiteMethInvc.getStartPosition();
            int lineNumber = detector.getLineNumber(offset);
            String signature = UtilAST.getSignature(callSiteMethInvc);
            // path += ("\t\t" + signature + " at line: " + lineNumber + "\n");
            listResult.add("\t\t" + signature + " at line: " + lineNumber);

            SimpleName foundSPName = null;
            LinkedHashSet<SimpleName> listSlices = n.getListSlices();
            for (SimpleName iSPName : listSlices) {
               foundSPName = findVulnerabilityInSlice_GET_BYTES(iSPName);
               if (foundSPName != null) {
                  break;
               }
            }
            if (foundSPName != null) {
               String fQualName = UtilAST.getSignature(foundSPName);
               int lineNum = detector.getLineNumber(foundSPName.getStartPosition());
               // path += ("\t\tFound vulnerability: " + fQualName + " at " + lineNum + "\n");
               listResult.add("\t\t" + FOUND_VULNERABILITY + ": \"" + fQualName + "\" at " + lineNum);

               // CFGNode contains n.getMethodDec() and n.getCallSiteMethodInvc()
               DetectionASTVisitor.getTree().addCFGNode(n, foundSPName, lineNum, Indicator.ROOT);
            }
         }
      }
      return listResult;
   }

   SimpleName findVulnerabilityInSlice_GET_BYTES(SimpleName spName) {
      MethodInvocation metInv = null;
      ASTNode astNode = UtilAST.findMethodInvocationParent(spName);
      if (astNode instanceof MethodInvocation) {
         metInv = (MethodInvocation) astNode;
      } else {
         return null;
      }

      String identifier = metInv.getName().getIdentifier();
      if (identifier.equals(IRuleInfo.GET_BYTES)) {
         Expression expr = metInv.getExpression();

         if (expr instanceof SimpleName) {
            SimpleName spNameVar = (SimpleName) metInv.getExpression();
            String t = UtilAST.getType(spNameVar);
            final String CONST_TYPE_STRING = "String";

            if (t != null && t.equals(CONST_TYPE_STRING)) {
               return spNameVar;
            }
         }
      }
      return null;
   }

   protected int findMatchedParameter(List<?> parameters, SimpleName spName) {
      int index = -1;
      for (int i = 0; i < parameters.size(); i++) {
         Object iObj = parameters.get(i);
         if (iObj instanceof SingleVariableDeclaration) {
            SingleVariableDeclaration svd = (SingleVariableDeclaration) iObj;
            SimpleName varName = svd.getName();

            if (spName.resolveBinding().equals(varName.resolveBinding())) {
               index = i;
               break;
            }
         }
      }
      return index;
   }
}
