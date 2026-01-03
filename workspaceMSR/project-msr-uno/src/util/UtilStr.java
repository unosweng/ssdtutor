/**
 * @file UtilStr.java
 */
package util;

import java.util.Date;

import org.junit.Test;

/**
 * @since JavaSE-1.8
 */
public class UtilStr {

   // Log.logV1(String)

   @Test
   public void test() {
      System.out.println(getClass("Log.logV1(String)"));
      System.out.println(getMethod("Log.logV1(String)"));
      System.out.println(getParm("Log.logV1(String)"));
      System.out.println(getNumParm("Log.logV1(String)"));

      System.out.println(getClass("Log.logV1(String, boolean)"));
      System.out.println(getMethod("Log.logV1(String, boolean)"));
      System.out.println(getParm("Log.logV1(String, boolean)"));
      System.out.println(getNumParm("Log.logV1(String, boolean)"));
   }

   public static String getClass(String input) {
      return input.split("\\.")[0].trim();
   }

   public static String getMethod(String input) {
      String token2nd = input.split("\\.")[1];
      String str = token2nd.split("\\(")[0];
      return str.trim();
   }

   public static String getParm(String input) {
      String token2nd = input.split("\\(")[1];
      String token1st = token2nd.split("\\)")[0].trim();
      return token1st;
   }

   public static int getNumParm(String input) {
      String token2nd = input.split("\\(")[1];
      String token1st = token2nd.split("\\)")[0].trim();
      String[] split = token1st.split("\\,");
      return split.length;
   }
   
   public static long getDateDiff(Date start, Date end, long type) {
      return ((end.getTime() - start.getTime()) / type);
   }
}
