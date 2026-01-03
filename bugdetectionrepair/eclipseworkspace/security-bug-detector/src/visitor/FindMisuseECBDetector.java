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
 * s
 * Refer to a rule in the following paper.
 * Ma et al. 2016 ASIACCS "CDRep Automatic Repair of Cryptographic Misuses in Android Applications"
 *   1.const-string v1 "AES/ECB/PKCSPadding"
 *   2.invoke-static javax.crypto.Cipher java.crypto.Cipher.getInstance(String v1)
 * </pre>
 */
public class FindMisuseECBDetector extends DetectionASTVisitor {
   TraceMisuse traceMisuse;
   //   final String className;

   /**********************************************************************************/

   public FindMisuseECBDetector() {
      this.positionToTrack = 0;
      ParserSAX parser = ParserSAX.getInstance(1);
      this.indicatorClass = parser.getIndicatorClass();
      this.indicatorPattern = "Pattern1";
      this.message = parser.getMessage();
      ParserSAX ruleParser = ParserSAX.getInstance(this.indicatorClass);
      this.rules = ruleParser.getRule();
      this.searchTypeRules = ruleParser.getSearchType();
      PrintHelper.printDebugMsgH("ECB Detector rules" + this.rules + " searchType[" + this.searchTypeRules + "]");
      traceMisuse = new TraceMisuse(this);
   }

   public FindMisuseECBDetector(String javaFilePath, CryptoMisuseDetectionTree view) {
      this.javaFilePath = javaFilePath;
      this.cryptoTreeViewer = view;
   }

   /**********************************************************************************/
   @Override
   public boolean visit(MethodDeclaration metDec) {
      // PrintHelper.printDebugMsgH("MethodDeclaraton: " + metDec.getName().getFullyQualifiedName());
      this.methodClassName = metDec.getName().getFullyQualifiedName();
      metDec.accept(new ASTVisitor() {
         @Override
         public boolean visit(VariableDeclarationFragment vdf) {
            checkVarDefUse(vdf, false);
            return true;
         }

         @Override
         public boolean visit(MethodInvocation metInv) {
            try {
//               checkIndicatorUsageA(metDec, metInv);
               // Check each method invocation in each method declaration
               // Compare it with Cipher.getInstance()
               
               if (checkIndicatorUsageA(metDec, metInv)) {
                  showResult(metDec, metInv);
               }else {
                  if (checkIndicatorUsageB(metDec, metInv, getPositionToTrack())) {
                     /* Perform Backtracking:
                      * - Argument to Cipher.getInstance also appears as a MethodDeclaration parameter
                      * - Back track the MethodDeclaration argument
                      * - Find a methodInvocation that contains a constant parameter
                      */
                     if (IGlobalProperty.PATTERN1_TRACE) {
                        traceMisuse.traceBackwardFromIndicator(metDec, metInv, getPositionToTrack());
                        updateTreeView();
                     }
                  }
               }
            } catch (Exception e) {
               PrintHelper.printErrorMsg("ECB failure on " + metDec.getName() + ":" + metInv.toString());
               e.printStackTrace();
            }
            System.gc();
            return true;
         }

      });
      return true;
   }
   /**********************************************************************************/

}
