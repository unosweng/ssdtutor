package util;

import java.util.HashMap;
import java.util.Map;

public class CodeFragments {
	Map<String, String> patternRules = new HashMap<String, String>();
	Map<String, String> patternName = new HashMap<String, String>();
	public CodeFragments() {
		// TODO Auto-generated constructor stub
	}

	public String getCodeFragments(String code, String pattern) {
		patternRules.put("Pattern1", "Cipher.getInstance(");
		patternRules.put("Pattern2", "MessageDigest.getInstance(");
		patternRules.put("Pattern3", "SecretKeySpec(");
		patternRules.put("Pattern4", "new PBEParameterSpec(");
		patternRules.put("Pattern5", "SecureRandom(");
		patternRules.put("Pattern6", "KeyGenerator.getInstance(");
		patternRules.put("Pattern7", "SecretKeyFactory.getInstance(");
		patternRules.put("Pattern8", "IvParameterSpec(");
		patternName.put("Pattern1", "Cipher");
		patternName.put("Pattern2", "MessageDigest");
		patternName.put("Pattern3", "SecretKeySpec");
		patternName.put("Pattern4", "PBEParameterSpec");
		patternName.put("Pattern5", "SecureRandom");
		patternName.put("Pattern6", "KeyGenerator");
		patternName.put("Pattern7", "SecretKeyFactory");
		patternName.put("Pattern8", "IvParameterSpec");
		code = removeComment(code);
		String[] stringList = code.split("\n");
		String s1 = stringList[0];
		String method = String.format("public static void m(){");
		String match = patternRules.get(pattern);
		String p = patternName.get(pattern);
		boolean hasVar = false;
		String var = null;
		int i = 1;
		for (i=1; i < stringList.length; i++) {
			String s = stringList[i];
			if(s.contains(match)) {
				String[] varName = s.split("=");
				if(varName[0].contains(p)) {
					hasVar = true;
					String[] varN = varName[0].split(" ");
					var = varN[varN.length-1];
				}
				stringList[i] = stringList[i].replace("\u0009", "   ");
				String s2 = stringList[i].replace("\"", "\\\"");
				method = method + "\\n" + s2;
				break;
			}
		}
		if(hasVar) {
			boolean hasEnd = true;
			for (int j=i+1; j < stringList.length; j++) {
				String s = stringList[j];
				if(!hasEnd) {
					stringList[j] = stringList[j].replace("\u0009", "   ");
					String s2 = stringList[j].replace("\"", "\\\"");
					method = method + "\\n" + s2;
					if(s.contains(";")) {
						hasEnd = true;
					}
				}
				else if(s.contains(var)) {
					stringList[j] = stringList[j].replace("\u0009", "   ");
					String s2 = stringList[j].replace("\"", "\\\"");
					method = method + "\\n" + s2;
					if(!s.contains(";")) {
						hasEnd = false;
					}
				}
			}
		}
		method += "}";
		return method;
	}
	
	public String removeComment(String c) {
		String clean = c.replaceAll( "//.*|(\"(?:\\\\[^\"]|\\\\\"|.)*?\")|(?s)/\\*.*?\\*/", "$1 " );
		return clean;
	}

}
