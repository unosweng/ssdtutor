/**
 * @file UtilChDistillerTest.java
 */
package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import visitors.CompilationUnitVisitor;

/**
 * @date Aug 3, 2016
 * @since JavaSE-1.8
 */
public class UtilChDistillerTest {

   // @Test
   public void getChangesTest() {
      File oldf = null; // new
                        // File("/runtime-code.review.tool/jedit.r7074/src/org/gjt/sp/jedit/View.java");
      File newf = null; // new
                        // File("/runtime-code.review.tool/jedit-rev-425/src/org/gjt/sp/jedit/View.java");
      List<SourceCodeChange> changes = null; // UtilChDistiller.getChanges(oldf,
                                             // newf);
      // print(changes);
      // System.out.println("------------------------");

      oldf = new File("C:\\Users\\ADMIN\\workspace2\\ds-sample\\example\\Super-1.java");
      newf = new File("C:\\Users\\ADMIN\\workspace2\\ds-sample\\example\\Super-2.java");
      changes = UtilChDistiller.getChanges(oldf, newf);
      for (SourceCodeChange iChange : changes) {
         if (iChange instanceof Insert) {
            Insert chgInsert = (Insert) iChange;
            System.out.println(chgInsert.getLabel()); // insert /
            System.out.println(chgInsert.getChangedEntity().getUniqueName()); //
            System.out.println(chgInsert.getRootEntity().getUniqueName()); //
            System.out.println(iChange.getChangedEntity().getType() + "\t" + iChange);
         }
         else if (iChange instanceof Delete) {
            Delete chgInsert = (Delete) iChange;
            System.out.println(chgInsert.getLabel()); // insert /6
            System.out.println(chgInsert.getChangedEntity().getUniqueName()); //
            System.out.println(chgInsert.getRootEntity().getUniqueName()); //
            System.out.println(iChange.getChangedEntity().getType() + "\t" + iChange);
         }
         else if (iChange instanceof Update) {
            Update chgInsert = (Update) iChange;
            chgInsert.getNewEntity().getStartPosition();
            System.out.println(chgInsert.getLabel()); // insert /
            System.out.println(chgInsert.getChangedEntity().getUniqueName()); //
            System.out.println(chgInsert.getNewEntity().getUniqueName()); //
            System.out.println("Root " + chgInsert.getRootEntity().getUniqueName()); //
            System.out.println("Parent " + chgInsert.getParentEntity().getUniqueName()); //
            System.out.println(chgInsert.getChangedEntity().getStartPosition()); //
         }
      }
      System.out.println("------------------------");
      System.out.println("=================================");
      System.out.println("Done.");
   }

   @Test
   public void getChangesInMethodsTest() {
      String s1 = getSource(System.getProperty("user.dir") + "\\example\\A.java");

      CompilationUnit cu1 = parse(s1.toCharArray());
      MethodVisitor visitor = new MethodVisitor();

      cu1.accept(visitor);
      // cu2.accept(visitor2);
      MethodDeclaration sourceMethodNode = visitor.methodNameAndItsNode.get("foo");
      MethodDeclaration destinationMethodNode = visitor.methodNameAndItsNode.get("bar");
      List<SourceCodeChange> changes = UtilChDistiller.diffNode(sourceMethodNode.getBody(), destinationMethodNode.getBody());
      printChanges(changes);
      System.out.println("---------------------------------------");
      CompilationUnitVisitor cUnitVisitor = new CompilationUnitVisitor();
      cu1.accept(cUnitVisitor);
   }

