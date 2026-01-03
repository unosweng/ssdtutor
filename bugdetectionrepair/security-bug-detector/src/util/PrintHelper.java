package util;

import input.IRuleInfo;

public class PrintHelper {

    public final static String DBG = "[DBG]\t";
    public final static String ERR = "[ERR]\t";
    public final static String DBG_H = "\n[DBG]\t";

    public static void printDebugMsg(String msg) {
        System.out.println(DBG + msg);
    }

    public static void printDebugMsgH(String msg) {
        System.out.println(DBG_H + msg);
    }

    public static void printDebugMsgT(String msg) {
       System.out.println(DBG + msg + "\n");
   }

    public static void printErrorMsg(String msg) {
       System.out.println(ERR + msg);
    }
    
    public static class PBEParamSpec {
        public static String printUsage(String fullyQualifiedName, int position, String args) {
            String msg = "Found at line: " + position + ", " + fullyQualifiedName + " uses " + IRuleInfo.PBE_PARAM_SPEC + args;
            System.out.println(DBG + msg);
            return msg;
        }
    }
    
    public static class SecretKeySpec {

        public static String printUsage(String fullyQualifiedName, int position, String args) {
            String msg = "Found at line: " + position + ", " + fullyQualifiedName + " uses " + IRuleInfo.SECRET_KEY_SPEC + args;
            System.out.println(DBG + msg);
            return msg;
        }

        public static String printFieldFound(String qualifiedClassName, String fieldName, int position) {
            String msg = "Field found: " + qualifiedClassName + "." + fieldName.replaceAll("\\r|\\n", "") + " line:" + position;
            System.out.println(DBG + msg);
            return msg;
        }

        public static String printMethodFound(String qualifiedClassName, String methodName, int position) {
            String msg = "Method found: " + qualifiedClassName + "." + methodName + " line:" + position;
            System.out.println(DBG + msg);
            return msg;
        }

        public static String printMethodExpr(String qualifiedClassName, String methodName, String expr, int position) {
            String msg = methodName + " of " + qualifiedClassName + " " + expr + " line:" + position;
            System.out.println(DBG + msg);
            return msg;
        }

        public static String printLocalMethodAssignment(String qualifiedClassName, String assignment, int position) {
            String msg = assignment + " in " + qualifiedClassName + " line:" + position;
            System.out.println(DBG + msg);
            return msg;
        }

        public static String printMethodAssignment(String qualifiedClassName, String methodName, String assignment, int position) {
            String msg = assignment + " in " + methodName + " in " + qualifiedClassName + " line:" + position;
            System.out.println(DBG + msg);
            return msg;
        }

        public static String printReturnExpr(String qualifiedClassName, String methodName, String expr, int position) {
            String msg = methodName + " of " + qualifiedClassName + " " + expr + " returned at line: " + position;
            System.out.println(DBG + msg);
            return msg;
        }
    }
}
