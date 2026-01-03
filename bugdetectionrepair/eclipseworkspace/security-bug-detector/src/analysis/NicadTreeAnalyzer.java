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

public class NicadTreeAnalyzer {
	private CryptoMisuseDetectionTree view;
	private DetectionASTVisitor visitor;
	private List<String> cryptoPaths;
	private IPackageFragment[] packages;
	private static String repairPath;
	private static String originalPath;
	private static String rootPath;

	public NicadTreeAnalyzer(CryptoMisuseDetectionTree view, DetectionASTVisitor visitor) {
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
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("sample");
			project.open(null);
			//         for (IProject project : projects) {
			//            if (!project.isOpen() || !project.isNatureEnabled(IGlobalProperty.JAVANATURE)) { // Check if we have a Java project.
			//               continue;
			analyzePackages(JavaCore.create(project).getPackageFragments());
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
				System.out.println(iPackage.getCompilationUnits());
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
			iUnit.open(null);
			String iUnitPath = iUnit.getResource().getLocation().toFile().getPath();
			String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
			String className = TableViewerHelper.getClassNameFromJavaFile(iUnit.getElementName());
			PrintHelper.printDebugMsg("Running Detector on: " + javaFilePath);
			System.out.println(javaFilePath);
			this.visitor.setJavaFilePath(javaFilePath);
			this.visitor.setFileName(iUnit.getElementName());
			this.visitor.setTreeView(this.view);
			this.visitor.setiPackages(this.packages);
			this.visitor.setClassName(className);
			this.visitor.setFilePath(iUnitPath);
			CompilationUnit comUnit = UtilAST.parse(iUnit);
			comUnit.accept(visitor);

		}
	}
}
