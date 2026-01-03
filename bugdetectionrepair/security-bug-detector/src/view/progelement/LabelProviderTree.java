package view.progelement;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import model.MethodElement;
import model.ProgramElement;

public class LabelProviderTree extends LabelProvider implements IStyledLabelProvider {

   @Override
   public StyledString getStyledText(Object element) {
      if (element instanceof ProgramElement) {
         ProgramElement p = (ProgramElement) element;
         String n = p.getName();
         int sz = p.list().length;
         StyledString styledString = new StyledString(n);
         if ((sz != 0) && !(element instanceof MethodElement)) {
            styledString.append(" ( " + sz + " ) ", StyledString.COUNTER_STYLER);
         }
         return styledString;
      }
      return null;
   }

   @Override
   public Image getImage(Object element) {
      return super.getImage(element);
   }

   @Override
   public void dispose() {
   }
}
