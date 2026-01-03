/**
 */
package analysis;


import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ui.internal.browser.InternalBrowserViewInstance;
import org.eclipse.ui.internal.browser.WebBrowserView;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.eclipse.ui.internal.browser.browsers.DefaultBrowser;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import model.ModelProvider;
import model.ProjectElement;
import util.UtilAST;
import view.CryptoMisuseDetectionTree;
import visitor.DeclarationVisitor;

import java.net.MalformedURLException;
import java.net.URL;

public class ProjectAnalysis {
	@Inject
	IWorkbenchPart part;
	private static final String JAVANATURE = "org.eclipse.jdt.core.javanature";
	private IProject[] projects;
	private static String projectName;
	int methodCount;
	private static final String csvSeperator = ",";
	private static final String lineSeperator = "\n";
	private Map<String, Integer> pkgCount = new HashMap<String, Integer>();
	private Map<String, Map<String, Integer>> classPkgCount = new HashMap<String, Map<String, Integer>>();
	StringBuilder sb = new StringBuilder();

	public ProjectAnalysis() {
		// Get all projects in the workspace.
		projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	public void analyze() {
//		System.out.println("Browser");
		//		IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
		//		IWebBrowser browser;
		//		try {
		//			browser = browserSupport.createBrowser("internalBrowser");
		//			browser.openURL(new URL("https://www.eclipse.org"));
		//		} catch (PartInitException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		} catch (MalformedURLException e) {
		//			// TODO Auto-generated catch block
		//			e.printStackTrace();
		//		}
		for (IProject project : projects) {
			try {
				if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) {
					continue;
				}
				IJavaProject javaProject = JavaCore.create(project);
				projectName = project.getName();
				System.out.println(projectName);
				analyzeJavaProject(javaProject);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		try {
			csvWriter(ModelProvider.INSTANCE.getProgramElements());
			//			csvWriter();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void analyzeJavaProject(IJavaProject project) throws CoreException, JavaModelException {
		// Check if we have a Java project.
		IPackageFragment[] packages = project.getPackageFragments();

		int pkg = 0;
		int classTotal = 0;
		for (IPackageFragment iPackage : packages) {
			if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
				if (iPackage.getCompilationUnits().length < 1) {
					continue;
				}
				analyzeCompilationUnit(iPackage);
				classTotal += iPackage.getCompilationUnits().length;
				pkg += 1;
			}
		}
	}

	private void analyzeCompilationUnit(IPackageFragment iPackage) throws JavaModelException {
		for (ICompilationUnit iUnit : iPackage.getCompilationUnits()) {
			CompilationUnit compilationUnit = UtilAST.parse(iUnit);
			DeclarationVisitor declVisitor = new DeclarationVisitor(this.projectName);
			compilationUnit.accept(declVisitor);
		}
	}

	private void csvWriter(List<ProjectElement> elements) throws IOException {
		StringBuilder sb = new StringBuilder();
		System.out.println(elements);
		for(ProjectElement element:elements){
			sb.append(element.getPrjName());
			sb.append(csvSeperator);
			sb.append(element.getGroupName());
			sb.append(csvSeperator);
			sb.append(element.getPkgName());
			sb.append(csvSeperator);
			sb.append(element.getClassName());
			sb.append(csvSeperator);
			sb.append(element.getMethodName());
			sb.append(csvSeperator);
			sb.append(element.getLocalVariable());
			sb.append(csvSeperator);
			sb.append(element.getFieldVariable());
			sb.append(lineSeperator);
		}
		String path = "/Users/dpradhan/Desktop/outputNew.csv";
		File csvFile = new File(path);
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}
		PrintWriter writer = new PrintWriter(csvFile);
		writer.write(sb.toString());
		System.out.println("File is exported");
		writer.close();
	}

	private void csvWriter() throws IOException {
		StringBuilder sb = new StringBuilder();
		for (Map.Entry<String, Map<String, Integer>> entry : classPkgCount.entrySet()) {
			String key = entry.getKey();
			Map<String, Integer> value = entry.getValue();
			sb.append(key);
			sb.append(csvSeperator);
			sb.append(value.get("package"));
			sb.append(csvSeperator);
			sb.append(value.get("class"));
			sb.append(lineSeperator);
		}
		String path = "/Users/dpradhan/Desktop/classPackage1.csv";
		File csvFile = new File(path);
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}
		PrintWriter writer = new PrintWriter(csvFile);
		writer.write(sb.toString());
		System.out.println("File is exported");
		writer.close();
	}
}