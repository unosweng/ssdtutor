/**
 */
package visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import input.IGlobalProperty;
import model.MethodElement.Indicator;
import util.ParserSAX;
import util.PrintHelper;
import util.TreeViewerHelper;
import util.UTFile;
import view.CryptoMisuseDetection;
import view.CryptoMisuseDetectionTree;

/**
 * @since JDK1.8
 */
public abstract class DetectionASTVisitor extends ASTVisitor {

   public IPackageFragment[] iPackages;
   public PackageDeclaration pkgDecl = null;
   public TypeDeclaration typeDecl;

   public Map<String, Map<Integer, Map<String, Map<String, String>>>> rules;
   public List<String> SearchPatterns = new ArrayList<>();
   public String indicatorClass;
   public String indicatorMethod;
   public String message;
   public String pkgName;
   public String className;
   public String repairPath, originalPath;
   public String fileName,filePath;
   public int positionToTrack;
   public int argumentPos, indicatorPos;

   public String searchType;
   public Map<String, Map<Integer, Map<String, String>>> searchTypeRules;

   protected String javaFilePath;
   protected String group;

   protected CryptoMisuseDetection cryptoViewer;
   protected CryptoMisuseDetectionTree cryptoTreeViewer;
   protected static TreeViewerHelper tree = new TreeViewerHelper();
   private int detectionCount = 0;

   public Set<VariableDeclarationFragment> setVarDefUse = new HashSet<>();
   public Set<VariableDeclarationFragment> setVarFieldUse = new HashSet<>();
   public Set<ReturnStatement> setReturnUse = new HashSet<>();

   private String argumentString;
   private Set<String> checkParams;

   /************************************************************/
   @Override
   public boolean visit(PackageDeclaration pkgDecl) {
      //		PrintHelper.printDebugMsgH("PackageDeclaration: " + pkgDecl.getName().getFullyQualifiedName());
      this.pkgDecl = pkgDecl;
      return true;
   }

   @Override
   public boolean visit(TypeDeclaration typeDecl) {
      //		PrintHelper.printDebugMsgH("TypeDeclaration: " + typeDecl.getName().getFullyQualifiedName());
      this.typeDecl = typeDecl;
      typeDecl.accept(new ASTVisitor() {

         @Override
         public boolean visit(FieldDeclaration fieldDec) {
            // Check each field declaration
            // PrintHelper.printDebugMsgH("FieldDeclaration: " + fieldDec.toString());

            List<?> fragments = fieldDec.fragments();
            for (Object iObj : fragments) {
               if (iObj instanceof VariableDeclarationFragment) {
                  // Check and save algorithm declaration binding
                  VariableDeclarationFragment vdf = (VariableDeclarationFragment) iObj;
                  checkVarDefUse(vdf, true);
               }
            }
            return true;
         }

         @Override
         public boolean visit(ReturnStatement rs) {
            Expression expr = rs.getExpression();
            if (expr instanceof StringLiteral) {
               StringLiteral strLit = (StringLiteral) expr;
               if (SearchPatterns.contains(strLit.getLiteralValue())) {
                  setReturnUse.add(rs);
               }
            }
            return true;
         }

      });
      return true;
   }

