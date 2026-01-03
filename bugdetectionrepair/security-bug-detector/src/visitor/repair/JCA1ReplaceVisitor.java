/**
 */
package visitor.repair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import model.MethodElement;
import util.ParserSAX;
import visitor.DetectionASTVisitor;

public class JCA1ReplaceVisitor extends DetectionASTVisitor {
	private CompilationUnit astRoot;
	private ASTRewrite rewrite;
	private String searchVal, newVal;
	Object reference;
	private boolean updated;
	private int argPos, referenceLine;
	Map<String, Map<Integer, String>> repairRules = new HashMap<>();

	public JCA1ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
		this.astRoot = astRoot;
		this.rewrite = rewrite;
		this.argPos = me.getArgPos();
		this.indicatorClass = me.getIndicatorClassName();
		this.indicatorMethod = me.getIndicatorMethodName();
		
		ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
		repairRules = ruleParser.getRepairRule();
		Map<Integer, String> indicatorRule = repairRules.get(this.indicatorMethod);
		
		this.newVal = indicatorRule.get(me.getIndicatorPos()+1);
		this.javaFilePath = javaFilePath;
		reference = me.getParameters().get(argPos);
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
		typeDecl.accept(new ASTVisitor() {
			@Override
			public boolean visit(StringLiteral sn) {
				if(checkVariableDeclaration(sn)) {
					replaceSimpleNameArguments(sn);
				}
				return true;
			}
		});
		return true;
	}

	public boolean visit(FieldDeclaration fieldDec) {
		fieldDec.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationFragment vdf) {
				if(checkVariableDeclaration(vdf)) {
					replaceSimpleNameArguments(vdf);
				}
				return true;
			}
		});
		return super.preVisit2(fieldDec);
	}

	public boolean visit(MethodDeclaration metDec) {
		metDec.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment vdf) {
				checkVarDefUse(vdf, false);
				return true;
			}

			public boolean visit(MethodInvocation metInv) {
				try {
					if (checkCipherGetInstance(metInv)) {
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

	private boolean checkCipherGetInstance(MethodInvocation metInv) {
		String quaName = getQualifiedName(metInv); // typeBinding.getQualifiedName();
		if (quaName == null) {
			return false;
		}
		
		boolean checkItem = (getLineNumber(metInv.getStartPosition()) == referenceLine);
		
		if (checkItem) {
			List<?> arguments = metInv.arguments();
			if(arguments.size()>argPos) {
				Object iObj = arguments.get(argPos);
				if (iObj instanceof StringLiteral) {
					String iPar = ((StringLiteral) iObj).getLiteralValue();
					this.searchVal = iPar;
					replaceStringLiteralArgument(metInv);
					return true;
				}
				else if(iObj instanceof SimpleName) {
					SimpleName iPar = (SimpleName) iObj;
					SimpleName sName = checkVdfSetContains(iPar, false);
					if (sName != null) {
						VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
						this.searchVal = ((StringLiteral) vdf.getInitializer()).getLiteralValue();
						replaceStringLiteralArgument(metInv);
						return true;
					}              
				}
			}
		}
		return false;
	}
	
	private boolean checkVariableDeclaration(VariableDeclarationFragment vdf) {
		boolean checkItem = (getLineNumber(vdf.getStartPosition()) == referenceLine);
		if(checkItem) {
			this.searchVal =((StringLiteral) vdf.getInitializer()).getLiteralValue();
			return true;
		}
		return false;
	}
	
	private boolean checkVariableDeclaration(StringLiteral sn) {
		boolean checkItem = false;
		try {
			StringLiteral referenceValue = (StringLiteral) reference;
			checkItem = (getLineNumber(sn.getStartPosition()) == referenceLine) && (sn.getLiteralValue().equals(referenceValue.getLiteralValue()));
		}
		catch(Exception e) {
			checkItem = (getLineNumber(sn.getStartPosition()) == referenceLine) && (sn.getLiteralValue().equals(reference));
		}
		
		if(checkItem) {
			this.searchVal = sn.getLiteralValue();
			return true;
		}
		return false;
	}
	
	private void replaceSimpleNameArguments(VariableDeclarationFragment vdf) {
		// TODO Auto-generated method stub
		StringLiteral argStr = (StringLiteral) vdf.getInitializer();
		StringLiteral newArg = astRoot.getAST().newStringLiteral();
		String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
		newArg.setLiteralValue(newStr);
		rewrite.replace(argStr, newArg, null);
		this.updated = true;
	}
	
	private void replaceSimpleNameArguments(StringLiteral sn) {
		StringLiteral newArg = astRoot.getAST().newStringLiteral();
		String newStr = sn.getLiteralValue().replace(this.searchVal, this.newVal);
		newArg.setLiteralValue(newStr);
		rewrite.replace(sn, newArg, null);
		this.updated = true;
	}

	private void replaceStringLiteralArgument(MethodInvocation mi) {
		Object arg = mi.arguments().get(argPos);
		if (arg instanceof SimpleName) {
			SimpleName iPar = (SimpleName) arg;
			SimpleName sName = checkVdfSetContains(iPar, false);
			if (sName != null) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
				StringLiteral argStr = (StringLiteral) vdf.getInitializer();
				StringLiteral newArg = astRoot.getAST().newStringLiteral();
				String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
				newArg.setLiteralValue(newStr);
				rewrite.replace(argStr, newArg, null);
				this.updated = true;
			}
		} else if (arg instanceof StringLiteral) {
			StringLiteral argStr = (StringLiteral) arg;
			StringLiteral newArg = astRoot.getAST().newStringLiteral();
			String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
			newArg.setLiteralValue(newStr);
			rewrite.replace(argStr, newArg, null);
			this.updated = true;
		}
	}

	public boolean isUpdated() {
		return updated;
	}
}
