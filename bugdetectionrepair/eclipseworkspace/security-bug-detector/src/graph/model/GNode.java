/*
 * @(#) MyNode.java
 *
 */
package graph.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;

public class GNode {
	private final String id;
	private final String name;
	private final IJavaElement javaElem;
	private final Integer lineNo;
	private List<GNode>  connections;

	public GNode(String id, String name, IJavaElement javaElem, Integer lineNo) {
		this.id = id;
		this.name = name;
		this.javaElem = javaElem;
		this.connections = new ArrayList<GNode>();
		this.lineNo = lineNo;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public IJavaElement getJavaElement() {
		return javaElem;
	}
	
	public Integer getLineNumber() {
		return lineNo;
	}

	public List<GNode> getConnectedTo() {
		return connections;
	}
}
