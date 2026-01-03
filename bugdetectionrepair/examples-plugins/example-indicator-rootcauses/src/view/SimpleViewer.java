
package view;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class SimpleViewer {
   public final static String VIEW_ID = "example-indicator-rootcauses.partdescriptor.callfallviewer";
   private StyledText styledText;

   @PostConstruct
   public void postConstruct(Composite parent, EMenuService menuService) {
      Composite container = new Composite(parent, SWT.NONE);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));
      styledText = new StyledText(container, SWT.BORDER);
   }

   public void appendText(String str) {
      styledText.append(str);
   }

   public void clear() {
      styledText.setText("");
   }
}