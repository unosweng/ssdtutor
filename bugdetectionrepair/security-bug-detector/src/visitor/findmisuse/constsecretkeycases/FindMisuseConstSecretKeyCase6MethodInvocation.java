/**
 */
package visitor.findmisuse.constsecretkeycases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jface.text.IDocument;

import model.MethodElement.Indicator;
import util.PrintHelper;
import util.UtilAST;
import visitor.DetectionASTVisitor;
import visitor.FindMisuse;
import visitor.FindMisuseConstSecretKeyDetector;

/**
 * @since J2SE-1.8 (Java SE 8 [1.8.0_71])
 */
public class FindMisuseConstSecretKeyCase6MethodInvocation extends FindMisuse {
   DetectionASTVisitor detector;
   SimpleName sNameToTrack = null;
   ClassInstanceCreation ciNameToTrack;
   String javaFilePathToTrack;
   IDocument docToTrack;
   List<MethodInvocation> methodInArgs = new ArrayList<>();
   List<VariableDeclarationStatement> listVarDec = new ArrayList<>();
   List<MethodInvocation> listMetInv = new ArrayList<>();
   Map<VariableDeclarationStatement, MethodInvocation> mapVardecMetinv = new HashMap<>();
   public int POSITION_TO_TRACK = 0;

   public FindMisuseConstSecretKeyCase6MethodInvocation(FindMisuseConstSecretKeyDetector detector) {
      this.detector = detector;
   }

   public boolean checkCase6_MethodInvocation(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
      sNameToTrack = null;
      mapVardecMetinv.clear();
      methodInArgs.clear();
      listVarDec.clear();
      listMetInv.clear();

      // capture method invocation in argument specified by domain specific indicator
      Object iObj = ciCre.arguments().get(this.POSITION_TO_TRACK);
      if (iObj instanceof MethodInvocation) {
         MethodInvocation imInvoc = (MethodInvocation) iObj;
         methodInArgs.add(imInvoc);
      }

      /*
       * if the argument indicated by the domain specific POSITION_TO_TRACK is not an instance of a MethodInvocation, then don't
       * bother visiting
       */
      if (methodInArgs.isEmpty()) {
         return false;
      }

      // methodInArgs contains a MethodInvocation - begin visiting
      metDec.accept(new ASTVisitor() {
         @Override
         public boolean visit(VariableDeclarationStatement vds) {
            // * Collect variable declaration statements above a class instance creation.
            if (vds.getStartPosition() < ciCre.getStartPosition()) {
               listVarDec.add(vds);
            }
            return true;
         }

         @Override
         public boolean visit(MethodInvocation mi) {
            // * Find a method invocation at the class instance creation.
            ASTNode parentMetInvoc = mi.getParent();
            if (parentMetInvoc instanceof ClassInstanceCreation) {
               ClassInstanceCreation parentCiCre = (ClassInstanceCreation) parentMetInvoc;
               if (parentCiCre.resolveTypeBinding().equals(ciCre.resolveTypeBinding())) {
                  listMetInv.add(mi);
               }
            }
            return true;
         }
      });

      // * Def-use relationship between "variable declaration" and "method invocation".
      matchMetInvVarDec();
      return !this.mapVardecMetinv.isEmpty();
   }

   void matchMetInvVarDec() {
      for (VariableDeclarationStatement iVarDec : this.listVarDec) {
         matchMetInvVarDec(iVarDec);
      }
   }

   void matchMetInvVarDec(VariableDeclarationStatement varDec) {
      List<VariableDeclarationFragment> varDecFrgList = new ArrayList<>();
      for (Object iObj : varDec.fragments()) {
         if (iObj instanceof VariableDeclarationFragment) {
            varDecFrgList.add((VariableDeclarationFragment) iObj);
         }
      }

      for (MethodInvocation iMetInv : this.listMetInv) {
         Expression iExpr = iMetInv.getExpression();
         if (iExpr == null) { // cover the case of a direct method invocation as a parameter
            mapVardecMetinv.put(varDec, iMetInv);
         } else if (iExpr instanceof SimpleName) {
            SimpleName varForMetInv = (SimpleName) iExpr;
            for (VariableDeclarationFragment vcf : varDecFrgList) {
               if (vcf != null && vcf.resolveBinding().equals(varForMetInv.resolveBinding())) {
                  mapVardecMetinv.put(varDec, iMetInv);
               }
            }
         }
      }
   }

