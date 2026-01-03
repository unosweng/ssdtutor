/**
 */
package visitor.repair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;

import input.IGlobalProperty;
import model.MethodElement;
import util.ParserSAX;
import util.RepairHelper;
import visitor.DetectionASTVisitor;

public class JCA3ReplaceVisitor extends DetectionASTVisitor {
	private CompilationUnit astRoot;
	private String searchVal, newVal, reference, referenceType;
	private ASTRewrite rewrite;
	private static boolean updated;
	private boolean hasDefinedFunction=false;
	private boolean retVal=false;
	private int argPos, referenceLine;
	Map<Integer, String> indicatorRule = new HashMap<>();

	public JCA3ReplaceVisitor(CompilationUnit astRoot, ASTRewrite rewrite, MethodElement me, String javaFilePath) {
		this.astRoot = astRoot;
		this.rewrite = rewrite;
		this.argPos = me.getArgPos();
		this.indicatorClass = me.getIndicatorClassName();
		this.indicatorMethod = me.getIndicatorMethodName();
		
		ParserSAX ruleParser = ParserSAX.getRepairInstance(this.indicatorClass);
		Map<String, Map<Integer, String>> repairRules = ruleParser.getRepairRule();
		
		indicatorRule = repairRules.get(this.indicatorMethod);
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
		typeDecl.accept(new ASTVisitor() {
			@Override
			public boolean visit(MethodDeclaration metDec) {
				if(metDec.getName().getFullyQualifiedName().equals(IGlobalProperty.GET_RANDOM_SECRET_KEY)) {
					hasDefinedFunction = true;
				}
				return true;
			}
		});
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
					return checkSecretKeySpec(cic);
				} catch (Exception e) {
					System.out.println("[ERR] " + metDec.getName());
					e.printStackTrace();
				}
				return true;
			}
		});
		return true;
	}

	private boolean checkSecretKeySpec(ClassInstanceCreation cic) {
		String quaName = getQualifiedName(cic); // typeBinding.getQualifiedName();
		if (quaName == null) {
			return false;
		}
		
		boolean checkItem = (getLineNumber(cic.getStartPosition()) == referenceLine);
		String cicString = cic.getType().toString();
		String ideMetInv = cicString.substring(cicString.lastIndexOf('.') + 1);
		
		if ((quaName.equals(this.indicatorClass) && ideMetInv.equals(this.indicatorMethod)) || checkItem) {
			List<?> arguments = cic.arguments();
			argPos = 1;
			
			Object iObj = arguments.get(argPos);
			this.newVal = indicatorRule.get(argPos+1);
			if (iObj instanceof StringLiteral) {
				String iPar = ((StringLiteral) iObj).getLiteralValue();
				this.searchVal = iPar;
				replaceStringLiteralArgument(cic, 1);
				retVal = true;
			}
			else if(iObj instanceof SimpleName) {
				SimpleName iPar = (SimpleName) iObj;
				SimpleName sName = checkVdfSetContains(iPar, false);
				if (sName != null) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) sName.getParent();
					this.searchVal = ((StringLiteral) vdf.getInitializer()).getLiteralValue();
					replaceStringLiteralArgument(cic, 1);
					retVal = true;
				}              
			}
			
			argPos = 0;
			iObj = arguments.get(argPos);
			
			reference = indicatorRule.get(argPos+1);
			reference = reference.substring(reference.lastIndexOf("-")+1, reference.length());
			ParserSAX ruleParser = ParserSAX.getRepairReference(reference);
			
			reference = ruleParser.getReferenceClass();
			referenceType = reference.substring(reference.lastIndexOf(".") + 1, reference.length());
			
			if (iObj instanceof MethodInvocation) {
				MethodInvocation mi = (MethodInvocation) iObj;
				if ((mi.getExpression() instanceof SimpleName) && mi.getName().getIdentifier().equals("getBytes")) {
					SimpleName iPar = (SimpleName) mi.getExpression();
					SimpleName sName = checkVdfSetContains(iPar, false);
					if (sName != null) {
						replaceSimpleNameArgument(mi, sName);
						if (!hasDefinedFunction) {
							replaceMethodInvocationArgument();
						}
						retVal = true;
					} 
				}
				else if ((mi.getExpression() instanceof StringLiteral) && mi.getName().getIdentifier().equals("getBytes")) {
					StringLiteral argStr = (StringLiteral) mi.getExpression();
					if (argStr != null) {
						replaceStringLiteralArgument(mi, argStr);
						if (!hasDefinedFunction) {
							replaceMethodInvocationArgument();
						}
						retVal = true;
					} 
				}
			} else if (iObj instanceof SimpleName) {
				SimpleName sn = (SimpleName) iObj;
				replaceSimpleNameArgument(sn);
				if (!hasDefinedFunction) {
					replaceMethodInvocationArgument();
				}
				retVal = true;
			} else if (iObj instanceof StringLiteral) {
				retVal = true;
			} else if (iObj instanceof ArrayCreation) {
				ArrayCreation ai = (ArrayCreation) iObj;
				if (!hasDefinedFunction) {
					replaceMethodInvocationArgument();
				}
				replaceSimpleNameArgument(ai);
			}
		}
		return retVal;
	}

	@SuppressWarnings("unchecked")
	private void replaceMethodInvocationArgument() {
		AST ast = astRoot.getAST();

		/*********************************************************************************/
		// create the new method declaration 
		// NOTE: no name-space checking is done here - potential for conflict
		MethodDeclaration md = ast.newMethodDeclaration();
		md.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
		md.setReturnType2(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE), 1));

		Block body = ast.newBlock();
		md.setBody(body);

		// add byte parameter to be made random
		List<Object> parameters = md.parameters();
		SingleVariableDeclaration variable = ast.newSingleVariableDeclaration();
		variable.setName(ast.newSimpleName(IGlobalProperty.VAR1));
		variable.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE)));
		parameters.add(variable);

		// add public static modifiers
		List<Modifier> modifiers = md.modifiers();
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
		modifiers.add(ast.newModifier(Modifier.ModifierKeyword.STATIC_KEYWORD));

		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName("clone"));
		mi.setExpression(ast.newSimpleName(IGlobalProperty.VAR1));

		VariableDeclarationFragment vdf = ast.newVariableDeclarationFragment();
		vdf.setName(ast.newSimpleName(IGlobalProperty.VAR2));
		vdf.setInitializer(mi);

		VariableDeclarationExpression vde = ast.newVariableDeclarationExpression(vdf);
		vde.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE), 1));

		/*********************************************************************************/
		// SecureRandom secureRandom = new SecureRandom();
		ClassInstanceCreation cic = ast.newClassInstanceCreation();
		cic.setType(ast.newSimpleType(ast.newName(referenceType)));

		VariableDeclarationFragment cvdf = ast.newVariableDeclarationFragment();
		cvdf.setName(ast.newSimpleName(IGlobalProperty.SECURERANDOM));
		cvdf.setInitializer(cic);

		VariableDeclarationExpression cvde = ast.newVariableDeclarationExpression(cvdf);
		cvde.setType(ast.newSimpleType(ast.newName(referenceType)));

		/*********************************************************************************/
		// secureRandom.nextBytes(Var2);
		MethodInvocation mi2 = ast.newMethodInvocation();
		mi2.setName(ast.newSimpleName("nextBytes"));
		mi2.setExpression(ast.newSimpleName(IGlobalProperty.SECURERANDOM));
		mi2.arguments().add(ast.newSimpleName(IGlobalProperty.VAR2));

		/*********************************************************************************/
		// create return statement
		ReturnStatement ret = ast.newReturnStatement();
		ret.setExpression(ast.newSimpleName(IGlobalProperty.VAR2));

		/*********************************************************************************/
		// add statements to body
		body.statements().add(ast.newExpressionStatement(vde));
		body.statements().add(ast.newExpressionStatement(cvde));
		body.statements().add(ast.newExpressionStatement(mi2));
		body.statements().add(ret);

		/*********************************************************************************/
		// add the new Method Declaration to the end of the Type Declaration 
		TypeDeclaration td = (TypeDeclaration) astRoot.types().get(0);
		ListRewrite listRewrite = rewrite.getListRewrite(td, TypeDeclaration.BODY_DECLARATIONS_PROPERTY);
		listRewrite.insertLast(md, null);

		/*********************************************************************************/
		// add the new import for java.security.SecureRandom
		RepairHelper repairHelper = new RepairHelper(null, null);
		repairHelper.listReWriteImport(astRoot, rewrite, reference);

		/*********************************************************************************/
		// set the flag
		updated = true;
		hasDefinedFunction = true;
	}

	private void replaceStringLiteralArgument(ClassInstanceCreation cic, int argPos) {
		Object arg = cic.arguments().get(argPos);
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
				updated = true;
			}
		} else if (arg instanceof StringLiteral) {
			StringLiteral argStr = (StringLiteral) arg;
			StringLiteral newArg = astRoot.getAST().newStringLiteral();
			String newStr = argStr.getLiteralValue().replace(this.searchVal, this.newVal);
			newArg.setLiteralValue(newStr);
			rewrite.replace(argStr, newArg, null);
			updated = true;
		}
	}

	@SuppressWarnings("unchecked")
	private void replaceSimpleNameArgument(MethodInvocation mn, SimpleName sn) {
		AST ast = this.astRoot.getAST();
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
		MethodInvocation getBytes = ast.newMethodInvocation();
		getBytes.setName(ast.newSimpleName("getBytes"));
		getBytes.setExpression(ast.newSimpleName(sn.getIdentifier()));
		mi.arguments().add(getBytes);
		rewrite.replace(mn, mi, null);
		updated = true;
	}

	@SuppressWarnings({"unchecked"})
	private void replaceSimpleNameArgument(SimpleName sn) {
		AST ast = this.astRoot.getAST();
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
		mi.arguments().add(ast.newSimpleName(sn.getIdentifier()));
		rewrite.replace(sn, mi, null);
		updated = true;
	}
	
	@SuppressWarnings({"unchecked",})
	private void replaceSimpleNameArgument(ArrayCreation sn) {
		AST ast = this.astRoot.getAST();
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
		ArrayCreation args = ast.newArrayCreation();
		args.setType(ast.newArrayType(ast.newPrimitiveType(PrimitiveType.BYTE)));
		if (sn.getInitializer() != null) {
			ArrayInitializer ai = ast.newArrayInitializer();
			for (Object value: sn.getInitializer().expressions()) {
				ai.expressions().add(ast.newNumberLiteral(value.toString()));
			}
			args.setInitializer(ai);
		} else {
			args.dimensions().add(ast.newNumberLiteral(sn.dimensions().get(0).toString()));
		}
		mi.arguments().add(args);
		rewrite.replace(sn, mi, null);
		updated = true;
	}
	
	@SuppressWarnings({"unchecked"})
	private void replaceStringLiteralArgument(MethodInvocation mn, StringLiteral argStr) {
		AST ast = this.astRoot.getAST();
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setName(ast.newSimpleName(IGlobalProperty.GET_RANDOM_SECRET_KEY));
		MethodInvocation getBytes = ast.newMethodInvocation();
		getBytes.setName(ast.newSimpleName("getBytes"));
		mi.arguments().add(getBytes);
		StringLiteral args = ast.newStringLiteral();
		args.setLiteralValue(argStr.getLiteralValue());
		getBytes.setExpression(args);
		rewrite.replace(mn, mi, null);
		updated = true;
	}


	public boolean isUpdated() {
		return updated;
	}
}
