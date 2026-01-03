package model;

import java.util.HashSet;

public enum ProgElementModelProvider {
   INSTANCE;

   private HashSet<ProgramElement> pElements = new HashSet<>();

   public HashSet<ProgramElement> getProgElements() {
      return pElements;
   }

   public ProgramElement addProgramElement(ProgramElement pElem) {
      for (ProgramElement iElem : pElements) {
         if (iElem.getName().equals(pElem.getName())) {
            return iElem;
         }
      }
      this.pElements.add(pElem);
      return null;
   }

   public void clearProgramElements() {
      this.pElements.clear();
   }
}
