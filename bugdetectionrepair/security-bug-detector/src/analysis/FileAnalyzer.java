/*
 * @(#) ProjectAnalyzer.java
 */
package analysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.*;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;

import model.ChangeDistillerElement;
import model.ProgramElement;

/**
 * @since J2SE-1.8
 */
public class FileAnalyzer {
	private ChangeDistillerElement pe;
	private static String javaCode;
	private static final String JAVANATURE = "org.eclipse.jdt.core.javanature";
	private List<Integer> range = new ArrayList<Integer>();


	public void analyze(String projectName, ChangeDistillerElement pe) {
		// =============================================================
		// 1st step: Project
		// =============================================================
		try {
			this.pe = pe;
			IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
			for (IProject project : projects) {
				if (!project.isOpen() || !project.isNatureEnabled(JAVANATURE) || !project.getName().equals(projectName)) { // Check if we have a Java project.
					continue;
				}
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
			if ((iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) || (iPackage.getElementName().equals(pe.getPkgName()))) {
				if (iPackage.getCompilationUnits().length < 1) {
					continue;
				}
				analyzeCompilationUnit(iPackage.getCompilationUnits());
			}
		}
	}

	private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits) throws JavaModelException {
		// =============================================================
		// 3rd step: ICompilationUnits
		// =============================================================
		for (ICompilationUnit iUnit : iCompilationUnits) {
			if (iUnit.getElementName().equals(pe.getClassName())) {
				openInEditor(iUnit.getPrimaryElement());
			}
		}
	}

	public void openInEditor(IJavaElement ie) {
		try {
			IEditorPart editorPart = JavaUI.openInEditor(ie, true, true);
			if (!(editorPart instanceof ITextEditor)) {
				return;
			}

			ITextEditor editor = (ITextEditor) editorPart;
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			javaCode = document.get();
			List<Integer> sourceRange = new ArrayList<Integer>();
			for(int i=0 ; i<document.getNumberOfLines(); i++) {
				IRegion lineInfo = document.getLineInformation(i);
//				String lineValue = document.get(lineInfo.getOffset(), lineInfo.getLength());
				String lineNumber = pe.getSourceRangeLine();
				String[] lineNumbers = lineNumber.split("~");
				String startValue = lineNumbers[0].strip();
				int start = Integer.parseInt(startValue);
				String endValue = lineNumbers[lineNumbers.length-1].strip();
				int end = Integer.parseInt(endValue);
				end = end + 1;
				if(i+1==start || i+1==end) {
					sourceRange.add(lineInfo.getOffset());
				}
//				sb.append(i+1);
////				sb.append("\t");
//				sb.append(lineValue);
//				sb.append("\n");
			}
			range = sourceRange;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getJavaCode() {
		return javaCode;
	}

	public List<Integer> getSourceRange() {
		return range;
	}

}