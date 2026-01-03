package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEdit;
import org.apache.commons.io.FileUtils;

import input.IGlobalProperty;
import model.MethodElement;
import visitor.repair.JCA1ReplaceVisitor;
import visitor.repair.JCA2ReplaceVisitor;
import visitor.repair.JCA3ReplaceVisitor;
import visitor.repair.JCA4ReplaceVisitor;
import visitor.repair.JCA5ReplaceVisitor;
import visitor.repair.JCA6ReplaceVisitor;
import visitor.repair.JCA7ReplaceVisitor;
import visitor.repair.JCA8ReplaceVisitor;
import visitor.repair.JCA9ReplaceVisitor;

public class RepairHelper {

   private String searchVal;
   private String newVal, root, repDir;
   private MethodElement me;
   private File orginalDir;
   private int i = 1;
   private static boolean haspatternConflict;
   private static HashMap <String, Integer> conflictIdSet = new HashMap<>();


   public RepairHelper(String searchVal, String newVal) {
      this.searchVal = searchVal;
      this.newVal = newVal;
   }

   public RepairHelper(MethodElement detectedItem) {
      this.me = detectedItem;
   }

   // returns the first open compilation unit
   private ICompilationUnit[] getOpenCompilationUnit(MethodElement me) throws JavaModelException {
      List<ICompilationUnit> iUnits = new ArrayList<>();

      IJavaProject jp = me.getJavaElement().getJavaProject();
      IPackageFragment[] packages = jp.getPackageFragments();
      root = jp.getResource().getLocation().toFile().getPath();
      for (IPackageFragment iPackage : packages) {
         boolean checkPackage = (iPackage.getElementName().equals(me.getPackageName())) || //
               (me.getPackageName()==null);
         if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE && //
               iPackage.getCompilationUnits().length >= 1 && checkPackage) {
            String file = me.getFileName();
            ICompilationUnit unit = iPackage.getCompilationUnit(file);
            if(!unit.getResource().getLocation().toFile().getPath().equals(me.getFilePath())) {
               continue;
            }
            iUnits.add(unit);
         }
      }
      return iUnits.stream().toArray(ICompilationUnit[]::new);
   }

   /***************************************************************************
    * 
    * Public Misc Helper Methods
    * 
    ***************************************************************************/
   @SuppressWarnings("unchecked")
   public void listReWriteImport(CompilationUnit astRoot, ASTRewrite rewrite, String newImport) {
      AST ast = astRoot.getAST();

      ImportDeclaration id = ast.newImportDeclaration();

      id.setName(ast.newName(newImport));
      ListRewrite listRewrite = rewrite.getListRewrite(astRoot, CompilationUnit.IMPORTS_PROPERTY);

      List<ImportDeclaration> list = listRewrite.getRewrittenList();
      boolean found = list.stream().anyMatch(item -> item.getName().getFullyQualifiedName().equals(id.getName().getFullyQualifiedName()));
      if (!found) {
         listRewrite.insertLast(id, null);
      }
   }


   /***************************************************************************
    * 
    * Public JCA Helper Methods
    * @throws Exception 
    * 
    ***************************************************************************/

   public void repair(int caseId) throws Exception {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      switch (caseId){
      case 1:
         replace_AES_ECB(iUnits);
         break;
      case 2:
         replace_SHA1(iUnits);
         break;
      case 3:
         replace_ConstantSecretKey(iUnits);
         break;
      case 4:
         replace_PBE_Constant_Salt(iUnits);
         break;
      case 5:
         replace_PBE_Constant_Interation(iUnits);
         break;
      case 6:
         replace_SecureRandom_ConstantSeed(iUnits);
         break;
      case 7:
         replace_Constant_KeyGenerator(iUnits);
         break;
      case 8:
         replace_Constant_SecretKeyFactory(iUnits);
         break;
      case 9:
         replace_Constant_IvParameterSpec(iUnits);
         break;
      }
   }
   //	// JCA 1
   public void replace_AES_ECB(MethodElement me) throws Exception {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_AES_ECB(iUnits);
   }

   // JCA 2
   public void replace_SHA1(MethodElement me) throws Exception {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_SHA1(iUnits);
   }

   // JCA 3
   public void replace_ConstantSecretKey(MethodElement me) throws Exception {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_ConstantSecretKey(iUnits);
   }

   // JCA 4
   public void replace_PBE_Constant_Salt(MethodElement me) throws JavaModelException  {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_PBE_Constant_Salt(iUnits);
   }

   // JCA 5
   public void replace_PBE_Constant_Interation(MethodElement me) throws JavaModelException {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_PBE_Constant_Interation(iUnits);
   }

   // JCA 6
   public void replace_SecureRandom_ConstantSeed(MethodElement me) throws JavaModelException {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_SecureRandom_ConstantSeed(iUnits);
   }

   // JCA 7
   public void replace_Constant_KeyGenerator(MethodElement me) throws Exception {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_Constant_KeyGenerator(iUnits);
   }

   //JCA8
   public void replace_Constant_SecretKeyFactory(MethodElement me) throws Exception {
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_Constant_SecretKeyFactory(iUnits);
   }

   //JCA9
   public void replace_Constant_IvParameterSpec(MethodElement me) throws Exception{
      ICompilationUnit[] iUnits = getOpenCompilationUnit(me);
      replace_Constant_IvParameterSpec(iUnits);
   }

   /***************************************************************************
    * 
    * Private Helper Methods
    * 
    ***************************************************************************/

   // JCA 1
   private void replace_AES_ECB(ICompilationUnit[] iCompilationUnits) throws Exception {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }
         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);
         
         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA1ReplaceVisitor visitor = new JCA1ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);
         //
         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath();
            if(me.getPatternConflict()==true) {
               repairPath = me.getRepairPath()+ IGlobalProperty.NAME_SEPARATOR + "1";
               File repairFile = new File(repairPath);
               File f = new File(me.getOriginalPath());
               if (!repairFile.exists()) {
                  try {
//                     File orgFile = new File(originalPath);
                     FileUtils.copyDirectory(f, repairFile);
                     String projectFilePath = repairPath + IGlobalProperty.PATH_SEPARATOR + ".project";
                     File projectFile = new File(projectFilePath);
                     projectFile.delete();
                     File newProject = new File(projectFilePath);
                     FileWriter f2 = new FileWriter(newProject, false);
                     String[] subString = repairPath.split("/");
                     String projectContent = UtilFile.getProjectFile(subString[subString.length-1]);
                     f2.write(projectContent);
                     f2.close();
                  } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               }
            }
            repairPath = repairPath + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   // JCA 2
   private void replace_SHA1(ICompilationUnit[] iCompilationUnits) throws Exception {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }
         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);
         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA2ReplaceVisitor visitor = new JCA2ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);

         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   // JCA 3
   private void replace_ConstantSecretKey(ICompilationUnit[] iCompilationUnits) throws Exception {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }
         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);

         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA3ReplaceVisitor visitor = new JCA3ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);

         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   // JCA 4
   private void replace_PBE_Constant_Salt(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }

         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);

         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA4ReplaceVisitor visitor = new JCA4ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);

         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath); 
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }

   }

   // JCA 5
   private void replace_PBE_Constant_Interation(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }

         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);

         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA5ReplaceVisitor visitor = new JCA5ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);

         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }

   }

   private void replace_SecureRandom_ConstantSeed(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }

         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);

         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA6ReplaceVisitor visitor = new JCA6ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);
         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   //JCA 7
   private void replace_Constant_KeyGenerator(ICompilationUnit[] iCompilationUnits) throws Exception {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }
         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);

         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA7ReplaceVisitor visitor = new JCA7ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);
         //
         if (!visitor.isUpdated()) {
            continue;
         }

         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   //JCA8

   private void replace_Constant_SecretKeyFactory(ICompilationUnit[] iCompilationUnits) throws Exception {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }
         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);
         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA8ReplaceVisitor visitor = new JCA8ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);
         //
         if (!visitor.isUpdated()) {
            continue;
         }

         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   //JCA9
   private void replace_Constant_IvParameterSpec(ICompilationUnit[] iCompilationUnits) throws Exception {
      for (ICompilationUnit iUnit : iCompilationUnits) {
         if (!iUnit.isOpen()) {
            continue;
         }
         ICompilationUnit workingCopy = iUnit.getWorkingCopy(null);

         // Creation of DOM/AST from a ICompilationUnit
         CompilationUnit astRoot = UtilAST.parse(workingCopy);
         ASTRewrite rewrite = ASTRewrite.create(astRoot.getAST()); // Creation of ASTRewrite
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         JCA9ReplaceVisitor visitor = new JCA9ReplaceVisitor(astRoot, rewrite, me, javaFilePath);

         astRoot.accept(visitor);

         if (!visitor.isUpdated()) {
            continue;
         }
         try {
            TextEdit edits = null;
            edits = rewrite.rewriteAST(); // Compute the edits
            workingCopy.applyTextEdit(edits, null); // Apply the edits.
            workingCopy.commitWorkingCopy(false, null); // Save the changes.
            Path fromFile = iUnit.getResource().getLocation().toFile().toPath();
            String repairPath = me.getRepairPath() + fromFile.toFile().getPath().replace(root, "");
            replaceFiles(fromFile, repairPath);
         } catch (Exception e) {
            // System.out.println("[DBG] Exception: " + iUnit.getElementName());
            // e.printStackTrace();
         }
      }
   }

   private void replaceFiles(Path fromFile, String toFile) {
      // TODO Auto-generated method stub
      File file = new File(toFile);
      file.delete();
      try {
         Files.copy(fromFile, file.toPath());
      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public static void setpatternConflict(boolean conflict) {
      haspatternConflict = conflict;
   }

   public boolean checkpatternConflict() {
      return haspatternConflict;
   }

   public static void setConflictId(HashMap <String, Integer> idMaps) {
      conflictIdSet = idMaps;
   }

   public static HashMap <String, Integer> getConflictId() {
      return conflictIdSet;
   }
}
