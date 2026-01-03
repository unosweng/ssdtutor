package model;

import java.util.ArrayList;
import java.util.List;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;

public enum ChangeDistillerProgramElement {
   INSTANCE;

   private List<ChangeDistillerElement> distElements = new ArrayList<ChangeDistillerElement>();;

   private ChangeDistillerProgramElement() {
   }

   public void addProgramElements(String oldFile, String newFile, String pkgName, String className, String methodName, String editOperation, String editLabel, EntityType entityType, String changedEntityName, String sourceRangeLine) {
      distElements.add(new ChangeDistillerElement(oldFile, newFile, pkgName, className, methodName, editOperation, editLabel, entityType, changedEntityName, sourceRangeLine));
   }

   public List<ChangeDistillerElement> getProgramElements() {
      return distElements;
   }

   public void clearProgramElements() {
      distElements.clear();
   }
}
