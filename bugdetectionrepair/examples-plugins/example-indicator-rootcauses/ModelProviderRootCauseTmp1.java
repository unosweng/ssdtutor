package model;

import java.util.ArrayList;
import java.util.List;

public class ModelProviderRootCauseTmp1 {
   private List<ModelRootCause> listRootCause;

   public ModelProviderRootCauseTmp1() {
      listRootCause = new ArrayList<ModelRootCause>();
      listRootCause.add(new ModelRootCause("Root Cause Candiate 1"));
      listRootCause.add(new ModelRootCause("Root Cause Candiate 2"));
   }

   public List<ModelRootCause> getRootCauseList() {
      return listRootCause;
   }
}
