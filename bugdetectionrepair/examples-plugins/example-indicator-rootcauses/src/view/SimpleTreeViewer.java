
package view;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import model.ModelIndicator;
import model.ModelProviderIndicator;
import view.provider.ProviderContentIndicator;
import view.provider.ProviderLabelIndicator;

public class SimpleTreeViewer {
   @Inject
   EPartService ePartService;
   private TreeViewer viewer;

   @PostConstruct
   public void postConstruct(Composite parent) {
      viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
      viewer.setContentProvider(new ProviderContentIndicator());
      viewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new ProviderLabelIndicator()));
      List<ModelIndicator> data = ModelProviderIndicator.INSTANCE.getPersons();
      ModelIndicator[] array = data.toArray(new ModelIndicator[data.size()]);
      viewer.setInput(array);
      addListener();
   }

   private void addListener() {
      Tree tree = (Tree) viewer.getControl();
      tree.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            TreeItem item = (TreeItem) e.item;
            if (item != null) {
               Object data = item.getData();
               if (data instanceof ModelIndicator) {
                  ModelIndicator person = (ModelIndicator) data;
                  System.out.println("[DBG] " + person);
               }
            }
         }
      });
      viewer.addDoubleClickListener(new IDoubleClickListener() {
         @Override
         public void doubleClick(DoubleClickEvent event) {
            IStructuredSelection thisSelection = (IStructuredSelection) event.getSelection();
            Object selectedNode = thisSelection.getFirstElement();
            viewer.setExpandedState(selectedNode, !viewer.getExpandedState(selectedNode));

            MPart findPart = ePartService.findPart(SimpleTableViewer.VIEW_ID);
            Object findPartObj = findPart.getObject();

            if (findPartObj instanceof SimpleTableViewer) {
               SimpleTableViewer v = (SimpleTableViewer) findPartObj;
               ModelIndicator mi = (ModelIndicator) selectedNode;
               v.setInput(mi.getListModelRootCause());
            }
         }
      });

      tree = (Tree) viewer.getControl();
      Listener listener = new Listener() {
         @Override
         public void handleEvent(Event event) {
            TreeItem treeItem = (TreeItem) event.item;
            final TreeColumn[] treeColumns = treeItem.getParent().getColumns();
            viewer.getTree().getDisplay().asyncExec(new Runnable() {
               @Override
               public void run() {
                  for (TreeColumn treeColumn : treeColumns)
                     treeColumn.pack();
               }
            });
         }
      };
      tree.addListener(SWT.Expand, listener);
   }

   @Focus
   public void setFocus() {
      viewer.getControl().setFocus();
   }
}