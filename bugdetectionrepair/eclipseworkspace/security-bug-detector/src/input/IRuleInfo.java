/**
 */
package input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class IRuleInfo {

   public static enum VarField {
      VARDEF, VARFIELD
   };

   public static enum SearchType {
      MATCHES, CONTAINS, EQUALS, CONSTANT
   };

   public static final String AES = "AES";

   public static final String AES_ECB = "AES/ECB";
   public static final String AES_CBC = "AES/CBC/PKCS5PADDING";

   // Hash Functions
   public static final String SHA_1 = "SHA-1";
   public static final String SHA1 = "SHA1";
   public static final String SHA = "SHA";
   public static final String SHA_3 = "SHA3-512";
   public static final String MD5 = "MD5";

   public static final String CIPHER = "javax.crypto.Cipher";
   public static final String SECRET_KEY_SPEC = "javax.crypto.spec.SecretKeySpec";
   public static final String PBE_PARAM_SPEC = "javax.crypto.spec.PBEParameterSpec";
   public static final String MESSAGE_DIGEST = "java.security.MessageDigest";
   public static final String SECURE_RANDOM = "java.security.SecureRandom";
   public static final String KEY_GENERATOR = "javax.crypto.KeyGenerator";
   public static final String Secret_Key_Factory = "javax.crypto.SecretKeyFactory";
   public static final String Iv_Parameter_Spec = "javax.crypto.spec.IvParameterSpec";
   public static final List<String> DETECTION_LIST = new ArrayList<>(Arrays.asList(CIPHER, SECRET_KEY_SPEC, PBE_PARAM_SPEC, MESSAGE_DIGEST, SECURE_RANDOM, KEY_GENERATOR, Secret_Key_Factory, Iv_Parameter_Spec));

   public static final String ECB_MSG = "Electronic Code Book";
   public static final String HF_MSG = "Hash Functions";
   public static final String PBE_MSG = "PBE Parameter Spec";
   public static final String SR_MSG = "Secure Random";
   public static final String SKS_MSG = "Const Secret Key Handler";
//
   public static final String GET_INSTANCE = "getInstance";
   public static final String SET_SEED = "setSeed";
   public static final String GENERATE_SEED = "generateSeed";
   public static final String GET_BYTES = "getBytes";
   public static final String GET_SALT_NEXT_BYTES = "getSaltNextBytes";
   public static final String NEW_SALT_VAR = "newSalt";
   public static final String SECURE_RANDOM_VAR = "secureRandom";
   public static final String SECURE_RANDOM_TYPE = "SecureRandom";

   public static final int PBE_MIN_ITERATIONS = 1000;
   public static final int SET_SEED_MIN_LEN = 55;

   public static final String JCA1_REPAIR_MENU = "&1 Do not use Electronic CodeBook mode for Encryption";
   public static final String JCA2_REPAIR_MENU = "&2 Do not use Reversible one-way hash";
   public static final String JCA3_REPAIR_MENU = "&3 Do not use Constant Secret Keys";
   public static final String JCA4_REPAIR_MENU = "&4 Do not use Constant Salts for PBE";
   public static final String JCA5_REPAIR_MENU = "&5 Do not use Fewer than 1000 Iterations for PBE";
   public static final String JCA6_REPAIR_MENU = "&6 Do not use Static seeds to seed SecureRandom";
}
