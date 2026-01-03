/**
 */
package visitor.findmisuse.constsecretkeycases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import model.MethodElement.Indicator;
import util.PrintHelper;
import util.UTFile;
import util.UtilAST;
import visitor.DetectionASTVisitor;
import visitor.FindMisuse;
import visitor.FindMisuseConstSecretKeyDetector;

/**
 * @since JDK1.8
 */
public class FindMisuseConstSecretKeyCase4ReturnedVar extends FindMisuse {
   DetectionASTVisitor detector;
   List<SimpleName> classInsArg = new ArrayList<SimpleName>();
   Map<SimpleName, MethodInvocation> mapVarMthInvoc = new HashMap<SimpleName, MethodInvocation>();
   QualifiedName qNameToTrack;
   String javaFilePathToTrack;
   IDocument docToTrack;
   public int POSITION_TO_TRACK;

   public FindMisuseConstSecretKeyCase4ReturnedVar(FindMisuseConstSecretKeyDetector detector) {
      this.detector = detector;
   }

   public boolean checkCase4_ReturnedVar(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
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
            SimpleName className_MethodInvoc = null;

            if (mi.getParent() instanceof VariableDeclarationFragment) {
               VariableDeclarationFragment vdf = (VariableDeclarationFragment) mi.getParent();
               returnedVarName = vdf.getName();

               Expression expression = mi.getExpression();
               if (expression != null && expression instanceof SimpleName) {
                  className_MethodInvoc = (SimpleName) expression; // static method call
               }
            }
            if (mi.getParent() instanceof Assignment) {
               Assignment amt = (Assignment) mi.getParent();
               Expression leftHandSide = amt.getLeftHandSide();
               if (leftHandSide instanceof SimpleName) {
                  returnedVarName = (SimpleName) leftHandSide;
               }
            }
            for (SimpleName iSName : classInsArg) {
               if (returnedVarName != null //
                     && iSName.resolveBinding().equals(returnedVarName.resolveBinding()) //
                     && className_MethodInvoc == null) // don't process static method calls
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

   public void checkReturnStmt(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
      for (Entry<SimpleName, MethodInvocation> entry : mapVarMthInvoc.entrySet()) {
         MethodInvocation methInvocToTrack = entry.getValue();
         IMethodBinding iMethodBinding = methInvocToTrack.resolveMethodBinding().getMethodDeclaration();
         IJavaElement javaElement = iMethodBinding.getJavaElement();
         if (javaElement instanceof IMethod) {
            IMethod iMethod = (IMethod) javaElement;

            MethodDeclaration methodDec = UtilAST.findMethodDec(iMethod, detector.typeDecl);
            if (methodDec == null) {
               String n1 = ciCre.arguments().get(POSITION_TO_TRACK).toString();
               String n2 = entry.getKey().getFullyQualifiedName();

               if (StringUtils.equals(n1, n2)) {
                  int line = detector.getLineNumber(entry.getKey().getStartPosition()) - 1;
                  String qName = metDec.resolveBinding().getDeclaringClass().getQualifiedName();
                  String msg = entry + " in " + metDec.getName() + " in " + qName + " at " + line;

                  PrintHelper.printDebugMsg(msg);
                  detector.appendMsgToView("\t\t" + msg);
                  DetectionASTVisitor.getTree().addMethodInvoc(metDec, methInvocToTrack, line, Indicator.ROOT);
               }
               return;
            }
            methodDec.accept(new ASTVisitor() {
               @Override
               public boolean visit(ReturnStatement node) {
                  Expression expr = node.getExpression();
                  if (expr instanceof QualifiedName) {
                     updateViewOnReturnStmt((QualifiedName) expr);
                     qNameToTrack = (QualifiedName) expr;
                     /* Find all statements that affect the field in the class.  */
                     findPackageToTrack();
                  }
                  return true;
               }
            });
         }
      }
   }

   void findPackageToTrack() {
      try {
         for (IPackageFragment iPackage : detector.iPackages) {
            IJavaProject project = iPackage.getJavaProject();
            if (!project.isOpen() || iPackage.getKind() != IPackageFragmentRoot.K_SOURCE //
                  || iPackage.getCompilationUnits().length < 1) {
               continue;
            }
            String pkgNameToTrack = this.qNameToTrack.getQualifier().resolveTypeBinding().getPackage().getName();
            if (pkgNameToTrack.equals(iPackage.getElementName())) {
               findCUnitToTrack(iPackage.getCompilationUnits());
               break;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   void findCUnitToTrack(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
      CompilationUnit comUnit = null;
      for (ICompilationUnit iUnit : iCompilationUnits) {
         String qNameToTrack = this.qNameToTrack.getQualifier().resolveTypeBinding().getName();
         if (UtilAST.contains(iUnit, qNameToTrack)) {
            javaFilePathToTrack = iUnit.getResource().getLocation().toFile().getAbsolutePath();
            comUnit = UtilAST.parse(iUnit);
            break;
         }
      }
      if (comUnit == null) {
         return;
      }
      comUnit.accept(new FieldFiner());
   }

   class FieldFiner extends ASTVisitor {
      Set<IBinding> bindField = new HashSet<IBinding>();

      @Override
      public boolean visit(FieldDeclaration node) {
         SimpleName sName = qNameToTrack.getName();
         List<?> fragments = node.fragments();
         for (Object iObj : fragments) {
            if (iObj instanceof VariableDeclarationFragment) {
               VariableDeclarationFragment vdf = (VariableDeclarationFragment) iObj;
               SimpleName iSName = vdf.getName();
               if (iSName.getIdentifier().equals(sName.getIdentifier())) {
                  bindField.add(iSName.resolveBinding());
                  break;
               }
            }
         }
         return true;
      }

      @Override
      public boolean visit(SimpleName node) {
         if (node.getParent() instanceof VariableDeclarationFragment) {
            return true;
         } else if (node.getParent() instanceof SingleVariableDeclaration) {
            return true;
         }
         IBinding binding = node.resolveBinding();
         if (binding == null) {
            return true;
         }
         if (bindField.contains(binding)) {
            if (node.getParent() instanceof Assignment) {
               MethodDeclaration encloseMethodDecl = findEncloseMethodDecl(node);
               if (encloseMethodDecl == null) {
                  return true;
               }
               String methName = encloseMethodDecl.getName().getIdentifier();
               int line = getLineNumber(node.getStartPosition());
               String fullyQualifiedName = qNameToTrack.getQualifier().resolveTypeBinding().getQualifiedName();
               String msg = node.getParent() + " in " + methName + " in " + fullyQualifiedName + " at " + line;
               PrintHelper.printDebugMsg(msg);
               detector.appendMsgToView("\t\t" + msg);
               DetectionASTVisitor.getTree().addMethodDecl(encloseMethodDecl, node, line, Indicator.ROOT);
            }
         }
         return true;
      }
   }

   MethodDeclaration findEncloseMethodDecl(ASTNode node) {
      if (node instanceof TypeDeclaration) {
         return null; // Not found.
      }
      if (node instanceof MethodDeclaration) {
         MethodDeclaration methodDeclNode = (MethodDeclaration) node;
         return methodDeclNode;
      }
      return findEncloseMethodDecl(node.getParent());
   }

   void updateViewOnReturnStmt(QualifiedName qName) {
      Name qualifier = qName.getQualifier();
      IBinding binding = qualifier.resolveBinding();
      if (binding instanceof IVariableBinding) {
         IVariableBinding iVarBind = (IVariableBinding) binding;
         String qualifiedName = iVarBind.getType().getQualifiedName();
         SimpleName sName = qName.getName();
         int line = detector.getLineNumber(qName.getStartPosition());
         String msg = qualifier + " of " + qualifiedName + " " + sName.getIdentifier() + " at " + line;
         PrintHelper.printDebugMsg(msg);
         detector.appendMsgToView("\t\t" + msg);
         DetectionASTVisitor.getTree().addQualifiedName(qName, line, Indicator.STEP);
      }
   }

   public int getLineNumber(int offset) {
      int lineNumber = 0;
      try {
         if (docToTrack == null) {
            String source = UTFile.readEntireFile(javaFilePathToTrack);
            docToTrack = new Document(source);
         }
         lineNumber = docToTrack.getLineOfOffset(offset) + 1;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return lineNumber;
   }
}