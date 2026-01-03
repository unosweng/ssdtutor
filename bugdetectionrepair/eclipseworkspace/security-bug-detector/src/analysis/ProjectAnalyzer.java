/*
 * @(#) ProjectAnalyzer.java
 */
package analysis;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.SystemUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
//import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import input.IGlobalProperty;
import model.ProgramElement;
import util.UtilAST;

/**
 * @since J2SE-1.8
 */
public class ProjectAnalyzer {
	private static final String JAVANATURE = "org.eclipse.jdt.core.javanature";
	IProject[] projects;
	List<String> targetFiles = new ArrayList<String>();
	Map<String, Map<String, List<String>>> targetMethodDeclList = new HashMap<String, Map<String, List<String>>>();
	Map<String, CompilationUnit> fileCUnitMap = new TreeMap<String, CompilationUnit>();
	Map<String, List<String>> commonFiles = new HashMap<String, List<String>>();
	Map<String, String> packageList = new HashMap<String, String>();
	Map<String, String> classList = new HashMap<String, String>();
	private String paramSeperator = ":";
	private static String pkgName, className;
	private String prjName;
	Map<String, List<ProgramElement>> fileInfo = new HashMap<>();


	public void analyze(Map<String, List<ProgramElement>> tableData) {
		// =============================================================
		// 1st step: Project
		// =============================================================
		try {
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			this.fileInfo = tableData;
			for (IProject project : projects) {
				if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE)) { // Check if we have a Java project.
					continue;
				}
				this.prjName = project.getName();
				analyzePackages(JavaCore.create(project).getPackageFragments());
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void analyzePackages(IPackageFragment[] packages) throws CoreException, JavaModelException {
		// =============================================================
		// 2nd step: Packages
		// =============================================================
		for (IPackageFragment iPackage : packages) {
			List<ProgramElement> cls = this.fileInfo.get(iPackage.getElementName());
			if(cls==null && (iPackage.getElementName()=="")) {
				cls = this.fileInfo.get("default");
			}

			if ((iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) && (cls!=null)) {
				if (iPackage.getCompilationUnits().length < 1) {
					continue;
				}
				analyzeCompilationUnit(iPackage.getCompilationUnits(), cls);
			}
		}
	}

	private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits, List<ProgramElement>cls) throws JavaModelException {
		//		String workspacePath = Platform.getInstanceLocation().getURL().getFile();

		// =============================================================
		// 3rd step: ICompilationUnits
		// =============================================================
		for (ICompilationUnit iUnit : iCompilationUnits) {
			for (ProgramElement clsElement: cls) {
				String clsName = clsElement.getName() + ".java";
				List<String> methodParams = new ArrayList<String>();
				if(clsName.equals(iUnit.getElementName())) {
					String file = iUnit.getPath().toOSString();
					String iFile = iUnit.getResource().getLocation().toFile().getAbsolutePath();
					String seperator =  IGlobalProperty.PATH_SEPARATOR;
					if(SystemUtils.IS_OS_WINDOWS) {
						seperator = IGlobalProperty.PATH_SEPARATOR_WINDOWS;
					}
					String filePath = iFile.replace(file, "");
					File folderPath = new File(filePath);
					String iFileAbsPath = iUnit.getPath().toFile().getAbsoluteFile().toString();
					iFileAbsPath = iFileAbsPath.substring(iFileAbsPath.indexOf(seperator)+1);
					iFileAbsPath = iFileAbsPath.substring(iFileAbsPath.indexOf(seperator));
					String replaceString = seperator +iFileAbsPath;
					String commonPath = iFileAbsPath.replace(replaceString,"");
					CompilationUnit iCUnit = UtilAST.parse(iUnit);
					fileCUnitMap.put(iFile, iCUnit);
					iCUnit.accept(new ASTVisitor() {
						public boolean visit(PackageDeclaration node) {
							pkgName = node.getName().getFullyQualifiedName();
							return true;
						}
						public boolean visit(TypeDeclaration node) {
							className = node.getName().getFullyQualifiedName();
							return true;
						}

						public boolean visit(MethodDeclaration node) {
							String methodParam = node.getName().getFullyQualifiedName() + paramSeperator + className + paramSeperator + pkgName;
							methodParams.add(methodParam);
							return true;
						}	
					});
					List<String> pathList = new ArrayList<String>();
					for(String folder: folderPath.list()) {
						if(folder.contains(this.prjName) && (folder.contains("repair") || folder.contains("original"))) {
							Map<String, List<String>> methodMap = new HashMap<String, List<String>>();
							String path =  filePath + File.separator + file.replace(this.prjName, folder);
							pathList.add(path);
							targetFiles.add(path);
							if(targetMethodDeclList.get(commonPath)==null){
								methodMap.put(file.replace(this.prjName, folder), methodParams);
								targetMethodDeclList.put(commonPath, methodMap);
							} else {
								methodMap = targetMethodDeclList.get(commonPath);
								methodMap.put(file.replace(this.prjName, folder), methodParams);
								targetMethodDeclList.put(commonPath, methodMap);
							}
						}
					}
					commonFiles.put(commonPath, pathList);
				}
			}
		}
	}

	public List<String> getTargetFiles() {
		return targetFiles;
	}

	public Map<String, Map<String, List<String>>> getTargetMethodDeclList() {
		return targetMethodDeclList;
	}

	public Map<String, List<String>> getCommonFiles() {
		return commonFiles;
	}

	public Map<String, CompilationUnit> getFileCUnitMap() {
		return fileCUnitMap;
	}

	public Map<String, String> getPackageList() {
		return packageList;
	}

	public Map<String, String> getClassList() {
		return classList;
	}
}