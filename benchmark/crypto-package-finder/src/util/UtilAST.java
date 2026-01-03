/**
 */
package util;

import java.io.IOException; 
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
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
 * @author
 * @date
 * @since J2SE-1.8
 */
public class UtilAST {
   public static CompilationUnit parse(ICompilationUnit unit) {
      ASTParser parser = ASTParser.newParser(AST.JLS8);
      configParser(parser);
      parser.setSource(unit);
      return (CompilationUnit) parser.createAST(null); // parse
   }

   public static CompilationUnit parse(String javaFilePath, boolean print) throws IOException {
      String contents = UTFile.readEntireFile(javaFilePath);
      if (print)
         System.out.println(contents);

      ASTParser parser = ASTParser.newParser(AST.JLS8);
      configParser(parser);
      parser.setUnitName(javaFilePath);
      parser.setEnvironment(null, null, null, true);
      parser.setSource(contents.toCharArray());
      CompilationUnit createAST = null;
      try {
        createAST  = (CompilationUnit) parser.createAST(null);
      } catch (Exception e) {
         System.out.println("[ERR] File: " + javaFilePath);
         e.printStackTrace();
         return null;
      }
      return createAST;
   }

   private static void configParser(ASTParser parser) {
      parser.setResolveBindings(true);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      Map<String, String> options = JavaCore.getOptions();
      options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
      options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
      options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
      parser.setCompilerOptions(options);
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
         return requestor.getCallerMethods();
      } catch (CoreException e) {
         e.printStackTrace();
      }
      return null;
   }

   public static MethodDeclaration findMethodDec(IMethod javaMethodElem, TypeDeclaration typeDec) {
      try {
         MethodDeclaration[] methods = typeDec.getMethods();
         // String iCallerName = javaMethodElem.getElementName();
         int offsetMethod = javaMethodElem.getSourceRange().getOffset();

         for (MethodDeclaration iMethodDec : methods) {
            // String identifier = iMethodDec.getName().getIdentifier();
            int offsetMethodToReturn = iMethodDec.getStartPosition();
            if (offsetMethod == offsetMethodToReturn) {
               return iMethodDec;
            }
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return null;
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
}
