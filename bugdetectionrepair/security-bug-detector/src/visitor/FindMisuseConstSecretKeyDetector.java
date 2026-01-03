/**
 */
package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import input.IGlobalProperty;
import util.ParserSAX;
import util.PrintHelper;
import view.CryptoMisuseDetection;

/**
 * <pre>
 * Refer to a rule in the following paper.
 * Ma et al. 2016 ASIACCS "CDRep Automatic Repair of Cryptographic Misuses in Android Applications"
 *   1.invoke-virtual, v2, Ljava/lang/String.getBytes()[B
 *   2.const-string, v4, "AES"
 *   3.invoke-direct {v3, v2, v4}, Ljava/crypto/spec/SecretKeySpec;! \<init\>([BLjava/lang/String;)V
 * </pre>
 */
public class FindMisuseConstSecretKeyDetector extends DetectionASTVisitor {
	TraceMisuse traceMisuse;

	public FindMisuseConstSecretKeyDetector() {
		this.positionToTrack = 0;
		ParserSAX parser = ParserSAX.getInstance(3);
		this.indicatorClass = parser.getIndicatorClass();
		this.message = parser.getMessage();
		ParserSAX ruleParser = ParserSAX.getInstance(this.indicatorClass);
		this.rules = ruleParser.getRule();
		this.searchTypeRules = ruleParser.getSearchType();
		PrintHelper.printDebugMsgH("SKS Detector rules" + this.rules + " searchType[" + this.searchTypeRules + "]");
		traceMisuse = new TraceMisuse(this);
	}

	public FindMisuseConstSecretKeyDetector(String javaFilePath, CryptoMisuseDetection view) {
		this();
		this.javaFilePath = javaFilePath;
		this.cryptoViewer = view;
	}

	@Override
	public boolean visit(MethodDeclaration metDec) {

		metDec.accept(new ASTVisitor() {

			@Override
			public boolean visit(VariableDeclarationFragment vdf) {
				checkVarDefUse(vdf, false);
				return true;
			}

			@Override
			public boolean visit(ClassInstanceCreation ciCre) {
				try {
					if (checkIndicatorUsageA(metDec, ciCre)) {
						showResult(metDec, ciCre, getArgumentString());
					} else {
						// must also check if arg is a method invocation
						if (checkIndicatorUsageB(metDec, ciCre, getPositionToTrack())) {
							if (IGlobalProperty.PATTERN3_TRACE) {
								traceMisuse.traceBackwardFromIndicator(metDec, ciCre, getPositionToTrack());
								updateTreeView();
							}

						}
					}
				} catch (Exception e) {
					PrintHelper.printErrorMsg("SKS failure on " + metDec.getName() + ":" + ciCre.toString());
					e.printStackTrace();
				}
				return true;
			}
		});
		return true;
	}

}