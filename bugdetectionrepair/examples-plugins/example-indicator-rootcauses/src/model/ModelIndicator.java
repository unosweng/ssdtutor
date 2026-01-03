package model;

import java.util.ArrayList;
import java.util.List;

public class ModelIndicator {
   List<ModelIndicator> listIndicator = new ArrayList<ModelIndicator>();
   List<ModelRootCause> listModelRootCause = new ArrayList<>();
   ModelIndicator parent = null;
   String name = null;

   public ModelIndicator(String name) {
      this.name = name;
      parent = this;
   }

   public ModelIndicator(String name, ModelIndicator parent) {
      this.name = name;
      this.parent = parent;
   }

   public ModelIndicator[] list() {
      ModelIndicator[] l = new ModelIndicator[listIndicator.size()];
      return listIndicator.toArray(l);
   }

   public void add(ModelIndicator p) {
      listIndicator.add(p);
   }

   public ModelIndicator getParent() {
      return this.parent;
   }

   public boolean hasChildren() {
      return !this.listIndicator.isEmpty();
   }

   public String getName() {
      return this.name;
   }

   public List<ModelRootCause> getListModelRootCause() {
      return listModelRootCause;
   }

   public void setListModelRootCause(List<ModelRootCause> listModelRootCause) {
      this.listModelRootCause = listModelRootCause;
   }

   public String toString() {
      return this.name;
   }
}
