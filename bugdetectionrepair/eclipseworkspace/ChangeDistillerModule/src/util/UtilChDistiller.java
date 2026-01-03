/*
 * @(#) UtilChDistiller.java
 *
 * Copyright 2015-2018 The Software Analysis Laboratory
 * Computer Science, The University of Nebraska at Omaha
 * 6001 Dodge Street, Omaha, NE 68182.
 */
package util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ch.uzh.ifi.seal.changedistiller.ChangeDistiller;
import ch.uzh.ifi.seal.changedistiller.ChangeDistiller.Language;
import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.ast.java.Comment;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaDeclarationConverter;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaMethodBodyConverter;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.distilling.FileDistiller;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.util.CompilationUtils;
import changedistiller.UTChangeDistiller;

/**
 * @author Myoungkyu Song
 * @date Feb 13, 2016
 * @since J2SE-1.8 (Java SE 8 [1.8.0_40])
 */
@SuppressWarnings("restriction")
public class UtilChDistiller {
   static final Injector sInjector;
   static JavaDeclarationConverter sDeclarationConverter;
   static JavaMethodBodyConverter sMethodBodyConverter;

   static {
      sInjector = Guice.createInjector(new JavaChangeDistillerModule());
      sDeclarationConverter = sInjector.getInstance(JavaDeclarationConverter.class);
      sMethodBodyConverter = sInjector.getInstance(JavaMethodBodyConverter.class);
   }

   /**
    * Get Distiller
    */
   public static Distiller getDistiller(StructureEntityVersion structureEntity) {
      return sInjector.getInstance(DistillerFactory.class).create(structureEntity);
   }

   /**
    * Get changes
    */
   public static List<SourceCodeChange> getChanges(File file1, File file2) {
      FileDistiller distiller = ChangeDistiller.createFileDistiller(Language.JAVA);
      try {
         File left = file1;
         File right = file2;
         distiller.extractClassifiedSourceCodeChanges(left, right);
      } catch (Exception e) {
         System.err.println("Warning: error while change distilling. " + e.getMessage());
      }

      List<SourceCodeChange> changes = distiller.getSourceCodeChanges();
      return changes;
   }

   public static List<SourceCodeChange> getChanges(String file1, String file2, String mName1, String mName2) {
      // *
      // Create Node1 from file1's method name1.
      JavaCompilation javaCom1 = CompilationUtils.compileSource(UtilFileChgDis.getContents(file1));
      AbstractMethodDeclaration absMethod1 = CompilationUtils.findMethod(javaCom1.getCompilationUnit(), mName1);
      Node root1 = new Node(JavaEntityType.METHOD, mName1);
      root1.setEntity(new SourceCodeEntity(mName1, JavaEntityType.METHOD, //
            new SourceRange(absMethod1.declarationSourceStart, absMethod1.declarationSourceEnd)));
      List<Comment> comments1 = CompilationUtils.extractComments(javaCom1);
      sMethodBodyConverter.initialize(root1, absMethod1, comments1, javaCom1.getScanner());
      absMethod1.traverse(sMethodBodyConverter, (ClassScope) null);

      // Create Node2 from file1's method name2.
      JavaCompilation javaCom2 = CompilationUtils.compileSource(UtilFileChgDis.getContents(file2));
      AbstractMethodDeclaration absMethod2 = CompilationUtils.findMethod(javaCom2.getCompilationUnit(), mName2);
      Node root2 = new Node(JavaEntityType.METHOD, mName2);
      root2.setEntity(new SourceCodeEntity(mName2, JavaEntityType.METHOD, //
            new SourceRange(absMethod2.declarationSourceStart, absMethod2.declarationSourceEnd)));
      List<Comment> comments2 = CompilationUtils.extractComments(javaCom2);
      sMethodBodyConverter.initialize(root2, absMethod2, comments2, javaCom2.getScanner());
      absMethod2.traverse(sMethodBodyConverter, (ClassScope) null);

      return diffNode(root1, root2);
   }

   public static IDocument getDoc(String file) {
      JavaCompilation javaCom = CompilationUtils.compileSource(UtilFileChgDis.getContents(file));
      return new Document(javaCom.getSource());
   }

   /**
    * Compute difference in nodes
    */
   public static List<SourceCodeChange> diffNode(Block normalizedBlock, Node aNode) {
      String[] nodeStatements1 = normalizedBlock.toString().split("\n");
      String[] nodeStatements2 = new String[nodeStatements1.length - 2];
      for (int i = 1; i < nodeStatements1.length - 1; i++) {
         nodeStatements1[i].concat("\n");
         nodeStatements2[i - 1] = nodeStatements1[i];
      }

      Node bNode = UtilChgDisNode.convertCodeSnippets(nodeStatements2);
      if (bNode == null || aNode == null) {
         throw new RuntimeException("[WRN] Exception !!");
      }
      return diffNode(aNode, bNode);
   }

