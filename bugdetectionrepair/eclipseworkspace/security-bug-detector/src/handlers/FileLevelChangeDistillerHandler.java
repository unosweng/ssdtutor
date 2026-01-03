package handlers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
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
			ProjectAnalyzer prjAnal = new ProjectAnalyzer();
			if(treeData.size() > 0) {
				prjAnal.analyze(treeData);
				commonFiles = prjAnal.getCommonFiles();
			}
		}
		if (findPartObj instanceof ChangeDistiller) {
			ChangeDistiller v = (ChangeDistiller) findPartObj;
			v.clear();
			diffFileLevel(v, commonFiles);  
			v.setInput(ChangeDistillerProgramElement.INSTANCE.getProgramElements());
		}
	}

	void diffFileLevel(ChangeDistiller v, Map<String, List<String>> commonFiles) {
	      File oldf, newf;
	      IDocument doc2, doc1;
	      String oldFile, newFile;
	      List<SourceCodeChange> changes;
	      for (List<String> value : commonFiles.values()) {
	         if (value.get(0).contains("_original")) {
	            newf = new File(value.get(1));
	            oldf = new File(value.get(0));
	            doc2 = UtilChDistiller.getDoc(value.get(1));
	            doc1 = UtilChDistiller.getDoc(value.get(0));
	            oldFile = value.get(0).replace("///", "/");
	            newFile = value.get(1).replace("///", "/");
	         } else {
	            oldf = new File(value.get(1));
	            newf = new File(value.get(0));
	            doc2 = UtilChDistiller.getDoc(value.get(0));
	            doc1 = UtilChDistiller.getDoc(value.get(1));
	            oldFile = value.get(1).replace("///", "/");
	            newFile = value.get(0).replace("///", "/");
	         }
	         changes = UtilChDistiller.getChanges(oldf, newf);
	         saveChanges(changes, doc1, doc2, oldFile, newFile);
	      }
	   }

	private void saveChanges(List<SourceCodeChange> changes, IDocument doc1, IDocument doc2, String oldFile, String newFile) {
		try {
			String methodName = null;
			String className = null;
			for (SourceCodeChange iChange : changes) {
				if (iChange instanceof Insert) {
					Insert chgInsert = (Insert) iChange;

					String rootName = chgInsert.getRootEntity().getUniqueName();
					int rootSplit = rootName.lastIndexOf(".");
					String insertStatement;
					if(!rootName.contains("(")) {
						className = rootName;
						methodName = "";
						String changeEntity = chgInsert.getChangedEntity().getType().toString().replace("_", " ");
						insertStatement = "Insert in " + StringUtils.capitalize(changeEntity.toLowerCase());
					} else {
						methodName = rootName.substring(rootSplit+1, rootName.length());
						className = rootName.substring(0, rootSplit);
						String changeEntity = chgInsert.getChangedEntity().getType().toString().replace("_", " ");
						insertStatement = "Insert in " + StringUtils.capitalize(changeEntity.toLowerCase()) + ": " + chgInsert.getChangedEntity().getUniqueName();
					}

					int bgnOffset = chgInsert.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgInsert.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;
					
					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, null, className, methodName, insertStatement, chgInsert.getLabel(), chgInsert.getChangedEntity().getType(), chgInsert.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Delete) {
					Delete chgDelete = (Delete) iChange;

					String rootName = chgDelete.getRootEntity().getUniqueName();
					int rootSplit = rootName.lastIndexOf(".");
					if(!rootName.contains("(")) {
						className = rootName;
						methodName = "";
					} else {
						methodName = rootName.substring(rootSplit+1, rootName.length());
						className = rootName.substring(0, rootSplit);
					}
					String changeEntity = chgDelete.getChangedEntity().getType().toString().replace("_", " ");
					String deleteStatement = "Delete in " + StringUtils.capitalize(changeEntity.toLowerCase()) + ": " + chgDelete.getChangedEntity().getUniqueName();
					int bgnOffset = chgDelete.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgDelete.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;

					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, null, className, methodName, deleteStatement , chgDelete.getLabel(), chgDelete.getChangedEntity().getType(), chgDelete.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Update) {
					Update chgUpdate = (Update) iChange;
					
					String rootName = chgUpdate.getRootEntity().getUniqueName();
					int rootSplit = rootName.lastIndexOf(".");
					if(!rootName.contains("(")) {
						className = rootName;
						methodName = "";
					} else {
						methodName = rootName.substring(rootSplit+1, rootName.length());
						className = rootName.substring(0, rootSplit);
					}
					String changeEntity = chgUpdate.getChangedEntity().getType().toString().replace("_", " ");
					String label = StringUtils.capitalize(chgUpdate.getLabel().split("_")[0].toLowerCase());
					String updateStatement = "Update in " + StringUtils.capitalize(changeEntity.toLowerCase())+ " " + label + ": " + chgUpdate.getChangedEntity().getUniqueName();
					int bgnOffset = chgUpdate.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgUpdate.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;

					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, null, className, methodName, updateStatement, chgUpdate.getLabel(), chgUpdate.getChangedEntity().getType(), chgUpdate.getChangedEntity().getUniqueName(), sourceChange);
				}
				else if (iChange instanceof Move) {
					Move chgMove = (Move) iChange;

					String rootName = chgMove.getRootEntity().getUniqueName();
					int rootSplit = rootName.lastIndexOf(".");
					if(!rootName.contains("(")) {
						className = rootName;
						methodName = "";
					} else {
						methodName = rootName.substring(rootSplit+1, rootName.length());
						className = rootName.substring(0, rootSplit);
					}
					String changeEntity = chgMove.getChangedEntity().getType().toString().replace("_", " ");
					int bgnOffset = chgMove.getChangedEntity().getSourceRange().getStart();
					int endOffset = chgMove.getChangedEntity().getSourceRange().getEnd();
					int bgnLine = doc2.getLineOfOffset(bgnOffset) + 1;
					int endLine = doc2.getLineOfOffset(endOffset) + 1;
					String sourceChange = bgnLine + " ~ " + endLine;
					String moveStatement = "Move in " + StringUtils.capitalize(changeEntity.toLowerCase()) + ": " + chgMove.getChangedEntity().getUniqueName();
					ChangeDistillerProgramElement.INSTANCE.addProgramElements(oldFile, newFile, null, className, methodName, moveStatement, chgMove.getLabel(), chgMove.getChangedEntity().getType(), chgMove.getChangedEntity().getUniqueName(), sourceChange);
				}
			}
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}