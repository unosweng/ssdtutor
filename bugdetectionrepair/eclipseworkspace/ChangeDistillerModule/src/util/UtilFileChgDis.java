package util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class UtilFileChgDis {
   public static String getContents(String fileName) {
      String L = System.getProperty("line.separator");
      BufferedReader in = null;
      StringBuilder buf = new StringBuilder();
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line;
         while ((line = in.readLine()) != null) {
            buf.append(line + L);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return buf.toString();
   }
}
