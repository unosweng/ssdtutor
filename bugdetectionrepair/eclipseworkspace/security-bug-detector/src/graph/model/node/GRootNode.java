/*
 * @(#) GMethodNode.java
 *
 */
package graph.model.node;

import org.eclipse.jdt.core.IJavaElement;

import graph.model.GNode;

public class GRootNode extends GNode {

	public GRootNode(String id, String name, IJavaElement javaElem, Integer lineNo) {
		super(id, name, javaElem, lineNo);
	}
}
