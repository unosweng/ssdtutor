package model;

import java.util.ArrayList;
import java.util.List;

public enum ModelProviderIndicator {
   INSTANCE;

   private List<ModelIndicator> indicators;

   private ModelProviderIndicator() {
      indicators = new ArrayList<ModelIndicator>();
      // group 1
      ModelIndicator p1 = new ModelIndicator("Result 1");
      ModelIndicator p1a = new ModelIndicator("pkg1.ClassA.m1", p1);
      ModelIndicator p1b = new ModelIndicator("pkg1.ClassB.m2 (Indicator)", p1);

      List<ModelRootCause> listRootCause1 = new ArrayList<ModelRootCause>();
      listRootCause1.add(new ModelRootCause("Root Cause Candiate r1-1"));
      listRootCause1.add(new ModelRootCause("Root Cause Candiate r1-2"));
      p1b.setListModelRootCause(listRootCause1);

      p1.add(p1a);
      p1.add(p1b);

      // group 2
      ModelIndicator p2 = new ModelIndicator("Result 2");
      ModelIndicator p2a = new ModelIndicator("pkg2.ClassB.m2", p2);
      ModelIndicator p2b = new ModelIndicator("pkg2.ClassC.m3 (Indicator)", p2);

      List<ModelRootCause> listRootCause2 = new ArrayList<ModelRootCause>();
      listRootCause2.add(new ModelRootCause("Root Cause Candiate r2-1"));
      listRootCause2.add(new ModelRootCause("Root Cause Candiate r2-2"));
      p2b.setListModelRootCause(listRootCause2);

      p2.add(p2a);
      p2.add(p2b);

      // sub group 3
      // ModelIndicator p3 = new ModelIndicator("Customer Group 3", p2);
      // ModelIndicator p3a = new ModelIndicator("Customer 3-a", p2);
      // ModelIndicator p3b = new ModelIndicator("Customer 3-b", p2);
      // p3.add(p3a);
      // p3.add(p3b);
      // p2.add(p3); // p2 links to p3

      indicators.add(p1);
      indicators.add(p2);
   }

   public List<ModelIndicator> getPersons() {
      return indicators;
   }
}
