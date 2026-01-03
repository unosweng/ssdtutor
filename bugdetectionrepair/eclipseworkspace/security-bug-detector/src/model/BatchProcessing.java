package model;

import java.util.ArrayList;
import java.util.List;

public enum BatchProcessing {
   INSTANCE;
   List<MethodElement> misuses = new ArrayList<MethodElement>();
   
   public void addMisuse(MethodElement pE) {
      this.misuses.add(pE);
   }
   
   public List<MethodElement> getMisuse() {
      return this.misuses;
   }
   public void clearMisuse() {
      this.misuses.clear();
   }
}