   public Map<VariableDeclarationStatement, MethodInvocation> getMapVardecMetinv() {
      return mapVardecMetinv;
   }

   public void findMethodList(MethodDeclaration metDec, VariableDeclarationStatement iVarDec, MethodInvocation iMetInv) {
      List<MethodDeclaration> listMethodDecl = new ArrayList<>();
      List<FieldDeclaration> listFieldDecl = new ArrayList<>();

      // Used to get the declaring class name
      Type t2 = iVarDec.getType();
      ITypeBinding declaringClass = t2.resolveBinding().getTypeDeclaration();

      /*************************************************************************/
      CompilationUnit comUnit = null;
      
      // @formatter:off
      if (declaringClass != null && declaringClass.getPackage() != null 
            && iVarDec != null && iVarDec.getType() != null) {
         String pkgName = declaringClass.getPackage().getName();
         String typeStr = iVarDec.getType().toString();
         comUnit = UtilAST.getCUnit(pkgName, typeStr);
      }
      // @formatter:on
      
      if (comUnit == null) {
         // set doc in getCUnit to use getLineNum()

         /**
          * ensure the variable used in the method invocation matches the variableDeclaration fragment found
          */
         if (methodInArgs.get(POSITION_TO_TRACK).getExpression() instanceof SimpleName) {
            sNameToTrack = (SimpleName) methodInArgs.get(POSITION_TO_TRACK).getExpression();
            SimpleName sn = ((VariableDeclarationFragment) iVarDec.fragments().get(0)).getName();
            if (!sn.getFullyQualifiedName().equals(sNameToTrack.getFullyQualifiedName())) {
               return;
            }
         }

         String pkg = metDec.resolveBinding().getDeclaringClass().getPackage().getName();
         String typeDecName = detector.typeDecl.getName().getFullyQualifiedName();
         String qName = metDec.resolveBinding().getDeclaringClass().getQualifiedName();

         UtilAST.getCUnit(pkg, typeDecName);
         int line = UtilAST.getLineNum(iVarDec.getStartPosition());
         VariableDeclarationFragment vdf = (VariableDeclarationFragment) iVarDec.fragments().get(0);
         String msg = PrintHelper.SecretKeySpec.printLocalMethodAssignment( //
               qName, //
               vdf.getName().getFullyQualifiedName(), //
               line);
         detector.appendMsgToView("*\t\t" + msg);
         DetectionASTVisitor.getTree().addMethodDecl(metDec, vdf.getName(), line, Indicator.STEP);
         return;
      }

      acceptCU(comUnit, listMethodDecl, listFieldDecl);
      printFieldsFound(declaringClass, listFieldDecl);
      printMethodsFound(declaringClass, listMethodDecl);
      findMethodReturn(declaringClass, iMetInv, listMethodDecl);

      if (sNameToTrack == null) {
         // collect declarations of the super class
         ITypeBinding superClass = t2.resolveBinding().getSuperclass();
         String pkg = superClass.getPackage().getName();
         String cName = superClass.getJavaElement().getElementName();
         CompilationUnit superClassComUnit = UtilAST.getCUnit(pkg, cName);
         if (superClassComUnit != null) {
            acceptCU(superClassComUnit, listMethodDecl, listFieldDecl);
            printFieldsFound(superClass, listFieldDecl);
            printMethodsFound(superClass, listMethodDecl);
            findMethodReturn(superClass, iMetInv, listMethodDecl);
         }
      }

      if (sNameToTrack != null) {
         for (MethodDeclaration md : listMethodDecl) {
            md.accept(new ASTVisitor() {
               @Override
               public boolean visit(ExpressionStatement esValue) {
                  if (esValue.getExpression() instanceof Assignment) {
                     Assignment assign = (Assignment) esValue.getExpression();
                     Expression lhs = assign.getLeftHandSide();
                     if (lhs instanceof SimpleName) {
                        SimpleName lhsName = (SimpleName) lhs;
                        if (lhsName.resolveBinding().getName().equals(sNameToTrack.resolveBinding().getName())) {

                           // reset CUnit.doc to print correct line number
                           String pkg = md.resolveBinding().getDeclaringClass().getPackage().getName();
                           String dcName = md.resolveBinding().getDeclaringClass().getName();
                           CompilationUnit cUnit = UtilAST.getCUnit(pkg, dcName);
                           IJavaElement javaElement = cUnit.getJavaElement();
                           if (javaElement == null) {
                              javaElement = declaringClass.getJavaElement();
                           }

                           int line = UtilAST.getLineNum(esValue.getStartPosition());
                           String msg = PrintHelper.SecretKeySpec.printMethodAssignment(//
                                 declaringClass.getQualifiedName(), md.getName().toString(), //
                                 assign.toString(), line);
                           detector.appendMsgToView("\t\t" + msg);

                           DetectionASTVisitor.getTree().addMethodDecl(md, javaElement, lhsName, line, Indicator.ROOT);
                        }
                     }
                  }
                  return true;
               }
            });
         }
      } else { // sNameToTrack is null - look in super class instead
         System.out.println("\tDid not find sNameToTrack:" + sNameToTrack);
      }
   }

