package examples.move.version1;

public class D {

   int m1(String a, int responseCode, int requestCode) {
      String b = a.substring(1);
      System.out.println(b);

      if (a.startsWith("RESPONSE-1")) {
         System.out.println(responseCode);
         System.out.println(a);
      } else {
         System.out.println(requestCode);
      }

      return 0;
   }
}