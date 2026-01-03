package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import javax.swing.tree.TreeNode;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import analysis.ProjectAnalysis;
import controlflowgraph.backward.CFGNode;
import graph.builder.GModelBuilder;
import graph.model.GConnection;
import graph.model.GNode;
import graph.model.node.GIndicatorNode;
import graph.model.node.GPathNode;
import graph.model.node.GRootNode;
import input.IGlobalProperty;
import model.BatchProcessing;
import model.MethodElement;
import model.MethodElement.Indicator;
import model.ProgElementModelProvider;
import model.ProgramElement;
import view.CryptoMisuseDetectionTree;
import view.progelement.ContentProviderProgElem;
import view.progelement.ContentProviderTree;
import view.progelement.LabelProviderIndicator;
import view.progelement.LabelProviderLineNumber;
import view.progelement.LabelProviderMethodParm;
import view.progelement.LabelProviderProgElem;
import view.progelement.LabelProviderTree;
import visitor.DetectionASTVisitor;

public class TreeViewerHelper {

   private static HashSet<ProgramElement> pkgElements = new HashSet<>();
   private static HashSet<ProgramElement> classElements = new HashSet<>();
   private static HashSet<MethodElement> methodElements = new HashSet<>();
   private static HashSet<String> treeList = new HashSet<>();
   private static Map<String, MethodElement> patternConflict = new HashMap<String, MethodElement>();
   private static HashSet<String> indicatorList = new HashSet<>();
   private List<GNode> treeNodes = new ArrayList<GNode>();
   public static ProgramElement addProgramElement(String name) {
      // create a new PE
      ProgramElement pe = new ProgramElement(name);
      ProgramElement pre = ProgElementModelProvider.INSTANCE.addProgramElement(pe);
      if (pre != null) {
         pe = pre;
      }
      return pe;
   }
   static int i = 0;

   // Add a package declaration to the list of pkgElements
   public static ProgramElement addPkgDecl(PackageDeclaration pd) {
      String pkgName = (pd == null) ? "default" : pd.getName().getFullyQualifiedName();
      ProgramElement pkgPE = findPackageElement(pkgName);
      pkgElements.add(pkgPE);
      return pkgPE;
   }

   // Add a type declaration to the list of classElements
   public static void addTypeDecl(TypeDeclaration td) {

      // get the name
      String className = td.getName().getIdentifier();

      ProgramElement parent = null;
      String pkgName = null;
      // get the parent
      if (td.getParent() instanceof CompilationUnit) {
         CompilationUnit cu = (CompilationUnit) td.getParent();
         PackageDeclaration cuPkg = cu.getPackage();
         parent = addPkgDecl(cuPkg);
      } else if (td.getParent() instanceof PackageDeclaration) {
         PackageDeclaration ppd = (PackageDeclaration) td.getParent();
         parent = addPkgDecl(ppd);
      } else if (td.getParent() instanceof TypeDeclaration) { // subclass
         TypeDeclaration ptd = (TypeDeclaration) td.getParent();
         String parentName = ptd.getName().getIdentifier();
         addTypeDecl(ptd);
         if(td.getParent().getParent() instanceof CompilationUnit) {
            CompilationUnit cu = (CompilationUnit) td.getParent().getParent();
            pkgName = cu.getPackage().getName().getFullyQualifiedName();
         } else if (td.getParent() instanceof PackageDeclaration) {
            PackageDeclaration ppd = (PackageDeclaration) td.getParent();
            pkgName = ppd.getName().getFullyQualifiedName();
         }
         parent = findClassElement(parentName, pkgName);
      }
      if (parent != null) {
         // add the className to the parent package program element
         ProgramElement classPE = findClassElement(className, parent);
         // check before adding child to the parent
         if (!parent.getListChildren().contains(classPE)) {
            parent.add(classPE);
            classElements.add(classPE);
         }
      } else {
         System.out.println("WARNING: addTypeDecl() - no parent found for td:" + td.getName().getIdentifier());
      }
   }

   public static MethodElement addStaticInitializer(Initializer init, IJavaElement javaElem) {
      MethodElement methodElem = null;
      String parentName = null;

      // get the root TypeDeclaration
      ASTNode pNode = init.getParent();
      while (pNode != null && !(pNode instanceof TypeDeclaration)) {
         pNode = pNode.getParent();
      }

      TypeDeclaration tdp = (TypeDeclaration) pNode;
      parentName = tdp.getName().getIdentifier();
      addTypeDecl(tdp);

      // find the parent package element
      ProgramElement parent = findClassElement(parentName);

      // add the method
      methodElem = new MethodElement("staticInitializer", parent);
      List<?> x = new ArrayList<>();
      methodElem.setParameters(x);
      methodElem.setJavaElement(javaElem);
      methodElements.add(methodElem);
      parent.add(methodElem);

      return methodElem;
   }

   public static MethodElement addMethodDecl(MethodDeclaration md, IJavaElement javaElem) {

      if (javaElem == null) {
         IMethodBinding binding = md.resolveBinding();
         javaElem = binding.getJavaElement();
      }

      MethodElement methodElem = null;
      String parentName = null;

      // get the name of the method declaration
      String methodName = md.getName().getIdentifier();

      // get the root TypeDeclaration
      ASTNode pNode = md.getParent();
      while (pNode != null && !(pNode instanceof TypeDeclaration)) {
         pNode = pNode.getParent();
      }

      TypeDeclaration tdp = (TypeDeclaration) pNode;
      parentName = tdp.getName().getIdentifier();

      addTypeDecl(tdp);
      // find the parent package element
      ProgramElement parent = findClassElement(parentName);
      // add the method
      methodElem = findMethodElement(methodName, parent);
      methodElem.setParameters(md.parameters());
      methodElem.setJavaElement(javaElem);
      if (!parent.getListChildren().contains(methodElem)) {
         parent.add(methodElem);
         methodElements.add(methodElem);
      }

      return methodElem;
   }

   public static MethodElement addMethodDecl(MethodDeclaration md, IJavaElement javaElem, String pkgName) {

      if (javaElem == null) {
         IMethodBinding binding = md.resolveBinding();
         javaElem = binding.getJavaElement();
      }

      MethodElement methodElem = null;
      String parentName = null;

      // get the name of the method declaration
      String methodName = md.getName().getIdentifier();

      // get the root TypeDeclaration
      ASTNode pNode = md.getParent();
      while (pNode != null && !(pNode instanceof TypeDeclaration)) {
         pNode = pNode.getParent();
      }
      TypeDeclaration tdp = (TypeDeclaration) pNode;
      parentName = tdp.getName().getIdentifier();

      addTypeDecl(tdp);

      ProgramElement parent = findClassElement(parentName);
      //		System.out.println(parent.getName());
      List<String> parentParams = new ArrayList<String>();
      parentParams.add(pkgName);

      while (pNode.getParent() instanceof TypeDeclaration) {
         pNode = pNode.getParent();
         TypeDeclaration td = (TypeDeclaration) pNode;
         String p1 = td.getName().getIdentifier();
         parentParams.add(p1);
      }

      if (parentParams.size()>1) {
         parent = findClassElement(parentName, parentParams);
      }
      // find the parent package element
      // add the method
      methodElem = findMethodElement(methodName, parent);
      methodElem.setParameters(md.parameters());
      methodElem.setJavaElement(javaElem);
      if (!parent.getListChildren().contains(methodElem)) {
         parent.add(methodElem);
         methodElements.add(methodElem);
      }

      return methodElem;
   }

   public String convertListToString(List<?> list) {

      List<String> newList = new ArrayList<>();
      for (Object item : list) {
         newList.add(item.toString());
      }

      return newList.stream().collect(Collectors.joining(" | "));
   }

   public void addIndicatorToList(DetectionASTVisitor detector, Object obj) {
      // ORDER : group, path, indName, args, line

      StringBuilder sb = new StringBuilder();
      String sep = IGlobalProperty.COLUMN_SEPARATOR;

      sb.append(detector.getGroup());
      sb.append(sep);

      if (obj instanceof MethodInvocation) {
         MethodInvocation mi = (MethodInvocation) obj;

         // get the root TypeDeclaration
         ASTNode pNode = mi.getParent();
         while (pNode != null && !(pNode instanceof TypeDeclaration) && !(pNode instanceof EnumDeclaration)) {
            pNode = pNode.getParent();
         }

         IJavaElement javaElement = null;
         if (pNode instanceof TypeDeclaration) {
            TypeDeclaration tdp = (TypeDeclaration) pNode;
            javaElement = tdp.resolveBinding().getJavaElement();
         } else if (pNode instanceof EnumDeclaration) {
            EnumDeclaration edp = (EnumDeclaration) pNode;
            javaElement = edp.resolveBinding().getJavaElement();
         } else {
            PrintHelper.printErrorMsg("Unknown instance type: " + pNode);
         }

         sb.append(javaElement.getPath().toOSString());
         sb.append(sep);

         String name = buildIndNodeName(mi);
         sb.append(name);
         sb.append(sep);

         sb.append(convertListToString(mi.arguments()));
         sb.append(sep);

         int lineNumber = detector.getLineNumber(mi.getStartPosition());
         sb.append(Integer.toString(lineNumber));
         sb.append(sep);

      } else if (obj instanceof ClassInstanceCreation) {
         ClassInstanceCreation cic = (ClassInstanceCreation) obj;

         ASTNode pNode = cic.getParent();
         while (pNode != null && !(pNode instanceof TypeDeclaration) && !(pNode instanceof EnumDeclaration)) {
            pNode = pNode.getParent();
         }

         IJavaElement javaElement = null;
         if (pNode instanceof TypeDeclaration) {
            TypeDeclaration tdp = (TypeDeclaration) pNode;
            javaElement = tdp.resolveBinding().getJavaElement();
         } else if (pNode instanceof EnumDeclaration) {
            EnumDeclaration edp = (EnumDeclaration) pNode;
            javaElement = edp.resolveBinding().getJavaElement();
         } else {
            PrintHelper.printErrorMsg("Unknown instance type: " + pNode);
         }

         sb.append(javaElement.getPath().toOSString());
         sb.append(sep);

         String name = buildIndNodeName(cic);
         sb.append(name);
         sb.append(sep);

         sb.append(convertListToString(cic.arguments()));
         sb.append(sep);

         int lineNumber = detector.getLineNumber(cic.getStartPosition());
         sb.append(Integer.toString(lineNumber));
         sb.append(sep);

      }

      indicatorList.add(sb.toString());
   }

