package visitor.repair;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
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

import model.MethodElement;
import util.ParserSAX;
import visitor.DetectionASTVisitor;

public class JCA7ReplaceVisitor extends DetectionASTVisitor {
	private CompilationUnit astRoot;
	private ASTRewrite rewrite;
	private String searchVal, newVal;
	private boolean updated;
	private MethodElement me;
	Object reference;
	private int argPos, referenceLine;
	Map<String, Map<Integer, String>> repairRules = new HashMap<>();
	Map<Integer, String> indicatorRule = new HashMap<>();


	public JCA7ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
		this.astRoot = astRoot;
		this.rewrite = rewrite;
		this.argPos = me.getArgPos();
		this.me = me;
		this.indicatorClass = me.getIndicatorClassName();

		ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
		repairRules = ruleParser.getRepairRule();

		this.javaFilePath = javaFilePath;
		referenceLine = me.getLineNumber();
		reference = me.getParameters().get(argPos);
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
					if (checkKeyGenerator(metInv)) {
						replaceStringLiteralArgument(metInv);
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

	public boolean isUpdated() {
		return updated;
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

	private void replaceSimpleNameArguments(StringLiteral sn) {
		String ideMetInv = me.getIndicatorMethodName();
		indicatorRule = repairRules.get(ideMetInv);
		if (indicatorRule.size()>0) {
			this.newVal = indicatorRule.get(me.getIndicatorPos()+1);
			StringLiteral newArg = astRoot.getAST().newStringLiteral();
			String newStr = sn.getLiteralValue().replace(this.searchVal, this.newVal);
			newArg.setLiteralValue(newStr);
			rewrite.replace(sn, newArg, null);
			this.updated = true;
		}
	}

	private void replaceSimpleNameArguments(VariableDeclarationFragment vdf) {
		if (vdf.getInitializer() instanceof StringLiteral) {
			String ideMetInv = me.getIndicatorMethodName();
			indicatorRule = repairRules.get(ideMetInv);
			if (indicatorRule.size()>0) {
				this.newVal = indicatorRule.get(argPos+1);
				Expression iObj = vdf.getInitializer();
				this.searchVal = ((StringLiteral) iObj).getLiteralValue();
				StringLiteral argStr = (StringLiteral) vdf.getInitializer();
				StringLiteral newArg = astRoot.getAST().newStringLiteral();
				String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
				newArg.setLiteralValue(newStr);
				rewrite.replace(argStr, newArg, null);
				this.updated = true;
			} 
		}
	}

	private boolean checkKeyGenerator(MethodInvocation metInv) {
		String quaName = getQualifiedName(metInv); // typeBinding.getQualifiedName();
		if (quaName == null) {
			return false;
		}

		boolean checkItem = (getLineNumber(metInv.getStartPosition()) == referenceLine);
		String ideMetInv = metInv.getName().getIdentifier();

		if ((quaName.equals(this.indicatorClass)) || checkItem) {
			indicatorRule = repairRules.get(ideMetInv);
			if(indicatorRule == null) {
				return false;
			}
			if (indicatorRule.size()>0 && metInv.arguments().size() > 0) {
				Object iObj = metInv.arguments().get(argPos);
				this.newVal = indicatorRule.get(argPos+1);
				if (iObj instanceof StringLiteral) {
					String iPar = ((StringLiteral) iObj).getLiteralValue();
					this.searchVal = iPar;
					return true;
				}else if(iObj instanceof SimpleName) {
					SimpleName iPar = (SimpleName) iObj;
					SimpleName sName = checkVdfSetContains(iPar, false);
					if (sName != null) {
						VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
						if (vdf.getInitializer() instanceof StringLiteral) {
							iObj = vdf.getInitializer();
							this.searchVal = ((StringLiteral) iObj).getLiteralValue();
							return true;
						} else if (vdf.getInitializer() instanceof NumberLiteral) {
							return true;
						}
					}              
				} else if (iObj instanceof NumberLiteral) {
					return true;
				}
			}
		}
		return false;
	}

	private void replaceStringLiteralArgument(MethodInvocation mi) {
		Object arg = mi.arguments().get(argPos);
		if (arg instanceof SimpleName) {
			SimpleName iPar = (SimpleName) arg;
			SimpleName sName = checkVdfSetContains(iPar, false);
			if (sName != null) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
				if (vdf.getInitializer() instanceof StringLiteral) {
					StringLiteral argStr = (StringLiteral) vdf.getInitializer();
					StringLiteral newArg = astRoot.getAST().newStringLiteral();
					String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
					newArg.setLiteralValue(newStr);
					rewrite.replace(argStr, newArg, null);
					this.updated = true;
				}else if (vdf.getInitializer() instanceof NumberLiteral) {
					NumberLiteral numLit = (NumberLiteral) vdf.getInitializer();
					if (Integer.valueOf(numLit.getToken()) < Integer.parseInt(this.newVal)) {
						AST ast = astRoot.getAST();
						NumberLiteral newNumberLiteral = ast.newNumberLiteral(this.newVal);
						rewrite.replace(numLit, newNumberLiteral, null);
						this.updated = true;
					}
				}

			}
		} else if (arg instanceof StringLiteral) {
			StringLiteral argStr = (StringLiteral) arg;
			StringLiteral newArg = astRoot.getAST().newStringLiteral();
			String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
			newArg.setLiteralValue(newStr);
			rewrite.replace(argStr, newArg, null);
			this.updated = true;
		}else if (arg instanceof NumberLiteral) {
			NumberLiteral numLit = (NumberLiteral) arg;
			if (Integer.valueOf(numLit.getToken()) < Integer.parseInt(this.newVal)) {
				AST ast = astRoot.getAST();
				NumberLiteral newNumberLiteral = ast.newNumberLiteral(this.newVal);
				rewrite.replace(numLit, newNumberLiteral, null);
				this.updated = true;
			}
		}
	}
}
