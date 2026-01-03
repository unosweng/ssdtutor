package visitors;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;

public class CompilationUnitVisitor extends ASTVisitor{

	@Override
	public void endVisit(ForStatement node) {
		// TODO Auto-generated method stub
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		// TODO Auto-generated method stub
		super.endVisit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		
		System.out.println("Start Position of for node "+node.getStartPosition());
		System.out.println("End Position of for node "+(node.getStartPosition()+node.getLength()));
		
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		System.out.println("Method Invocation start Position of for node "+node.getStartPosition());
		System.out.println("Method invocation End Position of for node "+(node.getStartPosition()+node.getLength()));
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// TODO Auto-generated method stub
		Block block = node.getBody();
		List<Statement> statements = block.statements();
		for(Statement statement :statements)
		{
			System.out.println("Statement is :"+ statement);
		}
		return super.visit(node);
	}

}
