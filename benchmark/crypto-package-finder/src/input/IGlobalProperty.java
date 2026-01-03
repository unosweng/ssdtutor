/**
 */
package input;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public interface IGlobalProperty {
   public String GIT_REPO_DIR = "/work/hsiy/shared/firebugs/git-repo/";

   public String TARGET_SAMPLE_DIR1 = "/firebugs-local/bugdetectionexample/runtime-bugdetectionexample/";

   public String TARGET_GIT_DIR1 = GIT_REPO_DIR + "AndroidUtilCode/" //
         + "utilcode/lib/src/main/java/com/blankj/utilcode/util/"; 

   public String TARGET_GIT_DIR = TARGET_GIT_DIR1;

   public boolean IS_FILE_CONTENTS_PRINT = false;
}
