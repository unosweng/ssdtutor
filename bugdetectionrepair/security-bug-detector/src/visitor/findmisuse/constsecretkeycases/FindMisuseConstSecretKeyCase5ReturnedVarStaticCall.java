/**
 */
package visitor.findmisuse.constsecretkeycases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashSet;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import controlflowgraph.backward.CFGNode;
import input.IRuleInfo;
import model.MethodElement.Indicator;
import util.PrintHelper;
import util.UtilAST;
import visitor.DetectionASTVisitor;
import visitor.FindMisuse;
import visitor.FindMisuseConstSecretKeyDetector;

/**
 * @since J2SE-1.8 (Java SE 8 [1.8.0_71])
 */
public class FindMisuseConstSecretKeyCase5ReturnedVarStaticCall extends FindMisuse {
   final String FOUND_VULNERABILITY = "Traced ARG1 and found";
   List<SimpleName> classInsArg = new ArrayList<>();
   Map<SimpleName, MethodInvocation> mapVarMthInvoc = new HashMap<SimpleName, MethodInvocation>();
   SimpleName sNameToTrack = null;
   public int POSITION_TO_TRACK = 0;

   public FindMisuseConstSecretKeyCase5ReturnedVarStaticCall(FindMisuseConstSecretKeyDetector detector) {
      this.detector = detector;
   }

