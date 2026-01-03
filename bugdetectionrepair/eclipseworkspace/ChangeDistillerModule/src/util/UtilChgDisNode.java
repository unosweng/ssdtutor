/**
 * @file: UtilChgDisNode.java
 * @date: Mar 12, 2016
 */
package util;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

import com.google.inject.Guice;
import com.google.inject.Injector;

import ch.uzh.ifi.seal.changedistiller.JavaChangeDistillerModule;
import ch.uzh.ifi.seal.changedistiller.ast.java.Comment;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaDeclarationConverter;
import ch.uzh.ifi.seal.changedistiller.ast.java.JavaMethodBodyConverter;
import ch.uzh.ifi.seal.changedistiller.distilling.Distiller;
import ch.uzh.ifi.seal.changedistiller.distilling.DistillerFactory;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.SourceRange;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.util.CompilationUtils;

/**
 * @since: JDK 1.8
 */
@SuppressWarnings("restriction")
public class UtilChgDisNode {
	static final Injector				sInjector;
	static JavaDeclarationConverter	sDeclarationConverter;
	static JavaMethodBodyConverter	sMethodBodyConverter;

	static {
		sInjector = Guice.createInjector(new JavaChangeDistillerModule());
		sDeclarationConverter = sInjector.getInstance(JavaDeclarationConverter.class);
		sMethodBodyConverter = sInjector.getInstance(JavaMethodBodyConverter.class);
	}

	/**
	 * Get Distiller  
	 */
	public static Distiller getDistiller(StructureEntityVersion structureEntity) {
		return sInjector.getInstance(DistillerFactory.class).create(structureEntity);
	}

	/**
	 * Convert method body to a distiller node  
	 */
	public static Node convertMethodBody(String methodName, JavaCompilation compilation) {
		AbstractMethodDeclaration method = CompilationUtils.findMethod(compilation.getCompilationUnit(), methodName);
		Node root = new Node(JavaEntityType.METHOD, methodName);
		root.setEntity(new SourceCodeEntity(methodName, JavaEntityType.METHOD, new SourceRange(method.declarationSourceStart, method.declarationSourceEnd)));
		List<Comment> comments = CompilationUtils.extractComments(compilation);
		sMethodBodyConverter.initialize(root, method, comments, compilation.getScanner());
		method.traverse(sMethodBodyConverter, (ClassScope) null);
		return root;
	}

	/**
	 * Convert code snippets to a distiller node  
	 */
	public static Node convertCodeSnippets(String[] snippets) {
		String dummyMethodName = "dummyMethodName";
		JavaCompilation cu = CompilationUtils.compileSource(getSourceCodeWithSnippets(dummyMethodName, snippets));
		Node nResult = convertMethodBody("dummyMethodName", cu);
		return nResult;
	}

	/**
	 * Get source code with code snippets 
	 */
	public static String getSourceCodeWithSnippets(String dummyMethodName, String... snippets) {
		StringBuilder src = new StringBuilder("public class Foo { ");
		src.append("public void " + dummyMethodName + "() { ");
		for (String statement : snippets) {
			src.append(statement).append(' ');
		}
		src.append("} }");
		return src.toString();
	}

	/**
	 * Compute difference from two Distiller nodes
	 */
	public List<SourceCodeChange> diffMethod(Node aRootOldRev, Node aRootNewRev) {
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "dummyMethodName", 0);
		Distiller distiller = getDistiller(structureEntity);
		if (aRootOldRev == null)
			System.out.println("aRootOldRev: null");
		if (aRootNewRev == null)
			System.out.println("aRootNewRev: null");
		distiller.extractClassifiedSourceCodeChanges(aRootOldRev, aRootNewRev);
		List<SourceCodeChange> changes = structureEntity.getSourceCodeChanges();
		printChanges(changes);
		return changes;
	}

	/**
	 * Print changes
	 */
	public static boolean printChanges(List<SourceCodeChange> changes) {
		for (int i = 0; i < changes.size(); i++) {
			SourceCodeChange change = changes.get(i);
			if (change instanceof Update) {
				Update chUpdate = (Update) change;
				System.out.println("[DBG0] Update: " + chUpdate);

				SourceCodeEntity ch = chUpdate.getChangedEntity();
				SourceCodeEntity nw = chUpdate.getNewEntity();

				String before_lb = ch.getLabel();
				String after_lb = nw.getLabel();

				if (!before_lb.equals(after_lb)) {
					System.out.println("Label not equal:" + before_lb + " and " + after_lb);
				}
				System.out.println(ch);
				System.out.println(nw);

			} else if (change instanceof Move) {
				Move chMove = (Move) change;
				System.out.println("[DBG1] Move: " + chMove);
			} else if (change instanceof Insert) {
				Insert chInsert = (Insert) change;
				System.out.println("[DBG2] Insert: " + chInsert.getChangedEntity().getLabel() + " " + //
						chInsert.getChangedEntity().getUniqueName());
			} else if (change instanceof Delete) {
				Delete chDelete = (Delete) change;
				System.out.println("[DBG3] Delete: " + chDelete.getChangedEntity().getLabel() + " " + //
						chDelete.getChangedEntity().getUniqueName());
			} else {
				throw new RuntimeException("[WRN] LOOK AT HERE, PLEASE!!");
			}
		}
		return true;
	}
}
