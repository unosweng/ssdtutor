/**
 * @file UtilChgDisNodeTest.java
 */
package util;

import java.util.Enumeration;

import org.junit.Test;

import ch.uzh.ifi.seal.changedistiller.treedifferencing.Node;

/**
 * @date Aug 3, 2016
 * @since JavaSE-1.8
 */
public class UtilChgDisNodeTest {

	@Test
	public void convertCodeSnippetsTest() {
		String[] snippets = {
				"if ($v1.isPrefixActive()) {",   
				"if (getFocusOwner() instanceof JTextComponent) {",     
					"$v2=getFocusOwner();",     
					"$v3=true;",     
					"getTextArea().requestFocus();",   
				"}",  
				"else   if ($v3) {",     
					"getTextArea().requestFocus();",   
				"}",  
				"else {",     
					"$v2=null;",   
				"}", 
			"}",  
			"else {",   
				"$v2=null;", 
			"}",
			"$v1.isPrefixActive()"
		};
		Node node = UtilChgDisNode.convertCodeSnippets(snippets);
		Enumeration<?> e = node.preorderEnumeration();
		while (e.hasMoreElements()) {
			Node n = (Node) e.nextElement();
			System.out.println(n);
		}
		System.out.println("------------------------");
		
		String[] snippets2 = {
				"$.v30.println($v1);", 
				"$.v30.println($v1.substring(0,$v1.length()));"
		};
		
		StringBuilder buf = new StringBuilder();
		node = UtilChgDisNode.convertCodeSnippets(snippets2);
		buf = new StringBuilder();
		node.print(buf);
		System.out.println(buf);
		System.out.println("------------------------");
		System.out.println("Done.");
	}
}
