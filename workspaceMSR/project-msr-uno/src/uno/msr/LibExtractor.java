package uno.msr;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import util.UtilAST;

public class LibExtractor {

   public static final String _root = "/opt/workspaceMSR/"; // Change to your system path

   public static final String _src = _root + "input-libs";

   public static List<String> _files = new ArrayList<String>();

   // // JCA
   // "java.security","java.security.cert","java.security.spec","java.security.interfaces","javax.crypto","javax.crypto.spec","javax.crypto.interfaces",
   // // JCE
   // // JSSE ????
   // "javax.net","javax.net.ssl",
   // // JGSS
   // "javax.security.auth.kerberos","sun.security.jgss","org.ietf.jgss",
   // // SASL
   // "javax.security.sasl","com.sun.security.sasl",
   // // PKCS
   // "sun.security.pkcs","sun.security.pkcs10",
   // // OCSP
   // "sun.security",
   // // JAAS
   // "javax.security.auth.login",
   // // BouncyCastle
   // "org.bouncycastle.crypto",
   // // SpongyCastle
   // "org.spongycastle.crypto",
   // // Google HTTP Client Library
   // "com.google.api.client.util",
   // // tink
   // "com.google.crypto.tink",
   // // jasypt
   // "org.jasypt"

   public static String[] _packages = { "java.security.*", "javax.crypto.*", "javax.net.*", "javax.security.*", "sun.security.*", "org.ietf.jgss.*", "com.sun.security.*", "org.bouncycastle.crypto.*", "org.spongycastle.crypto.*", "com.google.api.client.util.*", "com.google.crypto.tink.*", "org.jasypt.*" };

   public static List<Pattern> _list_patterns = new ArrayList<Pattern>();

   public static void main(String args[]) throws IOException {
      for (String pack : _packages) {
         _list_patterns.add(Pattern.compile(pack.replace(".*", "").replace(".", "\\.")));
         _list_patterns.add(Pattern.compile(pack.replace(".", "\\.").replace("*", ".*")));
      }

      Files.walk(Paths.get(_src)).filter(path -> !Files.isDirectory(path)).forEach(path -> _files.add(path.toAbsolutePath().toString()));
      System.out.println("Total: " + _files.size());

      StringBuffer sb = new StringBuffer();
      sb.append("path,package,file,class,method").append("\n");
      for (String file : _files) {
         System.out.println(file);
         String result = parse(file);
         if (result != null) {
            sb.append(result);
         }
      }
      writeFile(_root + "libapi.csv", sb.toString());
   }

   public static String parse(String path) throws IOException {
      String str = readFileToString(path);
      ASTParser parser = ASTParser.newParser(UtilAST.JLS_VERSION);
      parser.setSource(str.toCharArray());
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setResolveBindings(true);

      final CompilationUnit cu = (CompilationUnit) parser.createAST(null);

      if (cu.getPackage() == null) {
         return null;
      }

      String packageName = cu.getPackage().getName().toString();
      for (Pattern pattern : _list_patterns) {
         if (pattern.matcher(packageName).matches()) {
            MethodVisitor visitor = new MethodVisitor(path, packageName);
            cu.accept(visitor);
            return visitor.toString();
         }
      }

      return null;
   }

   public static String readFileToString(String filePath) throws IOException {
      StringBuilder fileData = new StringBuilder(1000);
      BufferedReader reader = new BufferedReader(new FileReader(filePath));
      char[] buf = new char[10];
      int numRead = 0;
      while ((numRead = reader.read(buf)) != -1) {
         String readData = String.valueOf(buf, 0, numRead);
         fileData.append(readData);
         buf = new char[1024];
      }
      reader.close();
      return fileData.toString();
   }

   public static void writeFile(String filepath, String content) {
      try {
         FileWriter myWriter = new FileWriter(filepath);
         myWriter.write(content);
         myWriter.close();
      } catch (IOException e) {
         // System.out.println("----" + e.getMessage());
      }
   }
}