   public void addMethodInvoc(Initializer init,  DetectionASTVisitor detector, String group, //
         String rootNodeName, int rootLineNumber, IJavaElement rootJavaElement, List<?> rootArgs, //
         String indNodeName, int indLineNumber, IJavaElement indJavaElement, List<?> indArgs) {

      String sep = IGlobalProperty.COLUMN_SEPARATOR;
      String pkgName = detector.getPackageName();
      String className = detector.getClassName();
      String indicatorClassName = detector.indicatorClass;
      String indicatorMethodName = detector.indicatorMethod;
      String fileName = detector.fileName;
      String projectName = detector.projectName;
      int argPos = detector.argumentPos;
      int indicatorPos = detector.indicatorPos;
      String repairPath = detector.repairPath;
      String originalPath = detector.originalPath;
      String filePath = detector.filePath;
      StringBuilder sb = new StringBuilder();
      sb.append(group);
      sb.append(sep);

      MethodElement parent = addStaticInitializer(init, indJavaElement);
      MethodElement ind = new MethodElement(indNodeName, parent);

      ind.setIndicator(rootNodeName == null ? Indicator.INDROOT : Indicator.IND);
      sb.append(indJavaElement.getPath().toOSString());
      sb.append(sep);
      sb.append(ind.getIndicator().name());
      sb.append(sep);
      sb.append(Integer.toString(indLineNumber));
      sb.append(sep);
      sb.append(indNodeName);
      sb.append(sep);
      sb.append(convertListToString(indArgs));
      sb.append(sep);
      sb.append(init);

      ind.setLineNumber(indLineNumber);
      ind.setProjectName(projectName);
      ind.setJavaElement(indJavaElement);
      ind.setParameters(indArgs);
      ind.setPackageName(pkgName);
      ind.setClassName(className);
      ind.setClassMethodName(detector.methodClassName);
      ind.setPatternName(detector.indicatorPattern);
      ind.setIndicatorClassName(indicatorClassName);
      ind.setIndicatorMethodName(indicatorMethodName);
      ind.setArgPos(argPos);
      ind.setGraph(treeNodes);
      ind.setIndicatorPos(indicatorPos);
      ind.setRepairPath(repairPath);
      ind.setOriginalPath(originalPath);
      ind.setFileName(fileName);
      ind.setFilePath(filePath);
      ind.setDetectionLine(TableViewerHelper.getDetectionLine(ind));

      parent.add(ind);

      if (rootNodeName != null) {
         StringBuilder pc = new StringBuilder();
         MethodElement root = new MethodElement(rootNodeName, ind);
         root.setIndicator(Indicator.ROOT);
         root.setLineNumber(rootLineNumber);
         root.setProjectName(projectName);
         root.setJavaElement(rootJavaElement);
         root.setParameters(rootArgs);
         root.setPackageName(pkgName);
         root.setPatternName(detector.indicatorPattern);
         root.setClassName(className);
         root.setClassMethodName(detector.methodClassName);
         root.setIndicatorClassName(indicatorClassName);
         root.setIndicatorMethodName(indicatorMethodName);
         root.setArgPos(argPos);
         root.setIndicatorPos(indicatorPos);
         root.setRepairPath(repairPath);
         root.setFileName(fileName);
         root.setOriginalPath(originalPath);
         root.setFilePath(filePath);
         root.setGraph(treeNodes);
         root.setDetectionLine(TableViewerHelper.getDetectionLine(root));
         //			ind.add(root);

         sb.append(sep);
         sb.append(rootJavaElement.getPath().toOSString());
         sb.append(sep);
         sb.append(root.getIndicator().name());
         sb.append(sep);
         sb.append(Integer.toString(rootLineNumber));
         sb.append(sep);
         sb.append(rootNodeName);
         sb.append(sep);
         sb.append(convertListToString(rootArgs));
         pc.append(rootArgs);
         pc.append(rootLineNumber);
         pc.append(rootJavaElement);
         if (!treeList.contains(sb.toString())) {
            if(patternConflict.get(pc.toString()) != null) {
               MethodElement me = patternConflict.get(pc.toString());
               if(me.getIndicatorClassName() != indicatorClassName) {
                  root.setPatternConflict(true);
                  me.setPatternConflict(true);
               }
            }
            else {
               root.setPatternConflict(false);
               patternConflict.put(pc.toString(), root);
            }
            ind.add(root);
         }
      }
      if (!treeList.contains(sb.toString())) {
         PrintHelper.printDebugMsgH("Adding to treeList:\n\t\t\t" + sb.toString() + "\n");
         treeList.add(sb.toString());
         methodElements.add(parent);
         BatchProcessing.INSTANCE.clearMisuse();
         BatchProcessing.INSTANCE.addMisuse(ind);
      }
      Runtime.getRuntime().gc();
      System.gc();
   }

   public void addMethodInvoc(MethodDeclaration metDecl, DetectionASTVisitor detector, String group, //
         String rootNodeName, int rootLineNumber, IJavaElement rootJavaElement, List<?> rootArgs, //
         String indNodeName, int indLineNumber, IJavaElement indJavaElement, List<?> indArgs, int depth) {

      String sep = IGlobalProperty.COLUMN_SEPARATOR;
      StringBuilder sb = new StringBuilder();
      sb.append(group);
      sb.append(sep);

      String pkgName = detector.getPackageName();
      String className = detector.getClassName();
      String indicatorClassName = detector.indicatorClass;
      String indicatorMethodName = detector.indicatorMethod;
      String repairPath = detector.repairPath;
      String projectName = detector.projectName;
      String originalPath = detector.originalPath;
      String[] indPaths = indJavaElement.getResource().getFullPath().toFile().toString().split("/");
      int argPos = detector.argumentPos;
      int indicatorPos = detector.indicatorPos;
      String filePath = detector.filePath;
      MethodElement parent = addMethodDecl(metDecl, null, pkgName);
      MethodElement ind = new MethodElement(indNodeName, parent);
      ind.setIndicator(rootNodeName == null ? Indicator.INDROOT : Indicator.IND);
      ind.setLineNumber(indLineNumber);
      ind.setProjectName(projectName);
      ind.setJavaElement(indJavaElement);
      ind.setParameters(indArgs);
      ind.setProjectName(projectName);
      ind.setPackageName(pkgName);
      ind.setPatternName(detector.indicatorPattern);
      ind.setClassName(className);
      ind.setClassMethodName(detector.methodClassName);
      ind.setGraph(treeNodes);
      ind.setIndicatorClassName(indicatorClassName);
      ind.setIndicatorMethodName(indicatorMethodName);
      ind.setArgPos(argPos);
      ind.setIndicatorPos(indicatorPos);
      ind.setRepairPath(repairPath);
      ind.setFileName(indPaths[indPaths.length-1]);
      ind.setOriginalPath(originalPath);
      ind.setFilePath(filePath);
      ind.setDetectionLine(TableViewerHelper.getDetectionLine(ind));


      ind = parent.getChildMethodElement(ind);
      //
      sb.append(indJavaElement.getPath().toOSString());
      sb.append(sep);
      sb.append(ind.getIndicator().name());
      sb.append(sep);
      sb.append(Integer.toString(indLineNumber));
      sb.append(sep);
      sb.append(indNodeName);
      sb.append(sep);
      sb.append(convertListToString(indArgs));
      sb.append(sep);
      sb.append(metDecl.getName().getIdentifier());

      //      
      if (rootNodeName != null) {
         MethodElement root = new MethodElement(rootNodeName, ind);
         StringBuilder pc = new StringBuilder();
         String[] rootPaths = rootJavaElement.getResource().getFullPath().toFile().toString().split("/");
         root.setIndicator(Indicator.ROOT);
         root.setLineNumber(rootLineNumber);
         root.setProjectName(projectName);
         root.setJavaElement(rootJavaElement);
         root.setParameters(rootArgs);
         root.setPackageName(pkgName);
         root.setClassName(className);
         root.setPatternName(detector.indicatorPattern);
         root.setClassMethodName(detector.methodClassName);
         root.setIndicatorClassName(indicatorClassName);
         root.setIndicatorMethodName(indicatorMethodName);
         root.setArgPos(argPos);
         root.setGraph(treeNodes);
         //			root.setModelBuilder(gNodes);
         root.setIndicatorPos(indicatorPos);
         root.setRepairPath(repairPath);
         root.setFileName(rootPaths[rootPaths.length-1]);
         root.setOriginalPath(originalPath);
         root.setFilePath(filePath);
         root.setDetectionLine(TableViewerHelper.getDetectionLine(root));
         pc.append(rootArgs);
         pc.append(rootLineNumber);
         pc.append(rootJavaElement);

         sb.append(sep);
         sb.append(rootJavaElement.getPath().toOSString());
         sb.append(sep);
         sb.append(root.getIndicator().name());
         sb.append("[" + depth + "]");
         sb.append(sep);
         sb.append(Integer.toString(rootLineNumber));
         sb.append(sep);
         sb.append(rootNodeName);
         sb.append(sep);
         sb.append(convertListToString(rootArgs));
         if (!treeList.contains(sb.toString())) {
            if(patternConflict.get(pc.toString()) != null) {
               MethodElement me = patternConflict.get(pc.toString());
               if(me.getIndicatorClassName() != indicatorClassName) {
                  root.setPatternConflict(true);
                  me.setPatternConflict(true);
               }
            }
            else {
               root.setPatternConflict(false);
               patternConflict.put(pc.toString(), root);
            }
            ind.add(root);
         }
      }
      if (!treeList.contains(sb.toString())) {
         PrintHelper.printDebugMsgH("Adding to treeList:\n\t\t\t" + sb.toString() + "\n");
         treeList.add(sb.toString());
         methodElements.add(parent);
         BatchProcessing.INSTANCE.clearMisuse();
         BatchProcessing.INSTANCE.addMisuse(ind);
      }
   }


