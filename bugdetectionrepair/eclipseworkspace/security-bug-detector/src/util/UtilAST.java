/**
 */
package util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

import input.IGlobalProperty;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class UtilAST implements IGlobalProperty {
   static final int INVALID_DOC = -1;
   static String fileContents = null;
   static Document doc = null;

   // Java Language Specification version used for ASTParser
   @SuppressWarnings("deprecation")
   static final int JLS_VERSION = AST.JLS8;
   
   public static ASTParser parse() {
      ASTParser parser = ASTParser.newParser(JLS_VERSION);
      configParser(parser);
      return parser;
   }

   public static CompilationUnit parse(char[] unit) {
      ASTParser parser = parse();
      parser.setSource(unit);
      return (CompilationUnit) parser.createAST(null); // parse
   }

   public static CompilationUnit parse(ICompilationUnit unit) {
      ASTParser parser = parse();
      parser.setSource(unit);
      return (CompilationUnit) parser.createAST(null); // parse
   }

   // Read classpathentry from .classpath to determine name of source folders
   public static List<String> readClassPathEntries(IProject project) throws JavaModelException {
      List<String> sourcePaths = new ArrayList<>();
      IJavaProject javaProject = JavaCore.create(project);
      IClasspathEntry[] entries = javaProject.getRawClasspath();
      for (int i = 0; i < entries.length; i++) {
         IClasspathEntry entry = entries[i];
         if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
            sourcePaths.add(entry.getPath().lastSegment());
         }
      }
      return sourcePaths;
   }

   public static IProject getOpenProject() {
      try {
         IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
         for (IProject project : projects) {
            if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) { // Check if we have a Java project.
               continue;
            }
            return project;
         }
      } catch (JavaModelException e) {
         e.printStackTrace();
      } catch (CoreException e) {
         e.printStackTrace();
      }

      return null;
   }

   public static String[] getOpenProjectSourcePath() {
      List<String> sourceList = new ArrayList<>();

      try {
         IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
         for (IProject project : projects) {
            if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) { // Check if we have a Java project.
               continue;
            }

            String url = "";
            if (System.getProperty("os.name").toLowerCase().contains("mac")) {
               url = File.pathSeparator;
            }
            url += project.getLocationURI().getPath();
            sourceList.add(url.substring(1));
         }
      } catch (JavaModelException e) {
         e.printStackTrace();
      } catch (CoreException e) {
         e.printStackTrace();
      }

      String[] ret = new String[sourceList.size()];
      return sourceList.toArray(ret);
   }

   public static String[] getProjectSourcePath(IProject project) throws CoreException {
      List<String> sourceList = new ArrayList<>();

      if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) { // Check if we have a Java project.
         return (String[]) sourceList.toArray();
      }

      String url = "";
      if (UTFile.isMacPlatform()) {
         url = File.pathSeparator;
      }
      url += project.getLocationURI().getPath() + "/";
      sourceList.add(url.substring(1));

      String[] ret = new String[sourceList.size()];
      return sourceList.toArray(ret);
   }

   /**
    * Return the first open java project
    * 
    * @return
    */
   public static String getFirstOpenProjectSourcePath() {
      String projectSourcePath = null;
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject project : projects) {
         try {
            // Check if we have an open Java project.
            if (project.isOpen() && project.isNatureEnabled(JAVANATURE)) {
               return project.getLocationURI().getPath() + "/";
            }
         } catch (CoreException e) {
            e.printStackTrace();
         }
      }
      return projectSourcePath;
   }

   public static String getWorkspaceRootSourcePath() {
      String projectSourcePath = null;
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject project : projects) {
         try {
            // Check if we have an open Java project.
            if (project.isOpen() && project.isNatureEnabled(JAVANATURE)) {
               return project.getParent().getLocationURI().getPath() + "/";
            }
         } catch (CoreException e) {
            e.printStackTrace();
         }
      }
      return projectSourcePath;
   }

   public static String findRTJar() {
      String ret = null;
      String[] split = classPathSystemProperty.split(File.pathSeparator);
      for (int i = 0; i < split.length; i++) {
         if (split[i].contains("rt.jar")) {
            return split[i];
         }
      }
      return ret;
   }

   public static CompilationUnit parse(String javaFilePath, boolean print) throws IOException {
//      System.out.println(javaFilePath);
      fileContents = UTFile.readEntireFile(javaFilePath);
      if (print) {
         System.out.println(fileContents);
      }
      ASTParser parser = ASTParser.newParser(JLS_VERSION);
      configParser(parser);
      parser.setUnitName(UTFile.getShortFileName(javaFilePath));

      String[] classpath = { findRTJar() };
      String[] openProjectSourcePath = getOpenProjectSourcePath();

      try {
         if (openProjectSourcePath == null) {
            System.out.println("!!! ERROR !!!: openProjectSourcePath is NULL !!!");
         }
         System.out.println("openProjectSourcePath: " + openProjectSourcePath.length);

         parser.setEnvironment(classpath, openProjectSourcePath, new String[] { IGlobalProperty.DEFAULT_FILE_ENCODING }, true);
      } catch (Exception e) {
         System.out.println(e);
      }

      parser.setSource(fileContents.toCharArray());
      parser.setSourceRange(0, fileContents.length());
      CompilationUnit cu = (CompilationUnit) parser.createAST(null);

      if (print && cu.getAST().hasBindingsRecovery()) {
         System.out.println("classpath=" + Arrays.toString(classpath));
         System.out.println("sourcepath=" + Arrays.toString(openProjectSourcePath));
         System.out.println("[DBG] Binding activated.");
      }
      return cu;
   }

   public static CompilationUnit parse(IProject project, String javaFilePath, boolean print) throws IOException, CoreException {
      fileContents = UTFile.readEntireFile(javaFilePath);
      if (print) {
         System.out.println(fileContents);
      }
      ASTParser parser = ASTParser.newParser(JLS_VERSION);
      configParser(parser);
      parser.setUnitName(UTFile.getShortFileName(javaFilePath));

      String[] classpath = { findRTJar() };
      String[] openProjectSourcePath = getProjectSourcePath(project);

      parser.setEnvironment(classpath, openProjectSourcePath, new String[] { IGlobalProperty.DEFAULT_FILE_ENCODING }, true);
      parser.setSource(fileContents.toCharArray());
      parser.setSourceRange(0, fileContents.length());
      CompilationUnit cu = (CompilationUnit) parser.createAST(null);

      if (print && cu.getAST().hasBindingsRecovery()) {
         System.out.println("classpath=" + Arrays.toString(classpath));
         System.out.println("sourcepath=" + Arrays.toString(openProjectSourcePath));
         System.out.println("[DBG] Binding activated.");
      }

      return cu;
   }

   private static void configParser(ASTParser parser) {
      parser.setResolveBindings(true);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setBindingsRecovery(true);
      Map<String, String> options = JavaCore.getOptions();
      options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
      options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
      options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
      parser.setCompilerOptions(options);
   }

   public static boolean contains(ICompilationUnit iUnit, String typeName) {
      boolean rst = false;
      try {
         IType[] types = iUnit.getAllTypes();
         for (IType iType : types) {
            String iTypeName = iType.getElementName();
            if (typeName.equals(iTypeName)) {
               rst = true;
               break;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return rst;
   }

   public static List<IMethod> getCallerMethods(IMethod method, IPackageFragment[] packages) {
      SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
      CallerMethodsSearchRequestor requestor = new CallerMethodsSearchRequestor();
      SearchEngine searchEngine = new SearchEngine();
      try {
         IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(packages);
         searchEngine.search(pattern, //
               new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, //
               searchScope, requestor, null);
         return requestor.getUniqueCallerMethods();
      } catch (Exception e) {
         return new ArrayList<IMethod>();
      }
   }

   // find matching methodDeclaration in typeDeclaration
   public static MethodDeclaration findMethodDec(IMethod javaMethodElem, TypeDeclaration typeDec) {
      String key1 = javaMethodElem.getKey();

      if (typeDec == null) {
         return null;
      }

      try {
         MethodDeclaration[] methods = typeDec.getMethods();
         for (MethodDeclaration iMethodDec : methods) {
            String key2 = iMethodDec.resolveBinding().getKey();
            if (StringUtils.equalsIgnoreCase(key1, key2)) {
               return iMethodDec;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   // find matching methodDeclaration in typeDeclaration
   public static MethodDeclaration findMethodDeclaration(IMethod iMethod, TypeDeclaration typeDec) {
      String key1 = iMethod.getKey();

      try {

         List<?> bd = typeDec.bodyDeclarations();
         for (int i = 0; i < bd.size(); i++) {
            if (bd.get(i) instanceof TypeDeclaration) {
               TypeDeclaration td = (TypeDeclaration) bd.get(i);
               MethodDeclaration[] methods = td.getMethods();
               for (MethodDeclaration iMethodDec : methods) {
                  String key2 = iMethodDec.resolveBinding().getKey();
                  if (StringUtils.equalsIgnoreCase(key1, key2)) {
                     return iMethodDec;
                  }
               }
            }
         }

      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
   }

   public static MethodDeclaration findConstructorInvocationParent(ASTNode n) {
      if (n.getParent() instanceof MethodDeclaration) {
         return (MethodDeclaration) n.getParent();
      } else {
         return findConstructorInvocationParent(n.getParent());
      }
   }

   public static ASTNode findMethodInvocationParent(ASTNode n) {
      if (n.getParent() instanceof MethodDeclaration) {
         return n;
      }
      if (n.getParent() instanceof VariableDeclarationFragment) {
         List<ASTNode> nodeList = new ArrayList<ASTNode>();
         VariableDeclarationFragment vdf = (VariableDeclarationFragment) n.getParent();
         vdf.accept(new ASTVisitor() {
            @Override
            public boolean visit(MethodInvocation miNode) {
               if (nodeList.size() == 0) {
                  nodeList.add(miNode);
               }
               return true;
            }
         });

         if (nodeList.size() != 0) {
            return nodeList.get(0);
         } else {
            return null;
         }
      }

      if (n != null && n.getParent() != null //
            && n.getParent() instanceof MethodInvocation) {
         return n.getParent();
      }
      return findMethodInvocationParent(n.getParent());
   }

   public static String getSignature(IMethod javaElemIMethod) {
      int count = 0;
      String parm = "";
      for (String p : javaElemIMethod.getParameterTypes()) {
         if (count++ == javaElemIMethod.getParameterTypes().length - 1) {
            break;
         }
         parm += (p + IGlobalProperty.COLUMN_SEPARATOR);
      }
      if (count != 0) {
         parm = parm + javaElemIMethod.getParameterTypes()[count - 1];
      }
      return javaElemIMethod.getElementName() + "(" + parm + ")";
   }

   public static String getFullSignature(IMethod javaElemIMethod) {
      int count = 0;
      String parm = "";
      for (String p : javaElemIMethod.getParameterTypes()) {
         if (count++ == javaElemIMethod.getParameterTypes().length - 1) {
            break;
         }
         parm += (p + IGlobalProperty.COLUMN_SEPARATOR);
      }
      if (count != 0) {
         parm = parm + javaElemIMethod.getParameterTypes()[count - 1];
      }

      String result = javaElemIMethod.getDeclaringType().getElementName() + "." //
            + javaElemIMethod.getElementName() //
            + "(" + count + ") (" + parm + ")";

      return result;
   }

   public static String getSignature(MethodInvocation callSiteMethInvc) {
      int count = 0;
      String parm = "";
      for (Object o : callSiteMethInvc.arguments()) {
         if (count++ == callSiteMethInvc.arguments().size() - 1) {
            break;
         }
         parm += (o.toString() + IGlobalProperty.COLUMN_SEPARATOR);
      }
      if (count != 0) {
         parm = parm + callSiteMethInvc.arguments().get(count - 1);
      }
      return callSiteMethInvc.getName().getIdentifier() + "(" + parm + ")";
   }

   public static String getSignature(ConstructorInvocation callSiteConstructorInvc) {
      int count = 0;
      String parm = "";
      for (Object o : callSiteConstructorInvc.arguments()) {
         if (count++ == callSiteConstructorInvc.arguments().size() - 1) {
            break;
         }
         parm += (o.toString() + IGlobalProperty.COLUMN_SEPARATOR);
      }
      if (count != 0) {
         parm = parm + callSiteConstructorInvc.arguments().get(count - 1);
      }
      return callSiteConstructorInvc.resolveConstructorBinding().getName() + "(" + parm + ")";
   }

   public static String getSignature(SimpleName spNameVar) {
      if (getType(spNameVar) != null) {
         return getType(spNameVar) + " " + spNameVar.getIdentifier();
      }
      return null;
   }

   public static String getType(SimpleName spNameVar) {
      IBinding binding = spNameVar.resolveBinding();
      if (binding != null && binding instanceof IVariableBinding) {
         IVariableBinding varBinding = (IVariableBinding) binding;
         return varBinding.getType().getName();
      }
      return null;
   }

   /**
    * <pre>
      VariableDeclarationStatement varDec = .. ;
      Type type = varDec.getType();
      CompilationUnit cUnit = UtilAST.getCUnit(type.toString());
      if (cUnit != null) {
       ..
      }
    * </pre>
    */
   public static CompilationUnit getCUnit(String pkgName, String typeName) {
      String fullyQualifiedName = StringUtils.isEmpty(pkgName) ? typeName : String.join(".", pkgName, typeName);
      CompilationUnit parsedCUnit = null;
      String f = getFirstOpenProjectSourcePath() + UTFile.getTypeFilePath();
      List<String> listTypeFile = UTFile.readFileToList(f);

      for (String iLine : listTypeFile) {
         String[] iTokens = iLine.split(IGlobalProperty.COLUMN_SEPARATOR);
         String iQName = iTokens[0].trim();

         if (iQName.equals(fullyQualifiedName)) {
            String filePath = iTokens[1].trim();
            try {
               parsedCUnit = UtilAST.parse(filePath, false);
               doc = new Document(fileContents);
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }

      return parsedCUnit;
   }

   public static List<String> getCryptoFilePaths(IProject project) throws CoreException {
      String[] projectSourcePath = getProjectSourcePath(project);
      String fileName = projectSourcePath[0] + UTFile.getDetectorFilePath();
      List<String> readFileToList = UTFile.readFileToList(fileName);
      return readFileToList;
   }

   public static String getGroupCategory(IProject project) throws CoreException {
      String[] projectSourcePath = getProjectSourcePath(project);
      String fileName = projectSourcePath[0] + "group";
      String readFile;
      try {
         readFile = UTFile.readFile(fileName);
         return readFile;
      } catch (Exception e) {
         UTFile.write(fileName, "default");
         return "default";
      }
   }

   public static String getFileContents() {
      return fileContents;
   }

   public static Document getDoc() {
      return doc;
   }

   public static ASTNode getParent(ASTNode node) {
      if (node instanceof VariableDeclaration) {
         return node;
      }
      if (node instanceof Statement) {
         return node;
      }
      if (node instanceof MethodInvocation) {
         return node;
      }
      ASTNode parentNode = node.getParent();
      return getParent(parentNode);
   }

   public static int getLineNum(int offset) {
      if (doc == null) {
         return INVALID_DOC;
      }

      int ret = 0;
      try {
         ret = getDoc().getLineOfOffset(offset) + 1;
      } catch (BadLocationException e) {
         e.printStackTrace();
      }
      return ret;
   }
}
