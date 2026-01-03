package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import util.UtilChDistiller;
import visitors.CompilationUnitVisitor;

public class ChangeDistillerTest {
   @Test
   public void testChangeDistiller() {
      File oldf = null; // new
      // File("/runtime-code.review.tool/jedit.r7074/src/org/gjt/sp/jedit/View.java");
      File newf = null; // new
      // File("/runtime-code.review.tool/jedit-rev-425/src/org/gjt/sp/jedit/View.java");
      List<SourceCodeChange> changes = null; // UtilChDistiller.getChanges(oldf,
      // newf);
      // print(changes);
      // System.out.println("------------------------");

      oldf = new File(
            // System.getProperty("user.dir") + "\\" +
            "example/Version1.java");
      newf = new File(
            // System.getProperty("user.dir") + "\\" +
            "example/Version2.java");
      changes = UtilChDistiller.getChanges(oldf, newf);
      printChanges(changes);
      /*
      System.out.println("-------------------------------------------------");
      String s1 = getContentOfFile(System.getProperty("user.dir")+"\\example\\Version1.java");	
      CompilationUnit cu1 = parse(s1.toCharArray());
      CompilationUnitVisitor cUnitVisitor = new CompilationUnitVisitor();
      cu1.accept(cUnitVisitor);*/
   }

   private void printChanges(List<SourceCodeChange> changes) {
      for (SourceCodeChange iChange : changes) {
         if (iChange instanceof Insert) {
            Insert chgInsert = (Insert) iChange;

            System.out.println(chgInsert.getLabel()); // insert /
            System.out.println(chgInsert.getRootEntity().getUniqueName()); //
            // parent entity to which it was added.Taken from newer version.
            System.out.println(chgInsert.getParentEntity().getUniqueName());

            // changed entity refers to source code entity that was
            // inserted.Taken from newer version.
            System.out.println(chgInsert.getChangedEntity().getType());
            System.out.println(chgInsert.getChangedEntity().getUniqueName()); //
            System.out.println(chgInsert.getChangedEntity().getSourceRange().getStart());
            System.out.println(chgInsert.getChangedEntity().getSourceRange().getEnd());

         } else if (iChange instanceof Delete) {
            Delete chgDelete = (Delete) iChange;
            System.out.println(chgDelete.getLabel()); // insert /6

            // parent entity from which it was deleted.Taken from previous
            // version.
            System.out.println(chgDelete.getParentEntity().getUniqueName());
            System.out.println(chgDelete.getRootEntity().getUniqueName()); //

            // here the changed entity corresponds to the deleted source
            // entity.
            System.out.println(chgDelete.getChangedEntity().getType());
            System.out.println(chgDelete.getChangedEntity().getUniqueName()); //
            System.out.println(chgDelete.getChangedEntity().getSourceRange().getStart());
            System.out.println(chgDelete.getChangedEntity().getSourceRange().getEnd());

         } else if (iChange instanceof Update) {
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
         } else if (iChange instanceof Move) {
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

   public CompilationUnit parse(char[] unit) {
      ASTParser parser = ASTParser.newParser(AST.JLS8);
      parser.setKind(ASTParser.K_COMPILATION_UNIT);
      parser.setSource(unit);
      parser.setResolveBindings(true);
      return (CompilationUnit) parser.createAST(null);
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

   private static String getContentOfFile(String filename) {
      char[] b = new char[1024];
      StringBuilder sb = new StringBuilder();
      try {
         FileReader reader = new FileReader(new File(filename));
         int n = reader.read(b);
         while (n > 0) {
            sb.append(b, 0, n);
            n = reader.read(b);
         }
         reader.close();
      } catch (IOException e) {
         e.printStackTrace();
      }
      return sb.toString();
   }
}
