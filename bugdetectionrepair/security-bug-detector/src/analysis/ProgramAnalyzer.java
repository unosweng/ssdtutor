/**
 */
package analysis;

import java.util.List;

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
import util.UtilAST;
import view.CryptoMisuseDetection;
import visitor.DetectionASTVisitor;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class ProgramAnalyzer {
   private CryptoMisuseDetection view;
   private DetectionASTVisitor visitor;
   private List<String> cryptoPaths;
   private IPackageFragment[] packages;

   public ProgramAnalyzer(CryptoMisuseDetection view, DetectionASTVisitor visitor) {
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

            if (cryptoPaths.size() > 0) {
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
            analyzeCompilationUnit(iPackage.getCompilationUnits());
         }
      }
   }

   private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
      // =============================================================
      // 3rd step: ICompilationUnits
      // =============================================================
      for (ICompilationUnit iUnit : iCompilationUnits) {
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();

         if (cryptoPaths.contains(javaFilePath)) {
            PrintHelper.printDebugMsg("Running Detector on: " + javaFilePath);
            this.visitor.setJavaFilePath(javaFilePath);
            this.visitor.setView(this.view);
            this.visitor.setiPackages(this.packages);
            CompilationUnit comUnit = UtilAST.parse(iUnit);
            comUnit.accept(visitor);
         }
      }
   }
}