   public MethodElement addMethodInvoc(MethodDeclaration md, MethodInvocation mi, int lineNumber, Indicator ind) {

      // add the method declaration
      // this is also the parent
      MethodElement parent = addMethodDecl(md, null);

      // get the name of the method invocation
      String methodName = mi.getName().getIdentifier();

      // add the method
      MethodElement methodElem = new MethodElement(methodName, parent);
      methodElem.setParameters(mi.arguments());
      methodElem.setJavaElement(parent.getJavaElement());
      methodElem.setIndicator(ind);

      if (lineNumber > 0) {
         methodElem.setLineNumber(lineNumber);
      }

      parent.add(methodElem);
      methodElements.add(methodElem);
      return methodElem;
   }

   public MethodElement addMethodInvoc(MethodDeclaration md, MethodInvocation mi, MethodInvocation mi2, int lineNumber, Indicator ind) {

      // associate mi as parent of mi2
      String methodName = mi2.getName().getIdentifier();

      // find the parent element of the MethodDeclaration
      ProgramElement findParentElement = findPackageElement(md.resolveBinding().getDeclaringClass().getName());

      // find the method element of the parent
      MethodElement findMethodElement = findMethodElement(md.getName().getIdentifier(), findParentElement);

      MethodElement find2 = findMethodElement(mi.getName().getIdentifier(), findMethodElement);

      // add the method
      MethodElement me = new MethodElement(methodName, find2);
      me.setParameters(mi2.arguments());
      me.setJavaElement(findMethodElement.getJavaElement());
      me.setIndicator(ind);
      if (lineNumber > 0) {
         me.setLineNumber(lineNumber);
      }
      find2.add(me);
      methodElements.add(me);
      return me;
   }

   public MethodElement addConstructorInvoc(MethodDeclaration md, ConstructorInvocation ci, int lineNumber, Indicator ind) {

      // add the method declaration
      // this is also the parent
      MethodElement parent = addMethodDecl(md, null);

      // get the name of the Constructor invocation
      String ciName = ci.resolveConstructorBinding().getName();

      // add the method
      MethodElement methodElem = new MethodElement(ciName, parent);
      methodElem.setParameters(ci.arguments());
      methodElem.setJavaElement(parent.getJavaElement());
      methodElem.setIndicator(ind);

      if (lineNumber > 0) {
         methodElem.setLineNumber(lineNumber);
      }

      parent.add(methodElem);
      methodElements.add(methodElem);

      return methodElem;
   }

   public void addCFGNode(CFGNode n, String str, int lineNum, Indicator ind) {
      // get MethodDecl and MethodInvoc from CFGNode
      MethodDeclaration md = n.getMethodDec();
      MethodInvocation mi = n.getCallSiteMethodInvc();

      // lineNum=0 to skip setting it
      MethodElement me = addMethodInvoc(md, mi, 0, Indicator.STEP);

      // add the SimpleName as indicator
      MethodElement spElem = new MethodElement(str, me);
      spElem.setParameters(new ArrayList<String>());
      spElem.setJavaElement(me.getJavaElement());
      spElem.setIndicator(ind);
      spElem.setLineNumber(lineNum);
      methodElements.add(spElem);
      me.add(spElem);
   }

   public void addCFGNode(CFGNode n, SimpleName spName, int lineNum, Indicator ind) {
      addCFGNode(n, spName.getIdentifier(), lineNum, ind);
   }

   public void addMethodElements(MethodDeclaration md, ClassInstanceCreation ciCre, int lineNumber, Indicator ind) {
      // add the method declaration
      // this is also the parent
      MethodElement parent = addMethodDecl(md, null);

      // get the name of the class instance creation
      String ciName = null;
      if (ciCre.getType() instanceof SimpleType) {
         SimpleType st = (SimpleType) ciCre.getType();
         ciName = st.getName().getFullyQualifiedName();
      }

      // add the method
      MethodElement methodElem = new MethodElement(ciName, parent);
      methodElem.setParameters(ciCre.arguments());
      methodElem.setJavaElement(parent.getJavaElement());
      methodElem.setLineNumber(lineNumber);
      methodElem.setIndicator(ind);
      methodElements.add(methodElem);
      parent.add(methodElem);

      PrintHelper.printDebugMsg("addMethodElements: " + md.getName().getIdentifier() + ":" + DetectionASTVisitor.getName(ciCre) + ":" + lineNumber + ":" + ind);
   }

   // Find a package program element in the list that matches the given pkgName
   private static ProgramElement findPackageElement(String pkgName) {
      ProgramElement foundPE = pkgElements.stream().filter(pe -> pkgName.equals(pe.getName())).findAny().orElse(null);
      if (foundPE != null) {
         return foundPE;
      }
      return addProgramElement(pkgName);

   }

   // Find a class program element in the list that matches the given className
   // Else - return a new instance
   private static ProgramElement findClassElement(String className) {
      ProgramElement peClass = new ProgramElement(className);
      ProgramElement x = classElements.stream()//
            .filter(ce -> className.equals(ce.getName())) //
            .findAny() //
            .orElse(peClass);
      return x;
   }

   private static ProgramElement findClassElement(String className, ProgramElement pe) {
      ProgramElement peClass = new ProgramElement(className, pe);
      ProgramElement x = classElements.stream()//
            .filter(ce -> className.equals(ce.getName()) && pe.getName().equals(ce.getParent().getName())) //
            .findAny() //
            .orElse(peClass);
      return x;
   }

   private static ProgramElement findClassElement(String className, String pkg) {
      ProgramElement peClass = new ProgramElement(className);
      ProgramElement x = classElements.stream()//
            .filter(ce -> className.equals(ce.getName()) && pkg.equals(ce.getParent().getName())) //
            .findAny() //
            .orElse(peClass);
      return x;
   }

   private static ProgramElement findClassElement(String className, List<String> params) {
      ProgramElement peClass = new ProgramElement(className);
      if (params.size()==2) {
         ProgramElement x = classElements.stream()//
               .filter(ce -> className.equals(ce.getName()) && params.get(1).equals(ce.getParent().getName()) && params.get(0).equals(ce.getParent().getParent().getName())) //
               .findAny() //
               .orElse(peClass);
         return x;
      }
      return peClass;
   }

   private static MethodElement findMethodElement(String methodName, ProgramElement pe) {
      MethodElement me = new MethodElement(methodName, pe);
      MethodElement x = new MethodElement(methodName, pe);
      //		System.out.println(pe.getParent().getName());
      //		System.exit(0);
      if(pe.getParent() == null) {
         x = methodElements.stream() //
               .filter(element -> methodName.equals(element.getName()) && pe.getName().equals(element.getParent().getName())) //
               .findAny() //
               .orElse(me);
      }
      else if(pe.getParent().getParent() == null) {
         x = methodElements.stream() //
               .filter(element -> methodName.equals(element.getName()) && pe.getName().equals(element.getParent().getName()) && pe.getParent().getName().equals(element.getParent().getParent().getName())) //
               .findAny() //
               .orElse(me);
      }
      else if(pe.getParent().getParent().getParent() == null) {
         x = methodElements.stream() //
               .filter(element -> methodName.equals(element.getName()) && pe.getName().equals(element.getParent().getName()) && pe.getParent().getName().equals(element.getParent().getParent().getName()) && pe.getParent().getParent().getName().equals(element.getParent().getParent().getParent().getName())) //
               .findAny() //
               .orElse(me);
      }
      else if(pe.getParent().getParent().getParent().getParent() == null){
         x = methodElements.stream() //
               .filter(element -> methodName.equals(element.getName()) && pe.getName().equals(element.getParent().getName()) && pe.getParent().getName().equals(element.getParent().getParent().getName()) && pe.getParent().getParent().getName().equals(element.getParent().getParent().getParent().getName()) && pe.getParent().getParent().getParent().getName().equals(element.getParent().getParent().getParent().getParent().getName())) //
               .findAny() //
               .orElse(me);
      } else {
         x = methodElements.stream() //
               .filter(element -> methodName.equals(element.getName()) && pe.getName().equals(element.getParent().getName())) //
               .findAny() //
               .orElse(me);
      }
      return x;
   }

