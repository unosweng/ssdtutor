package util;

import java.util.List;
import java.util.Map;

public class UtilList {

   public static String getElemList(List<String> list, String targetContained) {
      String replaced = targetContained.replaceAll("/", ",");
      String result = null;
      for (String iFile : list) {
         if (iFile.replaceAll("/", ",").contains(replaced)) {
            result = iFile;
            break;
         }
      }
      return result;
   }

public static String getElemList(Map<String, List<String>> commonFiles) {
	// TODO Auto-generated method stub
	for(Object value: commonFiles.values()) {
		System.out.println(value.toString());
	}
	return null;
}
}
