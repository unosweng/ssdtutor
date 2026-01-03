/**
 */
package visitor.repair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
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

public class JCA2ReplaceVisitor extends DetectionASTVisitor {
   private CompilationUnit astRoot;
   private ASTRewrite rewrite;
   private String searchVal, newVal, referenceVal, referenceType;
   private boolean updated;
   public boolean hasDefinedFunction=false;
   Object reference;
   private MethodElement me;
   private int argPos, referenceLine;
   Map<String, Map<Integer, String>> repairRules = new HashMap<>();


   public JCA2ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
      this.astRoot = astRoot;
      this.rewrite = rewrite;
      this.argPos = me.getArgPos();
      this.me = me;
      this.indicatorClass = me.getIndicatorClassName();
      this.indicatorMethod = me.getIndicatorMethodName();

      ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
      repairRules = ruleParser.getRepairRule();
      Map<Integer, String> indicatorRule = repairRules.get(this.indicatorMethod);

      this.javaFilePath = javaFilePath;
      if (me.getParameters().size()>0) {
         reference = me.getParameters().get(argPos);
      }
      this.newVal = indicatorRule.get(me.getIndicatorPos()+1);
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
         public boolean visit(StringLiteral sn) {
            if(checkVariableDeclaration(sn)) {
               replaceSimpleNameArguments(sn);
            }
            return true;
         }
      });
      return true;
   }
   
   @Override
   public boolean visit(FieldDeclaration fieldDec) {
      fieldDec.accept(new ASTVisitor() {
         @Override
         public boolean visit(VariableDeclarationFragment vdf) {
            System.out.println(vdf);
            if(checkVariableDeclaration(vdf)) {
               replaceSimpleNameArguments(vdf);
            }
            return true;
         }
      });
      return super.visit(fieldDec);
   }

   public boolean visit(MethodDeclaration metDec) {

      metDec.accept(new ASTVisitor() {
         @Override
         public boolean visit(VariableDeclarationFragment vdf) {
            checkVarDefUse(vdf, false);
            return true;
         }

         @Override
         public boolean visit(MethodInvocation metInv) {
            try {
               if (checkMessageDigestGetInstance(metInv)) {
                  replaceStringLiteralArgument(metInv, argPos);
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

   private boolean checkMessageDigestGetInstance(MethodInvocation metInv) {
      String quaName = getQualifiedName(metInv); // typeBinding.getQualifiedName();
      if (quaName == null) {
         return false;
      }
      boolean checkItem = (getLineNumber(metInv.getStartPosition()) == referenceLine);
      String ideMetInv = metInv.getName().getIdentifier();
      if(checkSecureRandom(this.newVal) && (quaName.equals(this.indicatorClass) && ideMetInv.equals(this.indicatorMethod))){
         referenceVal = this.newVal;
         referenceVal = referenceVal.substring(referenceVal.lastIndexOf("-")+1, referenceVal.length());
         ParserSAX ruleParser1 = ParserSAX.getRepairReference(referenceVal);
         referenceVal = ruleParser1.getReferenceClass();
         referenceType = referenceVal.substring(referenceVal.lastIndexOf(".") + 1, referenceVal.length());
         List<?> arguments = metInv.arguments();
         Object iObj = arguments.get(argPos);
         if (iObj instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) iObj;
            if ((mi.getExpression() instanceof SimpleName) && mi.getName().getIdentifier().equals("getBytes")) {
               SimpleName sName = (SimpleName) (mi.getExpression());
               if (sName != null) {
                  replaceSimpleNameArgument(mi, sName);
                  if (!hasDefinedFunction) {
                     replaceMethodInvocationArgument();
                  }
                  return true;
               } 
            }
            else if ((mi.getExpression() instanceof StringLiteral) && mi.getName().getIdentifier().equals("getBytes")) {
               StringLiteral argStr = (StringLiteral) mi.getExpression();
               if (argStr != null) {
                  replaceStringLiteralArgument(mi, argStr);
                  if (!hasDefinedFunction) {
                     replaceMethodInvocationArgument();
                  }
                  return true;
               } 
            }
         } else if (iObj instanceof SimpleName) {
            SimpleName sn = (SimpleName) iObj;
            replaceSimpleNameArgument(sn);
            if (!hasDefinedFunction) {
               replaceMethodInvocationArgument();
            }
            return true;
         } else if (iObj instanceof StringLiteral) {
            return true;
         } else if (iObj instanceof ArrayCreation) {
            ArrayCreation ai = (ArrayCreation) iObj;
            if (!hasDefinedFunction) {
               replaceMethodInvocationArgument();
            }
            replaceArrayArgument(ai);;
            return true;
         }
      }
      else if (checkItem) {
         Map<Integer, String> indicatorRule = repairRules.get(ideMetInv);
         if(indicatorRule!=null) {
            Object value = indicatorRule.get(me.getIndicatorPos()+1);
            if(value.toString().toLowerCase().contains(IGlobalProperty.SECURERANDOM.toLowerCase())) {

            } else {
               this.newVal = indicatorRule.get(me.getIndicatorPos()+1);
               List<?> arguments = metInv.arguments();
               if(arguments.size()>argPos) {
                  Object iObj = arguments.get(argPos);
                  if (iObj instanceof StringLiteral) {
                     String iPar = ((StringLiteral) iObj).getLiteralValue();
                     this.searchVal = iPar;
                     return true;
                  }
                  else if(iObj instanceof SimpleName) {
                     SimpleName iPar = (SimpleName) iObj;
                     SimpleName sName = checkVdfSetContains(iPar, false);
                     if (sName != null) {
                        VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
                        this.searchVal = ((StringLiteral) vdf.getInitializer()).getLiteralValue();
                        return true;
                     }              
                  }
               }
            }
         }
      }
      return false;
   }

   private boolean checkVariableDeclaration(VariableDeclarationFragment vdf) {
      System.out.println(getLineNumber(vdf.getStartPosition()));
      boolean checkItem = (getLineNumber(vdf.getStartPosition()) == referenceLine);
      if(checkItem) {
         this.searchVal =((StringLiteral) vdf.getInitializer()).getLiteralValue();
         return true;
      }
      return false;
   }

   private void replaceSimpleNameArguments(VariableDeclarationFragment vdf) {
      // TODO Auto-generated method stub
      System.out.println(vdf);
      StringLiteral argStr = (StringLiteral) vdf.getInitializer();
      StringLiteral newArg = astRoot.getAST().newStringLiteral();
      String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
      newArg.setLiteralValue(newStr);
      rewrite.replace(argStr, newArg, null);
      this.updated = true;
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
      cic.setType(ast.newSimpleType(ast.newName(referenceType)));

      VariableDeclarationFragment cvdf = ast.newVariableDeclarationFragment();
      cvdf.setName(ast.newSimpleName(IGlobalProperty.SECURERANDOM));
      cvdf.setInitializer(cic);

      VariableDeclarationExpression cvde = ast.newVariableDeclarationExpression(cvdf);
      cvde.setType(ast.newSimpleType(ast.newName(referenceType)));

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
      repairHelper.listReWriteImport(astRoot, rewrite, referenceVal);

      /*********************************************************************************/
      // set the flag
      updated = true;
      hasDefinedFunction = true;

   }

   private boolean checkVariableDeclaration(StringLiteral sn) {
      boolean checkItem = false;
      try {
         StringLiteral referenceValue = (StringLiteral) reference;
         checkItem = (getLineNumber(sn.getStartPosition()) == referenceLine) && (sn.getLiteralValue().equals(referenceValue.getLiteralValue()));
      }
      catch(Exception e) {
         checkItem = (getLineNumber(sn.getStartPosition()) == referenceLine);
      }
      if(checkItem) {
         this.searchVal = sn.getLiteralValue();
         return true;
      } 
      return checkItem;
   }

   private void replaceSimpleNameArguments(StringLiteral sn) {
      StringLiteral newArg = astRoot.getAST().newStringLiteral();
      String newStr = sn.getLiteralValue().replace(this.searchVal, this.newVal);
      newArg.setLiteralValue(newStr);
      rewrite.replace(sn, newArg, null);
      this.updated = true;
   }

   private void replaceStringLiteralArgument(MethodInvocation mi, int argPos) {
      Object arg = mi.arguments().get(argPos);
      if (arg instanceof SimpleName) {
         SimpleName iPar = (SimpleName) arg;
         SimpleName sName = checkVdfSetContains(iPar, false);
         if (sName != null) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
            StringLiteral argStr = (StringLiteral) vdf.getInitializer();
            StringLiteral newArg = astRoot.getAST().newStringLiteral();
            String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
            newArg.setLiteralValue(newStr);
            rewrite.replace(argStr, newArg, null);
            this.updated = true;
         }
      } else if (arg instanceof StringLiteral) {
         StringLiteral argStr = (StringLiteral) arg;
         StringLiteral newArg = astRoot.getAST().newStringLiteral();
         String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
         newArg.setLiteralValue(newStr);
         rewrite.replace(argStr, newArg, null);
         this.updated = true;
      }
   }

   @SuppressWarnings("unchecked")
   private void replaceSimpleNameArgument(MethodInvocation mn, SimpleName sn) {
      AST ast = this.astRoot.getAST();
      MethodInvocation mi = ast.newMethodInvocation();
      mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
      MethodInvocation getBytes = ast.newMethodInvocation();
      getBytes.setName(ast.newSimpleName("getBytes"));
      getBytes.setExpression(ast.newSimpleName(sn.getIdentifier()));
      mi.arguments().add(getBytes);
      rewrite.replace(mn, mi, null);
      updated = true;
   }

   @SuppressWarnings({"unchecked"})
   private void replaceSimpleNameArgument(SimpleName sn) {
      AST ast = this.astRoot.getAST();
      MethodInvocation mi = ast.newMethodInvocation();
      mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
      mi.arguments().add(ast.newSimpleName(sn.getIdentifier()));
      rewrite.replace(sn, mi, null);
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
   private void replaceArrayArgument(ArrayCreation ai) {
      AST ast = this.astRoot.getAST();
      MethodInvocation mi = ast.newMethodInvocation();
      mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
      ArrayCreation arrayCreation = ast.newArrayCreation();
      arrayCreation.setInitializer(ai.getInitializer());
      arrayCreation.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE)));
      arrayCreation.dimensions().add(ast.newNumberLiteral(ai.dimensions().get(0).toString()));
      mi.arguments().add(arrayCreation);
      rewrite.replace(ai, mi, null);
      updated = true;
   }

   private boolean checkSecureRandom(String str) {
      return str.toLowerCase().contains(IGlobalProperty.SECURERANDOM.toLowerCase());
   }

   public boolean isUpdated() {
      return updated;
   }
}
