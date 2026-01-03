package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
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
public class FindMisuseInitializationVector extends DetectionASTVisitor {
   TraceMisuse traceMisuse;

   /**********************************************************************************/

   public FindMisuseInitializationVector() {
      ParserSAX parser = ParserSAX.getInstance(8);
      this.indicatorClass = parser.getIndicatorClass();
      this.indicatorPattern = "Pattern8";
      this.message = parser.getMessage();
      ParserSAX ruleParser = ParserSAX.getInstance(this.indicatorClass);
      this.rules = ruleParser.getRule();
      this.searchTypeRules = ruleParser.getSearchType();
      PrintHelper.printDebugMsgH("Initialization Vector Detector rules"+ this.rules + " searchType[" + this.searchTypeRules + "]");
      traceMisuse = new TraceMisuse(this);
   }

   public FindMisuseInitializationVector(String javaFilePath, CryptoMisuseDetectionTree view) {
      this.javaFilePath = javaFilePath;
      this.cryptoTreeViewer = view;
   }

   /**********************************************************************************/
   @Override
   public boolean visit(MethodDeclaration metDec) {
      this.methodClassName = metDec.getName().getFullyQualifiedName();
      metDec.accept(new ASTVisitor() {

         @Override
         public boolean visit(VariableDeclarationFragment vdf) {
            checkVarDefUse(vdf, false);
            return true;
         }
            
         @Override
         public boolean visit(ClassInstanceCreation ciCre) {
            try {
               //               checkIndicatorUsageIV(metDec, ciCre);
               if (checkIndicatorUsageIV(metDec, ciCre)) {
                  showResult(metDec, ciCre, getArgumentString());
               } 
               else {
                  // must also check if arg is a method invocation
                  if (checkIndicatorUsageB(metDec, ciCre, getPositionToTrack())) {
                     if (IGlobalProperty.PATTERN7_TRACE) {
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
   /**********************************************************************************/

}
