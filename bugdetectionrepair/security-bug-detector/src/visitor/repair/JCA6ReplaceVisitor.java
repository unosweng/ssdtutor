/**
 */
package visitor.repair;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import input.IGlobalProperty;
import model.MethodElement;
import util.ParserSAX;
import visitor.DetectionASTVisitor;

public class JCA6ReplaceVisitor extends DetectionASTVisitor {
	private CompilationUnit astRoot;
	private ASTRewrite rewrite;
	private boolean updated;
	private int argPos, referenceLine;
	Map<String, Map<Integer, String>> repairRules = new HashMap<>();

	public JCA6ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
		this.astRoot = astRoot;
		this.rewrite = rewrite;
		this.argPos = me.getArgPos();
		this.indicatorClass = me.getIndicatorClassName();
		
		ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
		repairRules = ruleParser.getRepairRule();
		
		this.javaFilePath = javaFilePath;
		referenceLine = me.getLineNumber();
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
				return super.preVisit2(vdf);
			}

			public boolean visit(MethodInvocation metInv) {
				try {
					if (checkSecureRandomSetSeed(metInv)) {
						return true;
					}
				} catch (Exception e) {
					System.out.println("[ERR] " + metDec.getName());
					e.printStackTrace();
				}
				return true;
			}
		});
		return true;
	}

	private boolean checkSecureRandomSetSeed(MethodInvocation metInv) {
		String quaName = getQualifiedName(metInv);
		if (quaName == null) {
			return false;
		}
		
		boolean checkItem = (getLineNumber(metInv.getStartPosition()) == referenceLine);
		String ideMetInv = metInv.getName().getIdentifier();
		if ((quaName.equals(this.indicatorClass)) || checkItem) {
			Object obj = metInv.arguments().get(this.argPos);
			Map<Integer, String> indicatorRule = repairRules.get(ideMetInv);
			if(!(indicatorRule==null)) {
				if (obj instanceof MethodInvocation) {
					MethodInvocation mi = (MethodInvocation) obj;
					if ((mi.getExpression() instanceof SimpleName) && mi.getName().getIdentifier().equals("getBytes")) {
						if (metInv.getExpression() instanceof SimpleName) {
							SimpleName sName = (SimpleName) (metInv.getExpression());
							replaceMethodInvocationArgument(sName, mi);
							return true;
						} 
					} else if((mi.getExpression() instanceof StringLiteral) && mi.getName().getIdentifier().equals("getBytes")) {
						if (metInv.getExpression() instanceof SimpleName) {
							SimpleName sName = (SimpleName) (metInv.getExpression());
							replaceMethodInvocationArgument(sName, mi);
							return true;
						}
					}
				}
			}
		}  
		return false;
	}

	@SuppressWarnings("unchecked")
	private void replaceMethodInvocationArgument(SimpleName sName, MethodInvocation mi) {
		AST ast = astRoot.getAST();

		MethodInvocation generateSeed = ast.newMethodInvocation();
		generateSeed.setName(ast.newSimpleName(IGlobalProperty.GENERATE_SEED));
		NumberLiteral seedLen = ast.newNumberLiteral(String.valueOf(IGlobalProperty.SET_SEED_MIN_LEN));
		generateSeed.arguments().add(seedLen);
		generateSeed.setExpression(ast.newSimpleName(sName.getFullyQualifiedName()));

		rewrite.replace(mi, generateSeed, null);
		updated = true;
	}

	public boolean isUpdated() {
		return updated;
	}
}
