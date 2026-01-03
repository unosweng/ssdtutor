/*
 * @(#) GClassNode.java
 *
 */
package graph.model.node;

import org.eclipse.jdt.core.IJavaElement;

import graph.model.GNode;

public class GIndicatorNode extends GNode {
   public GIndicatorNode(String id, String name, IJavaElement javaElem, Integer lineNo) {
		super(id, name, javaElem, lineNo);
	}
}
