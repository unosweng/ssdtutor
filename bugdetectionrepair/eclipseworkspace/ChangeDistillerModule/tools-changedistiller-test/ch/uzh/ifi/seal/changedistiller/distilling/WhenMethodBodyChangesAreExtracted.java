package ch.uzh.ifi.seal.changedistiller.distilling;

/*
 * #%L
 * ChangeDistiller
 * %%
 * Copyright (C) 2011 - 2013 Software Architecture and Evolution Lab, Department of Informatics, UZH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Ignore;
import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.ast.java.JavaCompilation;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.StructureEntityVersion;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;
import ch.uzh.ifi.seal.changedistiller.util.CompilationUtils;

// simple test cases. exhaustive test cases with classification check in separate tests suite
public class WhenMethodBodyChangesAreExtracted extends WhenChangesAreExtracted {

	private final static String TEST_DATA = "src_change2" + File.separator;

	@Ignore
	@Test
	public void unchangedMethodBodyShouldNotHaveAnyChanges() throws Exception {
		JavaCompilation compilation = CompilationUtils.compileFile(TEST_DATA + "TestLeft.java");
		Node rootLeft = convertMethodBody("foo", compilation);
		Node rootRight = convertMethodBody("foo", compilation);
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "foo", 0);
		Distiller distiller = getDistiller(structureEntity);
		distiller.extractClassifiedSourceCodeChanges(rootLeft, rootRight);
		assertThat(structureEntity.getSourceCodeChanges().isEmpty(), is(true));
	}

	@Ignore
	@Test
	public void changedMethodBodyShouldHaveChanges() throws Exception {
		JavaCompilation compilationLeft = CompilationUtils.compileFile(TEST_DATA + "TestLeft.java");
		JavaCompilation compilationRight = CompilationUtils.compileFile(TEST_DATA + "TestRight.java");
		Node rootLeft = convertMethodBody("foo", compilationLeft);
		Node rootRight = convertMethodBody("foo", compilationRight);
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "foo", 0);
		Distiller distiller = getDistiller(structureEntity);
		distiller.extractClassifiedSourceCodeChanges(rootLeft, rootRight);
		assertThat(structureEntity.getSourceCodeChanges().size(), is(11));
	}

	@Ignore
	@Test
	public void changedMethodBody() {
		JavaCompilation compilationLeft = CompilationUtils.compileFile(TEST_DATA + "TestLeft.java");
		JavaCompilation compilationRight = CompilationUtils.compileFile(TEST_DATA + "TestRight.java");
		Node rootLeft = convertMethodBody("foo", compilationLeft);
		Node rootRight = convertMethodBody("foo", compilationRight);
		System.out.println("[DBG] Print the left and right subtrees:");
		printPreoder(rootRight);
		System.out.println("------------------------------------------");
		printPreoder(rootLeft);
		System.out.println("==========================================");
		// Create structure entity.
		StructureEntityVersion structureEntity = new StructureEntityVersion(JavaEntityType.METHOD, "foo", 0);
		Distiller distiller = getDistiller(structureEntity);
		// Extract changes.
		distiller.extractClassifiedSourceCodeChanges(rootLeft, rootRight);
		List<SourceCodeChange> changes = structureEntity.getSourceCodeChanges();
		printChanges(changes);
		System.out.println("==========================================");
	}
	//
	// @Test
	// public void changedMethodBodyAST() {
	// String _S_ = File.separator;
	// String dirPath = "resources" + _S_ + "testdata" + _S_ + "src_change2" +
	// _S_;
	// String contentOfLeftFile = UtilFile.getContentOfFile(dirPath +
	// "TestLeft.java");
	// String contentOfRightFile = UtilFile.getContentOfFile(dirPath +
	// "TestRight.java");
	// CompilationUnit astOfLeftFile = parse(contentOfLeftFile.toCharArray());
	// CompilationUnit astOfRightFile = parse(contentOfRightFile.toCharArray());
	// // AST Visitor
	// MethodDeclVisitor astVisitor = new MethodDeclVisitor();
	// astOfLeftFile.accept(astVisitor);
	//// astOfRightFile.accept(astVisitor);
	//// MethodDeclaration fooOfLeftFile = astVisitor.getMethod();
	//// MethodDeclaration fooOfRightFile = astVisitor.getMethod();
	//// IMethodBinding binding = fooOfLeftFile.resolveBinding();
	//// System.out.println(binding.getDeclaringClass().getQualifiedName() + "."
	// + fooOfLeftFile.getName().getFullyQualifiedName());
	//// System.out.println(fooOfRightFile.getName().getFullyQualifiedName());
	// }

	class MethodDeclVisitor extends ASTVisitor {
		MethodDeclaration myMethod = null;

		@Override
		public boolean visit(MethodDeclaration node) {
			String name = node.getName().getFullyQualifiedName();
			if (myMethod == null && name.contains("foo")) {
				myMethod = node;
			}
			return super.visit(node);
		}

		public MethodDeclaration getMethod() {
			return myMethod;
		}
	}

	/**
	 * @method
	 */
	void printChanges(List<SourceCodeChange> changes) {
		for (int i = 0; i < changes.size(); i++) {
			SourceCodeChange change = changes.get(i);
			if (change instanceof Update) {
				Update chUpdate = (Update) change;
				System.out.println("[DBG0] " + chUpdate);
			} else if (change instanceof Move) {
				Move chMove = (Move) change;
				System.out.println("[DBG1] " + chMove);
			} else if (change instanceof Insert) {
				Insert chInsert = (Insert) change;
				System.out.println("[DBG2] " + chInsert.getChangedEntity().getLabel() + " " + //
						chInsert.getChangedEntity().getUniqueName());
			} else if (change instanceof Delete) {
				Delete chDelete = (Delete) change;
				System.out.println("[DBG3] " + chDelete.getChangedEntity().getLabel() + " " + //
						chDelete.getChangedEntity().getUniqueName());
			} else {
				throw new RuntimeException("LOOK AT HERE, PLEASE!!");
			}
		}
	}

	/**
	 * @method
	 */
	void printPreoder(DefaultMutableTreeNode n) {
		Enumeration<?> itr = n.preorderEnumeration();
		while (itr.hasMoreElements()) {
			Object obj = (Object) itr.nextElement();
			System.out.println(obj);
		}
	}

	CompilationUnit parse(char[] unit) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(unit);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}
}
