/*
 * @(#) GPackageNode.java
 *
 */
package graph.model.node;

import org.eclipse.jdt.core.IJavaElement;

import graph.model.GNode;

public class GPathNode extends GNode {
	
	public GPathNode(String id, String name, IJavaElement javaElem, Integer lineNo) {
		super(id, name, javaElem, lineNo);
	}
}
