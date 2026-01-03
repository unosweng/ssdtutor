/**
 */
package visitor.findmisuse.constsecretkeycases;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;

import input.IRuleInfo;
import visitor.DetectionASTVisitor;
import visitor.FindMisuse;
import visitor.FindMisuseConstSecretKeyDetector;

/**
 * @since JDK1.8
 */
public class FindMisuseConstSecretKeyCase1NoMethodParm extends FindMisuse {
    DetectionASTVisitor detector;
    Set<IBinding> setVarDefUseBind;

    public FindMisuseConstSecretKeyCase1NoMethodParm(FindMisuseConstSecretKeyDetector detector, Set<IBinding> setVarDefUseBind) {
        this.detector = detector;
        this.setVarDefUseBind = setVarDefUseBind;
    }

    public boolean checkCase1_NoMethodParm(ClassInstanceCreation ciCre) {
        boolean flag_SecretKeySpec_GetBytes_ConstKey = false;
        List<?> args = ciCre.arguments();
        for (Object iObj : args) {
            if (iObj instanceof MethodInvocation) {
                MethodInvocation metInv = (MethodInvocation) iObj;
                String identifier = metInv.getName().getIdentifier();
                if (identifier.equals(IRuleInfo.GET_BYTES)) {
                    Expression expr = metInv.getExpression();
                    if (expr instanceof SimpleName) {
                        SimpleName spName = (SimpleName) metInv.getExpression();
                        if (setVarDefUseBind.contains(spName.resolveBinding())) {
                            // System.out.println("[DBG] (4) Matched condition #1");
                            flag_SecretKeySpec_GetBytes_ConstKey = true;
                        }
                    } else if (expr instanceof StringLiteral) {
                        flag_SecretKeySpec_GetBytes_ConstKey = true;
                    }
                }
            }
            else if (iObj instanceof StringLiteral) {
                StringLiteral strLit = (StringLiteral) iObj;
                String litVal = strLit.getLiteralValue();
                if (flag_SecretKeySpec_GetBytes_ConstKey && !litVal.isEmpty() /* litVal.equals(AES) */) {
                    // System.out.println("[DBG] (5) Matched condition #2");
                    return true;
                }
            }
        }
        return false;
    }
}