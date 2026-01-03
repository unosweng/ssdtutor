/**
 */
package input;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import util.EnvironmentHelper;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public interface IGlobalProperty {

   public static final String COLUMN_SEPARATOR = ",";
   public static final String FILE_SEPARATOR = ".";
   public static final String NAME_SEPARATOR = "_";
   public static final String PATH_SEPARATOR = "/";
   public static final String PATH_SEPARATOR_WINDOWS = "\\";
   public static final String ISO_88659_1 = "ISO-88659-1";
   public static final String UTF_8 = "UTF-8";
   public static final String DEFAULT_FILE_ENCODING = ISO_88659_1;

   public static final String JAVANATURE = "org.eclipse.jdt.core.javanature";
   public String TARGET_SAMPLE_DIR1 = "/firebugs-local/bugdetectionexample/runtime-security-bug-detector/";
   public String TARGET_SAMPLE_DIR1_WIN = "F:\\data\\git-repository\\firebugs-local\\bugdetectionexample\\runtime-security-bug-detector\\case3-misuse-const-secret-key";

   public String TARGET_GIT_DIR1 = "/data/firebugs/git-repo/AndroidUtilCode/" //
         + "utilcode/lib/src/main/java/com/blankj/utilcode/util/"; // "/data/firebugs/git-repo/AndroidUtilCode";

   public String CASE3_CONST_SECKEY = "case3-misuse-const-secret-key/";

   public String TARGET_GIT_DIR = TARGET_SAMPLE_DIR1 + CASE3_CONST_SECKEY;

   public boolean IS_FILE_CONTENTS_PRINT = false;

   // String[] sources = { //
   // TARGET_SAMPLE_DIR1 + CASE3_CONST_SECKEY + "src" //
   // };
   // String[] CLASSPATH_MAC = { "/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/jre/lib/rt.jar" };

   /*
    * See Oracle documentation on how classes are found https://docs.oracle.com/javase/6/docs/technotes/tools/findingclasses.html
    */
   // String classPathSystemProperty = System.getProperty("sun.boot.class.path");
   
   /* The latest JDK versions (JDK9+) do not support boot class path. 
    * See "INPUT.properties" file to update one's own JDK8 RT JAR path.
    */
   String classPathSystemProperty = EnvironmentHelper.getInputProperty("JDK8_RT_JAR"); 
   
   String PATH_SEC_RULE_FILES = EnvironmentHelper.getInputProperty("PATH_SEC_RULE_FILES"); 

   public String PATH_TYPE_FILEPATH_CSV = "PATH_TYPE_FILEPATH.csv";
   public String PATH_DETECTOR_FILEPATH_CSV = "PATH_DETECTOR_FILEPATH.csv";

   public String MAC_PATH_TYPE_FILEPATH_CSV = "PATH_TYPE_FILEPATH_MAC.csv";
   public String MAC_PATH_DETECTOR_FILEPATH_CSV = "PATH_DETECTOR_FILEPATH_MAC.csv";

   public String JCA_DETECTION = "jcaDetection";

   public boolean PATTERN3_CASE1 = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case1"));
   public boolean PATTERN3_CASE2 = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case2"));
   public boolean PATTERN3_CASE2_SKIP_BACKTRACKING = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case2.skip.backtracking"));
   public boolean PATTERN3_CASE3 = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case3"));
   public boolean PATTERN3_CASE4 = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case4"));
   public boolean PATTERN3_CASE5 = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case5"));
   public boolean PATTERN3_CASE5_USE_CFG = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case5.useCFG"));
   public boolean PATTERN3_CASE6 = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.case6"));

   public boolean PATTERN1_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern1.trace"));
   public boolean PATTERN2_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern2.trace"));
   public boolean PATTERN3_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.trace"));
   public boolean PATTERN4_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern4.trace"));
   public boolean PATTERN5_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern5.trace"));
   public boolean PATTERN6_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern6.trace"));
   public boolean PATTERN7_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern7.trace"));
//   public boolean PATTERN8_TRACE = Boolean.valueOf(EnvironmentHelper.getProperty("pattern8.trace"));

   public boolean PATTERN1_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern1.run"));
   public boolean PATTERN2_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern2.run"));
   public boolean PATTERN3_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern3.run"));
   public boolean PATTERN4_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern4.run"));
   public boolean PATTERN5_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern5.run"));
   public boolean PATTERN6_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern6.run"));
   public boolean PATTERN7_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern7.run"));
//   public boolean PATTERN8_RUN = Boolean.valueOf(EnvironmentHelper.getProperty("pattern8.run"));

   public List<String> PATTERN1_SEARCH_LIST = Arrays.asList(EnvironmentHelper.getProperty("pattern1.searchList").split(COLUMN_SEPARATOR));
   public String PATTERN1_SEARCH_TYPE = StringUtils.trimToEmpty(EnvironmentHelper.getProperty("pattern1.searchType"));

   public List<String> PATTERN2_SEARCH_LIST = Arrays.asList(EnvironmentHelper.getProperty("pattern2.searchList").split(COLUMN_SEPARATOR));
   public String PATTERN2_SEARCH_TYPE = StringUtils.trimToEmpty(EnvironmentHelper.getProperty("pattern2.searchType"));

   public List<String> PATTERN3_SEARCH_LIST = Arrays.asList(EnvironmentHelper.getProperty("pattern3.searchList").split(COLUMN_SEPARATOR));
   public String PATTERN3_SEARCH_TYPE = StringUtils.trimToEmpty(EnvironmentHelper.getProperty("pattern3.searchType"));

   public List<String> PATTERN4_SEARCH_LIST = Arrays.asList(EnvironmentHelper.getProperty("pattern4.searchList").split(COLUMN_SEPARATOR));
   public String PATTERN4_SEARCH_TYPE = StringUtils.trimToEmpty(EnvironmentHelper.getProperty("pattern4.searchType"));

   public List<String> PATTERN5_SEARCH_LIST = Arrays.asList(EnvironmentHelper.getProperty("pattern5.searchList").split(COLUMN_SEPARATOR));
   public String PATTERN5_SEARCH_TYPE = StringUtils.trimToEmpty(EnvironmentHelper.getProperty("pattern5.searchType"));

   public static final int MAX_RECURSION_DEPTH = 3;

   public static final String ARG1 = "ARG1";
   public static final String ARG2 = "ARG2";

   public static final String SALT = "SALT";
   public static final String NOI = "NOI"; // Number of Iterations

   public static final String JCA_COUNT_FILENAME = "jcaCount.csv";
   public static final String JCA_COUNT_FILENAME_MAC = "jcaCountMac.csv";

   public static final String JCA_INDICATOR_COUNT = "jcaIndicatorCount.csv";
   public static final String JCA_INDICATOR_COUNT_MAC = "jcaIndicatorCountMac.csv";
   
   public static final String GET_RANDOM_SECRET_KEY = "getRandomSecretKey";
   public static final String VAR1 = "VAR1";
   public static final String VAR2 = "VAR2";
   public static final String SECURERANDOM = "secureRandom";
   public static final String GENERATE_SEED = "generateSeed";
   public static final int SET_SEED_MIN_LEN = 55;
   
   public static final String ORIGINAL = "original";
   public static final String REPAIR = "repair";
}
