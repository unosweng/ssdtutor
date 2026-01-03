/*
 * @(#) GenerateFilePathOfType.java
 *
 * Computer Science, The University of Nebraska at Omaha
 */
package handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import input.IGlobalProperty;
import input.IRuleInfo;
import util.PrintHelper;
import util.UTFile;
import util.UtilAST;

/**
 * @since J2SE-1.8 (Java SE 8 [1.8.0_71])
 */
public class GenerateFilePathOfTypeHandler implements IGlobalProperty {
   final String JAVANATURE = "org.eclipse.jdt.core.javanature";
   String pkgName;
   IPackageFragment[] packages;
   String csvTypesFileName = UTFile.getRuntimeDirPath();
   String csvDetectorFileName = csvTypesFileName;
   List<String> listCryptoPath = new ArrayList<>();

   @Execute
   public void execute() {
      PrintHelper.printDebugMsgH("Generate File Path of Type (Preprocess 1)");
      try {
         analyze();
      } catch (CoreException | IOException e) {
         e.printStackTrace();
      }

      PrintHelper.printDebugMsgT("Done.");
   }

   public void analyze() throws CoreException, IOException {
      // =============================================================
      // 1st step: Project
      // =============================================================
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject project : projects) {
         if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) {
            continue;
         }
         
         listCryptoPath.clear();
         String[] projectSourcePath = UtilAST.getProjectSourcePath(project);
         csvTypesFileName = projectSourcePath[0] + UTFile.getTypeFilePath();
         csvDetectorFileName = projectSourcePath[0] + UTFile.getDetectorFilePath();
         UTFile.truncate(csvTypesFileName);
         UTFile.truncate(csvDetectorFileName);

         PrintHelper.printDebugMsgH("Analyze Project: " + project.getName());
         PrintHelper.printDebugMsg("Type File: " + csvTypesFileName);
         PrintHelper.printDebugMsg("Crypto File: " + csvDetectorFileName);
         
         IPackageFragment[] packageFragments = JavaCore.create(project).getPackageFragments();
         analyzePackages(project, packageFragments);
         
         // Create a list with the distinct elements using stream.
         List<String> listDistinct = listCryptoPath.stream().distinct().collect(Collectors.toList());
         UTFile.writeFile(csvDetectorFileName, listDistinct);
      }
   }

   void analyzePackages(IProject project, IPackageFragment[] packages) throws CoreException, JavaModelException, IOException {
      // =============================================================
      // 2nd step: Packages
      // =============================================================
      for (IPackageFragment iPackage : packages) {
         if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
            // System.out.println(iPackage.getKind() + ":" + iPackage.getCompilationUnits().length + ":" + iPackage.toString());
            ICompilationUnit[] compilationUnits = iPackage.getCompilationUnits();
            if (compilationUnits.length < 1) {
               continue;
            }
            this.packages = packages;
            analyzeCompilationUnit(project, compilationUnits);
         }
      }
   }

   void analyzeCompilationUnit(IProject project, ICompilationUnit[] iCompilationUnits) throws IOException, CoreException {
      // =============================================================
      // 3rd step: ICompilationUnits
      // =============================================================
      List<String> listTypePath = new ArrayList<>();
      for (ICompilationUnit iUnit : iCompilationUnits) {
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         CompilationUnit comUnit = UtilAST.parse(project, javaFilePath, false);

         PrintHelper.printDebugMsg(javaFilePath);
         // first check import declarations to determine if a crypto import exists
         comUnit.accept(new ASTVisitor() {
            
            public boolean visit(ImportDeclaration node) {
               String importName = node.getName().getFullyQualifiedName();
               if (IRuleInfo.DETECTION_LIST.contains(importName)) {
                  listCryptoPath.add(javaFilePath); // just need the java file path here
               }
               return true;
            }

            /*
             * Example use case without an import:
             *  java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
             */
            public boolean visit(MethodInvocation mi) {
               if (listCryptoPath.contains(javaFilePath)) {
                  return true;
               }
               if (mi.resolveMethodBinding() != null &&
                     mi.resolveMethodBinding().getDeclaringClass() != null) {
                  String qName = mi.resolveMethodBinding().getDeclaringClass().getQualifiedName();
                  if (IRuleInfo.DETECTION_LIST.contains(qName)) {
                     listCryptoPath.add(javaFilePath); // just need the java file path here
                  }
               }
               return true;
            }
         });
         
         // now visit both the package and type declarations
         comUnit.accept(new ASTVisitor() {
            public boolean visit(PackageDeclaration node) {
               pkgName = node.getName().getFullyQualifiedName();
               return true;
            }

            public boolean visit(TypeDeclaration node) {
               String outputTypePath = pkgName + "." + node.getName().getFullyQualifiedName() + IGlobalProperty.COLUMN_SEPARATOR + javaFilePath;
               listTypePath.add(outputTypePath);
               return true;
            }
         });
      }

      Collections.sort(listTypePath);
      UTFile.writeFile(csvTypesFileName, listTypePath);
   }
}