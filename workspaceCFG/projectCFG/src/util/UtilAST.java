/**
 */
package util;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * @since J2SE-1.8
 */
public class UtilAST {
   static final int INVALID_DOC = -1;
   static String fileContents = null;

   public static ASTParser parse() {
      ASTParser parser = ASTParser.newParser(AST.JLS16);
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

   private static void configParser(ASTParser parser) {
      parser.setResolveBindings(true);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setBindingsRecovery(true);
      Map<String, String> options = JavaCore.getOptions();
      options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_11);
      options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_11);
      options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_11);
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

   public static String readEntireFile(String filename) throws IOException {
      FileReader in = new FileReader(filename);
      StringBuilder contents = new StringBuilder();
      char[] buffer = new char[4096];
      int read = 0;
      do {
         contents.append(buffer, 0, read);
         read = in.read(buffer);
      }
      while (read >= 0);
      in.close();
      return contents.toString();
   }

   public static List<IMethod> getCallerMethods(IMethod method, IPackageFragment[] packages) {
      SearchPattern pattern = SearchPattern.createPattern(method, IJavaSearchConstants.REFERENCES);
      MethodCallerSearchRequestor requestor = new MethodCallerSearchRequestor();
      SearchEngine searchEngine = new SearchEngine();
      try {
         IJavaSearchScope searchScope = SearchEngine.createJavaSearchScope(packages);
         searchEngine.search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, searchScope, requestor, null);
         return requestor.getUniqueCallerMethods();
      } catch (CoreException e) {
         return new ArrayList<IMethod>();
      }
   }

   public static String getSignature(IMethod javaElemIMethod) {
      int count = 0;
      String parm = "";
      for (String p : javaElemIMethod.getParameterTypes()) {
         if (count++ == javaElemIMethod.getParameterTypes().length - 1) {
            break;
         }
         parm += (p + ",");
      }
      if (count != 0) {
         parm = parm + javaElemIMethod.getParameterTypes()[count - 1];
      }
      return javaElemIMethod.getElementName() + "(" + parm + ")";
   }

   public static MethodDeclaration findMethodDec(IMethod javaMethodElem, TypeDeclaration typeDec) {
      String key1 = javaMethodElem.getKey();

      if (typeDec == null) {
         return null;
      }

      try {
         MethodDeclaration[] methods = typeDec.getMethods();
         for (MethodDeclaration iMethodDec : methods) {
            String key2 = iMethodDec.resolveBinding().getKey();
            if (key1.toLowerCase().equals(key2.toLowerCase())) {
               return iMethodDec;
            }
            // if (StringUtils.equalsIgnoreCase(key1, key2)) {
            // return iMethodDec;
            // }
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
                  if (key1.toLowerCase().equals(key2.toLowerCase())) {
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
}
