/**
 */
package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import input.IGlobalProperty;
import util.ParserSAX;
import util.PrintHelper;
import view.CryptoMisuseDetection;

/**
 * <pre>
 * Refer to a rule in the following paper.
 * Ma et al. 2016 ASIACCS "CDRep Automatic Repair of Cryptographic Misuses in Android Applications"
 * 
 * Do not use a constant seed to seed SecureRandom
 * ===============================================
 *
 *   1.p0, [B
 *   2....
 *   3.const-string/jumbo v1, "SHA1PRNG"
 *   4.invoke-static {v2, v1},
 *       Ljava/security/SecureRandom;->getInstance (Ljava/lang/String;Ljava/lang/String;) Ljava/security/SecureRandom;
 *   5.move-result-object v1
 *   6.invoke-virtual {v1, p0}, Ljava/security/ SecureRandom;->setSeed([B)V
 * 
 * </pre>
 */
public class FindMisuseSecureRandomDetecter extends DetectionASTVisitor {
	TraceMisuse traceMisuse;

	public FindMisuseSecureRandomDetecter() {
		ParserSAX parser = ParserSAX.getInstance(5);
		this.indicatorClass = parser.getIndicatorClass();
		this.message = parser.getMessage();
		ParserSAX ruleParser = ParserSAX.getInstance(this.indicatorClass);
		this.rules = ruleParser.getRule();
		this.searchTypeRules = ruleParser.getSearchType();
		PrintHelper.printDebugMsgH("SecureRandom.setSeed rules" + this.rules + " searchType[" + this.searchTypeRules + "]");
		traceMisuse = new TraceMisuse(this);
	}

	public FindMisuseSecureRandomDetecter(String javaFilePath, CryptoMisuseDetection view) {
		this();
		this.javaFilePath = javaFilePath;
		this.cryptoViewer = view;
	}

	/**
	 * @ case #1
	 * 
	 * <pre>
	 * 
	 * SecureRandom.getInstance().setSeed("string".getBytes);
	 * byte[] seed = new byte[20];
	 * SecureRandom.getInstance().setSeed(seed);
	 * SecureRandom.getInstance().setSeed(new byte[20]);
	 * 
	 * </pre>
	 */
	@Override
	public boolean visit(MethodDeclaration metDec) {

		metDec.accept(new ASTVisitor() {
			@Override
			public boolean visit(VariableDeclarationFragment vdf) {
				checkVarDefUse(vdf, false);
				return true;
			}

			@Override
			public boolean visit(MethodInvocation metInv) {
				try {
					// Check each method invocation in each method declaration
					// Compare it with Cipher.getInstance()
					if (checkIndicatorUsageA(metDec, metInv)) {
						showResult(metDec, metInv);
					} else {
						if (checkIndicatorUsageB(metDec, metInv, getPositionToTrack())) {

							/* Perform Backtracking:
							 * - Argument also appears as a MethodDeclaration parameter
							 * - Back track the MethodDeclaration argument
							 */
							if (IGlobalProperty.PATTERN5_TRACE) {
								traceMisuse.traceBackwardFromIndicator(metDec, metInv, getPositionToTrack());
								updateTreeView();
							}
						}
					}
				} catch (Exception e) {
					PrintHelper.printErrorMsg("SR.setSeed failure on " + metDec.getName() + ":" + metInv.toString());
					e.printStackTrace();
				}
				return true;
			}

		});
		return true;
	}

}
