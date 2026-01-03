package model;

import java.util.ArrayList;
import java.util.List;

public enum ModelProviderRootCause {
   INSTANCE;

   private List<ModelRootCause> listRootCause;

   private ModelProviderRootCause() {
      listRootCause = new ArrayList<ModelRootCause>();
      listRootCause.add(new ModelRootCause("Default 1"));
      listRootCause.add(new ModelRootCause("Default 2"));
   }

   public List<ModelRootCause> getRootCauseList() {
      return listRootCause;
   }
}
