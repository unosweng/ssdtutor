package util;

import java.util.Iterator;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.regex.Pattern;

public class EnvironmentHelper {

   private static PropertyResourceBundle bundle = (PropertyResourceBundle) ResourceBundle.getBundle("environment");
   private static PropertyResourceBundle inputBundle = (PropertyResourceBundle) ResourceBundle.getBundle("INPUT");

   public static String getProperty(String propVal) {
      String retVal = bundle.getString(propVal);

      String SYS_PROP_STRT_CHAR_REG_EX_TXT = "\\$\\{";
      String SYS_PROP_END_CHAR_REG_EX_TXT = "}";
      String SYS_PROP_REG_EX_TXT = "^.*" + SYS_PROP_STRT_CHAR_REG_EX_TXT + "[a-zA-Z0-9_.]+\\" + SYS_PROP_END_CHAR_REG_EX_TXT + ".*$";

      Pattern SYS_PROP_REG_EX = Pattern.compile(SYS_PROP_REG_EX_TXT);

      // determine if the property value contains a "reference" to a System
      // property
      if (propVal != null && SYS_PROP_REG_EX.matcher(propVal).matches()) {
         String[] tokens = propVal.split(SYS_PROP_STRT_CHAR_REG_EX_TXT);
         TreeSet<String> sysPropNames = new TreeSet<String>();
         String sysPropNme = null;
         Iterator<String> iterator = null;

         // Intercept the System property names
         for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].indexOf(SYS_PROP_END_CHAR_REG_EX_TXT) > (-1)) {
               sysPropNme = tokens[i].substring(0, tokens[i].indexOf(SYS_PROP_END_CHAR_REG_EX_TXT));
               sysPropNames.add(sysPropNme);
            }
         }

         // Inject the System property values
         iterator = sysPropNames.iterator();
         while (iterator.hasNext()) {
            sysPropNme = iterator.next();
            retVal = retVal.replaceAll(SYS_PROP_STRT_CHAR_REG_EX_TXT + sysPropNme + SYS_PROP_END_CHAR_REG_EX_TXT, System.getProperty(sysPropNme));
         }
      }
      return retVal;
   }

   public static String getInputProperty(String propVal) {
      String retVal = inputBundle.getString(propVal);

      String SYS_PROP_STRT_CHAR_REG_EX_TXT = "\\$\\{";
      String SYS_PROP_END_CHAR_REG_EX_TXT = "}";
      String SYS_PROP_REG_EX_TXT = "^.*" + SYS_PROP_STRT_CHAR_REG_EX_TXT + "[a-zA-Z0-9_.]+\\" + SYS_PROP_END_CHAR_REG_EX_TXT + ".*$";

      Pattern SYS_PROP_REG_EX = Pattern.compile(SYS_PROP_REG_EX_TXT);

      // determine if the property value contains a "reference" to a System
      // property
      if (propVal != null && SYS_PROP_REG_EX.matcher(propVal).matches()) {
         String[] tokens = propVal.split(SYS_PROP_STRT_CHAR_REG_EX_TXT);
         TreeSet<String> sysPropNames = new TreeSet<String>();
         String sysPropNme = null;
         Iterator<String> iterator = null;

         // Intercept the System property names
         for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].indexOf(SYS_PROP_END_CHAR_REG_EX_TXT) > (-1)) {
               sysPropNme = tokens[i].substring(0, tokens[i].indexOf(SYS_PROP_END_CHAR_REG_EX_TXT));
               sysPropNames.add(sysPropNme);
            }
         }

         // Inject the System property values
         iterator = sysPropNames.iterator();
         while (iterator.hasNext()) {
            sysPropNme = iterator.next();
            retVal = retVal.replaceAll(SYS_PROP_STRT_CHAR_REG_EX_TXT + sysPropNme + SYS_PROP_END_CHAR_REG_EX_TXT, System.getProperty(sysPropNme));
         }
      }
      return retVal;
   }

}