   @Override
   public boolean visit(Initializer init) {

      init.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodInvocation mi) {
            if (checkIndicatorUsage(init, mi, getPositionToTrack())) {
               showResult(init, mi);
            }
            return true;
         }
      });
      return true;
   }

   /************************************************************/

   public String searchValue(String input) {
      String str = StringUtils.upperCase(input);
      switch(this.getSearchType()) {
      case "CONTAINS":
         for (String p : checkParams) {
            if (str.contains(p)) {
               return p;
            }
         }
      case "EQUALS":
         for (String p : checkParams) {
            if (str.equals(p)) {
               return p;
            }
         }
      default:
         return null;
      }
   }

   /************************************************************/

   public SimpleName checkVdfSetContains(SimpleName sName) {
      if (sName == null || sName.resolveBinding() == null) {
         return null;
      }
      IBinding sNameBinding = sName.resolveBinding();
      Set<VariableDeclarationFragment> set = new HashSet<>();
      set = setVarDefUse;
      Iterator<VariableDeclarationFragment> iterator = set.iterator();
      while (iterator.hasNext()) {
         VariableDeclarationFragment vdf = iterator.next();
         SimpleName name = vdf.getName();
         if (sNameBinding.equals(name.resolveBinding())) {
            return name;
         }
      }
      return null;
   }

   public SimpleName checkVdfSetContains(SimpleName sName, boolean fieldUse) {
      if (sName == null || sName.resolveBinding() == null) {
         return null;
      }
      IBinding sNameBinding = sName.resolveBinding();
      Set<VariableDeclarationFragment> set = new HashSet<>();
      if (fieldUse) {
         set = setVarFieldUse;
      } else{
         set = setVarDefUse;
      }
      Iterator<VariableDeclarationFragment> iterator = set.iterator();
      while (iterator.hasNext()) {
         VariableDeclarationFragment vdf = iterator.next();
         SimpleName name = vdf.getName();
         if (sNameBinding.equals(name.resolveBinding())) {
            return name;
         }
      }
      return null;
   }

   public boolean checkWeakRules(Map<String, Map<Integer, Map<String, Map<String, String>>>> classRule, String methodName, int argPos, Expression initializer) {
      if (classRule.get(methodName).size() == 0) {
         return false;
      }
      Map<Integer, Map<String, Map<String, String>>> attributesRules = classRule.get(methodName);
      Map<String, Map<String, String>> attributesRule = attributesRules.get(argPos + 1);
      String expression = initializer.toString().replace('"', ' ').trim().toUpperCase();
      Map<String, String> attributeParams = attributesRule.get(expression);
      if (attributeParams == null) {
         for (String key: attributesRule.keySet()) {
            if (expression.toLowerCase().matches(key.toLowerCase())) {
               attributeParams = attributesRule.get(key.toUpperCase());
            }
         }

         if(attributeParams == null) {
            this.checkParams = attributesRule.keySet();
            expression = searchValue(expression);
            attributeParams = attributesRule.get(expression);
         }
      }
      if (attributeParams ==null) {
         return false;
      }
      if (attributeParams.get("minimum") != null) {
         boolean is_less = Integer.parseInt(expression) < Integer.parseInt(attributeParams.get("minimum"));
         if (is_less) {
            return true;
         }
         return false;
      }
      String paramSecurity = attributeParams.get("security");
      if (paramSecurity != null && !(paramSecurity.toString().equals("strong"))) {
         return true;
      }
      return false;
   }

   public boolean checkWeakRules(Map<String, Map<Integer, Map<String, Map<String, String>>>> classRule, String methodName, int argPos, String expression) {
      if (classRule.get(methodName) == null) {
         return false;
      }
      Map<Integer, Map<String, Map<String, String>>> attributesRules = classRule.get(methodName);
      Map<String, Map<String, String>> attributesRule = attributesRules.get(argPos + 1);
      expression = expression.strip().toUpperCase();
      Map<String, String> attributeParams = attributesRule.get(expression);
      if (attributeParams == null) {
         for (String key: attributesRule.keySet()) {
            if (expression.toLowerCase().matches(key.toLowerCase())) {
               attributeParams = attributesRule.get(key.toUpperCase());
            }
         }
         if(attributeParams == null) {
            this.checkParams = attributesRule.keySet();
            expression = searchValue(expression);
            attributeParams = attributesRule.get(expression);
         }
      }
      if (attributeParams ==null) {
         return false;
      }
      String paramSecurity = attributeParams.get("security");
      if (paramSecurity != null && !(paramSecurity.toString().equals("strong"))) {
         return true;
      }
      return false;
   }

   /**
    * @param vdf
    *           - The VariableDeclarationFragment to inspect
    * @param fieldUse
    *           - boolean to add to variable field use set
    */
   public void checkVarDefUse(VariableDeclarationFragment vdf, boolean fieldUse) {
      Expression initializer = vdf.getInitializer();
      if (fieldUse && initializer == null) {
         setVarFieldUse.add(vdf);
         return;
      }
      if (initializer != null) {
         if (initializer instanceof StringLiteral) {
            setVarDefUse.add(vdf);
         } else if (initializer instanceof ConditionalExpression) {
            ConditionalExpression ce = (ConditionalExpression) initializer;
            Expression thenExpression = ce.getThenExpression();
            Expression elseExpression = ce.getElseExpression();

            // check to see if either then or else expressions are simpleNames
            // and if they are contained in the VARDEF set
            if (thenExpression instanceof SimpleName) {
               SimpleName s = checkVdfSetContains((SimpleName) thenExpression, fieldUse);
               if (s != null) {
                  setVarDefUse.add(vdf);
               }
            }

            if (elseExpression instanceof SimpleName) {
               SimpleName s = checkVdfSetContains((SimpleName) elseExpression, fieldUse);
               if (s != null) {
                  setVarDefUse.add(vdf);
               }
            }
         } else if (initializer instanceof ArrayInitializer) {
            ArrayInitializer ai = (ArrayInitializer) initializer;
            for (Object expr : ai.expressions()) {

               // search types of initializers here
               // not looking for vectors: arrays[][]
               if (expr instanceof StringLiteral) {
                  setVarDefUse.add(vdf);
                  return;
               } else if (expr instanceof NumberLiteral) {
                  setVarDefUse.add(vdf);
                  return;
               } else if (expr instanceof CastExpression) {
                  CastExpression ce = (CastExpression) expr;
                  Expression ex = ce.getExpression();
                  if (ex instanceof NumberLiteral) {
                     setVarDefUse.add(vdf);
                     return;
                  }
               }
            }
         } else if (initializer instanceof NumberLiteral) {
            setVarDefUse.add(vdf);
         } else if (initializer instanceof MethodInvocation) {
            MethodInvocation mi = (MethodInvocation) initializer;
            Expression ex = mi.getExpression();
            if (ex instanceof StringLiteral) {
               setVarDefUse.add(vdf);
            } else if (ex instanceof SimpleName) {
               SimpleName sn = (SimpleName) ex;
               SimpleName sName = checkVdfSetContains(sn, fieldUse);
               if (sName != null) {
                  setVarDefUse.add(vdf);
               }
            }
         } else if (initializer instanceof ArrayCreation) {
            ArrayCreation ac = (ArrayCreation) initializer;
            if(ac.getInitializer() != null) {
               ArrayInitializer ai = (ArrayInitializer) ac.getInitializer();
               for (Object expr : ai.expressions()) {

                  // search types of initializers here
                  // not looking for vectors: arrays[][]
                  if (expr instanceof StringLiteral) {
                     setVarDefUse.add(vdf);
                     return;
                  } else if (expr instanceof NumberLiteral) {
                     setVarDefUse.add(vdf);
                     return;
                  } else if (expr instanceof CastExpression) {
                     CastExpression ce = (CastExpression) expr;
                     Expression ex = ce.getExpression();
                     if (ex instanceof NumberLiteral) {
                        setVarDefUse.add(vdf);
                        return;
                     }
                  }
               }
            }
         }
      }
   }

   public void checkVarDefUseBind(Set<IBinding> setVarDefUseBind, Object iObj, String constValue) {
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) iObj;
      Expression initializer = vdf.getInitializer();
      if (initializer == null) {
         return;
      }
      String strInitializer = initializer.toString().replace("\"", "");
      if (strInitializer.contains(constValue)) {
         setVarDefUseBind.add(vdf.getName().resolveBinding());
      }
   }

   protected boolean check_MethodParm_MethodInvocArgument(MethodDeclaration metDec, MethodInvocation metInv) {
      List<?> arguments = metInv.arguments();
      List<?> parameters = metDec.parameters();

      for (Object arg : arguments) {
         if (arg instanceof SimpleName) {
            SimpleName sArg = (SimpleName) arg;

            for (Object parm : parameters) {
               if (parm instanceof SingleVariableDeclaration) {
                  SingleVariableDeclaration svd = (SingleVariableDeclaration) parm;
                  SimpleName varName = svd.getName();
                  if (sArg.resolveBinding().equals(varName.resolveBinding())) {
                     return true;
                  }
               }
            }
         }
      }
      return false;
   }

   protected boolean check_miArg_FieldMember(MethodInvocation metInv, Boolean field, int posToTrack) {

      if (metInv.arguments() != null && metInv.arguments().size() > 0) {
         Object iObj = metInv.arguments().get(posToTrack);

         if (iObj instanceof SimpleName) {
            SimpleName sName = (SimpleName) iObj;
            SimpleName sFound = checkVdfSetContains(sName, field);
            if (sFound != null) {
               return true;
            }
         }
      }

      return false;
   }

   public static String getQualifiedName(MethodInvocation metInv) {
      if (metInv.resolveMethodBinding() == null //
            || metInv.resolveMethodBinding().getDeclaringClass() == null) {
         return null;
      }
      return metInv.resolveMethodBinding().getDeclaringClass().getQualifiedName();
   }

   public static String getQualifiedName(ClassInstanceCreation ciCre) {
      if (ciCre.resolveConstructorBinding() == null //
            || ciCre.resolveConstructorBinding().getDeclaringClass() == null) {
         return null;
      }

      String quaName = ciCre.resolveConstructorBinding().getDeclaringClass().getQualifiedName();
      return quaName;
   }

   public static String getName(ClassInstanceCreation ciCre) {
      if (ciCre.resolveConstructorBinding() == null //
            || ciCre.resolveConstructorBinding().getDeclaringClass() == null) {
         return null;
      }

      String name = ciCre.resolveConstructorBinding().getDeclaringClass().getName();
      return name;
   }

   /*****************************************************************************************/
   /* SHOW METHODS */

   public void showResult(Initializer init, MethodInvocation mi) {
      String info = this.indicatorClass + "." + this.indicatorMethod + "(" + this.SearchPatterns + ")";
      show(mi, info, Indicator.INDROOT);
   }

   public void showResult(MethodDeclaration metDec, MethodInvocation metInv) {
      String info = this.indicatorClass + "." + this.indicatorMethod + "(" + this.SearchPatterns + ")";
      show(metDec, metInv, info, Indicator.INDROOT);
   }

   public void showResult(MethodDeclaration metDec, ClassInstanceCreation ciCre, String args) {
      String info = this.indicatorClass + args;
      show(metDec, ciCre, info, Indicator.INDROOT);
   }

   public void show(MethodDeclaration md, ClassInstanceCreation ciCre, String info, Indicator ind) {
      try {
         String source = UTFile.readEntireFile(javaFilePath);
         IDocument doc = new Document(source);
         int lineNumber = doc.getLineOfOffset(ciCre.getStartPosition()) + 1;
         String quaName = md.resolveBinding().getDeclaringClass().getQualifiedName();
         String msg = "[DBG] Found at line: " //
               + lineNumber + ", " + quaName + "." + md.getName() + " USE " + info;
         PrintHelper.printDebugMsg(msg);
         PrintHelper.printDebugMsg("      " + this.javaFilePath);
         if (this.cryptoViewer != null) {
            this.cryptoViewer.appendText(msg);
            // Detector will handle building of the tree now
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public void show(MethodDeclaration md, MethodInvocation mi, MethodInvocation mi2, String info, Indicator ind) {
      try {
         String source = UTFile.readEntireFile(javaFilePath);
         IDocument doc = new Document(source);
         int lineNumber = doc.getLineOfOffset(mi2.getStartPosition()) + 1;
         String quaName = md.resolveBinding().getDeclaringClass().getQualifiedName();
         String msg = "[DBG] Found at line: " //
               + lineNumber + ", " + quaName + "." + md.getName() + " USE " + info;

         PrintHelper.printDebugMsg("      " + this.javaFilePath);
         if (this.cryptoViewer != null) {
            this.cryptoViewer.appendText(msg);
            tree.addMethodInvoc(md, mi, mi2, lineNumber, ind);
            updateTreeView();
         }
         if (this.cryptoTreeViewer != null) {
            tree.addMethodInvoc(md, mi, mi2, lineNumber, ind);
            updateTreeView();
         }

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   // Used by ECB, HashFun and SecureRandom Detectors
   public void show(MethodDeclaration metDec, MethodInvocation metInv, String info, Indicator ind) {

      try {
         String source = UTFile.readEntireFile(javaFilePath);
         IDocument doc = new Document(source);
         int lineNumber = doc.getLineOfOffset(metInv.getStartPosition()) + 1;
         String quaName = metDec.resolveBinding().getDeclaringClass().getQualifiedName();
         String msg = "[DBG] Found at line: " //
               + lineNumber + ", " + quaName + "." + metDec.getName() + " USE " + info;

         PrintHelper.printDebugMsg(msg);
         PrintHelper.printDebugMsg("      " + this.javaFilePath);
         if (this.cryptoViewer != null) {
            this.cryptoViewer.appendText(msg);
            // Detector should handle building of the tree now
            // tree.addMethodInvoc(metDec, metInv, lineNumber, ind);
            // updateTreeView();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   public void show(MethodInvocation metInv, String info, Indicator ind) {
      try {
         String source = UTFile.readEntireFile(javaFilePath);
         IDocument doc = new Document(source);
         int lineNumber = doc.getLineOfOffset(metInv.getStartPosition()) + 1;
         String msg = "[DBG] Found at line: " + lineNumber + ", USE " + info;

         PrintHelper.printDebugMsg(msg);
         PrintHelper.printDebugMsg("      " + this.javaFilePath);
         if (this.cryptoViewer != null) {
            this.cryptoViewer.appendText(msg);
            // Detector should handle building of the tree now
            // tree.addMethodInvoc(metDec, metInv, lineNumber, ind);
            // updateTreeView();
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   /*****************************************************************************************/
   /* CHECK INDICATOR USAGE METHODS */

   public boolean checkIndicatorUsage(Initializer init, MethodInvocation metInv, int argPos) {
      String quaName = getQualifiedName(metInv);
      if (quaName == null) {
         return false;
      }

      String ideMetInv = metInv.getName().getIdentifier();
      if (quaName.contains(this.indicatorClass) && ideMetInv.equals(this.indicatorMethod)) {
         Object iObj = metInv.arguments().get(argPos);

         if (iObj instanceof SimpleName) {
            SimpleName iPar = (SimpleName) iObj;
            SimpleName sName = checkVdfSetContains(iPar, false);
            if (sName != null) {

               // first check if sName.parent.initializer instanceof ConditionalExpression
               VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
               if (vdf.getInitializer() instanceof ConditionalExpression) {
                  ConditionalExpression ce = (ConditionalExpression) vdf.getInitializer();
                  Expression thenExpression = ce.getThenExpression();
                  Expression elseExpression = ce.getElseExpression();

                  SimpleName s = null;
                  if (thenExpression instanceof SimpleName) {
                     s = checkVdfSetContains((SimpleName) thenExpression, false);
                  }

                  if (elseExpression instanceof SimpleName) {
                     s = checkVdfSetContains((SimpleName) elseExpression, false);
                  }

                  if (s != null) {
                     sName = s;
                  }
               }

               tree.buildCfgTree(this, init, metInv, sName);
               updateTreeView();
               return true;
            }
            return false;
         } else if (iObj instanceof StringLiteral) {
            String iPar = ((StringLiteral) iObj).getLiteralValue();
            if(searchTypeRules != null) {
               if(searchTypeRules.get(ideMetInv)!= null) {
                  if(searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
                     this.setSearchType(searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
                  }
               }
               else {
                  this.setSearchType("equals");
               }
               boolean isWeak = checkWeakRules(rules, ideMetInv, argPos, iPar);
               if (isWeak) {
                  tree.buildCfgTree(this, init, metInv);
                  updateTreeView();
                  return true;
               }
               //               }
            }
         }
      }

      return false;
   }

   public boolean checkIndicatorUsageA(MethodDeclaration metDec, ClassInstanceCreation cic) {
      String quaName = getQualifiedName(cic);
      if (quaName == null) {
         return false;
      }
      boolean returnVal = false;
      if (quaName.contains(this.indicatorClass)) {
         tree.addIndicatorToList(this, cic);
         List<?> args = cic.arguments();
         String cicString = cic.getType().toString();
         String cicType = cicString.substring(cicString.lastIndexOf('.') + 1);
         for (int argPos=0; argPos<args.size(); argPos++) {
            this.indicatorMethod = cicType;
            this.argumentPos = argPos;
            this.indicatorPos = argPos;
            Object arg = args.get(argPos);
            if (arg instanceof SimpleName) {
               SimpleName iPar = (SimpleName) arg;
               SimpleName sName = checkVdfSetContains(iPar, false);
               if (sName != null) {
                  VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
                  if (searchTypeRules.get(cicType) == null) {
                     continue;
                  }
                  else {
                     if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                        this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                        boolean isWeak = checkWeakRules(rules, cicType, argPos, vdf.getInitializer());
                        if (isWeak) {
                           if (vdf.getInitializer() instanceof ConditionalExpression) {
                              ConditionalExpression ce = (ConditionalExpression) vdf.getInitializer();
                              Expression thenExpression = ce.getThenExpression();
                              Expression elseExpression = ce.getElseExpression();

                              SimpleName s = null;
                              if (thenExpression instanceof SimpleName) {
                                 s = checkVdfSetContains((SimpleName) thenExpression, false);
                              }

                              if (elseExpression instanceof SimpleName) {
                                 s = checkVdfSetContains((SimpleName) elseExpression, false);
                              }

                              if (s != null) {
                                 sName = s;
                              }
                           } else if (vdf.getInitializer() instanceof MethodInvocation) {

                              // Check to see if the SimpleName is part of a initializer expression
                              // byte[] raw = key.getBytes("UTF-8");

                              MethodInvocation mi = (MethodInvocation) vdf.getInitializer();
                              Expression ex = mi.getExpression();
                              if (ex instanceof SimpleName) {
                                 SimpleName s = (SimpleName) ex;
                                 SimpleName sn = checkVdfSetContains(s, false);
                                 if (sn != null) {
                                    sName = sn;
                                 } else {
                                    return false;
                                 }
                              }
                           }

                           tree.buildCfgTree(this, metDec, cic, sName);
                           updateTreeView();
                           returnVal = true;
                        }
                     }
                  }
               }
            } else if (arg instanceof StringLiteral) {
               String iPar = ((StringLiteral) arg).getLiteralValue();
               if (searchTypeRules.get(cicType) == null) {
                  continue;
               }
               else {
                  if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                     this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                     boolean isWeak = checkWeakRules(rules, cicType, argPos, iPar);
                     if (isWeak) {
                        tree.buildCfgTree(this, metDec, cic, null);
                        updateTreeView();
                        returnVal = true;
                     }
                  }
               }
            } else if (arg instanceof MethodInvocation) {
               MethodInvocation mi = (MethodInvocation) arg;
               String identifier = mi.getName().getIdentifier();
               if (identifier.equals("getBytes")) {
                  Expression expr = mi.getExpression();
                  if (expr instanceof SimpleName) {
                     SimpleName s = null;
                     s = checkVdfSetContains((SimpleName) mi.getExpression(), false);
                     if (s != null) {
                        if (searchTypeRules.get(cicType) == null) {
                           continue;
                        }
                        else {
                           if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                              this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                              boolean isWeak = checkWeakRules(rules, cicType, argPos, s);
                              if (isWeak) {
                                 tree.buildCfgTree(this, metDec, cic, s);
                                 updateTreeView();
                                 returnVal = true;
                              }
                           }
                        }
                     }
                  } else if (expr instanceof StringLiteral) {
                     String iPar = ((StringLiteral) expr).getLiteralValue();
                     if (searchTypeRules.get(cicType) == null) {
                        continue;
                     }
                     else {
                        if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                           this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                           boolean isWeak = checkWeakRules(rules, cicType, argPos, iPar);
                           if (isWeak) {
                              tree.buildCfgTree(this, metDec, cic, null);
                              updateTreeView();
                              returnVal = true;
                           }
                        }
                     }
                  }
               }
            } else if (arg instanceof ArrayCreation) {
               tree.buildCfgTree(this, metDec, cic, null);
               updateTreeView();
               returnVal = true;
            } else if (arg instanceof NumberLiteral) {
               NumberLiteral numLit = (NumberLiteral) arg;
               if (searchTypeRules.get(cicType) == null) {
                  continue;
               }
               else {
                  if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                     this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                     boolean isWeak = checkWeakRules(rules, cicType, argPos, numLit);
                     if (isWeak) {
                        tree.buildCfgTree(this, metDec, cic, null);
                        updateTreeView();
                        returnVal = true;
                     }
                  }
               }
            }
         }
      }
      return returnVal;
   }

   public boolean checkIndicatorUsageIV(MethodDeclaration metDec, ClassInstanceCreation cic) {
      String quaName = getQualifiedName(cic);
      if (quaName == null) {
         return false;
      }
      boolean returnVal = false;
      if (quaName.contains(this.indicatorClass)) {
         tree.addIndicatorToList(this, cic);
         List<?> args = cic.arguments();
         String cicString = cic.getType().toString();
         String cicType = cicString.substring(cicString.lastIndexOf('.') + 1);
         for (int argPos=0; argPos<args.size(); argPos++) {
            this.indicatorMethod = cicType;
            this.argumentPos = argPos;
            this.indicatorPos = argPos;
            Object arg = args.get(argPos);
            if (arg instanceof SimpleName) {
               SimpleName iPar = (SimpleName) arg;
               SimpleName sName = checkVdfSetContains(iPar, false);
               if (sName != null) {
                  VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
                  if (searchTypeRules.get(cicType) == null) {
                     continue;
                  }
                  else {
                     if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                        this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                        boolean isWeak = checkWeakRules(rules, cicType, argPos, vdf.getInitializer());
                        if (isWeak) {
                           if (vdf.getInitializer() instanceof ConditionalExpression) {
                              ConditionalExpression ce = (ConditionalExpression) vdf.getInitializer();
                              Expression thenExpression = ce.getThenExpression();
                              Expression elseExpression = ce.getElseExpression();

                              SimpleName s = null;
                              if (thenExpression instanceof SimpleName) {
                                 s = checkVdfSetContains((SimpleName) thenExpression, false);
                              }

                              if (elseExpression instanceof SimpleName) {
                                 s = checkVdfSetContains((SimpleName) elseExpression, false);
                              }

                              if (s != null) {
                                 sName = s;
                              }
                           } else if (vdf.getInitializer() instanceof MethodInvocation) {

                              // Check to see if the SimpleName is part of a initializer expression
                              // byte[] raw = key.getBytes("UTF-8");

                              MethodInvocation mi = (MethodInvocation) vdf.getInitializer();
                              Expression ex = mi.getExpression();
                              if (ex instanceof SimpleName) {
                                 SimpleName s = (SimpleName) ex;
                                 SimpleName sn = checkVdfSetContains(s, false);
                                 if (sn != null) {
                                    sName = sn;
                                 } else {
                                    return false;
                                 }
                              }
                           }

                           tree.buildCfgTree(this, metDec, cic, sName);
                           updateTreeView();
                           returnVal = true;
                        }
                     }
                  }
               }
            } else if (arg instanceof StringLiteral) {
               String iPar = ((StringLiteral) arg).getLiteralValue();
               if (searchTypeRules.get(cicType) == null) {
                  continue;
               }
               else {
                  if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                     this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                     boolean isWeak = checkWeakRules(rules, cicType, argPos, iPar);
                     if (isWeak) {
                        tree.buildCfgTree(this, metDec, cic, null);
                        updateTreeView();
                        returnVal = true;
                     }
                  }
               }
            } else if (arg instanceof MethodInvocation) {
               MethodInvocation mi = (MethodInvocation) arg;
               String identifier = mi.getName().getIdentifier();
               if (identifier.equals("getBytes")) {
                  Expression expr = mi.getExpression();
                  if (expr instanceof SimpleName) {
                     SimpleName s = null;
                     s = checkVdfSetContains((SimpleName) mi.getExpression(), false);
                     if (s != null) {
                        if (searchTypeRules.get(cicType) == null) {
                           continue;
                        }
                        else {
                           if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                              this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                              boolean isWeak = checkWeakRules(rules, cicType, argPos, s);
                              if (isWeak) {
                                 tree.buildCfgTree(this, metDec, cic, s);
                                 updateTreeView();
                                 returnVal = true;
                              }
                           }
                        }
                     }
                  } else if (expr instanceof StringLiteral) {
                     String iPar = ((StringLiteral) expr).getLiteralValue();
                     if (searchTypeRules.get(cicType) == null) {
                        continue;
                     }
                     else {
                        if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                           this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                           boolean isWeak = checkWeakRules(rules, cicType, argPos, iPar);
                           if (isWeak) {
                              tree.buildCfgTree(this, metDec, cic, null);
                              updateTreeView();
                              returnVal = true;
                           }
                        }
                     }
                  }
               }
            } else if (arg instanceof NumberLiteral) {
               NumberLiteral numLit = (NumberLiteral) arg;
               if (searchTypeRules.get(cicType) == null) {
                  continue;
               }
               else {
                  if(searchTypeRules.get(cicType).get(argPos+1) != null) {
                     this.setSearchType(searchTypeRules.get(cicType).get(argPos+1).get("searchType"));
                     boolean isWeak = checkWeakRules(rules, cicType, argPos, numLit);
                     if (isWeak) {
                        tree.buildCfgTree(this, metDec, cic, null);
                        updateTreeView();
                        returnVal = true;
                     }
                  }
               }
            }
         }
      }
      return returnVal;
   }

   /**
    * Check if metInv.arg is contained within the method declaration
    * 
    * @param metDec
    * @param metInv
    * @param argPos
    * @return boolean
    */
   public boolean checkIndicatorUsageA(MethodDeclaration metDec, MethodInvocation metInv) {
      String quaName = getQualifiedName(metInv);
      boolean returnVal = false;
      if (quaName == null) {
         return false;
      }
      String ideMetInv = metInv.getName().getIdentifier();
      tree.addIndicatorToList(this, metInv);
      if (quaName.contains(this.indicatorClass)) {
         for (int argPos = 0; argPos<metInv.arguments().size(); argPos++) {
            this.indicatorMethod = ideMetInv;
            this.argumentPos = argPos;
            this.indicatorPos = argPos;
            Object arg = metInv.arguments().get(argPos);
            if (arg instanceof SimpleName) {
               SimpleName iPar = (SimpleName) arg;
               SimpleName sName = checkVdfSetContains(iPar, false);
               if (sName != null) {
                  VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
                  if (searchTypeRules.get(ideMetInv) == null) {
                     continue;
                  }
                  else {
                     if(searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
                        this.setSearchType(searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
                        boolean isWeak = checkWeakRules(rules, ideMetInv, argPos, vdf.getInitializer());
                        if (isWeak) {
                           if (vdf.getInitializer() instanceof ConditionalExpression) {
                              ConditionalExpression ce = (ConditionalExpression) vdf.getInitializer();
                              Expression thenExpression = ce.getThenExpression();
                              Expression elseExpression = ce.getElseExpression();

                              SimpleName s = null;
                              if (thenExpression instanceof SimpleName) {
                                 s = checkVdfSetContains((SimpleName) thenExpression, false);
                              }

                              if (elseExpression instanceof SimpleName) {
                                 s = checkVdfSetContains((SimpleName) elseExpression, false);
                              }

                              if (s != null) {
                                 sName = s;
                              }
                           }
                           tree.buildCfgTree(this, metDec, metInv, sName);
                           updateTreeView();
                           returnVal = true;
                        }
                     }
                  }
               } 
            } else if (arg instanceof StringLiteral) {
               if(searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
                  this.setSearchType(searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
                  String iPar = ((StringLiteral) arg).getLiteralValue();
                  boolean isWeak = checkWeakRules(rules, ideMetInv, argPos, iPar);
                  if (isWeak) {
                     tree.buildCfgTree(this, metDec, metInv);
                     updateTreeView();
                     returnVal = true;
                  }
               }
            } else if (arg instanceof MethodInvocation) {
               MethodInvocation mi = (MethodInvocation) arg;
               String identifier = mi.getName().getIdentifier();
               if (identifier.equals("getBytes")) {
                  Expression expr = mi.getExpression();
                  if (expr instanceof SimpleName) {
                     SimpleName s = null;
                     s = checkVdfSetContains((SimpleName) mi.getExpression(), false);
                     if (s != null) {
                        VariableDeclarationFragment vdf = (VariableDeclarationFragment) s.getParent();
                        if (searchTypeRules.get(ideMetInv) == null) {
                           continue;
                        }
                        else {
                           if(searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
                              this.setSearchType(searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
                              boolean isWeak = checkWeakRules(rules, ideMetInv, argPos, vdf.getInitializer());
                              if (isWeak) {
                                 tree.buildCfgTree(this, metDec, metInv);
                                 updateTreeView();
                                 returnVal = true;
                              }
                           }
                        }
                     }
                  } else if (expr instanceof StringLiteral) {
                     if(searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
                        this.setSearchType(searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
                        String iPar = ((StringLiteral) expr).getLiteralValue();
                        boolean isWeak = checkWeakRules(rules, ideMetInv, argPos, iPar);
                        if (isWeak) {
                           tree.buildCfgTree(this, metDec, metInv);
                           updateTreeView();
                           returnVal = true;
                        }
                     }
                  }
               }
            }else if (arg instanceof ClassInstanceCreation) {
               List arguments = ((ClassInstanceCreation) arg).arguments();
               if (arguments.size()>0){
                  Object arg0 = arguments.get(0);
                  if (arg0 instanceof SimpleName) {
                     SimpleName sNameArg0 = checkVdfSetContains((SimpleName) arg0, false);
                     if (sNameArg0 != null) {
                        VariableDeclarationFragment vdf = (VariableDeclarationFragment) sNameArg0.getParent();
                        if (searchTypeRules.get(ideMetInv) == null) {
                           continue;
                        }
                        else {
                           if(searchTypeRules.get(ideMetInv).get(argPos+1) != null) {
                              this.setSearchType(searchTypeRules.get(ideMetInv).get(argPos+1).get("searchType"));
                              boolean isWeak =checkWeakRules(rules, ideMetInv, argPos, vdf.getInitializer());
                              if (isWeak) {
                                 tree.buildCfgTree(this, metDec, metInv, sNameArg0);
                                 updateTreeView();
                                 returnVal = true;
                              }
                           }
                        }
                     }
                  }
               } 
            }
         }
      }
      return returnVal;
   }


   /**
    * Check if cic.arg is a method invocation - requires backtracking
    * 
    * @param metDec
    * @param cic
    * @param posToTrack
    * @return boolean
    */
   public boolean checkIndicatorUsageB(MethodDeclaration metDec, ClassInstanceCreation cic, int posToTrack) {
      String quaName = getQualifiedName(cic);
      if (quaName == null) {
         return false;
      }

      if (!quaName.contains(this.indicatorClass)) {
         return false;
      }
      tree.addIndicatorToList(this, cic);
      List<?> args = cic.arguments();
      // check argument
      Object arg0 = args.get(posToTrack);
      // check to see if the argument is a method Invocation ( not IRuleInfo.GET_BYTES )
      if (arg0 instanceof MethodInvocation) {
         MethodInvocation metInv = (MethodInvocation) arg0;
         String identifier = metInv.getName().getIdentifier();
         if (identifier.equals("getBytes")) {
            return false;
         }
         else {
            return true;
         }
      }
      return false;
   }

   /**
    * Check if metInv.arg is a method invocation - requires backtracking
    * 
    * @param metDec
    * @param metInv
    * @param posToTrack
    * @return boolean
    */
   public boolean checkIndicatorUsageB(MethodDeclaration metDec, MethodInvocation metInv, int posToTrack) {
      boolean ret = false;
      String quaName = getQualifiedName(metInv); // typeBinding.getQualifiedName();
      if (quaName == null) {
         return ret;
      }

      //      String ideMetInv = metInv.getName().getIdentifier();
      if (quaName.contains(this.indicatorClass)){
         tree.addIndicatorToList(this, metInv);
         // 1. check to see if the miArg exists in the mdParms
         ret = check_MethodParm_MethodInvocArgument(metDec, metInv);
         if (ret) {
            return true;
         }
         // 2. check to see if the miArg exists as a Field Declaration
         ret = check_miArg_FieldMember(metInv, true, posToTrack);
         if (ret) {
            return true;
         }
      }
      return ret;
   }

   /**
    * @ case #1
    * 
    * <pre>
    * new PBEParameterSpec(a.getBytes(), 10)
    * new PBEParameterSpec(new byte[20], 10)
    * </pre>
    */
   /**
    * Check if ciCre.arg exists in the metDec
    * 
    * @param metDec
    * @param ciCre
    * @return boolean
    */
   //   public boolean checkIndicatorUsagePBE_A(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
   //      String strArg0 = IGlobalProperty.ARG1;
   //      String strArg1 = IGlobalProperty.ARG2;
   //      SimpleName sNameArg0 = null;
   //      SimpleName sNameArg1 = null;
   //      String quaName = getQualifiedName(ciCre);
   //      if (quaName == null) {
   //         return false;
   //      }
   //
   //      if (!quaName.contains(this.indicatorClass)) {
   //         return false;
   //      }
   //
   //      tree.addIndicatorToList(this, ciCre);
   //
   //      List<?> args = ciCre.arguments();
   //
   //      // check argument 1 - salt
   //      Object arg0 = args.get(0);
   //      if (arg0 instanceof ArrayCreation) {
   //         strArg0 = IGlobalProperty.SALT;
   //      } else if (arg0 instanceof SimpleName) {
   //         sNameArg0 = checkVdfSetContains((SimpleName) arg0, IRuleInfo.VarField.VARDEF);
   //         if (sNameArg0 != null) {
   //            strArg0 = IGlobalProperty.SALT;
   //         }
   //      } else if (arg0 instanceof MethodInvocation) {
   //         MethodInvocation metInv = (MethodInvocation) arg0;
   //         String identifier = metInv.getName().getIdentifier();
   //         if (identifier.equals("getBytes")) {
   //            Expression expr = metInv.getExpression();
   //            if (expr instanceof SimpleName) {
   //               sNameArg0 = checkVdfSetContains((SimpleName) metInv.getExpression(), IRuleInfo.VarField.VARDEF);
   //               if (sNameArg0 != null) {
   //                  strArg0 = IGlobalProperty.SALT;
   //               }
   //            } else if (expr instanceof StringLiteral) {
   //               strArg0 = IGlobalProperty.SALT;
   //            }
   //         }
   //      }
   //
   //      // check argument 2 - iteration count
   //      Object arg1 = args.get(1);
   //      if (arg1 instanceof NumberLiteral) {
   //         NumberLiteral numLit = (NumberLiteral) arg1;
   //         if (Integer.valueOf(numLit.getToken()) < IRuleInfo.PBE_MIN_ITERATIONS) {
   //            strArg1 = IGlobalProperty.NOI;
   //         }
   //      } else if (arg1 instanceof SimpleName) {
   //         sNameArg1 = checkVdfSetContains((SimpleName) arg1, IRuleInfo.VarField.VARDEF);
   //         if (sNameArg1 != null) {
   //            if (sNameArg1.getParent() instanceof VariableDeclarationFragment) {
   //               VariableDeclarationFragment vdf = (VariableDeclarationFragment) sNameArg1.getParent();
   //               Expression initializer = vdf.getInitializer();
   //               if (initializer instanceof NumberLiteral) {
   //                  NumberLiteral numLit = (NumberLiteral) initializer;
   //                  if (Integer.valueOf(numLit.getToken()) < IRuleInfo.PBE_MIN_ITERATIONS) {
   //                     strArg1 = IGlobalProperty.NOI;
   //                  }
   //               }
   //            } else {
   //               strArg1 = IGlobalProperty.NOI;
   //            }
   //         }
   //      }
   //
   //      setArgumentString("(" + strArg0 + IGlobalProperty.COLUMN_SEPARATOR + strArg1 + ")");
   //      if (strArg0.contains(IGlobalProperty.SALT) || strArg1.contains(IGlobalProperty.NOI)) {
   //         tree.buildCfgTree(this, metDec, ciCre, sNameArg0, sNameArg1);
   //         updateTreeView();
   //         return true;
   //      }
   //      return false;
   //   }

   /**
    * @ case #2 - determine if backTracking is necessary
    * 
    * <pre>
    * new PBEParameterSpec(metDec.argument, 10)
    * new PBEParameterSpec(fieldDec.argument, 10)
    * new PBEParameterSpec(getSalt(), 10)
    * </pre>
    */
   /**
    * Check if the ciCre.arg(0) is a method invocation
    * 
    * @param ciCre
    * @return boolean
    */
   public boolean checkIndicatorUsagePBE_B(ClassInstanceCreation ciCre) {
      String quaName = getQualifiedName(ciCre);
      if (quaName == null) {
         return false;
      }

      if (!quaName.contains(this.indicatorClass)) {
         return false;
      }

      tree.addIndicatorToList(this, ciCre);

      List<?> args = ciCre.arguments();

      // check argument 1 - salt
      Object arg0 = args.get(0);

      // check to see if the argument is a method Invocation ( not IRuleInfo.GET_BYTES )
      if (arg0 instanceof MethodInvocation) {
         MethodInvocation metInv = (MethodInvocation) arg0;
         String identifier = metInv.getName().getIdentifier();
         if (identifier.equals("getBytes")) {
            return false;
         }
         return true;
      }

      return false;
   }

   /*****************************************************************************************/

   public void updateTreeView() {
      if (this.cryptoViewer != null) {
         tree.updateTreeView(cryptoViewer.getTreeViewer());
      }
      if (this.cryptoTreeViewer != null) {
         tree.updateTreeView(cryptoTreeViewer.getTreeViewer());
      }
   }

   public void setJavaFilePath(String p) {
      this.javaFilePath = p;
   }

   public void setView(CryptoMisuseDetection view) {
      this.cryptoViewer = view;
   }

   public void setTreeView(CryptoMisuseDetectionTree view) {
      this.cryptoTreeViewer = view;
   }

   public void appendMsgToView(String msg) {
      if (this.cryptoViewer != null) {
         this.cryptoViewer.appendText(msg);
      }
   }

   public int getLineNumber(int offset) {
      int lineNumber = 0;
      try {
         String source = UTFile.readEntireFile(javaFilePath);
         Document doc = new Document(source);
         lineNumber = doc.getLineOfOffset(offset) + 1;

      } catch (Exception e) {
         e.printStackTrace();
      }
      return lineNumber;
   }

   public void setiPackages(IPackageFragment[] iPackages) {
      this.iPackages = iPackages;
   }

   public void setTypeDec(TypeDeclaration typeDecl) {
      this.typeDecl = typeDecl;
   }

   public int getDetectionCount() {
      return detectionCount;
   }

   public int incDetectionCount() {
      return ++detectionCount;
   }

   public static TreeViewerHelper getTree() {
      return tree;
   }

   public String getGroup() {
      return group;
   }

   public void setGroup(String group) {
      this.group = group;
   }

   public int getPositionToTrack() {
      return positionToTrack;
   }

   public void setPositionToTrack(int positionToTrack) {
      this.positionToTrack = positionToTrack;
   }

   public String getSearchType() {
      return searchType;
   }

   public void setSearchType(String searchType) {
      this.searchType = searchType;
   }

   public String getPackageName() {
      return pkgName;
   }

   public void setPackageName(String string) {
      this.pkgName = string;
   }

   public String getClassName() {
      return className;
   }

   public void setClassName(String string) {
      this.className = string;
   }

   public String getRepairPath() {
      return repairPath;
   }

   public void setRepairPath(String string) {
      this.repairPath = string;
   }

   public String getOriginalPath() {
      return originalPath;
   }

   public void setFilePath(String string) {
      this.filePath = string;
   }

   public String getFilePath() {
      return filePath;
   }

   public void setOriginalPath(String string) {
      this.originalPath = string;
   }

   public String getArgumentString() {
      return argumentString;
   }

   public void setArgumentString(String argumentString) {
      this.argumentString = argumentString;
   }

   public String getFileName() {
      return fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }
}