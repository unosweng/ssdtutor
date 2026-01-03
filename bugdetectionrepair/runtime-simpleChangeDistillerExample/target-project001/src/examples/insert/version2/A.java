package examples.insert.version2;

public class A {
   int m1(String a) {
      String b = a.substring(1);
      System.out.println(a);
      System.out.println(b);
      return 0;
   }

   int m2(String a) {
      String b = a.substring(1);
      System.out.println(b);
      for (int i = 0; i < a.length(); i++) {
         System.out.println(a.charAt(i));
      }
      return 0;
   }

   int m3(String a) {
      String b = a.substring(1);
      System.out.println(b);
      if (a.startsWith("COMMAND"))
         System.out.println(a);

      return 0;
   }
}