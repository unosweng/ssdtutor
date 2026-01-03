package handlers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

import analysis.ProjectAnalyzer;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import model.ChangeDistillerProgramElement;
import util.UtilChDistiller;
import view.ChangeDistiller;
import view.CryptoMisuseDetectionTree;
import model.ProgramElement;

public class FileLevelChangeDistillerHandler {
	final String _L = System.lineSeparator();
	Map<String, List<String>> commonFiles = new HashMap<String, List<String>>();
	private String removeString, className;
	@Inject
	private EPartService epartService;


	@Execute
	public void execute() {
		System.out.println("FileLevelChangeDistiller!");
		MPart findPart = epartService.findPart(ChangeDistiller.VIEW_ID);
		Object findPartObj = findPart.getObject();
		MPart findPart1 = epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID);
		Object findPartObj1 = findPart1.getObject();
		Map<String, List<ProgramElement>> treeData = new HashMap<>();

		if (findPartObj1 instanceof CryptoMisuseDetectionTree) {
			CryptoMisuseDetectionTree v = (CryptoMisuseDetectionTree) findPartObj1;
			treeData = v.getTreeData();
			System.out.println(treeData);
			ProjectAnalyzer prjAnal = new ProjectAnalyzer();
			if(treeData.size() > 0) {
				prjAnal.analyze(treeData);
				commonFiles = prjAnal.getCommonFiles();
			}
		}

		if (findPartObj instanceof ChangeDistiller) {
			ChangeDistiller v = (ChangeDistiller) findPartObj;
			v.clear();
			ProjectAnalyzer prjAnal = new ProjectAnalyzer();
			if(treeData.size() > 0) {
				prjAnal.analyze(treeData);
				commonFiles = prjAnal.getCommonFiles();
				diffFileLevel(v, commonFiles);
			}    
			v.setInput(ChangeDistillerProgramElement.INSTANCE.getProgramElements());
		}
	}

	void diffFileLevel(ChangeDistiller v, Map<String, List<String>> commonFiles) {
		File oldf, newf;
		List<SourceCodeChange> changes;
		for (List<String> value : commonFiles.values()) {
			newf = new File(value.get(1));
			oldf = new File(value.get(0));
			IDocument doc2 = UtilChDistiller.getDoc(value.get(1));
			IDocument doc1 = UtilChDistiller.getDoc(value.get(0));
			changes = UtilChDistiller.getChanges(oldf, newf);
			saveChanges(changes, doc1, doc2, value.get(1).replace("///", "/"), value.get(0).replace("///", "/"));
		}
	}

	private void saveChanges(List<SourceCodeChange> changes, IDocument doc1, IDocument doc2, String oldFile, String newFile) {
		try {
			for (SourceCodeChange iChange : changes) {
				if (iChange instanceof Insert) {
					Insert chgInsert = (Insert) iChange;

					String rootName = chgInsert.getRootEntity().getUniqueName();
					String[] rootSplit = rootName.replace(".", "/").split("/");
					String methodName = rootSplit[rootSplit.length-1];
					if(!methodName.contains("(")) {
						className = methodName;
						className = className + ".java";
						removeString = "." + className;
						methodName = "";
					} else {
						String[] methodSplit = methodName.split("\\(");
						className = rootSplit[rootSplit.length-2];
						className = className + ".java";
						removeString = "." + className + "." + methodName;
						methodName = methodSplit[0];
					}
					String pkgName = rootName.replace(removeString, "");

					int bgnOffset = chgInsert.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgInsert.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;

					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Insert", chgInsert.getLabel(), chgInsert.getChangedEntity().getType(), chgInsert.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Delete) {
					Delete chgDelete = (Delete) iChange;

					String rootName = chgDelete.getRootEntity().getUniqueName();
					String[] rootSplit = rootName.replace(".", "/").split("/");
					String methodName = rootSplit[rootSplit.length-1];
					if(!methodName.contains("(")) {
						className = methodName;
						className = className + ".java";
						removeString = "." + className;
						methodName = "";
					} else {
						String[] methodSplit = methodName.split("\\(");
						className = rootSplit[rootSplit.length-2];
						className = className + ".java";
						removeString = "." + className + "." + methodName;
						methodName = methodSplit[0];
					}
					String pkgName = rootName.replace(removeString, "");

					int bgnOffset = chgDelete.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgDelete.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;

					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Delete", chgDelete.getLabel(), chgDelete.getChangedEntity().getType(), chgDelete.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Update) {
					Update chgUpdate = (Update) iChange;

					String rootName = chgUpdate.getRootEntity().getUniqueName();
					String[] rootSplit = rootName.replace(".", "/").split("/");
					String methodName = rootSplit[rootSplit.length-1];
					if(!methodName.contains("(")) {
						className = methodName;
						className = className + ".java";
						removeString = "." + className;
						methodName = "";
					} else {
						String[] methodSplit = methodName.split("\\(");
						className = rootSplit[rootSplit.length-2];
						className = className + ".java";
						removeString = "." + className + "." + methodName;
						methodName = methodSplit[0];
					}
					String pkgName = rootName.replace(removeString, "");

					int bgnOffset = chgUpdate.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgUpdate.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;

					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Update", chgUpdate.getLabel(), chgUpdate.getChangedEntity().getType(), chgUpdate.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Move) {
					Move chgMove = (Move) iChange;

					String rootName = chgMove.getRootEntity().getUniqueName();
					String[] rootSplit = rootName.replace(".", "/").split("/");
					String methodName = rootSplit[rootSplit.length-1];
					if(!methodName.contains("(")) {
						className = methodName;
						className = className + ".java";
						removeString = "." + className;
						methodName = "";
					} else {
						String[] methodSplit = methodName.split("\\(");
						className = rootSplit[rootSplit.length-2];
						className = className + ".java";
						removeString = "." + className + "." + methodName;
						methodName = methodSplit[0];
					}
					String pkgName = rootName.replace(removeString, "");

					int bgnOffset = chgMove.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgMove.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;

					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Move", chgMove.getLabel(), chgMove.getChangedEntity().getType(), chgMove.getChangedEntity().getUniqueName(), sourceChange);
				}
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}