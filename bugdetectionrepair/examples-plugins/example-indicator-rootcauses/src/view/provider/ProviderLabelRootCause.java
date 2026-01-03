package view.provider;

import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Text;

import model.ModelRootCause;

public class ProviderLabelRootCause extends StyledCellLabelProvider {

   public ProviderLabelRootCause(final Text searchText) {
   }

   @Override
   public void update(ViewerCell cell) {
      String cellText = getCellText(cell);
      cell.setText(cellText);
      super.update(cell);
   }

   protected String getCellText(ViewerCell cell) {
      ModelRootCause rootcause = (ModelRootCause) cell.getElement();
      String cellText = rootcause.getName();
      return cellText;
   }
}
