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
import view.CryptoMisuseDetectionTree;

/**
 * <pre>
 * Refer to a rule in the following paper.
 * Ma et al. 2016 ASIACCS "CDRep Automatic Repair of Cryptographic Misuses in Android Applications"
 * 
 * Do not use a constant salt
 * ==========================
 * 1.const/4 v2, 0x0 
 * 2.new-instance v3, Ljava/crypto/spec/PBEParameterSpec;!<init>([BI)V
 * 3.const/16 v4, 0x64
 * 4.invoke-direct {v3, v2, v4}, Ljava/crypto/spec/PBEParameterSpec; !<init>([BI)V
 *
 * Do not use iteration count < 1000
 * =================================
 * Register v4 holds the parameter that sets the iteration count
 * 
 * </pre>
 */
public class FindMisusePBEDetecter extends DetectionASTVisitor {
   TraceMisuse traceMisuse;

   public FindMisusePBEDetecter() {
      ParserSAX parser = ParserSAX.getInstance(4);
      this.indicatorClass = parser.getIndicatorClass();
      this.message = parser.getMessage();
      this.indicatorPattern = "Pattern4";
      ParserSAX ruleParser = ParserSAX.getInstance(this.indicatorClass);
      this.rules = ruleParser.getRule();
      this.searchTypeRules = ruleParser.getSearchType();
      PrintHelper.printDebugMsgH("PBE Detector rules" + this.rules + " searchType[" + this.searchTypeRules + "]");
      traceMisuse = new TraceMisuse(this);
   }

   public FindMisusePBEDetecter(String javaFilePath, CryptoMisuseDetectionTree view) {
      this();
      this.javaFilePath = javaFilePath;
      this.cryptoTreeViewer = view;
   }

   public boolean visit(MethodDeclaration metDec) {
      this.methodClassName = metDec.getName().getFullyQualifiedName();
      metDec.accept(new ASTVisitor() {

         public boolean visit(VariableDeclarationFragment vdf) {
            checkVarDefUse(vdf, false);
            return true;
         }

         public boolean visit(ClassInstanceCreation ciCre) {
            try {
               //               checkIndicatorUsageA(metDec, ciCre);
               if (checkIndicatorUsageA(metDec, ciCre)) {
                  showResult(metDec, ciCre, getArgumentString());
               }
               else {
                  // must also check if arg is a method invocation
                  if (checkIndicatorUsagePBE_B(ciCre)) {
                     if (IGlobalProperty.PATTERN4_TRACE) {
                        traceMisuse.traceBackwardFromIndicator(metDec, ciCre, getPositionToTrack());
                        updateTreeView();
                     }

                  }
               }
            } catch (Exception e) {
               PrintHelper.printErrorMsg("PBE failure on " + metDec.getName() + ":" + ciCre.toString());
               e.printStackTrace();
            }
            return true;
         }
      });
      return true;
   }
}
