package handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
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
import util.UtilList;

public class MethodLevelChangeDistillerHandler {
	final String _L = System.lineSeparator();
	Map<String, List<String>> commonFiles = new HashMap<String, List<String>>();
	Map<String, Map<String, List<String>>> targetMethods = new HashMap<String, Map<String, List<String>>>();
	List<String> targetFiles = new ArrayList<String>();

	@Inject
	private EPartService epartService;
	@Execute
	public void execute() {
		System.out.println("MethodLevelChangeDistiller!");
		MPart findPart = epartService.findPart(ChangeDistiller.VIEW_ID);
		Object findPartObj = findPart.getObject();
		MPart findPart1 = epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID);
		Object findPartObj1 = findPart1.getObject();
		Map<String, List<ProgramElement>> tableData = new HashMap<>();

		if (findPartObj1 instanceof CryptoMisuseDetectionTree) {
			CryptoMisuseDetectionTree v = (CryptoMisuseDetectionTree) findPartObj1;
			tableData = v.getTreeData();
			ProjectAnalyzer prjAnal = new ProjectAnalyzer();
			if(tableData.size() > 0) {
				prjAnal.analyze(tableData);
				commonFiles = prjAnal.getCommonFiles();
			}
		}

		if (findPartObj instanceof ChangeDistiller) {
			ChangeDistiller v = (ChangeDistiller) findPartObj;
			v.clear();
			ProjectAnalyzer prjAnal = new ProjectAnalyzer();
			if(tableData.size() > 0) {
				prjAnal.analyze(tableData);
				targetMethods = prjAnal.getTargetMethodDeclList();
				targetFiles = prjAnal.getTargetFiles();
				diffMethodLevel(v, targetMethods, targetFiles);
				v.setInput(ChangeDistillerProgramElement.INSTANCE.getProgramElements());
			}    
			
		}
	}

	void diffMethodLevel(ChangeDistiller v, Map<String, Map<String, List<String>>> targetMethods, List<String> targetFiles) {
		for(Entry<String, Map<String, List<String>>> values: targetMethods.entrySet()) {
			Map<String, List<String>> value = values.getValue();
			Set<String> paths = value.keySet();
			List<String> filePath = new ArrayList<String>(paths);
			String file1 = null;
			String file2 = null;
			file1 = UtilList.getElemList(targetFiles, filePath.get(1));
			file2 = UtilList.getElemList(targetFiles, filePath.get(0));
			Collection<List<String>> methods = value.values();
			List<String> methodList = methods.iterator().next();
			for (String m: methodList) {
				try {
					List<SourceCodeChange> changes = UtilChDistiller.getChanges(file1, file2, m.split(":")[0], m.split(":")[0]);
					IDocument doc1 = UtilChDistiller.getDoc(file1);
					IDocument doc2 = UtilChDistiller.getDoc(file2);
					saveChanges(changes, doc1, doc2, m, file1, file2);
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}

	private static void saveChanges(List<SourceCodeChange> changes, IDocument doc1, IDocument doc2, String methodParam, String oldFile, String newFile) {
		try {
			for (SourceCodeChange iChange : changes) {
				if (iChange instanceof Insert) {
					Insert chgInsert = (Insert) iChange;
					int bgnOffset = chgInsert.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgInsert.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;
					String pkgName = methodParam.split(":")[2];
					String className = methodParam.split(":")[1];
					className = className + ".java";
					String methodName = methodParam.split(":")[0];
					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Insert", chgInsert.getLabel(), chgInsert.getChangedEntity().getType(), chgInsert.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Move) {
					Move chgMove = (Move) iChange;
					
					int bgnOffset = chgMove.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgMove.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;
					String pkgName = methodParam.split(":")[2];
					String className = methodParam.split(":")[1];
					className = className + ".java";
					String methodName = methodParam.split(":")[0];
					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Move", chgMove.getLabel(), chgMove.getChangedEntity().getType(), chgMove.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Delete) {
					Delete chgDelete = (Delete) iChange;
					
					int bgnOffset = chgDelete.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgDelete.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;
					String pkgName = methodParam.split(":")[2];
					String className = methodParam.split(":")[1];
					className = className + ".java";
					String methodName = methodParam.split(":")[0];
					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Delete", chgDelete.getLabel(), chgDelete.getChangedEntity().getType(), chgDelete.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Update) {
					Update chgUpdate = (Update) iChange;
					
					int bgnOffset = chgUpdate.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgUpdate.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;
					String pkgName = methodParam.split(":")[2];
					String className = methodParam.split(":")[1];
					className = className + ".java";
					String methodName = methodParam.split(":")[0];
					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, pkgName, className, methodName, "Update", chgUpdate.getLabel(), chgUpdate.getChangedEntity().getType(), chgUpdate.getChangedEntity().getUniqueName(), sourceChange);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}