   private void findMethodReturn(ITypeBinding declaringClass, MethodInvocation iMetInv, List<MethodDeclaration> listMethodDecl) {
      // now that we have a list of Fields and Methods
      // we can find all methods that use the member field of the getter method
      for (MethodDeclaration md : listMethodDecl) {
         if (md.resolveBinding().getName().equals(iMetInv.resolveMethodBinding().getName())) {
            md.accept(new ASTVisitor() {
               @Override
               public boolean visit(ReturnStatement node) {
                  Expression expr = node.getExpression();
                  if (expr instanceof SimpleName) {
                     String msg = PrintHelper.SecretKeySpec.printMethodExpr(declaringClass.getQualifiedName(), md.resolveBinding().getName(), //
                           expr.toString(), UtilAST.getLineNum(md.getStartPosition()));
                     detector.appendMsgToView("\t\t" + msg);
                     sNameToTrack = (SimpleName) expr;
                  }
                  return true;
               }
            });

         }
      }
   }

   private void printMethodsFound(ITypeBinding declaringClass, List<MethodDeclaration> listMethodDecl) {
      for (MethodDeclaration md : listMethodDecl) {
         PrintHelper.SecretKeySpec.printMethodFound(declaringClass.getQualifiedName(), md.getName().getFullyQualifiedName(), UtilAST.getLineNum(md.getStartPosition()));
      }
   }

   private void printFieldsFound(ITypeBinding declaringClass, List<FieldDeclaration> listFieldDecl) {
      for (FieldDeclaration fd : listFieldDecl) {
         PrintHelper.SecretKeySpec.printFieldFound(declaringClass.getQualifiedName(), fd.toString(), UtilAST.getLineNum(fd.getStartPosition()));
      }
   }

   private void acceptCU(CompilationUnit comUnit, List<MethodDeclaration> listMethodDecl, List<FieldDeclaration> listFieldDecl) {
      comUnit.accept(new ASTVisitor() {
         @Override
         public boolean visit(FieldDeclaration fd) {
            listFieldDecl.add(fd);
            return true;
         }

         @Override
         public boolean visit(MethodDeclaration md) {
            listMethodDecl.add(md);
            return true;
         }
      });
   }
}
