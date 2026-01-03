/**
 */
package visitor.repair;

import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import model.MethodElement;
import util.ParserSAX;
import visitor.DetectionASTVisitor;

public class JCA5ReplaceVisitor extends DetectionASTVisitor {
	private CompilationUnit astRoot;
	private ASTRewrite rewrite;
	private static boolean updated;
	private String newVal;
	private int argPos, referenceLine;

	public JCA5ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
		this.astRoot = astRoot;
		this.rewrite = rewrite;
		this.argPos = me.getArgPos();
		this.indicatorClass = me.getIndicatorClassName();
		this.indicatorMethod = me.getIndicatorMethodName();
		
		ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
		Map<String, Map<Integer, String>> repairRules = ruleParser.getRepairRule();
		Map<Integer, String> indicatorRule = repairRules.get(this.indicatorMethod);
		
		this.newVal = indicatorRule.get(me.getIndicatorPos()+1);
		referenceLine = me.getLineNumber();
		this.javaFilePath = javaFilePath;
	}

	@Override
	public boolean visit(PackageDeclaration pkgDecl) {
		return super.visit(pkgDecl);
	}

	/**
	 * A type declaration is the union of a class declaration and an interface declaration.
	 */
	@Override
	public boolean visit(TypeDeclaration typeDecl) {
		return super.visit(typeDecl);
	}

	public boolean visit(FieldDeclaration fieldDec) {
		return super.preVisit2(fieldDec);
	}

	public boolean visit(MethodDeclaration metDec) {

		metDec.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment vdf) {
				checkVarDefUse(vdf, false);
				return true;
			}

			public boolean visit(ClassInstanceCreation cic) {
				try {
					return checkPBEParamSpec(cic);
				} catch (Exception e) {
					System.out.println("[ERR] " + metDec.getName());
					e.printStackTrace();
				}
				return true;
			}
		});
		return true;
	}

	private boolean checkPBEParamSpec(ClassInstanceCreation cic) {
		String quaName = getQualifiedName(cic); // typeBinding.getQualifiedName();
		if (quaName == null) {
			return false;
		}

		boolean checkItem = (getLineNumber(cic.getStartPosition()) == referenceLine);
		String cicString = cic.getType().toString();
		String ideMetInv = cicString.substring(cicString.lastIndexOf('.') + 1);
		
		if ((quaName.equals(this.indicatorClass) && ideMetInv.equals(this.indicatorMethod)) || checkItem) {
			Object iObj = cic.arguments().get(this.argPos);
			if (iObj instanceof NumberLiteral) {
				NumberLiteral numLit = (NumberLiteral) iObj;
				if (Integer.valueOf(numLit.getToken()) < Integer.parseInt(this.newVal)) {

					AST ast = astRoot.getAST();
					NumberLiteral newNumberLiteral = ast.newNumberLiteral(this.newVal);

					rewrite.replace(numLit, newNumberLiteral, null);
					updated = true;

					return true;
				}
			}else if(iObj instanceof SimpleName) {
				SimpleName sn = (SimpleName) iObj;
				SimpleName sName = checkVdfSetContains(sn, false);
				if (sName != null) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
					NumberLiteral numLit = (NumberLiteral) vdf.getInitializer();
					AST ast = astRoot.getAST();
					NumberLiteral newNumberLiteral = ast.newNumberLiteral(this.newVal);

					rewrite.replace(numLit, newNumberLiteral, null);
					updated = true;
					return true;
				}
			}
		}
		return false;
	}

	public boolean isUpdated() {
		return updated;
	}
}
