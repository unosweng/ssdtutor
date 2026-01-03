package util;

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
}