   /**
    * Open editor in specific line number
    * 
    * @param javaElement
    *           The java file to be opened
    * @param lineNumber
    *           The line number
    */
   public static void openInEditor(MethodElement me) {
      IJavaElement javaElement = me.getJavaElement();
      int lineNumber = me.getLineNumber();
      try {
         IEditorPart editorPart = JavaUI.openInEditor(javaElement, true, true);
         if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
            return;
         }

         ITextEditor editor = (ITextEditor) editorPart;
         IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
         if (document != null) {
            IRegion lineInfo = null;
            lineInfo = document.getLineInformation(lineNumber - 1);
            if (lineInfo != null) {
               editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void createDoubleClickListener(TreeViewer view) {
      IDoubleClickListener listener = new IDoubleClickListener() {
         @Override
         public void doubleClick(DoubleClickEvent event) {
            TreeSelection selection = (TreeSelection) event.getSelection();
            TreePath[] treePaths = selection.getPaths();
            TreePath treePath = treePaths[0];
            Object seg = treePath.getLastSegment();
            if (seg instanceof MethodElement) {
               MethodElement me = (MethodElement) seg;
               openInEditor(me);
            }
         }
      };
      view.addDoubleClickListener(listener);
   }

   public static void createProgElemColumns(TreeViewer view) {
      view.setContentProvider(new ContentProviderProgElem());
      view.getTree().setHeaderVisible(true);
      String[] titles = { "Program Element", "Line", "Parameters", "Indicator" };
      int[] widths = { 300, 100, 300, 100 };

      int index = 0;
      TreeViewerColumn col = createTableViewerColumn(view, titles[index], widths[index], index);
      col.setLabelProvider(new DelegatingStyledCellLabelProvider(new LabelProviderProgElem()));

      ++index;
      col = createTableViewerColumn(view, titles[index], widths[index], index);
      col.setLabelProvider(new DelegatingStyledCellLabelProvider(new LabelProviderLineNumber()));

      ++index;
      col = createTableViewerColumn(view, titles[index], widths[index], index);
      col.setLabelProvider(new DelegatingStyledCellLabelProvider(new LabelProviderMethodParm()));

      ++index;
      col = createTableViewerColumn(view, titles[index], widths[index], index);
      col.setLabelProvider(new DelegatingStyledCellLabelProvider(new LabelProviderIndicator()));

   }

   public static void createTreeColumns(TreeViewer view) {
      view.setContentProvider(new ContentProviderTree());
      view.getTree().setHeaderVisible(true);
      view.expandAll();
      String[] titles = { "Program Element", "Detection", "Type", "Line", "FileName"};
      int[] widths = { 300, 300, 100, 100, 200};
      int index = 0;
      TreeViewerColumn col = createTreeViewerColumn(view, titles[index], widths[index], index);
      col.setLabelProvider(new DelegatingStyledCellLabelProvider(new LabelProviderTree()));
   }

   public static void createContext(TreeViewer view, Composite parent) {
      Menu contextMenu = new Menu(view.getTree());
      view.getTree().setMenu(contextMenu);
//      createMenu(view, contextMenu);
      //		createMenuItems(view, contextMenu, me);
   }

//   private static void createMenu(TreeViewer view, Menu parent) {
//      final MenuItem menuItemClear = new MenuItem(parent, SWT.PUSH);
//      menuItemClear.setText("Calculate");
//      menuItemClear.addSelectionListener(new SelectionAdapter() {
//         @Override
//         public void widgetSelected(SelectionEvent e) {
//            ProjectAnalysis projectAnalyzer = new ProjectAnalysis();
//            projectAnalyzer.analyze();
//         }
//      });
//   }

   private static TreeViewerColumn createTreeViewerColumn(TreeViewer view , String title, int width, final int colNumber) {
      final TreeViewerColumn viewerColumn = new TreeViewerColumn(view, SWT.NONE);
      final TreeColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(width);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

   private static TreeViewerColumn createTableViewerColumn(TreeViewer view, String title, int width, final int colNumber) {
      final TreeViewerColumn viewerColumn = new TreeViewerColumn(view, SWT.NONE);
      final TreeColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(width);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

   public static void createContextMenu(TreeViewer view, Composite parent) {
      Menu contextMenu = new Menu(view.getTree());
      view.getTree().setMenu(contextMenu);
      createMenuItems(view, contextMenu);
   }

   private static void createMenuItems(TreeViewer view, Menu parent) {
      // NOTE: order of declaration determines order of appearance in menu

      final MenuItem menuItemClear = new MenuItem(parent, SWT.PUSH);
      menuItemClear.setText("Clear");
      menuItemClear.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            clearTreeView(view);
         }
      });

      final MenuItem menuItemCollapseAll = new MenuItem(parent, SWT.PUSH);
      menuItemCollapseAll.setText("Collapse All");
      menuItemCollapseAll.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            view.collapseAll();
         }
      });

