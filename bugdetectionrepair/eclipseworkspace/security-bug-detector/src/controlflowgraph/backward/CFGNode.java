package controlflowgraph.backward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import input.IRuleInfo;
import util.PrintHelper;
import util.UtilAST;
import visitor.DetectionASTVisitor;

/**
 * @since JDK1.8
 */
public class CFGNode extends DefaultMutableTreeNode {
   private static final long serialVersionUID = 1112220L;
   private boolean isRoot = false;
   private boolean isField = false;
   private ASTNode rootCallSite;
   private String IDCallee;
   private String ideMetInv;
   public IMethod iMethodCallee;
   private TypeDeclaration typeDecCallee;
   private MethodDeclaration methodDecCallee;
   public int parmPositionToBackTrack = -1;
   public Map<String, VariableDeclarationFragment> variableDeclFragment;
   private MethodInvocation callSiteMethodInvc;
   private ConstructorInvocation callSiteConstructorInvc;
   private SimpleName sNameToTrack = null;

   protected DetectionASTVisitor detector;

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
      this.typeDecCallee = typeDec;

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

   // attempt to return the MethodDeclaration of the parent
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

   public String getParameters() {
      String paramStr = "";
      List<?> parameters = this.methodDecCallee.parameters();
      if (parameters != null && parameters.isEmpty() == false) {
         for (int i = 0; i < parameters.size(); i++) {
            Object iObj = parameters.get(i);
            if (i == parameters.size() - 1) {
               paramStr += iObj.toString();
            } else {
               paramStr += (iObj.toString() + ", ");
            }
         }
      }
      return paramStr;
   }

   public void findSlices(int pos) {
      if (-1 != pos && this.getRootCallSite() instanceof ClassInstanceCreation) {
         ClassInstanceCreation rootCallSite = (ClassInstanceCreation) this.getRootCallSite();
         Object object = rootCallSite.arguments().get(pos);

         if (object instanceof SimpleName) {
            SimpleName s = (SimpleName) object;
            addSlice(s);
         }
      }

      for (MethodInvocation mi : this.listMethodInv) {
         // System.out.println("mi: " + mi.getName().getFullyQualifiedName());
         mi.accept(new ASTVisitor() {
            @Override
            public boolean visit(SimpleName node) {
               IBinding binding = node.resolveBinding();
               if (binding != null) {
                  addSlice(node);
               }
               return true;
            }
         });
      }
   }

   public void addSlice(SimpleName node) {
      if (!listSlices.contains(node)) {
         listSlices.add(node);
      }
   }

