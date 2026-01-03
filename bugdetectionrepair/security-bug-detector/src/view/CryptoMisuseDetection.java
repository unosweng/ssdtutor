/**
 */
package view;

import java.util.HashSet;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

import util.TreeViewerHelper;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */ 
public class CryptoMisuseDetection {
   public static final String VIEW_ID = "bugdetectionexample.partdescriptor.cryptomisusedetection";
   public final static String POPUPMENU_ID = "bugdetectionexample.popupmenu.mypopupmenu";
   private StyledText styledText = null;
   private TreeViewer treeViewer;

   @PostConstruct
   public void postConstruct(Composite parent, EMenuService menuService) {
      Composite container = new Composite(parent, SWT.NONE);
      container.setLayout(new FillLayout(SWT.HORIZONTAL));
      styledText = new StyledText(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
      Font font = new Font(container.getDisplay(), new FontData("Courier New", 13, SWT.NORMAL));
      styledText.setFont(font);
      menuService.registerContextMenu(styledText, POPUPMENU_ID);

      this.treeViewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      TreeViewerHelper.createProgElemColumns(treeViewer);
      TreeViewerHelper.createContextMenu(treeViewer, parent);
      TreeViewerHelper.createDoubleClickListener(treeViewer);
   }

   public void setText(String str) {
      this.styledText.setText(str);
   }

   public void appendText(String str) {
      this.styledText.append(str + System.getProperty("line.separator"));
   }

   @Focus
   public void setFocus() {
      this.treeViewer.getControl().setFocus();
   }

   public TreeViewer getTreeViewer() {
      return this.treeViewer;
   }

   public void clearTreeViewer() {
      TreeViewerHelper.clearTreeView(this.treeViewer);
   }

   public HashSet<String> getTreeList() {
      return TreeViewerHelper.getTreeList();
   }

   public HashSet<String> getIndicatorList() {
      return TreeViewerHelper.getIndicatorList();
   }
}
