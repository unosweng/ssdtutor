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
import view.CryptoMisuseDetectionTree;

/**
 * <pre>
 * Refer to a rule in the following paper.
 * Ma et al. 2016 ASIACCS "CDRep Automatic Repair of Cryptographic Misuses in Android Applications"
 *  1.const-string v2, "SHA-1"
 *  2.invoke-static {v2}, Ljava/security/ MessageDigest;!getInstance(Ljava/lang/String;) Ljava/security/MessageDigest
 * </pre>
 */
public class FindMisuseHashFunDetector extends DetectionASTVisitor {
   TraceMisuse traceMisuse;

   /**********************************************************************************/

   public FindMisuseHashFunDetector() {
      this.positionToTrack = 0;
      ParserSAX parser = ParserSAX.getInstance(2);
      this.indicatorPattern = "Pattern2";
      this.indicatorClass = parser.getIndicatorClass();
      this.message = parser.getMessage();
      ParserSAX ruleParser = ParserSAX.getInstance(this.indicatorClass);
      this.rules = ruleParser.getRule();
      this.searchTypeRules = ruleParser.getSearchType();
      PrintHelper.printDebugMsgH("HashFun Detector rules" + this.rules + " searchType[" + this.searchTypeRules + "]");
      traceMisuse = new TraceMisuse(this);
   }

   public FindMisuseHashFunDetector(String javaFilePath, CryptoMisuseDetectionTree view) {
      this.javaFilePath = javaFilePath;
      this.cryptoTreeViewer = view;
   }

   /**********************************************************************************/
   @Override
   public boolean visit(MethodDeclaration metDec) {
      this.methodClassName = metDec.getName().getFullyQualifiedName();
      metDec.accept(new ASTVisitor() {

         public boolean visit(VariableDeclarationFragment vdf) {
            checkVarDefUse(vdf, false);
            return true;
         }

         public boolean visit(MethodInvocation metInv) {
            try {
//               checkIndicatorUsageA(metDec, metInv);
               // Check each method invocation in each method declaration
               // Compare it with Cipher.getInstance()
               if (checkIndicatorUsageA(metDec, metInv)) {
                  showResult(metDec, metInv);
               } 
               else {
                  if (checkIndicatorUsageB(metDec, metInv, getPositionToTrack())) {
                     /* Perform Backtracking:
                      * - Argument to Cipher.getInstance also appears as a MethodDeclaration parameter
                      * - Back track the MethodDeclaration argument
                      * - Find a methodInvocation that contains a constant parameter
                      */
                     if (IGlobalProperty.PATTERN2_TRACE) {
                        traceMisuse.traceBackwardFromIndicator(metDec, metInv, getPositionToTrack());
                        updateTreeView();
                     }
                  }
               }
            } catch (Exception e) {
               PrintHelper.printErrorMsg("visit(MD/MI) : " + metDec.getName().getFullyQualifiedName());
               e.printStackTrace();
            }
            return true;
         }
      });
      return true;
   }

   /**********************************************************************************/

}
