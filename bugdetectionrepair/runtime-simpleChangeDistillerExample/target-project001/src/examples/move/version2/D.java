package examples.move.version2;

public class D {

   int m1(String a, int responseCode, int requestCode) {
      String b = a.substring(1);
      System.out.println(b);

      if (a.startsWith("RESPONSE-1")) {
         System.out.println(responseCode);
      } else {
         System.out.println(requestCode);
         System.out.println(a);
      }

      return 0;
   }
}