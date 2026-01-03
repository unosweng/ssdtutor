/**
 * @(#) ModelProvider.java
 */
package model;

import java.util.ArrayList;
import java.util.List;

/**
 * @since J2SE-1.8
 */
public enum ModelProvider {
   INSTANCE;
   private List<ProjectElement> progElements = new ArrayList<ProjectElement>();;

   public void addProgramElements(String prjName, String groupName, String pkgName, String className, String methodName, int localVar, int FieldVar) {
      progElements.add(new ProjectElement(prjName, groupName, pkgName, className, methodName, localVar, FieldVar));
   }

   public List<ProjectElement> getProgramElements() {
      return progElements;
   }

   public void clearProgramElements() {
      progElements.clear();
   }
}
