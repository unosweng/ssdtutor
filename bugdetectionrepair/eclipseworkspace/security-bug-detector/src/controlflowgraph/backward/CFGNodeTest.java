package controlflowgraph.backward;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

public class CFGNodeTest {
   static Map<String, CFGNode> map = new HashMap<String, CFGNode>();

   public static void main(String[] args) {
      CFGNode callee = getNode("callee.root"); // new CGNode("callee1");
      CFGNode caller1 = getNode("caller1");
      callee.add(caller1);

      CFGNode caller11 = getNode("caller1");
      CFGNode caller2 = getNode("caller2");
      caller11.add(caller2);

      CFGNode caller22 = getNode("caller2");
      CFGNode caller3 = getNode("caller3");
      CFGNode caller4 = getNode("caller4");
      caller22.add(caller3);
      caller22.add(caller4);


      Enumeration<?> walker = callee.breadthFirstEnumeration();
      while (walker.hasMoreElements()) {
         CFGNode iNode = (CFGNode) walker.nextElement();
         if (iNode.isLeaf()) {
            printPath(iNode);
         }
      }

      // Enumeration<?> enu = caller3.pathFromAncestorEnumeration(callee);
      // while (enu.hasMoreElements()) {
      // CGNode n = (CGNode) enu.nextElement();
      // System.out.print(n + " <- ");
      // }
   }

   static void printPath(DefaultMutableTreeNode iNode) {
      TreeNode[] iPath = iNode.getPath();
      for (TreeNode iNodeInPath : iPath) {
         System.out.print(iNodeInPath + " <- ");
      }
      System.out.println();
   }
   
   private static CFGNode getNode(String ID) {
      CFGNode cgNode = map.get(ID);
      if (cgNode == null) {
         cgNode = new CFGNode(ID);
         map.put(ID, cgNode);
         return cgNode;
      }
      return cgNode;
   }
}