   private void printChanges(List<SourceCodeChange> changes) {
      for (SourceCodeChange iChange : changes) {
         if (iChange instanceof Insert) {
            Insert chgInsert = (Insert) iChange;

            System.out.println(chgInsert.getLabel()); // insert /
            System.out.println(chgInsert.getRootEntity().getUniqueName()); //
            // parent entity to which it was added.Taken from newer version.
            System.out.println(chgInsert.getParentEntity().getUniqueName());

            // changed entity refers to source code entity that was inserted.Taken from newer version.
            System.out.println(chgInsert.getChangedEntity().getType());
            System.out.println(chgInsert.getChangedEntity().getUniqueName()); //
            System.out.println(chgInsert.getChangedEntity().getSourceRange().getStart());
            System.out.println(chgInsert.getChangedEntity().getSourceRange().getEnd());

         }
         else if (iChange instanceof Delete) {
            Delete chgDelete = (Delete) iChange;
            System.out.println(chgDelete.getLabel()); // insert /6

            // parent entity from which it was deleted.Taken from previous version.
            System.out.println(chgDelete.getParentEntity().getUniqueName());
            System.out.println(chgDelete.getRootEntity().getUniqueName()); //

            // here the changed entity corresponds to the deleted source entity.
            System.out.println(chgDelete.getChangedEntity().getType());
            System.out.println(chgDelete.getChangedEntity().getUniqueName()); //
            System.out.println(chgDelete.getChangedEntity().getSourceRange().getStart());
            System.out.println(chgDelete.getChangedEntity().getSourceRange().getEnd());

         }
         else if (iChange instanceof Update) {
            Update chgUpdate = (Update) iChange;

            System.out.println(chgUpdate.getLabel()); //
            System.out.println("Root " + chgUpdate.getRootEntity().getUniqueName()); //

            // parent entity in which it was updated.
            System.out.println("Parent " + chgUpdate.getParentEntity().getUniqueName()); //

            // entity that got updated
            System.out.println(chgUpdate.getChangedEntity().getType()); //
            System.out.println(chgUpdate.getChangedEntity().getUniqueName()); //
            System.out.println(chgUpdate.getChangedEntity().getStartPosition()); //
            System.out.println(chgUpdate.getChangedEntity().getEndPosition()); //

            // newEntity is what the changedEntity becomes in newer version.
            System.out.println(chgUpdate.getNewEntity().getType()); //
            System.out.println(chgUpdate.getNewEntity().getUniqueName()); //
            System.out.println(chgUpdate.getNewEntity().getStartPosition()); //
            System.out.println(chgUpdate.getNewEntity().getEndPosition()); //
         }
         else if (iChange instanceof Move) {
            Move chgMove = (Move) iChange;
            System.out.println(chgMove.getLabel());
            // old parent entity
            System.out.println(chgMove.getParentEntity());
            // new parent entity
            System.out.println(chgMove.getNewParentEntity());

            System.out.println(chgMove.getRootEntity());
            System.out.println(chgMove.getChangeType());

            // newEntity is what the changedEntity becomes in newer version.
            System.out.println(chgMove.getNewEntity().getType()); //
            System.out.println(chgMove.getNewEntity().getUniqueName()); //
            System.out.println(chgMove.getNewEntity().getStartPosition()); //
            System.out.println(chgMove.getNewEntity().getEndPosition()); //

            // changedEntity is old Entity becomes in older version.
            System.out.println(chgMove.getChangedEntity().getType()); //
            System.out.println(chgMove.getChangedEntity().getUniqueName()); //
            System.out.println(chgMove.getChangedEntity().getStartPosition()); //
            System.out.println(chgMove.getChangedEntity().getEndPosition()); //

         }
      }
   }

   // @Test
   public void getChangesMethodTest() {
      String s1 = getSource("/runtime-code.review.tool/example-project-sample7/src/rev0/A.java");
      String s2 = getSource("/runtime-code.review.tool/example-project-sample7-rev-298/src/rev0/A.java");
      CompilationUnit cu1 = parse(s1.toCharArray());
      CompilationUnit cu2 = parse(s2.toCharArray());
      Visitor visitor1 = new Visitor();
      Visitor visitor2 = new Visitor();
      cu1.accept(visitor1);
      cu2.accept(visitor2);
      MethodDeclaration m1 = visitor1.n;
      MethodDeclaration m2 = visitor2.n;
      List<SourceCodeChange> changes = UtilChDistiller.diffNode(m1.getBody(), m2.getBody());
      print(changes);
      System.out.println("------------------------");
   }

   @SuppressWarnings("deprecation")
   public static CompilationUnit parse(char[] unit) {
      ASTParser parser = ASTParser.newParser(AST.JLS8);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setSource(unit);
      parser.setResolveBindings(true);
      return (CompilationUnit) parser.createAST(null);
   }

   private void print(List<SourceCodeChange> changes) {
      for (SourceCodeChange iChange : changes) {
         if (iChange instanceof Insert) {
            System.out.println(iChange.getChangedEntity().getType() + "\t" + iChange);
            equalMethodName(iChange, null);
         }
      }
   }

   private boolean equalMethodName(SourceCodeChange chg, MethodDeclaration methodDecl) {
      String srcChgParentName = chg.getRootEntity().getUniqueName();
      String parentNodeName = srcChgParentName.split(Pattern.quote("("))[0];
      String[] pNameList = parentNodeName.split(Pattern.quote("."));
      String parentMethodName = pNameList[pNameList.length - 1];
      return parentMethodName.equals(methodDecl.getName().getFullyQualifiedName());
   }

   public String getSource(String fileName) {
      StringBuilder buf = new StringBuilder();
      try (BufferedReader in = Files.newBufferedReader(Paths.get(fileName), Charset.forName("UTF-8"))) {
         String line = null;
         while ((line = in.readLine()) != null) {
            buf.append(line + "\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
      }
      return buf.toString();
   }
}

class Visitor extends ASTVisitor {
   MethodDeclaration n;

   public boolean visit(MethodDeclaration node) {
      if (node.getName().toString().contains("m1")) {
         this.n = node;
      }
      return true;
   }
}

class MethodVisitor extends ASTVisitor {
   public Map<String, MethodDeclaration> methodNameAndItsNode = new HashMap<String, MethodDeclaration>();

   public boolean visit(MethodDeclaration node) {
      if (!methodNameAndItsNode.containsKey(node.getName().toString())) {
         methodNameAndItsNode.put(node.getName().toString(), node);
      }
      return true;
   }
}