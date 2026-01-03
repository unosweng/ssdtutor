package analysis;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import util.UtilAST;
import visitor.CFGVisitor;

public class ProjectAnalyzer {

   public ProjectAnalyzer() {

   }

   public void analyze() throws CoreException {
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject project : projects) {
         if (!project.isOpen() || !project.isNatureEnabled("org.eclipse.jdt.core.javanature")) {
            continue;

         }
         analyzePackages(JavaCore.create(project).getPackageFragments());
      }
   }

   protected void analyzePackages(IPackageFragment[] packages) {
      try {
         for (IPackageFragment iPackage : packages) {
            if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
               if (iPackage.getCompilationUnits().length < 1) {
                  continue;
               }
               analyzeCompilationUnit(iPackage.getCompilationUnits(), packages);
            }
         }
      } catch (JavaModelException e) {
         e.printStackTrace();
      }
   }

   private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits, IPackageFragment[] packages) {
      CFGVisitor visitor = new CFGVisitor(packages);
      for (ICompilationUnit iUnit : iCompilationUnits) {
         System.out.println("[DBG] Build CFG for " + iUnit.getElementName());
         CompilationUnit comUnit = UtilAST.parse(iUnit);
         comUnit.accept(visitor);
      }
   }
}