   public void findBackwardSlices(DetectionASTVisitor detector, String ideMetInv, Map<String, VariableDeclarationFragment> variableDecl) {
      int parmPos = -1;
      this.ideMetInv = ideMetInv;
      if (this.isRoot) {
         parmPos = this.parmPositionToBackTrack;
      } else if (this.getParent() != null) {
         TreeNode p = this.getParent();
         CFGNode parent = null;
         if (p instanceof CFGNode) {
            parent = (CFGNode) p;
            parmPos = parent.getParmPositionToBackTrack();
         } else {
            return;
         }
      } else {
         return;
      }
   

      // if no parameter position was found - just return
      if (parmPos == -1) {
         return;
      }
      Object objArg = new Object();
      //      // # Trace backward from a call site with an affecting parameter.
      if (this.callSiteMethodInvc != null && //
            this.callSiteMethodInvc.arguments() != null && //
            this.callSiteMethodInvc.arguments().size() > parmPos) {
         //         objArgs = this.callSiteMethodInvc.arguments();
         objArg = this.callSiteMethodInvc.arguments().get(parmPos);
      } else if (this.callSiteConstructorInvc != null && //
            this.callSiteConstructorInvc.arguments() != null && //
            this.callSiteConstructorInvc.arguments().size() > parmPos) {
         objArg = this.callSiteConstructorInvc.arguments().get(parmPos);
      }
      if (objArg instanceof StringLiteral) {
         // check to see if the string matches our search pattern
         StringLiteral strLiteral = (StringLiteral) objArg;
         detector.indicatorMethod = ideMetInv;
         detector.argumentPos = parmPos;
         if(detector.searchTypeRules.get(ideMetInv) != null) {
            parmPos = 0;
            detector.indicatorPos = parmPos;
            if(detector.searchTypeRules.get(ideMetInv).get(parmPos+1) != null) {
               detector.setSearchType(detector.searchTypeRules.get(ideMetInv).get(parmPos+1).get("searchType"));
               boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, parmPos, strLiteral.getLiteralValue());
               if (isWeak) {
                  // build tree from root to this call Site Invocation
                  PrintHelper.printDebugMsg("Found StringLiteral[" + strLiteral.getLiteralValue() + "] in SearchPatterns" + detector.SearchPatterns);
                  DetectionASTVisitor.getTree().buildCfgTree(detector, this);
               }

               // remove callSite from the list
               this.listMethodInv.remove(this.callSiteMethodInvc);

               if (!this.listMethodInv.isEmpty()) {
                  this.callSiteMethodInvc = this.listMethodInv.get(0);
               }
            }
         }
      } else if (objArg instanceof SimpleName) {
         // case 2 - variable used - collect def-use statements.
         SimpleName varSPName = (SimpleName) objArg;
         if(variableDecl.get(varSPName.resolveBinding().toString())!=null) {
            VariableDeclarationFragment vdf = variableDecl.get(varSPName.resolveBinding().toString());
            detector.indicatorMethod = ideMetInv;
            detector.argumentPos = parmPos;
            if(vdf.getInitializer() instanceof MethodInvocation) {
               MethodInvocation metInv = (MethodInvocation) vdf.getInitializer();
               if(metInv.getExpression() instanceof StringLiteral) {
                  if(detector.searchTypeRules.get(ideMetInv) != null) {
                     parmPos = 0;
                     detector.indicatorPos = parmPos;
                     if(detector.searchTypeRules.get(ideMetInv).get(parmPos+1) != null) {
                        detector.setSearchType(detector.searchTypeRules.get(ideMetInv).get(parmPos+1).get("searchType"));
                        boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, parmPos, vdf.getInitializer());
                        if (isWeak) {
                           // build tree from root to this call Site Invocation
                           PrintHelper.printDebugMsg("Found StringLiteral[" + vdf.getInitializer() + "] in SearchPatterns" + detector.SearchPatterns);
                           DetectionASTVisitor.getTree().buildCfgTree(detector, this);
                        }
                     }
                  }
               }
            }
            else {
               if(detector.searchTypeRules.get(ideMetInv) != null) {
                  parmPos = 0;
                  detector.indicatorPos = parmPos;
                  if(detector.searchTypeRules.get(ideMetInv).get(parmPos+1) != null) {
                     detector.setSearchType(detector.searchTypeRules.get(ideMetInv).get(parmPos+1).get("searchType"));
                     boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, parmPos, vdf.getInitializer());
                     if (isWeak) {
                        // build tree from root to this call Site Invocation
                        PrintHelper.printDebugMsg("Found StringLiteral[" + vdf.getInitializer() + "] in SearchPatterns" + detector.SearchPatterns);
                        DetectionASTVisitor.getTree().buildCfgTree(detector, this);
                     }
                  }
               }
            }
         } else {
            parmPositionToBackTrack = findMatchedParameter( //
                  methodDecCallee.parameters(), varSPName);
            findBackwardSlices(varSPName);
         }
      }
      // case 3 - possibly leaf node - call 'getBytes' and pass the return value.
      else if (objArg instanceof MethodInvocation) {
         MethodInvocation metInv = (MethodInvocation) objArg;
         Expression expr = metInv.getExpression();
         if (expr instanceof SimpleName) {
            SimpleName spName = (SimpleName) metInv.getExpression();
            findBackwardSlices(spName);
         }
         if (expr instanceof StringLiteral) {
            String iPar = ((StringLiteral) expr).getLiteralValue();
            detector.indicatorMethod = ideMetInv;
            detector.argumentPos = parmPos;
            if (detector.searchTypeRules.get(ideMetInv) != null) {
               parmPos = 0;
               detector.indicatorPos = parmPos;
               if(detector.searchTypeRules.get(ideMetInv).get(parmPos+1) != null) {
                  detector.setSearchType(detector.searchTypeRules.get(ideMetInv).get(parmPos+1).get("searchType"));
                  boolean isWeak = detector.checkWeakRules(detector.rules, ideMetInv, parmPos, iPar);
                  if (isWeak) {
                     PrintHelper.printDebugMsg("Found StringLiteral[" + iPar + "] in SearchPatterns" + detector.SearchPatterns);
                     DetectionASTVisitor.getTree().buildCfgTree(detector, this);
                  }
               }
            }
         }
      }
   }

   public void findBackwardSlices(SimpleName spName) {
      this.methodDecCallee.accept(new ASTVisitor() {
         @Override
         public boolean visit(SimpleName node) {
            IBinding binding = node.resolveBinding();
            if (binding != null && binding.equals(spName.resolveBinding())) {
               //               System.out.println("\tadd slice: " + node.getFullyQualifiedName());
               addSlice(node);
            }
            return true;
         }
      });
   }

   public void backTrackFromMethodInvocationArg(DetectionASTVisitor detector, MethodInvocation mi) {

      this.typeDecCallee.accept(new ASTVisitor() {
         @Override
         public boolean visit(MethodInvocation node) {
            IBinding binding = node.resolveMethodBinding();
            if (binding != null && binding.equals(mi.resolveMethodBinding())) {

               IMethodBinding iMethodBinding = mi.resolveMethodBinding().getMethodDeclaration();
               IJavaElement javaElement = iMethodBinding.getJavaElement();
               if (javaElement instanceof IMethod) {
                  IMethod iMethod = (IMethod) javaElement;
                  MethodDeclaration methodDec = UtilAST.findMethodDec(iMethod, detector.typeDecl);
                  HashSet<SimpleName> snSet = new HashSet<>();

                  if (methodDec == null) {
                     return false;
                  }

                  methodDec.accept(new ASTVisitor() {
                     @Override
                     public boolean visit(ReturnStatement rNode) {
                        Expression expr = rNode.getExpression();
                        if (expr instanceof SimpleName) {
                           sNameToTrack = (SimpleName) expr;
                           return true;
                        }
                        return false;
                     }

                     // used to collect a set of StringLiteral assignment to a SimpleName
                     @Override
                     public boolean visit(Assignment assign) {
                        Expression lhs = assign.getLeftHandSide();
                        if (lhs instanceof SimpleName) {
                           SimpleName lhsSn = (SimpleName) lhs;
                           Expression rhs = assign.getRightHandSide();
                           if (rhs instanceof StringLiteral) {
                              // PrintHelper.printDebugMsg("Found Assignment of StringLiteral: " + assign);
                              snSet.add(lhsSn);
                              return true;
                           }
                        }
                        return false;
                     }
                  });

                  if (sNameToTrack != null) {

                     // begin ridiculous logic to find root cause
                     /*
                      * See A133 - ObscuredSharedPreferences.java
                      * new PBEParameterSpec(getSalt(), 2000);
                      * byte[] salt = Arrays.copyOf(id.getBytes(UTF8), 8);
                      * id = "ROBOLECTRICYOUAREBAD";
                      */
                     methodDec.accept(new ASTVisitor() {
                        @Override
                        public boolean visit(VariableDeclarationFragment vdf) {
                           SimpleName sn = vdf.getName();
                           if (sn.getIdentifier().equals(sNameToTrack.getIdentifier())) {
                              Expression init = vdf.getInitializer();
                              if (init instanceof MethodInvocation) {
                                 MethodInvocation m = (MethodInvocation) init;
                                 for (Object arg : m.arguments()) {
                                    if (arg instanceof MethodInvocation) {
                                       MethodInvocation argMi = (MethodInvocation) arg;
                                       SimpleName argSn = argMi.getName();
                                       if (argSn.getFullyQualifiedName().equals(IRuleInfo.GET_BYTES)) {
                                          Expression expr = argMi.getExpression();
                                          if (expr instanceof SimpleName) {
                                             SimpleName esn = (SimpleName) expr;
                                             // now check if esn matches a name assigned to a constant
                                             for (SimpleName s : snSet) {
                                                if (s.resolveBinding().equals(esn.resolveBinding())) {
                                                   buildCFGTree(detector, s);
                                                   return true;
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                           return false;
                        }
                     });

                  }
               }
            }
            return true;
         }
      });
   }

   public void buildCFGTree(DetectionASTVisitor detector, SimpleName s) {
      DetectionASTVisitor.getTree().buildCfgTree(detector, this, s);
   }

   int findMatchedParameter(List<?> parameters, SimpleName spName) {
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

   public String getID() {
      return IDCallee;
   }

   public IMethod getIMethod() {
      return iMethodCallee;
   }

   public TypeDeclaration getTypeDec() {
      return typeDecCallee;
   }

   public void setTypeDec(TypeDeclaration typeDec) {
      this.typeDecCallee = typeDec;
   }

   public MethodDeclaration getMethodDec() {
      return methodDecCallee;
   }

   public void setMethodDec(MethodDeclaration methodDec) {
      this.methodDecCallee = methodDec;
   }

   public List<MethodInvocation> getListMethodInv() {
      return listMethodInv;
   }

   public void setListMethodInv(List<MethodInvocation> listMethodInv) {
      this.listMethodInv = listMethodInv;
   }

   public MethodInvocation getCallSiteMethodInvc() {
      return callSiteMethodInvc;
   }

   public void setCallSiteMethodInvc(MethodInvocation methInvc) {
      this.callSiteMethodInvc = methInvc;
   }

   @Override
   public boolean isRoot() {
      return isRoot;
   }

   public void setRoot(boolean isRoot) {
      this.isRoot = isRoot;
   }

   public ASTNode getRootCallSite() {
      return rootCallSite;
   }

   public void setRootCallSite(ASTNode root) {
      this.rootCallSite = root;
   }

   public int getParmPositionToBackTrack() {
      return parmPositionToBackTrack;
   }

   public void setParmPositionToBackTrack(int parmPositionToBackTrack) {
      this.parmPositionToBackTrack = parmPositionToBackTrack;
   }

   public LinkedHashSet<SimpleName> getListSlices() {
      return listSlices;
   }

   @Override
   public String toString() {
      return this.IDCallee;
   }

   public ConstructorInvocation getCallSiteConstructorInvc() {
      return callSiteConstructorInvc;
   }

   public void setCallSiteConstructorInvc(ConstructorInvocation callSiteConstructorInvc) {
      this.callSiteConstructorInvc = callSiteConstructorInvc;
   }

   public List<ConstructorInvocation> getListConstructorInv() {
      return listConstructorInv;
   }

   public void setListConstructorInv(List<ConstructorInvocation> listConstructorInv) {
      this.listConstructorInv = listConstructorInv;
   }

   public Map<String, VariableDeclarationFragment> getVariableDeclFragment() {
      return variableDeclFragment;
   }

   public void setVariableDeclFragment(Map<String, VariableDeclarationFragment> variableDeclFragment) {
      this.variableDeclFragment = variableDeclFragment;
   }

   public boolean isField() {
      return isField;
   }

   public void setField(boolean isField) {
      this.isField = isField;
   }

   public SimpleName getsNameToTrack() {
      return sNameToTrack;
   }

   public void setsNameToTrack(SimpleName sNameToTrack) {
      this.sNameToTrack = sNameToTrack;
   }

}
