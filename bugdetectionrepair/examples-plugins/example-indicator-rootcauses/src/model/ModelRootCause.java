package model;

public class ModelRootCause {
   String name = null;

   public ModelRootCause(String name) {
      this.name = name;
   }

   public ModelRootCause(String name, ModelRootCause parent) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String toString() {
      return this.name;
   }
}
