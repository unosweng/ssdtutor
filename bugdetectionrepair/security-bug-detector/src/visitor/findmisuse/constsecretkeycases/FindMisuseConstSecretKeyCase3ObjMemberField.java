/**
 */
package visitor.findmisuse.constsecretkeycases;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.QualifiedName;

import visitor.DetectionASTVisitor;
import visitor.FindMisuse;
import visitor.FindMisuseConstSecretKeyDetector;

/**
 * @since JDK1.8
 */
public class FindMisuseConstSecretKeyCase3ObjMemberField extends FindMisuse {
   DetectionASTVisitor detector;

   public FindMisuseConstSecretKeyCase3ObjMemberField(FindMisuseConstSecretKeyDetector detector) {
      this.detector = detector;
   }

   /*
    *  Argument(s) linked to object's member field(s) - QualifiedName
    *  Example: SecretKeySpec(newManager.finalKey, "AES");
    */
   public boolean checkCase3_ObjMemberField(MethodDeclaration metDec, ClassInstanceCreation ciCre) {
      List<QualifiedName> listQualifiedName = new ArrayList<QualifiedName>();
      List<?> arguments = ciCre.arguments();
      for (Object iObj : arguments) {
         if (iObj instanceof QualifiedName) {
            QualifiedName iQName = (QualifiedName) iObj;
            listQualifiedName.add(iQName);
         }
      }
      if (!listQualifiedName.isEmpty()) {
         // TODO Need to trace across classes
         return true;
      }
      return false;
   }
}
