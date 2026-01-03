package visitor;

import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

public class DetectionASTVisitorTest {

   String s = "^AES.ECB";

   public boolean searchPatterns(String input) {
      String str = StringUtils.upperCase(input);

      Pattern pattern = Pattern.compile(s);
      Matcher matcher = pattern.matcher(str);

      if (matcher.find()) {
         return true;
      }

      return false;
   }

   @Test
   public void testSearchPatterns() {

      boolean ret = searchPatterns("AES/ECB/PKCS5Padding");
      assertTrue(ret);
   }

}
