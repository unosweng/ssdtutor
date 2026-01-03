package util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import model.MethodElement;
import visitor.DetectionASTVisitor;

public class CheckPatternConflict extends DetectionASTVisitor{

	Map<Integer, Map<String, String>> positionRelation = new HashMap<>();
	private String unitPath;
	private Map<String, Map<Integer, Map<String, String>>> variableHistory;
	
	public CheckPatternConflict(ICompilationUnit unit, MethodElement me, String javaFilePath) {
		// TODO Auto-generated constructor stub
		
	}

	public CheckPatternConflict(ICompilationUnit unit, MethodElement me, String javaFilePath,
			Map<String, Map<Integer, Map<String, String>>> variableHistory) {
		// TODO Auto-generated constructor stub
		this.javaFilePath = javaFilePath;
		this.indicatorClass = me.getIndicatorClassName();
		this.unitPath = unit.getResource().getLocation().toFile().getPath();
		this.variableHistory = variableHistory;
	}

	public boolean visit(FieldDeclaration fieldDec) {
		fieldDec.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationFragment vdf) {
				int position = getLineNumber(vdf.getStartPosition());
				Map<String, String> classRelation = new HashMap<>();
				System.out.println(variableHistory);
				if (variableHistory.get(unitPath) != null) {
					positionRelation = variableHistory.get(unitPath);
					if (positionRelation.get(position) != null) {
						classRelation = positionRelation.get(position);
						if (classRelation.get(vdf.getName().getFullyQualifiedName()) != null) {
							System.out.println("here");
						}
						else {
							classRelation.put(vdf.getName().getFullyQualifiedName(), indicatorClass);
						}
					}
					else {
						classRelation.put(vdf.getName().getFullyQualifiedName(), indicatorClass);
						positionRelation.put(position, classRelation);
					}
				}
				else {
					classRelation.put(vdf.getName().getFullyQualifiedName(), indicatorClass);
					positionRelation.put(position, classRelation);
					variableHistory.put(unitPath, positionRelation);
				}
				System.out.println(variableHistory);
				return true;
			}
		});
		return super.preVisit2(fieldDec);
	}

}
