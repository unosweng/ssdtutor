package view.progelement;

import org.eclipse.jface.viewers.StyledString;

import model.MethodElement;

public class LabelProviderMethodParm extends LabelProviderProgElem {

   @Override
   public StyledString getStyledText(Object element) {
      if (element instanceof MethodElement) {
         return new StyledString(((MethodElement) element).getParameterStr());
      }
      return new StyledString(""); // super.getStyledText(element);
   }
}
