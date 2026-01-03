/**
 * @(#) DeclarationVisitor.java
 */
package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import model.ModelProvider;


/**
 * @since J2SE-1.8
 */
public class DeclarationVisitor extends ASTVisitor {
	private String className;
	private String pkgName;
	private String projectName;
	private int localVar = 0;
	private int fieldVar = 0;

	public DeclarationVisitor(String projectName) {
		this.projectName = projectName;
	}

	@Override
	public boolean visit(PackageDeclaration pkgDecl) {
		pkgName = pkgDecl.getName().getFullyQualifiedName();
		return super.visit(pkgDecl);
	}

	/**
	 * A type declaration is the union of a class declaration and an interface declaration.
	 */
	@Override
	public boolean visit(TypeDeclaration typeDecl) {
		className = typeDecl.getName().getIdentifier();
		typeDecl.accept(new ASTVisitor() {
			@Override
			public boolean visit(FieldDeclaration fieldDecl) {
				fieldVar += 1;
				return true;
			}
		});
		return super.visit(typeDecl);
	}

	@Override
	public boolean visit(MethodDeclaration methodDecl) {
		String methodName = methodDecl.getName().getFullyQualifiedName();
		//check declaration of local variable
		methodDecl.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationFragment vdf) {
				localVar += 1;
				return true;
			}
		});
		ModelProvider.INSTANCE.addProgramElements(projectName, null, pkgName, className, methodName, localVar, fieldVar);
		localVar = 0;
		fieldVar = 0;
		return super.visit(methodDecl);
	}
}