   public boolean checkCase5_ReturnedVarStaticCall(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
      sNameToTrack = null;
      mapVarMthInvoc.clear();
      classInsArg.clear();

      List<?> arguments = ciCre.arguments();
      for (Object iObj : arguments) {
         if (iObj instanceof SimpleName) {
            SimpleName iSpName = (SimpleName) iObj;
            classInsArg.add(iSpName);
         }
      }

      metDec.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodInvocation mi) {
            SimpleName returnedVarName = null;
            SimpleName classNameMethodInvoc = null;
            Object parent = mi.getParent();

            // Example ArrayAccess: Util.split(combinedSecrets, 16, 20)[0]
            // get parent of parent to resolve array access
            if (parent instanceof ArrayAccess) {
               parent = mi.getParent().getParent();
            }

            if (parent instanceof VariableDeclarationFragment) {
               VariableDeclarationFragment vdf = (VariableDeclarationFragment) parent;
               returnedVarName = vdf.getName();

               Expression expression = mi.getExpression();
               if (expression instanceof SimpleName) {
                  classNameMethodInvoc = (SimpleName) expression; // static method call
               }
            }
            if (parent instanceof Assignment) {
               Assignment amt = (Assignment) parent;
               Expression leftHandSide = amt.getLeftHandSide();
               if (leftHandSide instanceof SimpleName) {
                  returnedVarName = (SimpleName) leftHandSide;
               }
            }

            for (SimpleName iSName : classInsArg) {
               if (returnedVarName != null //
                     && iSName.resolveBinding().equals(returnedVarName.resolveBinding()) //
                     && classNameMethodInvoc != null) // process a static method call
               {
                  mapVarMthInvoc.put(iSName, mi);
               }
            }
            return true;
         }
      });

      if (!mapVarMthInvoc.isEmpty()) {
         return true;
      }
      return false;
   }

   public Map<SimpleName, MethodInvocation> getMapVarMthInvoc() {
      return mapVarMthInvoc;
   }

   public void findMethodList(SimpleName sName, MethodInvocation iMetInv) {
      List<MethodDeclaration> listMethodDecl = new ArrayList<>();

      // get binding from Method Invocation
      ITypeBinding typeBinding = iMetInv.getExpression().resolveTypeBinding();
      if (typeBinding == null) { // Added null checker at 4/23/2019
         return;
      }
      String className = typeBinding.getTypeDeclaration().getName();
      String pkgName = typeBinding.getPackage().getName();

      // Get Compilation Unit from preprocessor
      CompilationUnit compUnit = UtilAST.getCUnit(pkgName, className);
      if (compUnit == null) {
         return;
      }

      compUnit.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodDeclaration md) {
            listMethodDecl.add(md);
            return true;
         }
      });

      // Used to get the declaring class name
      ITypeBinding declaringClass = typeBinding.getTypeDeclaration();

      // for (MethodDeclaration md : listMethodDecl) {
      // PrintHelper.SecretKeySpec.printMethodFound(declaringClass.getQualifiedName(), md.getName().getFullyQualifiedName(),
      // UtilAST.getLineNum(md.getStartPosition()));
      // }

      // now that we have a list of Fields and Methods
      // we can find all methods that use the member field of the getter method
      for (MethodDeclaration md : listMethodDecl) {
         if (isMatch(md, iMetInv)) {
            md.accept(new ASTVisitor() {
               @Override
               public boolean visit(ReturnStatement node) {
                  Expression expr = node.getExpression();
                  if (expr instanceof SimpleName) {
                     SimpleName sn = (SimpleName) expr;
                     int line = UtilAST.getLineNum(sn.getStartPosition());
                     String msg = PrintHelper.SecretKeySpec.printReturnExpr(declaringClass.getQualifiedName(), md.resolveBinding().getName(), //
                           expr.toString(), line);
                     detector.appendMsgToView("\t\t" + msg);

                     // XXX
                     DetectionASTVisitor.getTree().addMethodDecl(md, sn, line, Indicator.STEP);

                     sNameToTrack = (SimpleName) expr;
                  }
                  return true;
               }
            });

         }
      }

      if (sNameToTrack != null) {
         for (MethodDeclaration md : listMethodDecl) {
            md.accept(new ASTVisitor() {
               @SuppressWarnings("unused")
               @Override
               public boolean visit(VariableDeclarationStatement vds) {
                  List<?> fragments = vds.fragments();
                  for (Object f : vds.fragments()) {
                     if (f instanceof VariableDeclarationFragment) {
                        VariableDeclarationFragment frag = (VariableDeclarationFragment) f;
                        if (frag.resolveBinding().getName().equals(sNameToTrack.resolveBinding().getName())) {
                           int line = UtilAST.getLineNum(vds.getStartPosition());
                           String msg = PrintHelper.SecretKeySpec.printMethodExpr(declaringClass.getQualifiedName(), md.resolveBinding().getName(), //
                                 frag.toString(), line);
                           detector.appendMsgToView("\t\t" + msg);

                           // XXX
                           DetectionASTVisitor.getTree().addMethodDecl(md, frag.getName(), line, Indicator.STEP);
                        }
                     }
                  }

                  return true;
               }

               @Override
               public boolean visit(ExpressionStatement esValue) {
                  if (esValue.getExpression() instanceof Assignment) {
                     Assignment assign = (Assignment) esValue.getExpression();
                     Expression lhs = assign.getLeftHandSide();
                     if (lhs instanceof SimpleName) {
                        SimpleName lhsName = (SimpleName) lhs;
                        if (lhsName.resolveBinding().getName().equals(sNameToTrack.resolveBinding().getName())) {
                           int line = UtilAST.getLineNum(esValue.getStartPosition());
                           String msg = PrintHelper.SecretKeySpec.printMethodAssignment(declaringClass.getQualifiedName(), md.getName().toString(), //
                                 assign.toString(), line);
                           detector.appendMsgToView("\t\t" + msg);

                           // XXX
                           DetectionASTVisitor.getTree().addMethodDecl(md, lhsName, line, Indicator.STEP);
                        }
                     }
                  }
                  return true;
               }
            });
         }
      }
   }

   /**
    * isMatch - checks the inputs match in both name and in parameter count and types
    * 
    * @param md
    * @param iMetInv
    * @return boolean
    */
   private boolean isMatch(MethodDeclaration md, MethodInvocation iMetInv) {

      // if the names don't match - return false
      if (!md.resolveBinding().getName().equals(iMetInv.resolveMethodBinding().getName())) {
         return false;
      }

      ITypeBinding[] p1 = md.resolveBinding().getMethodDeclaration().getParameterTypes();
      ITypeBinding[] p2 = iMetInv.resolveMethodBinding().getMethodDeclaration().getParameterTypes();
      // if the number of parameters don't match - return false
      if (p1.length != p2.length) {
         return false;
      }

      // check that each parameter type matches in order
      boolean parmsMatch = true;
      for (int i = 0; i < p1.length; i++) {
         parmsMatch &= p1[i].getQualifiedName().equalsIgnoreCase(p2[i].getQualifiedName());
      }
      return parmsMatch;
   }

   /**********************************************************************************************************/
   /*
    * Forward Control Flow Graph - Starts Here
    */
   /********************************************************************************************************/

   /**
    * traceFoward - traces forward from methodDeclartion, first finding a methodInvocation from domain specific information
    * 
    * @param md
    * @param mi
    * @param ciCre
    */
   public void traceForward(MethodDeclaration md, MethodInvocation mi, ClassInstanceCreation ciCre) {
      nodeCount = 0;
      mapCFGNodes.clear();

      // use domain specific information (from mapVarMthInvoc)
      // as starting point of AST visitor recursion on MethodInvocations
      PrintHelper.printDebugMsg("traceForward at MD: " + md.resolveBinding().getName());

      IMethodBinding miBind = mi.resolveMethodBinding();
      if (miBind != null) {
         PrintHelper.printDebugMsg("\t starting at MI: " + miBind.getName());
      }

      // set rootMiFound=false on top level calls
      buildCFG(md, mi, false);
      markCallSiteRootCallee(ciCre);

      displayCFG();

      findForwardSlices();
      // findVulnerabilityInSlices();
   }

   /**
    * buildCFG - recursive method to build a control flow graph
    * 
    * @param md
    * @param mi
    * @param rootMiFound
    */
   private void buildCFG(MethodDeclaration md, MethodInvocation mi, boolean rootMiFound) {
      CFGNode callerNode;
      IJavaElement callerJavaElement = md.resolveBinding().getJavaElement();

      if (callerJavaElement instanceof IMethod) {
         IMethod callerIMethod = (IMethod) callerJavaElement;
         callerNode = getCFGNode(callerIMethod, null);
         nodeCount = mapCFGNodes.values().size();

         md.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation node) {
               IMethodBinding calleeResolveMethodBinding = node.resolveMethodBinding();
               if (calleeResolveMethodBinding == null) {
                  return true;
               }

               // check to see if root method invocation has been found or if MethodInvocation bindings match
               if (rootMiFound || calleeResolveMethodBinding.equals(mi.resolveMethodBinding())) {
                  IJavaElement calleeJavaElement = calleeResolveMethodBinding.getJavaElement();
                  if (calleeJavaElement instanceof IMethod) {
                     IMethod calleeIMethod = (IMethod) calleeJavaElement;
                     CFGNode calleeNode = getCFGNode(calleeIMethod, callerNode.getID());

                     if (calleeNode != null) {

                        // XXX
                        int line = detector.getLineNumber(node.getStartPosition());
                        DetectionASTVisitor.getTree().addMethodInvoc(md, node, line, Indicator.INDROOT);

                        callerNode.add(calleeNode);
                        nodeCount = mapCFGNodes.values().size();
                        traceMethodInvocationForward(node, calleeIMethod);
                     }
                  }
               }
               return true;
            }
         });
      }
   }

   /**
    * traceMethodInvocationForward : creates an AST visitor for methodDeclarations of supplied invocation
    * 
    * @param mi
    * @param iMethod
    */
   private void traceMethodInvocationForward(MethodInvocation mi, IMethod iMethod) {
      ICompilationUnit iCompUnit = iMethod.getCompilationUnit();
      if (iCompUnit == null) {
         return;
      }
      CompilationUnit compUnit = UtilAST.parse(iCompUnit);
      compUnit.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodDeclaration node) {
            String mdKey = node.resolveBinding().getKey();
            String miKey = mi.resolveMethodBinding().getKey();

            if (StringUtils.equalsIgnoreCase(mdKey, miKey)) {
               buildCFG(node, null, true); // set to true on recursive calls
            }
            return true;
         }
      });
   }

   void findForwardSlices() {
      for (CFGNode iCaller : mapCFGNodes.values()) {
         if (iCaller.isLeaf()) { // the last caller of vulnerable callee
            TreeNode[] pathFromRootToLeaf = iCaller.getPath();
            for (TreeNode iNodeInPath : pathFromRootToLeaf) {
               defUseAnalysisForwardSlice((CFGNode) iNodeInPath);
            }
         }
      }
   }

   /*
    * defUse Analysis Forward Slice
    */
   private void defUseAnalysisForwardSlice(CFGNode n) {
      if (n.isRoot()) {
         n.findSlices(this.POSITION_TO_TRACK);
      } else {
         n.findSlices(-1);
      }
   }

   void findVulnerabilityInSlices() {
      for (CFGNode iCaller : mapCFGNodes.values()) {
         if (iCaller.isLeaf()) { // the last caller of vulnerable callee

            List<String> info2 = findVulnerabilityInSlice(iCaller);
            for (String iMsg : info2) {
               if (iMsg.contains(FOUND_VULNERABILITY)) {
                  detector.appendMsgToView(iMsg);
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

}
