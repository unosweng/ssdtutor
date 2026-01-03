package examples.update.version2;

public class C {

   int m1(String a) {
      String b = a.substring(1);
      System.out.println(b);
      if (a.startsWith("REQUEST"))
         System.out.println(a);

      return 0;
   }
}