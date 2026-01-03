package analysis;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import input.IGlobalProperty;
import util.PrintHelper;
import util.TableViewerHelper;
import util.UtilAST;
import util.UtilFile;
import view.CryptoMisuseDetectionTree;
import visitor.DetectionASTVisitor;

public class ProgramTreeAnalyzer {
   private CryptoMisuseDetectionTree view;
   private DetectionASTVisitor visitor;
   private List<String> cryptoPaths;
   private IPackageFragment[] packages;
   private static String repairPath;
   private static String originalPath;
   private static String rootPath;

   public ProgramTreeAnalyzer(CryptoMisuseDetectionTree view, DetectionASTVisitor visitor) {
      this.view = view;
      this.visitor = visitor;
   }

   public void analyze() {
      if (this.view == null || this.visitor == null) {
         PrintHelper.printDebugMsgH("ERROR - view/visitor is null");
         return;
      }

      // =============================================================
      // 1st step: Project
      // =============================================================
      try {
         IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
         for (IProject project : projects) {
            if (!project.isOpen() || !project.isNatureEnabled(IGlobalProperty.JAVANATURE)) { // Check if we have a Java project.
               continue;
            }

            // determine if javaFilePath exists in Crypto detection list
            cryptoPaths = UtilAST.getCryptoFilePaths(project);
            String group = UtilAST.getGroupCategory(project);
            this.visitor.setGroup(group);
            setRootPath(project.getLocation().toFile().getPath());
            UtilFile.getDirectoryPath(project);

            if (cryptoPaths.size() > 0) {
               File f = new File(originalPath);
               if (!f.exists()) {
                  try {
                     FileUtils.copyDirectory(project.getLocation().toFile(), f);
                     String projectFilePath = originalPath + IGlobalProperty.PATH_SEPARATOR + ".project";
                     File projectFile = new File(projectFilePath);
                     projectFile.delete();
                     File newProject = new File(projectFilePath);
                     FileWriter f2 = new FileWriter(newProject, false);
                     String[] subString = originalPath.split("/");
                     String projectContent = UtilFile.getProjectFile(subString[subString.length-1]);
                     f2.write(projectContent);
                     f2.close();
                  } catch (IOException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                  }
               }
               File repairFile = new File(repairPath);
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
               analyzePackages(JavaCore.create(project).getPackageFragments());
            } else {
               PrintHelper.printDebugMsg("SKIP: " + project.getName() + " -> DETECTOR file is empty");
            }
         }
      } catch (JavaModelException e) {
         e.printStackTrace();
      } catch (CoreException e) {
         e.printStackTrace();
      }
   }

   protected void analyzePackages(IPackageFragment[] packages) throws CoreException, JavaModelException {
      // =============================================================
      // 2nd step: Packages
      // =============================================================
      for (IPackageFragment iPackage : packages) {
         if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
            if (iPackage.getCompilationUnits().length < 1) {
               continue;
            }
            this.packages = packages;
            this.visitor.setPackageName(iPackage.getElementName());
            analyzeCompilationUnit(iPackage.getCompilationUnits());
         }
      }
   }

   private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
      // =============================================================
      // 3rd step: ICompilationUnits
      // =============================================================
      for (ICompilationUnit iUnit : iCompilationUnits) {
         String iUnitPath = iUnit.getResource().getLocation().toFile().getPath();
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         String className = TableViewerHelper.getClassNameFromJavaFile(iUnit.getElementName());
         if (cryptoPaths.contains(javaFilePath)) {
            PrintHelper.printDebugMsg("Running Detector on: " + javaFilePath);
            this.visitor.setJavaFilePath(javaFilePath);
            this.visitor.setFileName(iUnit.getElementName());
            this.visitor.setTreeView(this.view);
            this.visitor.setiPackages(this.packages);
            this.visitor.setClassName(className);
            this.visitor.setRepairPath(getRepairPath());
            this.visitor.setFilePath(iUnitPath);
            this.visitor.setOriginalPath(getOriginalPath());
            CompilationUnit comUnit = UtilAST.parse(iUnit);
            comUnit.accept(visitor);
         }
      }
   }

   public String getRepairPath() {
      return repairPath;
   }

   public static void setRepairPath(String string) {
      repairPath = string;
   }

   public String getOriginalPath() {
      return originalPath;
   }

   public static void setOriginalPath(String string) {
      originalPath = string;
   }

   public String getRootPath() {
      return rootPath;
   }

   public static void setRootPath(String string) {
      rootPath = string;
   }
}
