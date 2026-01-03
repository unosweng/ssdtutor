
package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import graph.model.GNode;
import model.MethodElement;
import model.ProgramElement;
import util.TableViewerHelper;
import util.TreeViewerHelper;

public class CryptoMisuseDetectionTree {
   public static final String VIEW_ID = "security-bug-detector.partdescriptor.cryptomisusedetectiontree";
   public final static String POPUPMENU_ID = "security-bug-detector.popupmenu.mypopupmenu";
   private TreeViewer treeViewer;
   private TableViewer viewer;
   private String indicatorClass;
   private MethodElement me = null;
   private List<GNode> treeNodes = new ArrayList<GNode>();

   @PostConstruct
   public void postConstruct(Composite parent, EMenuService menuService) {
      SashForm sashFormMain = new SashForm(parent, SWT.HORIZONTAL);
      Group grpTree = new Group(sashFormMain, SWT.NONE);
      grpTree.setLayout(new FillLayout(SWT.HORIZONTAL));
      treeViewer = new TreeViewer(grpTree, SWT.MULTI | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
      TreeViewerHelper.createTreeColumns(treeViewer);
      TreeViewerHelper.createContext(treeViewer, sashFormMain);
      GridData gridData = new GridData();
      gridData.widthHint = 10;
      gridData.verticalAlignment = GridData.FILL;
      gridData.horizontalAlignment = GridData.FILL;
      gridData.grabExcessVerticalSpace = true;
      gridData.grabExcessHorizontalSpace = true;
      treeViewer.getControl().setLayoutData(gridData);
      treeViewer.expandAll();
      Group grpTable = new Group(sashFormMain, SWT.NONE);
      grpTable.setLayout(new FillLayout(SWT.HORIZONTAL));
      viewer = new TableViewer(grpTable, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
      createTableColumns();
      addListener();
      final Table table = viewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);
      viewer.setContentProvider(ArrayContentProvider.getInstance());
      GridData gridData1 = new GridData();
      gridData1.verticalAlignment = GridData.FILL;
      gridData1.horizontalAlignment = GridData.FILL;
      gridData1.grabExcessVerticalSpace = true;
      gridData1.grabExcessHorizontalSpace = true;
      viewer.getControl().setLayoutData(gridData1);
   }

   private void addListener() {
      Tree tree = (Tree) treeViewer.getControl();
      tree.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            TreeItem item = (TreeItem) event.item;
            me = (MethodElement) item.getData();
            List<Object> data = TableViewerHelper.TableViewerData(me);
            viewer.setInput(data);   
         }
      });
      Table table = (Table) viewer.getControl();
      table.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent event) {
            TableItem item = (TableItem) event.item;
            me = (MethodElement) item.getData();
            TreeViewerHelper.openInEditor(me);
            indicatorClass = me.getIndicatorClassName();
            treeNodes = new ArrayList<GNode>();
            if (me.getIndicator().toString().toLowerCase().equals("root")) {
               treeNodes = me.getGraph();
               TableViewerHelper.createContextMenu(viewer, me);
            }
            if (me.getIndicator().toString().toLowerCase().equals("indroot")) {
               TableViewerHelper.createContextMenu(viewer, me);
               treeNodes = me.getGraph();
            }
            if (me.getIndicator().toString().toLowerCase().equals("ind")) {
               treeNodes = me.getGraph();
               Menu contextMenu = new Menu(viewer.getTable());
               viewer.getTable().setMenu(contextMenu);
            }
         }
      });
   }
   
   private void createTableColumns() {
      String[] titles = {"Detection", "Type", "Line", "FileName"};
      int[] bounds = {300, 100, 100, 200};
      int index = 0;
      TableViewerColumn col = createTableColumn(titles[index], bounds[index], index);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            MethodElement p = (MethodElement) element;
            return p.getDetectionLine();
         }
      });
      ++index;
      col = createTableColumn(titles[index], bounds[index], index);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            MethodElement p = (MethodElement) element;
            return p.getIndicator().toString();
         }
      });
      ++index;
      col = createTableColumn(titles[index], bounds[index], index);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            MethodElement p = (MethodElement) element;
            return String.valueOf(p.getLineNumber());
         }
      });
      ++index;
      col = createTableColumn(titles[index], bounds[index], index);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            MethodElement p = (MethodElement) element;
            return p.getFileName();
         }
      });
   }

   private TableViewerColumn createTableColumn(String title, int bound, final int colNumber) {
      final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
      final TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(bound);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

   @Focus
   public void setFocus() {
      this.treeViewer.getControl().setFocus();
   }
   public TreeViewer getTreeViewer() {
      return this.treeViewer;
   }

   public Map<String, List<ProgramElement>> getTreeData() {
      TreeItem[] treeItems = treeViewer.getTree().getItems();
      ProgramElement item1= null;
      Map<String, List<ProgramElement>> items = new HashMap<>();
      for(TreeItem treeItem: treeItems) {
         item1 = (ProgramElement) treeItem.getData();
         items.put(item1.getName(), item1.getListChildren());
      }
      return items;
   }

   public String getMisusePattern() {
      return indicatorClass;
   }

   public List<GNode> treeNodes() {
      return treeNodes;
   }

   public void settreeNodes(List<GNode> treeNodes) {
      this.treeNodes = treeNodes;
   }

   public List<String> getTableData() {
      TableItem[] tableItems = viewer.getTable().getItems();
      List<String> items = new ArrayList<String>();
      if(tableItems.length>0) {
         MethodElement item1 = (MethodElement) tableItems[0].getData();
         int lastIndex = item1.getOriginalPath().lastIndexOf("/");
         String subString = item1.getFilePath().substring(lastIndex+1);
         int fileIndex = subString.indexOf("/");
         String filePath = subString.substring(fileIndex);
         String orgPath = item1.getOriginalPath() + filePath;
         items.add(orgPath);
         String repPath = item1.getRepairPath() + filePath;
         items.add(repPath);
         items.add(filePath);
         items.add(item1.getFilePath().replace(filePath, ""));
      }
      return items;
   }

   public MethodElement getMethodElement() {
      TableItem[] tableItems = viewer.getTable().getItems();
      MethodElement item = null;
      if(tableItems.length>0) {
         item = (MethodElement) tableItems[0].getData();
      }
      return item;
   }

   public void clearTreeViewer() {
      TreeViewerHelper.clearTreeView(this.treeViewer);
   }
   
   public void clearTableViewer() {
      TreeViewerHelper.clearTableView(this.viewer);
   }

   public HashSet<String> getTreeList() {
      return TreeViewerHelper.getTreeList();
   }

   public HashSet<String> getIndicatorList() {
      return TreeViewerHelper.getIndicatorList();
   }

}