   /**
    * Compute difference in nodes
    */
   public static List<SourceCodeChange> diffNode(ASTNode astNode1, ASTNode astNode2) {
      String[] aNodeStatements1 = astNode1.toString().split("\n");
      String[] aNodeStatements2 = new String[aNodeStatements1.length - 2];
      for (int i = 1; i < aNodeStatements1.length - 1; i++) {
         aNodeStatements1[i].concat("\n");
         aNodeStatements2[i - 1] = aNodeStatements1[i];
      }
      Node aNode = UtilChgDisNode.convertCodeSnippets(aNodeStatements1);

      String[] bNodeStatements1 = astNode2.toString().split("\n");
      String[] bNodeStatements2 = new String[bNodeStatements1.length - 2];
      for (int i = 1; i < bNodeStatements1.length - 1; i++) {
         bNodeStatements1[i].concat("\n");
         bNodeStatements2[i - 1] = bNodeStatements1[i];
      }
      Node bNode = UtilChgDisNode.convertCodeSnippets(bNodeStatements1);
      return diffNode(aNode, bNode);
   }

   /**
    * Compute difference in nodes
    */
   public static List<SourceCodeChange> diffNode(ASTNode normalizedBlock, Node aNode) {
      String[] nodeStatements1 = normalizedBlock.toString().split("\n");
      String[] nodeStatements2 = new String[nodeStatements1.length - 2];
      for (int i = 1; i < nodeStatements1.length - 1; i++) {
         nodeStatements1[i].concat("\n");
         nodeStatements2[i - 1] = nodeStatements1[i];
      }
      Node bNode = UtilChgDisNode.convertCodeSnippets(nodeStatements1);
      return diffNode(aNode, bNode);
   }

   /**
    * Compute difference in nodes
    */
   public static List<SourceCodeChange> diffNode2(ASTNode n1, ASTNode n2) {
      String[] nodeStatements1 = getStmts(n1);
      String[] nodeStatements2 = getStmts(n2);
      Node bNode1 = UtilChgDisNode.convertCodeSnippets(nodeStatements1);
      Node bNode2 = UtilChgDisNode.convertCodeSnippets(nodeStatements2);
      return diffNode(bNode1, bNode2);
   }

   /**
    * Compute difference in nodes
    */
   public static List<SourceCodeChange> diffNode(ArrayList<String> firStmts, ArrayList<String> secStmts) {
      String[] nodeStatements1 = new String[firStmts.size()];
      String[] nodeStatements2 = new String[secStmts.size()];
      for (int i = 0; i < firStmts.size(); i++) {
         nodeStatements1[i] = firStmts.get(i);
      }
      for (int j = 0; j < secStmts.size(); j++) {
         nodeStatements2[j] = secStmts.get(j);
      }
      Node bNode1 = UtilChgDisNode.convertCodeSnippets(nodeStatements1);
      Node bNode2 = UtilChgDisNode.convertCodeSnippets(nodeStatements2);
      return diffNode(bNode1, bNode2);
   }

   /**
    * Get statements
    */
   private static String[] getStmts(ASTNode n1) {
      String[] nodeStatements1 = n1.toString().split("\n");
      String[] nodeStatements2 = new String[nodeStatements1.length - 2];
      for (int i = 1; i < nodeStatements1.length - 1; i++) {
         nodeStatements1[i].concat("\n");
         nodeStatements2[i - 1] = nodeStatements1[i];
      }
      return nodeStatements1;
   }

   /**
    * Compute difference in nodes
    */
   private static List<SourceCodeChange> diffNode(Node aNode, Node bNode) {
      UTChangeDistiller changeDistill = new UTChangeDistiller();
      List<SourceCodeChange> changes = changeDistill.diff(aNode.copy(), bNode.copy());
      if (changes == null) {
         throw new RuntimeException("[WRN] Exception !!");
      }
      return changes;
   }

   /**
    * Print changes
    */
   public static boolean printChanges(List<SourceCodeChange> changes) {
      for (int i = 0; i < changes.size(); i++) {
         SourceCodeChange change = changes.get(i);
         if (change instanceof Update) {
            Update chUpdate = (Update) change;
            System.out.println("[DBG0] Update: " + chUpdate);

            SourceCodeEntity ch = chUpdate.getChangedEntity();
            SourceCodeEntity nw = chUpdate.getNewEntity();

            String before_lb = ch.getLabel();
            String after_lb = nw.getLabel();

            if (!before_lb.equals(after_lb)) {
               System.out.println("Label not equal:" + before_lb + " and " + after_lb);
            }
            System.out.println(ch);
            System.out.println(nw);

         }
         else if (change instanceof Move) {
            Move chMove = (Move) change;
            System.out.println("[DBG1] Move: " + chMove);
         }
         else if (change instanceof Insert) {
            Insert chInsert = (Insert) change;
            System.out.println("[DBG2] Insert: " + chInsert.getChangedEntity().getLabel() + " " + //
                  chInsert.getChangedEntity().getUniqueName());
         }
         else if (change instanceof Delete) {
            Delete chDelete = (Delete) change;
            System.out.println("[DBG3] Delete: " + chDelete.getChangedEntity().getLabel() + " " + //
                  chDelete.getChangedEntity().getUniqueName());
         }
         else {
            throw new RuntimeException("[WRN] LOOK AT HERE, PLEASE!!");
         }
      }
      return true;
   }

}