      final MenuItem menuItemExpand = new MenuItem(parent, SWT.PUSH);
      menuItemExpand.setText("Expand");
      menuItemExpand.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            TreeItem[] selection = view.getTree().getSelection();
            if (selection != null && selection.length > 0) {
               TreeColumn[] treeColumns = selection[0].getParent().getColumns();
               for (TreeColumn treeColumn : treeColumns) {
                  treeColumn.pack();
               }
               expandTree(view, selection[0]);
            }
         }
      });

      final MenuItem menuItemExpandAll = new MenuItem(parent, SWT.PUSH);
      menuItemExpandAll.setText("Expand All");
      menuItemExpandAll.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            view.expandAll();
         }
      });

      //		new MenuItem(parent, SWT.SEPARATOR);
      //
      //		final Menu repairMenu = new Menu(parent);
      //		final MenuItem repair1 = new MenuItem(repairMenu, SWT.PUSH);
      //		repair1.setText(IRuleInfo.JCA1_REPAIR_MENU);
      //		repair1.addSelectionListener(new SelectionAdapter() {
      //			@Override
      //			public void widgetSelected(SelectionEvent e) {
      //				TreeItem[] selection = view.getTree().getSelection();
      //				if (selection != null && selection.length > 0) {
      //					Object data = selection[0].getData();
      //					if (data instanceof MethodElement) {
      //						MethodElement me = (MethodElement) data;
      //						openInEditor(me);
      //
      //						RepairHelper repairHelper = new RepairHelper(IRuleInfo.AES_ECB, IRuleInfo.AES_CBC);
      //						try {
      //							repairHelper.replace_AES_ECB(me);
      //						} catch (Exception e1) {
      //							e1.printStackTrace();
      //						}
      //					}
      //				}
      //
      //			}
      //		});
      //
      //		final MenuItem repair2 = new MenuItem(repairMenu, SWT.PUSH);
      //		repair2.setText(IRuleInfo.JCA2_REPAIR_MENU);
      //		repair2.addSelectionListener(new SelectionAdapter() {
      //			@Override
      //			public void widgetSelected(SelectionEvent e) {
      //				TreeItem[] selection = view.getTree().getSelection();
      //				if (selection != null && selection.length > 0) {
      //					Object data = selection[0].getData();
      //					if (data instanceof MethodElement) {
      //						MethodElement me = (MethodElement) data;
      //						openInEditor(me);
      //
      //						RepairHelper repairHelper = new RepairHelper(IRuleInfo.SHA_1, IRuleInfo.SHA_3);
      //						try {
      //							repairHelper.replace_SHA1(me);
      //						} catch (Exception e1) {
      //							e1.printStackTrace();
      //						}
      //					}
      //				}
      //
      //			}
      //		});
      //
      //		final MenuItem repair3 = new MenuItem(repairMenu, SWT.PUSH);
      //		repair3.setText(IRuleInfo.JCA3_REPAIR_MENU);
      //		repair3.addSelectionListener(new SelectionAdapter() {
      //			@Override
      //			public void widgetSelected(SelectionEvent e) {
      //				TreeItem[] selection = view.getTree().getSelection();
      //				if (selection != null && selection.length > 0) {
      //					Object data = selection[0].getData();
      //					if (data instanceof MethodElement) {
      //						MethodElement me = (MethodElement) data;
      //						openInEditor(me);
      //
      //						RepairHelper repairHelper = new RepairHelper(null, null);
      //						try {
      //							repairHelper.replace_ConstantSecretKey(me);
      //						} catch (Exception e1) {
      //							e1.printStackTrace();
      //						}
      //					}
      //				}
      //
      //			}
      //		});
      //
      //		final MenuItem repair4 = new MenuItem(repairMenu, SWT.PUSH);
      //		repair4.setText(IRuleInfo.JCA4_REPAIR_MENU);
      //		repair4.addSelectionListener(new SelectionAdapter() {
      //			@Override
      //			public void widgetSelected(SelectionEvent e) {
      //				TreeItem[] selection = view.getTree().getSelection();
      //				if (selection != null && selection.length > 0) {
      //					Object data = selection[0].getData();
      //					if (data instanceof MethodElement) {
      //						MethodElement me = (MethodElement) data;
      //						openInEditor(me);
      //
      //						RepairHelper repairHelper = new RepairHelper(null, null);
      //						try {
      //							repairHelper.replace_PBE_Constant_Salt(me);
      //						} catch (Exception e1) {
      //							e1.printStackTrace();
      //						}
      //					}
      //				}
      //
      //			}
      //		});
      //
      //		final MenuItem repair5 = new MenuItem(repairMenu, SWT.PUSH);
      //		repair5.setText(IRuleInfo.JCA5_REPAIR_MENU);
      //		repair5.addSelectionListener(new SelectionAdapter() {
      //			@Override
      //			public void widgetSelected(SelectionEvent e) {
      //				TreeItem[] selection = view.getTree().getSelection();
      //				if (selection != null && selection.length > 0) {
      //					Object data = selection[0].getData();
      //					if (data instanceof MethodElement) {
      //						MethodElement me = (MethodElement) data;
      //						openInEditor(me);
      //
      //						RepairHelper repairHelper = new RepairHelper(null, null);
      //						try {
      //							repairHelper.replace_PBE_Constant_Interation(me);
      //						} catch (Exception e1) {
      //							e1.printStackTrace();
      //						}
      //					}
      //				}
      //
      //			}
      //		});
      //
      //		final MenuItem repair6 = new MenuItem(repairMenu, SWT.PUSH);
      //		repair6.setText(IRuleInfo.JCA6_REPAIR_MENU);
      //		repair6.addSelectionListener(new SelectionAdapter() {
      //			@Override
      //			public void widgetSelected(SelectionEvent e) {
      //				TreeItem[] selection = view.getTree().getSelection();
      //				if (selection != null && selection.length > 0) {
      //					Object data = selection[0].getData();
      //					if (data instanceof MethodElement) {
      //						MethodElement me = (MethodElement) data;
      //						openInEditor(me);
      //
      //						RepairHelper repairHelper = new RepairHelper(null, null);
      //						try {
      //							repairHelper.replace_SecureRandom_ConstantSeed(me);
      //						} catch (Exception e1) {
      //							e1.printStackTrace();
      //						}
      //					}
      //				}
      //
      //			}
      //		});
      //
      //		final MenuItem repair = new MenuItem(parent, SWT.CASCADE);
      //		repair.setText("Repair");
      //		repair.setMenu(repairMenu);

   }

   public static void clearTreeView(TreeViewer view) {
      ProgElementModelProvider.INSTANCE.clearProgramElements();
      pkgElements.clear();
      classElements.clear();
      methodElements.clear();
      treeList.clear();
      indicatorList.clear();
      view.getTree().deselectAll();
      view.setInput(null);
   }
   
   public static void clearTableView(TableViewer viewer) {
      ProgElementModelProvider.INSTANCE.clearProgramElements();
      pkgElements.clear();
      classElements.clear();
      methodElements.clear();
      treeList.clear();
      indicatorList.clear();
      viewer.setInput(null);
      viewer.getTable().clearAll();
      System.gc();
      Runtime.getRuntime().gc();
   }

   static void expandTree(TreeViewer view, TreeItem it) {
      ProgramElement p = (ProgramElement) it.getData();
      if (p == null) {
         return;
      }
      view.setExpandedState(p, true);
      for (TreeItem child : it.getItems()) {
         expandTree(view, child);
      }
   }

   /*
    // rudimentary attempt - data set never has orphans
    private void removeOrphans(HashSet<ProgramElement> data) {
        boolean flag = false;
        for (ProgramElement pe : data) {
            if (!pe.hasChildren()) {
                data.remove(pe);
                flag = true;
                System.out.println("Removed: " + pe);
            }
        }

        if (flag)
            removeOrphans(data);
    }
    */

   public void updateTreeView(TreeViewer view) {
      view.getTree().deselectAll(); // resolved issue: stack overflow errors.
      HashSet<ProgramElement> data = ProgElementModelProvider.INSTANCE.getProgElements();
      if (!data.isEmpty()) {
         // removeOrphans(data);

         ProgramElement[] array = data.toArray(new ProgramElement[data.size()]);
         view.getTree().deselectAll();
         view.setInput(array);
      }
   }

   //	public void updateZestView(ZestViewer view) {
   //		view.update();
   //	}

   public void addQualifiedName(QualifiedName qName, int line, Indicator ind) {
      if (qName.getParent() instanceof ReturnStatement) {
         ReturnStatement ret = (ReturnStatement) qName.getParent();
         if (ret.getParent().getParent() instanceof MethodDeclaration) {
            MethodDeclaration md = (MethodDeclaration) ret.getParent().getParent();
            MethodElement parent = addMethodDecl(md, null);

            MethodElement qme = new MethodElement(qName.getFullyQualifiedName(), parent);
            qme.setIndicator(ind);
            qme.setJavaElement(parent.getJavaElement());
            qme.setLineNumber(line);
            qme.setParameters(new ArrayList<>());
            parent.add(qme);
         }
      }

   }

   public void addMethodDecl(MethodDeclaration md, IJavaElement javaElement, SimpleName sName, int line, Indicator ind) {
      MethodElement parent = addMethodDecl(md, javaElement);

      MethodElement me = new MethodElement(sName.getFullyQualifiedName(), parent);
      me.setIndicator(ind);
      me.setJavaElement(parent.getJavaElement());
      me.setLineNumber(line);
      me.setParameters(new ArrayList<>());
      parent.add(me);
   }

   public void addMethodDecl(MethodDeclaration md, SimpleName sName, int line, Indicator ind) {
      MethodElement parent = addMethodDecl(md, null);

      MethodElement me = new MethodElement(sName.getFullyQualifiedName(), parent);
      me.setIndicator(ind);
      me.setJavaElement(parent.getJavaElement());
      me.setLineNumber(line);
      me.setParameters(new ArrayList<>());
      parent.add(me);
   }

   public List<String> getVariableDeclarationFragmentArguments(SimpleName sName) {
      List<String> args = new ArrayList<>();
      if (sName.getParent() != null) {
         if (sName.getParent() instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
            if (vdf.getInitializer() instanceof StringLiteral) {
               StringLiteral strLit = (StringLiteral) vdf.getInitializer();
               args = Arrays.asList(strLit.getLiteralValue());
            }
         } else if (sName.getParent() instanceof Assignment) {
            Assignment assign = (Assignment) sName.getParent();
            Expression rightHandSide = assign.getRightHandSide();
            if (rightHandSide instanceof StringLiteral) {
               StringLiteral strLit = (StringLiteral) rightHandSide;
               args = Arrays.asList(strLit.getLiteralValue());
            }
         }
      }
      return args;
   }

   public String buildIndNodeName(MethodInvocation mi) {
      String name = null;

      if (mi.getExpression() instanceof SimpleName) {
         SimpleName sName = (SimpleName) mi.getExpression();
         StringBuilder sb = new StringBuilder();
         sb.append(sName.getFullyQualifiedName());
         sb.append(".");
         sb.append(mi.resolveMethodBinding().getName());
         name = sb.toString();
      } else {
         name = mi.resolveMethodBinding().getName();
      }

      return name;
   }

   public String buildIndNodeName(ClassInstanceCreation cic) {
      return cic.resolveConstructorBinding().getName();
   }

   /************************************************************************************************/
   /* BUILD CFG TREE METHODS */

   public void buildCfgTree(DetectionASTVisitor detector, Initializer init, MethodInvocation metInv) {
      int indLineNumber = detector.getLineNumber(metInv.getStartPosition());
      String indNodeName = buildIndNodeName(metInv);
      List<?> indArgs = metInv.arguments();
      IJavaElement indJavaElement = null;

      // get the root TypeDeclaration
      ASTNode pNode = init.getParent();
      while (pNode != null && !(pNode instanceof TypeDeclaration)) {
         pNode = pNode.getParent();
      }

      TypeDeclaration tdp = (TypeDeclaration) pNode;
      indJavaElement = tdp.resolveBinding().getJavaElement();

      addMethodInvoc(init, detector, detector.getGroup(), //
            null, 0, null, null, indNodeName, indLineNumber, indJavaElement, indArgs);
   }

   public void buildCfgTree(DetectionASTVisitor detector, CFGNode node, SimpleName sName) {
      int rootLineNumber = detector.getLineNumber(sName.getStartPosition());
      String rootNodeName = sName.resolveBinding().getName();
      IJavaElement rootJavaElement = sName.resolveBinding().getJavaElement();
      List<String> rootArgs = getVariableDeclarationFragmentArguments(sName);

      ASTNode rootCallSite = node.getRootCallSite();
      if (rootCallSite instanceof ClassInstanceCreation) {
         ClassInstanceCreation cic = (ClassInstanceCreation) rootCallSite;

         // gather indicator elements
         int indLineNumber = detector.getLineNumber(cic.getStartPosition());
         String indNodeName = buildIndNodeName(cic);

         MethodDeclaration md = node.getMethodDec();
         IJavaElement indJavaElement = md.resolveBinding().getJavaElement();
         List<?> indArgs = cic.arguments();
         GNode srcGNode = new GPathNode(indJavaElement.toString() + detector.className, detector.className, indJavaElement, 0);
         GModelBuilder.instance().getNodes().add(srcGNode);
         GNode dstGNode = new GRootNode(rootJavaElement.toString() + rootArgs.get(detector.argumentPos), rootArgs.get(detector.argumentPos), rootJavaElement, rootLineNumber);
         GModelBuilder.instance().getNodes().add(dstGNode);
         String conId = srcGNode.getId() + dstGNode.getId();
         GConnection con = new GConnection(conId, String.valueOf(rootLineNumber), srcGNode, dstGNode);
         GModelBuilder.instance().getConnections().add(con);
         srcGNode.getConnectedTo().add(dstGNode);
         srcGNode = dstGNode;
         dstGNode = new GIndicatorNode(indJavaElement.toString() + sName.getFullyQualifiedName(), cic.toString(), indJavaElement, indLineNumber);
         GModelBuilder.instance().getNodes().add(dstGNode);
         conId = srcGNode.getId() + dstGNode.getId();
         con = new GConnection(conId, String.valueOf(indLineNumber), srcGNode, dstGNode);
         GModelBuilder.instance().getConnections().add(con);
         srcGNode.getConnectedTo().add(dstGNode);
         addMethodInvoc(md, detector, detector.getGroup(), //
               rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
               indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      }
   }

   public void buildCfgTree(DetectionASTVisitor detector, Initializer init, MethodInvocation metInv, SimpleName sName) {

      int rootLineNumber = detector.getLineNumber(sName.getStartPosition());
      String rootNodeName = sName.resolveBinding().getName();
      IJavaElement rootJavaElement = sName.resolveBinding().getJavaElement();
      List<String> rootArgs = getVariableDeclarationFragmentArguments(sName);

      int indLineNumber = detector.getLineNumber(metInv.getStartPosition());
      String indNodeName = buildIndNodeName(metInv);

      // get the root TypeDeclaration
      ASTNode pNode = init.getParent();
      while (pNode != null && !(pNode instanceof TypeDeclaration)) {
         pNode = pNode.getParent();
      }

      TypeDeclaration tdp = (TypeDeclaration) pNode;
      IJavaElement indJavaElement = tdp.resolveBinding().getJavaElement();
      List<?> indArgs = metInv.arguments();

      addMethodInvoc(init, detector, detector.getGroup(), //
            rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
            indNodeName, indLineNumber, indJavaElement, indArgs);
   }

   public void buildCfgTree(DetectionASTVisitor detector, MethodDeclaration metDec, MethodInvocation metInv) {
      int indLineNumber = detector.getLineNumber(metInv.getStartPosition());
      String indNodeName = buildIndNodeName(metInv);

      IJavaElement mdJavaElement = metDec.resolveBinding().getJavaElement();
      IJavaElement indJavaElement = mdJavaElement;
      treeNodes = new ArrayList<GNode>();
      List<?> indArgs = metInv.arguments();
      Scanner scanner = new Scanner(metDec.toString());
      String nodeName = scanner.nextLine();
      while(!nodeName.contains("{")){
         nodeName = scanner.nextLine();
      }
      if (metDec.getReturnType2()!=null){
         int startIndex = nodeName.indexOf(metDec.getReturnType2().toString()) + metDec.getReturnType2().toString().length() + 1 ;
         int endIndex = nodeName.indexOf("{");
         if (detector.getPackageName() == "") {
            nodeName = metDec.getReturnType2() + " " + detector.className.replace(".java", "") + "." + nodeName.substring(startIndex, endIndex);
         }
         else {
            nodeName = metDec.getReturnType2() + " " +  detector.getPackageName() + "." + detector.className.replace(".java", "") + "." + nodeName.substring(startIndex, endIndex);
         }  
      }
      nodeName = nodeName.replace("\n", "");
      GNode srcGNode = new GPathNode(indJavaElement.toString() + detector.getLineNumber(metDec.getStartPosition()), nodeName, indJavaElement, detector.getLineNumber(metDec.getStartPosition()));
      GModelBuilder.instance().getNodes().add(srcGNode);
      treeNodes.add(srcGNode);
      GNode dstGNode = new GRootNode(indJavaElement.toString() + indLineNumber, metInv.getParent().getParent().toString().replace("\n", ""), indJavaElement, indLineNumber);
      GModelBuilder.instance().getNodes().add(dstGNode);
      treeNodes.add(dstGNode);
      String conId = srcGNode.getId() + dstGNode.getId();
      String edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      GConnection con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
      GModelBuilder.instance().getConnections().add(con);
      srcGNode.getConnectedTo().add(dstGNode);

      addMethodInvoc(metDec, detector, detector.getGroup(), //
            null, 0, null, null, //
            indNodeName, indLineNumber, indJavaElement, indArgs, 1);
   }

   public void buildCfgTree(DetectionASTVisitor detector, MethodDeclaration metDec, //
         ClassInstanceCreation cic, SimpleName sArg0, SimpleName sArg1) {

      // gather indicator elements
      int indLineNumber = detector.getLineNumber(cic.getStartPosition());
      String indNodeName = buildIndNodeName(cic);
      IJavaElement indJavaElement = metDec.resolveBinding().getJavaElement();
      List<?> indArgs = cic.arguments();
      if (sArg0 == null && sArg1 == null) {
         addMethodInvoc(metDec, detector, detector.getGroup(), //
               null, 0, null, null, //
               indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      } else {
         // gather 1st root elements
         processArgZero(detector, metDec, sArg0, indLineNumber, indNodeName, indJavaElement, indArgs);

         // gather 2nd root elements
         processArgOne(detector, metDec, sArg1, indLineNumber, indNodeName, indJavaElement, indArgs);
      }
   }

   public void buildCfgTree(DetectionASTVisitor detector, MethodDeclaration metDec, //
         ClassInstanceCreation cic, SimpleName sArg0) {

      // gather indicator elements
      int indLineNumber = detector.getLineNumber(cic.getStartPosition());
      String indNodeName = buildIndNodeName(cic);   
      IJavaElement indJavaElement = metDec.resolveBinding().getJavaElement();
      List<?> indArgs = cic.arguments();
      Scanner scanner = new Scanner(metDec.toString());
      String nodeName = scanner.nextLine();
      while(!nodeName.contains("{")){
         nodeName = scanner.nextLine();
      }
      if (metDec.getReturnType2()!=null){
         int startIndex = nodeName.indexOf(metDec.getReturnType2().toString()) + metDec.getReturnType2().toString().length() + 1 ;
         int endIndex = nodeName.indexOf("{");
         if (detector.getPackageName() == "") {
            nodeName = metDec.getReturnType2() + " " + detector.className.replace(".java", "") + "." + nodeName.substring(startIndex, endIndex);
         }
         else {
            nodeName = metDec.getReturnType2() + " " +  detector.getPackageName() + "." + detector.className.replace(".java", "") + "." + nodeName.substring(startIndex, endIndex);
         }  
      }
      treeNodes = new ArrayList<GNode>();
      
      nodeName = nodeName.replace("\n", "");
      GNode srcGNode = new GPathNode(indJavaElement.toString() + detector.getLineNumber(metDec.getStartPosition()), nodeName, indJavaElement, detector.getLineNumber(metDec.getStartPosition()));
      GModelBuilder.instance().getNodes().add(srcGNode);
      treeNodes.add(srcGNode);
      GNode dstGNode = new GIndicatorNode(indJavaElement.toString() + indLineNumber, cic.getParent().getParent().toString().replace("\n", ""), indJavaElement, indLineNumber);
      GModelBuilder.instance().getNodes().add(dstGNode);
      treeNodes.add(dstGNode);
      String conId = srcGNode.getId() + dstGNode.getId();
      String edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      GConnection con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
      GModelBuilder.instance().getConnections().add(con);
      srcGNode.getConnectedTo().add(dstGNode);

      if (sArg0 == null) {
         addMethodInvoc(metDec, detector, detector.getGroup(), //
               null, 0, null, null, //
               indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      } else {
         // gather 1st root elements
         dstGNode = new GRootNode(indJavaElement.toString() + detector.getLineNumber(sArg0.getStartPosition()), sArg0.getParent().getParent().toString().replace("\n", ""), indJavaElement, detector.getLineNumber(sArg0.getStartPosition()));
         GModelBuilder.instance().getNodes().add(dstGNode);
         treeNodes.add(dstGNode);
         conId = srcGNode.getId() + dstGNode.getId();
         edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
         con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
         GModelBuilder.instance().getConnections().add(con);
         srcGNode.getConnectedTo().add(dstGNode);
         processArgZero(detector, metDec, sArg0, indLineNumber, indNodeName, indJavaElement, indArgs);
      }
   }

   private void processArgOne(DetectionASTVisitor detector, MethodDeclaration metDec, SimpleName sArg1, int indLineNumber, String indNodeName, IJavaElement indJavaElement, List<?> indArgs) {
      int rootLineNumber;
      String rootNodeName;
      IJavaElement rootJavaElement;
      if (sArg1 != null) {
         rootLineNumber = detector.getLineNumber(sArg1.getStartPosition());
         rootNodeName = sArg1.resolveBinding().getName();
         rootJavaElement = sArg1.resolveBinding().getJavaElement();
         List<String> rootArgs = new ArrayList<>();

         ASTNode parent = sArg1.getParent();
         if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) parent;
            Expression initializer = vdf.getInitializer();
            if (initializer instanceof NumberLiteral) {
               NumberLiteral nl = (NumberLiteral) initializer;
               rootArgs.add(nl.toString());
            }
         }


         addMethodInvoc(metDec, detector, detector.getGroup(), //
               rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
               indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      } else { // special case for numberLiterals to report as INDROOT
         addMethodInvoc(metDec, detector, detector.getGroup(), //
               null, 0, null, null, //
               indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      }
   }

   private void processArgZero(DetectionASTVisitor detector, MethodDeclaration metDec, SimpleName sArg0, int indLineNumber, String indNodeName, IJavaElement indJavaElement, List<?> indArgs) {
      int rootLineNumber;
      String rootNodeName;
      IJavaElement rootJavaElement;
      if (sArg0 != null) {
         rootLineNumber = detector.getLineNumber(sArg0.getStartPosition());
         rootNodeName = sArg0.resolveBinding().getName();
         rootJavaElement = sArg0.resolveBinding().getJavaElement();
         List<String> rootArgs = new ArrayList<>();

         ASTNode parent = sArg0.getParent();
         if (parent instanceof VariableDeclarationFragment) {
            VariableDeclarationFragment vdf = (VariableDeclarationFragment) parent;
            Expression initializer = vdf.getInitializer();
            if (initializer instanceof ArrayInitializer) {
               ArrayInitializer ai = (ArrayInitializer) initializer;
               for (Object o : ai.expressions()) {
                  rootArgs.add(o.toString());
               }
            } else if (initializer instanceof MethodInvocation) {
               MethodInvocation mi = (MethodInvocation) initializer;
               Expression ex = mi.getExpression();
               if (ex instanceof StringLiteral) {
                  StringLiteral sl = (StringLiteral) ex;
                  rootArgs.add(sl.getLiteralValue());
               }
            } else if (initializer instanceof StringLiteral) {
               StringLiteral sl = (StringLiteral) initializer;
               rootArgs.add(sl.getLiteralValue());
            } else if (initializer instanceof NumberLiteral) {
               NumberLiteral sl = (NumberLiteral) initializer;
               rootArgs.add(sl.toString());
            }

         }

         addMethodInvoc(metDec, detector, detector.getGroup(), //
               rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
               indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      } else {
         addMethodInvoc(metDec, detector, detector.getGroup(), //
               null, 0, null, null, indNodeName, indLineNumber, indJavaElement, indArgs, 1);
      }
   }

   public void buildCfgTree(DetectionASTVisitor detector, MethodDeclaration metDec, MethodInvocation metInv, SimpleName sName) {

      int rootLineNumber = detector.getLineNumber(sName.getStartPosition());
      String rootNodeName = sName.resolveBinding().getName();
      IJavaElement rootJavaElement = sName.resolveBinding().getJavaElement();
      List<String> rootArgs = getVariableDeclarationFragmentArguments(sName);
      int indLineNumber = detector.getLineNumber(metInv.getStartPosition());
      String indNodeName = buildIndNodeName(metInv);
      IJavaElement mdJavaElement = metDec.resolveBinding().getJavaElement();
      IJavaElement indJavaElement = mdJavaElement;
      List<?> indArgs = metInv.arguments();
      Scanner scanner = new Scanner(metDec.toString());
      treeNodes = new ArrayList<GNode>();
      System.gc();
      String nodeName = scanner.nextLine();
      while(!nodeName.contains("{")){
         nodeName = scanner.nextLine();
      }
      if (metDec.getReturnType2()!=null){
         int startIndex = nodeName.indexOf(metDec.getReturnType2().toString()) + metDec.getReturnType2().toString().length() + 1 ;
         int endIndex = nodeName.indexOf("{");
         if (detector.getPackageName() == "") {
            nodeName = metDec.getReturnType2() + " " + detector.className.replace(".java", "") + "." + nodeName.substring(startIndex, endIndex);
         }
         else {
            nodeName = metDec.getReturnType2() + " " +  detector.getPackageName() + "." + detector.className.replace(".java", "") + "." + nodeName.substring(startIndex, endIndex);
         }  
      }
      nodeName = nodeName.replace("\n", "");
      GNode srcGNode = new GPathNode(indJavaElement.toString() + detector.getLineNumber(metDec.getStartPosition()), nodeName, indJavaElement, detector.getLineNumber(metDec.getStartPosition()));
      GModelBuilder.instance().getNodes().add(srcGNode);
      treeNodes.add(srcGNode);
      VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
      GNode dstGNode = new GRootNode(rootJavaElement.toString() + rootLineNumber, vdf.getParent().toString().replace("\n", ""), rootJavaElement, rootLineNumber);
      GModelBuilder.instance().getNodes().add(dstGNode);
      treeNodes.add(dstGNode);
      String conId = srcGNode.getId() + dstGNode.getId();
      String edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      GConnection con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
      GModelBuilder.instance().getConnections().add(con);
      srcGNode.getConnectedTo().add(dstGNode);
      dstGNode = new GIndicatorNode(indJavaElement.toString() + indLineNumber, metInv.getParent().getParent().toString().replace("\n", ""), indJavaElement, indLineNumber);
      GModelBuilder.instance().getNodes().add(dstGNode);
      treeNodes.add(dstGNode);
      conId = srcGNode.getId() + dstGNode.getId();
      edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
      GModelBuilder.instance().getConnections().add(con);
      srcGNode.getConnectedTo().add(dstGNode);
      addMethodInvoc(metDec, detector, detector.getGroup(), //
            rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
            indNodeName, indLineNumber, indJavaElement, indArgs, 1);
   }

   public void buildCfgTree(DetectionASTVisitor detector, MethodDeclaration metDec, MethodInvocation metInv, ConstructorInvocation ciInv) {

      int rootLineNumber = detector.getLineNumber(ciInv.getStartPosition());
      String rootNodeName = ciInv.resolveConstructorBinding().getName();
      IJavaElement rootJavaElement = ciInv.resolveConstructorBinding().getJavaElement();
      List<?> rootArgs = ciInv.arguments();
      treeNodes = new ArrayList<GNode>();

      int indLineNumber = detector.getLineNumber(metInv.getStartPosition());
      String indNodeName = buildIndNodeName(metInv);

      IJavaElement mdJavaElement = metDec.resolveBinding().getJavaElement();
      IJavaElement indJavaElement = mdJavaElement;
      List<?> indArgs = metInv.arguments();
      GNode srcGNode = new GPathNode(indJavaElement.toString() + detector.getLineNumber(metDec.getStartPosition()), detector.className, indJavaElement, detector.getLineNumber(metDec.getStartPosition()));
      GModelBuilder.instance().getNodes().add(srcGNode);
      treeNodes.add(srcGNode);
      GNode dstGNode = new GRootNode(rootJavaElement.toString() + rootLineNumber, rootArgs.get(detector.argumentPos).toString(), rootJavaElement, rootLineNumber);
      GModelBuilder.instance().getNodes().add(dstGNode);
      treeNodes.add(dstGNode);
      String conId = srcGNode.getId() + dstGNode.getId();
      String edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      GConnection con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
      GModelBuilder.instance().getConnections().add(con);
      srcGNode.getConnectedTo().add(dstGNode);
      srcGNode = dstGNode;
      dstGNode = new GIndicatorNode(indJavaElement.toString() + indLineNumber, metInv.toString(), indJavaElement, indLineNumber);
      GModelBuilder.instance().getNodes().add(dstGNode);
      treeNodes.add(dstGNode);
      conId = srcGNode.getId() + dstGNode.getId();
      edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
      GModelBuilder.instance().getConnections().add(con);
      srcGNode.getConnectedTo().add(dstGNode);
      addMethodInvoc(metDec, detector, detector.getGroup(), //
            rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
            indNodeName, indLineNumber, indJavaElement, indArgs, 1);
   }

   public void buildCfgTree(DetectionASTVisitor detector, CFGNode node) {
      MethodDeclaration metDecl = node.getMethodDec();
      if (metDecl == null) {
         return;
      }
      IJavaElement mdJavaElement = node.iMethodCallee;
      String filePath = node.iMethodCallee.getResource().getLocation().toFile().getAbsolutePath();
      treeNodes = new ArrayList<GNode>();
      CFGNode startNode = node;
      String rootName = startNode.getMethodDec().getReturnType2()+ " " + startNode.toString();
      
      int lineNumber = detector.getLineNumber(node.getMethodDec().getStartPosition(), filePath);
      GNode dstGNode = new GPathNode(startNode.getID(), rootName, mdJavaElement, lineNumber);
      treeNodes.add(dstGNode);
      GModelBuilder.instance().getNodes().add(dstGNode);
      MethodInvocation callSiteMethodInvc = node.getCallSiteMethodInvc();
      ConstructorInvocation callSiteConstructorInvc = node.getCallSiteConstructorInvc();
      TreeNode parent = node.getParent();
      CFGNode cfgParent = null;
      int rootLineNumber = -1;
      String rootNodeName = "";
      rootName = "";
      IJavaElement rootJavaElement = null;
      List<?> rootArgs = new ArrayList<>();
      CFGNode pNode1 = (CFGNode) parent;

      if (parent != null && parent instanceof CFGNode) {
         cfgParent = (CFGNode) parent;
      }

      if (node.getRootCallSite() == null && cfgParent != null && cfgParent.getRootCallSite() != null) {
         node = cfgParent;
      }
      // root call site is used as an override
      if (node.getRootCallSite() != null && node.getRootCallSite() instanceof SimpleName) {
         SimpleName sName = (SimpleName) node.getRootCallSite();
         rootLineNumber = detector.getLineNumber(sName.getStartPosition());
         rootNodeName = sName.resolveBinding().getName();
         rootJavaElement = sName.resolveBinding().getJavaElement();
         if (sName.getParent() != null) {
            if (sName.getParent() instanceof VariableDeclarationFragment) {
               VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
               if (vdf.getInitializer() instanceof StringLiteral) {
                  rootName = vdf.getParent().toString();
                  StringLiteral strLit = (StringLiteral) vdf.getInitializer();
                  rootArgs = Arrays.asList(strLit.getLiteralValue());
               }
            }
         }
      } else if (callSiteMethodInvc != null) {
         Object objArg = new Object();
         System.out.println(callSiteMethodInvc.arguments().size());
         System.out.println(pNode1.parmPositionToBackTrack);
         objArg = callSiteMethodInvc.arguments().get(pNode1.parmPositionToBackTrack);
//         filePath = node.iMethodCallee.getResource().getLocation().toFile().getAbsolutePath();
         rootJavaElement = mdJavaElement;
         rootArgs = callSiteMethodInvc.arguments();
         if (objArg instanceof SimpleName) {
            Map<String, VariableDeclarationFragment> variableDecl = node.getVariableDeclFragment();
            SimpleName varSPName = (SimpleName) objArg;
            if(variableDecl.get(varSPName.resolveBinding().toString())!=null) {
               VariableDeclarationFragment vdf = variableDecl.get(varSPName.resolveBinding().toString());
               varSPName = vdf.getName();
               rootLineNumber = detector.getLineNumber(varSPName.getStartPosition(), filePath);
               rootName = vdf.getParent().toString();
               rootNodeName = varSPName.getFullyQualifiedName();
            }
         }
         else if (objArg instanceof StringLiteral) {
            StringLiteral varSPName = (StringLiteral) objArg;
            rootLineNumber = detector.getLineNumber(varSPName.getStartPosition(), filePath);
            rootName = varSPName.getParent().toString();
            rootNodeName = varSPName.getLiteralValue();
         }
         else {
            rootName = pNode1.getMethodDec().getReturnType2()+ " " + node.toString();
            rootLineNumber = detector.getLineNumber(callSiteMethodInvc.getStartPosition(), filePath);
            rootNodeName = callSiteMethodInvc.resolveMethodBinding().getName();
         }
      } else if (callSiteConstructorInvc != null) {
         Object objArg = new Object();
         objArg = callSiteConstructorInvc.arguments().get(pNode1.parmPositionToBackTrack);
//         filePath = node.iMethodCallee.getResource().getLocation().toFile().getAbsolutePath();
         rootJavaElement = mdJavaElement;
         rootArgs = callSiteConstructorInvc.arguments();
         if (objArg instanceof SimpleName) {
            Map<String, VariableDeclarationFragment> variableDecl = node.getVariableDeclFragment();
            SimpleName varSPName = (SimpleName) objArg;
            if(variableDecl.get(varSPName.resolveBinding().toString())!=null) {
               VariableDeclarationFragment vdf = variableDecl.get(varSPName.resolveBinding().toString());
               varSPName = vdf.getName();
               rootName = vdf.getParent().toString();
               rootName = vdf.getName().getFullyQualifiedName().toString();
               rootLineNumber = detector.getLineNumber(varSPName.getStartPosition(), filePath);
               rootNodeName = varSPName.getFullyQualifiedName();
            }
         }
         else if (objArg instanceof StringLiteral) {
            StringLiteral varSPName = (StringLiteral) objArg;
            rootLineNumber = detector.getLineNumber(varSPName.getStartPosition(), filePath);
            rootName = varSPName.getParent().toString();
            rootNodeName = varSPName.getLiteralValue();
         }
         else {
            rootLineNumber = detector.getLineNumber(callSiteConstructorInvc.getStartPosition(), filePath);
            rootName = pNode1.getMethodDec().getReturnType2()+ " " + pNode1.toString();
            rootNodeName = callSiteConstructorInvc.resolveConstructorBinding().getName();
         }
      }
      if (rootLineNumber == -1) {
         return;
      }
      GNode srcGNode;
      if (rootName != "") {
         rootName = rootName.replace("\n", "");
         srcGNode = new GRootNode(String.valueOf(rootLineNumber), rootName, rootJavaElement, rootLineNumber);
      } else {
         srcGNode = new GRootNode(String.valueOf(rootLineNumber), rootNodeName, rootJavaElement, rootLineNumber);
      }
      GModelBuilder.instance().getNodes().add(srcGNode);
      treeNodes.add(srcGNode);
      String conId = srcGNode.getId() + dstGNode.getId();
      String edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
      GConnection con = new GConnection(conId, edgeInfo, dstGNode, srcGNode);
      GModelBuilder.instance().getConnections().add(con);
      dstGNode.getConnectedTo().add(srcGNode);

      int depth = 1;
      // STEP: loop through parent's until the root node is found
      while (parent != null && (parent instanceof CFGNode)) {
         CFGNode pNode = (CFGNode) parent;
         ASTNode rootCallSite = pNode.getRootCallSite();
         srcGNode = dstGNode;
         MethodDeclaration pMetDecl = pNode.getMethodDec();
         IJavaElement pMdJavaElement = pMetDecl.resolveBinding().getJavaElement();
         rootName = pNode.getMethodDec().getReturnType2()+ " " + pNode.toString();
         filePath = pNode.iMethodCallee.getResource().getLocation().toFile().getAbsolutePath();
         lineNumber = detector.getLineNumber(pNode.getMethodDec().getStartPosition(), filePath);
         dstGNode = new GPathNode(pNode.getID(), rootName, pMdJavaElement, lineNumber);
         GModelBuilder.instance().getNodes().add(dstGNode);
         treeNodes.add(dstGNode);
         conId = srcGNode.getId() + dstGNode.getId();
         edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
         con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
         GModelBuilder.instance().getConnections().add(con);
         srcGNode.getConnectedTo().add(dstGNode);
         if (!pNode.isRoot()) {
            parent = parent.getParent();
            ++depth;
         } else {
            // found the root node
            //			ASTNode rootCallSite = pNode.getRootCallSite();
            if (rootCallSite instanceof MethodInvocation) {
               MethodInvocation mi = (MethodInvocation) rootCallSite;
               // indicator items
               String indNodeName = buildIndNodeName(mi);
               int indLineNumber = detector.getLineNumber(mi.getStartPosition());
               IJavaElement indJavaElement = pNode.iMethodCallee;
               List<?> indArgs = mi.arguments();

               // if the javaElements are not the same
               // then ROOT and IND are in two different method declarations
               if (!indJavaElement.equals(rootJavaElement)) {
                  ++depth;
               }
               srcGNode = dstGNode;
               rootName = mi.getParent().getParent().toString();
               rootName = rootName.replace("\n", "");
               dstGNode = new GIndicatorNode(String.valueOf(indLineNumber), rootName, indJavaElement, indLineNumber);
               GModelBuilder.instance().getNodes().add(dstGNode);
               treeNodes.add(dstGNode);
               conId = srcGNode.getId() + dstGNode.getId();
               edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
               con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
               GModelBuilder.instance().getConnections().add(con);
               srcGNode.getConnectedTo().add(dstGNode);
               detector.filePath = rootJavaElement.getResource().getLocation().toFile().getPath();
               addMethodInvoc(metDecl, detector, detector.getGroup(), //
                     rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
                     indNodeName, indLineNumber, indJavaElement, indArgs, depth);
            }
            else if (rootCallSite instanceof ClassInstanceCreation) {
               ClassInstanceCreation cic = (ClassInstanceCreation) rootCallSite;
               String indNodeName = buildIndNodeName(cic);
               int indLineNumber = detector.getLineNumber(cic.getStartPosition());
               IJavaElement indJavaElement = mdJavaElement;
               List<?> indArgs = cic.arguments();
               // if the javaElements are not the same
               // then ROOT and IND are in two different method declarations
               if (!indJavaElement.equals(rootJavaElement)) {
                  ++depth;
               }
               srcGNode = dstGNode;
               ICompilationUnit compilationUnit = pNode.iMethodCallee.getCompilationUnit();
               IPackageDeclaration[] pkgDecl;
               String pkgName = "";
               try {
                  pkgDecl = compilationUnit.getPackageDeclarations();
                  if (pkgDecl.length != 1) {
                     System.out.println("\tPackage Declaration not found for: " + compilationUnit.getElementName());
                     // throw new RuntimeException("[WRN] SEE HERE (FindMisuse.getCGNode)");
                  } else {
                     pkgName = pkgDecl[0].getElementName();
                  }
               } catch (JavaModelException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
               rootName = cic.getParent().getParent().toString();
               rootName = rootName.replace("\n", "");
               dstGNode = new GIndicatorNode(String.valueOf(indLineNumber), rootName, indJavaElement, indLineNumber);
               GModelBuilder.instance().getNodes().add(dstGNode);
               treeNodes.add(dstGNode);
               conId = srcGNode.getId() + dstGNode.getId();
               edgeInfo = "(" + String.valueOf(srcGNode.getLineNumber()) + "," + String.valueOf(dstGNode.getLineNumber()) + ")";
               con = new GConnection(conId, edgeInfo, srcGNode, dstGNode);
               GModelBuilder.instance().getConnections().add(con);
               srcGNode.getConnectedTo().add(dstGNode);
               detector.filePath = rootJavaElement.getResource().getLocation().toFile().getPath();
               addMethodInvoc(metDecl, detector, detector.getGroup(), //
                     rootNodeName, rootLineNumber, rootJavaElement, rootArgs, //
                     indNodeName, indLineNumber, indJavaElement, indArgs, depth);
            }
            break;
         }
      }
   }

   /************************************************************************************************/
   /* ADD CFG NODE METHODS */

   public void addCFGNode(CFGNode node) {
      IMethod im = node.getIMethod();
      try {
         IPackageDeclaration[] pds = im.getCompilationUnit().getPackageDeclarations();
         String pkgName = pds[0].getElementName();
         String typeName = im.getDeclaringType().getElementName();
         String nodeName = im.getElementName();

         ProgramElement pkgElem = findPackageElement(pkgName);
         ProgramElement classElem = findClassElement(typeName);
         pkgElem.add(classElem);

         findMethodElement(nodeName, classElem);

      } catch (JavaModelException e) {
         e.printStackTrace();
      }
   }

   public void addCFGNode(CFGNode parent, CFGNode child, Indicator ind) {
      // get MethodDecl and MethodInvoc from parent CFGNode
      MethodDeclaration pmd = parent.getMethodDec();
      MethodInvocation pmi = parent.getCallSiteMethodInvc();

      MethodElement pme = null;
      if (pmd != null && pmi != null) {
         pme = addMethodInvoc(pmd, pmi, 0, Indicator.STEP);
      } else if (pmd != null) {
         pme = addMethodDecl(pmd, null);
      }

      // get child md and mi from CFGNode
      MethodDeclaration cmd = child.getMethodDec();

      if (cmd != null) {
         MethodElement cme = null;
         MethodInvocation cmi = child.getCallSiteMethodInvc();
         if (cmi != null) {
            cme = addMethodInvoc(cmd, cmi, 0, Indicator.STEP);
         } else {
            cme = addMethodDecl(cmd, null);
         }
         pme.add(cme);
      }
   }

   /************************************************************************************************/

   public static HashSet<String> getTreeList() {
      return treeList;
   }
   
   public void clearTreeViewer() {
      indicatorList.clear();
      treeNodes.clear();
      pkgElements.clear();
      classElements.clear();
      methodElements.clear();
      treeList.clear();
      patternConflict.clear();
      System.gc();
      Runtime.getRuntime().gc();
   }

   public static HashSet<String> getIndicatorList() {
      return indicatorList;
   }

}
