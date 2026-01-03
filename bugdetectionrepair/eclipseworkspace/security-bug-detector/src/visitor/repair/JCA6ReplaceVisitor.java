/**
 */
package visitor.repair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import input.IGlobalProperty;
import model.MethodElement;
import util.ParserSAX;
import util.RepairHelper;
import visitor.DetectionASTVisitor;

public class JCA6ReplaceVisitor extends DetectionASTVisitor {
   private CompilationUnit astRoot;
   private ASTRewrite rewrite;
   private boolean updated;
   private int argPos, referenceLine;
   public boolean hasDefinedFunction=false;
   Map<String, Map<Integer, String>> repairRules = new HashMap<>();

   public JCA6ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
      this.astRoot = astRoot;
      this.rewrite = rewrite;
      this.argPos = me.getArgPos();
      this.indicatorClass = me.getIndicatorClassName();

      ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
      repairRules = ruleParser.getRepairRule();

      this.javaFilePath = javaFilePath;
      referenceLine = me.getLineNumber();
   }

   @Override
   public boolean visit(PackageDeclaration pkgDecl) {
      return super.visit(pkgDecl);
   }

   /**
    * A type declaration is the union of a class declaration and an interface declaration.
    */
   @Override
   public boolean visit(TypeDeclaration typeDecl) {
      typeDecl.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodDeclaration metDec) {
            if(metDec.getName().getFullyQualifiedName().equals(IGlobalProperty.GET_RANDOM_SECRET_KEY)) {
               hasDefinedFunction = true;
            }
            return true;
         }
      });
      return super.visit(typeDecl);
   }

   public boolean visit(FieldDeclaration fieldDec) {
      return super.preVisit2(fieldDec);
   }

   public boolean visit(MethodDeclaration metDec) {

      metDec.accept(new ASTVisitor() {
         public boolean visit(VariableDeclarationFragment vdf) {
            return super.preVisit2(vdf);
         }

         public boolean visit(MethodInvocation metInv) {
            try {
               if (checkSecureRandomSetSeed(metInv)) {
                  return true;
               }
            } catch (Exception e) {
               System.out.println("[ERR] " + metDec.getName());
               e.printStackTrace();
            }
            return true;
         }
      });
      return true;
   }

   private boolean checkSecureRandomSetSeed(MethodInvocation metInv) {
      String quaName = getQualifiedName(metInv);
      if (quaName == null) {
         return false;
      }

      boolean checkItem = (getLineNumber(metInv.getStartPosition()) == referenceLine);
      String ideMetInv = metInv.getName().getIdentifier();
      if ((quaName.equals(this.indicatorClass)) || checkItem) {
         Map<Integer, String> indicatorRule = repairRules.get(ideMetInv);
         if(metInv.arguments().size() == 0) {
            MethodInvocation mi = (MethodInvocation) metInv;
            if((mi.getExpression() instanceof StringLiteral) && mi.getName().getIdentifier().equals("getBytes")) {
               if (metInv.getExpression() instanceof StringLiteral) {
                  StringLiteral argStr = (StringLiteral) mi.getExpression();
                  if (argStr != null) {
                     replaceStringLiteralArgument(mi, argStr);
                     if (!hasDefinedFunction) {
                        replaceMethodInvocationArgument();
                     }
                     return true;
                  }
                  System.out.println((SimpleName) (metInv.getName()));
                  return true;
               }
            }
         } else {
            Object obj = metInv.arguments().get(this.argPos);

            if(!(indicatorRule==null)) {
               if (obj instanceof MethodInvocation) {
                  MethodInvocation mi = (MethodInvocation) obj;
                  if ((mi.getExpression() instanceof SimpleName) && mi.getName().getIdentifier().equals("getBytes")) {
                     if (metInv.getExpression() instanceof SimpleName) {
                        SimpleName sName = (SimpleName) (metInv.getExpression());
                        replaceMethodInvocationArgument(sName, mi);
                        return true;
                     } 
                  } else if((mi.getExpression() instanceof StringLiteral) && mi.getName().getIdentifier().equals("getBytes")) {
                     if (metInv.getExpression() instanceof SimpleName) {
                        SimpleName sName = (SimpleName) (metInv.getExpression());
                        replaceMethodInvocationArgument(sName, mi);
                        return true;
                     }
                  }
               }
            }
         }
      }  
      return false;
   }

   @SuppressWarnings("unchecked")
   private void replaceMethodInvocationArgument(SimpleName sName, MethodInvocation mi) {
      AST ast = astRoot.getAST();

      MethodInvocation generateSeed = ast.newMethodInvocation();
      generateSeed.setName(ast.newSimpleName(IGlobalProperty.GENERATE_SEED));
      NumberLiteral seedLen = ast.newNumberLiteral(String.valueOf(IGlobalProperty.SET_SEED_MIN_LEN));
      generateSeed.arguments().add(seedLen);
      generateSeed.setExpression(ast.newSimpleName(sName.getFullyQualifiedName()));

      rewrite.replace(mi, generateSeed, null);
      updated = true;
   }

   @SuppressWarnings({"unchecked"})
   private void replaceStringLiteralArgument(MethodInvocation mn, StringLiteral argStr) {
      AST ast = this.astRoot.getAST();
      MethodInvocation mi = ast.newMethodInvocation();
      mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
      MethodInvocation getBytes = ast.newMethodInvocation();
      getBytes.setName(ast.newSimpleName("getBytes"));
      mi.arguments().add(getBytes);
      StringLiteral args = ast.newStringLiteral();
      args.setLiteralValue(argStr.getLiteralValue());
      getBytes.setExpression(args);
      rewrite.replace(mn, mi, null);
      updated = true;
   }

   @SuppressWarnings("unchecked")
   private void replaceMethodInvocationArgument() {
      AST ast = astRoot.getAST();

      /*********************************************************************************/
      // create the new method declaration 
      // NOTE: no name-space checking is done here - potential for conflict
      MethodDeclaration md = ast.newMethodDeclaration();
      md.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
      md.setReturnType2(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE), 1));

      Block body = ast.newBlock();
      md.setBody(body);

      // add byte parameter to be made random
      List<Object> parameters = md.parameters();
      SingleVariableDeclaration variable = ast.newSingleVariableDeclaration();
      variable.setName(ast.newSimpleName(IGlobalProperty.VAR1));
      variable.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE)));
      parameters.add(variable);

      // add public static modifiers
      List<Modifier> modifiers = md.modifiers();
      modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
      modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));

      MethodInvocation mi = ast.newMethodInvocation();
      mi.setName(ast.newSimpleName("clone"));
      mi.setExpression(ast.newSimpleName(IGlobalProperty.VAR1));

      VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
      vdf.setName(ast.newSimpleName(IGlobalProperty.VAR2));
      vdf.setInitializer(mi);

      VariableDeclarationExpression vde = ast.newVariableDeclarationExpression(vdf);
      vde.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE), 1));

      /*********************************************************************************/
      // SecureRandom secureRandom = new SecureRandom();
      ClassInstanceCreation cic = ast.newClassInstanceCreation();
      cic.setType(ast.newSimpleType(ast.newName("SecureRandom")));

      VariableDeclarationFragment cvdf = ast.newVariableDeclarationFragment();
      cvdf.setName(ast.newSimpleName(IGlobalProperty.SECURERANDOM));
      cvdf.setInitializer(cic);

      VariableDeclarationExpression cvde = ast.newVariableDeclarationExpression(cvdf);
      cvde.setType(ast.newSimpleType(ast.newName("SecureRandom")));

      /*********************************************************************************/
      // secureRandom.nextBytes(Var2);
      MethodInvocation mi2 = ast.newMethodInvocation();
      mi2.setName(ast.newSimpleName("nextBytes"));
      mi2.setExpression(ast.newSimpleName(IGlobalProperty.SECURERANDOM));
      mi2.arguments().add(ast.newSimpleName(IGlobalProperty.VAR2));

      /*********************************************************************************/
      // create return statement
      ReturnStatement ret = ast.newReturnStatement();
      ret.setExpression(ast.newSimpleName(IGlobalProperty.VAR2));

      /*********************************************************************************/
      // add statements to body
      body.statements().add(ast.newExpressionStatement(vde));
      body.statements().add(ast.newExpressionStatement(cvde));
      body.statements().add(ast.newExpressionStatement(mi2));
      body.statements().add(ret);

      /*********************************************************************************/
      // add the new Method Declaration to the end of the Type Declaration 
      TypeDeclaration td = (TypeDeclaration) astRoot.types().get(0);
      ListRewrite listRewrite = rewrite.getListRewrite(td, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
      listRewrite.insertLast(md, null);

      /*********************************************************************************/
      // add the new import for java.security.SecureRandom
      RepairHelper repairHelper = new RepairHelper(null, null);
      repairHelper.listReWriteImport(astRoot, rewrite, "java.security.SecureRandom");

      /*********************************************************************************/
      // set the flag
      updated = true;
      hasDefinedFunction = true;
   }

   public boolean isUpdated() {
      return updated;
   }
}
