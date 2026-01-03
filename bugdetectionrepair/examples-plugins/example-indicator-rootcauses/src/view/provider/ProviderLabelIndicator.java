package view.provider;

import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.IStyledLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;

import model.ModelIndicator;

public class ProviderLabelIndicator extends LabelProvider implements IStyledLabelProvider {

   public ProviderLabelIndicator() {
   }

   @Override
   public StyledString getStyledText(Object element) {
      if (element instanceof ModelIndicator) {
         ModelIndicator p = (ModelIndicator) element;
         String n = p.getName();
         int sz = p.list().length;
         StyledString styledString = new StyledString(n);
         if (sz != 0) {
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
