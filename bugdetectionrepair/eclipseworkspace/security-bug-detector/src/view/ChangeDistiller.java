package view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import org.eclipse.e4.ui.workbench.modeling.EPartService;

//import analysis.FileAnalyzer;
//import model.ModelProvider;
import model.ChangeDistillerProgramElement;
import model.ChangeDistillerElement;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Text;
//import handler.Diff2htmlHandler;

public class ChangeDistiller {

   public static final String VIEW_ID = "security-bug-detector.partdescriptor.changedistiller";
   public final static String POPUPMENU_ID = "security-bug-detector.popupmenu.mypopupmenu";
   private TableViewer viewer;
   private StyledText oldData;
   private StyledText newData;
   private StyledText styledText = null;

   @Inject
   EPartService epartService;
   @Inject
   public ChangeDistiller() {

   }

   @PostConstruct
   public void postConstruct(Composite parent, EMenuService menuService) {
      parent.setLayout(new FillLayout(SWT.HORIZONTAL));

      SashForm sashFormMain = new SashForm(parent, SWT.VERTICAL);
      SashForm sashFormSub = new SashForm(sashFormMain, SWT.HORIZONTAL);

      // Buggy Version
      Group grpBuggyVersion = new Group(sashFormSub, SWT.NONE);
      grpBuggyVersion.setText("Buggy Version");
      grpBuggyVersion.setLayout(new FillLayout(SWT.HORIZONTAL));

      oldData = new StyledText(grpBuggyVersion, SWT.BORDER);

      // Repaired Version
      Group grpRepairedVersion = new Group(sashFormSub, SWT.NONE);
      grpRepairedVersion.setText("Repaired Version");
      grpRepairedVersion.setLayout(new FillLayout(SWT.HORIZONTAL));

      newData = new StyledText(grpRepairedVersion, SWT.BORDER);
      sashFormSub.setWeights(new int[] {212, 235});

      // Repair Edit Information Table
      Group grpEditOperations = new Group(sashFormMain, SWT.NONE);
      grpEditOperations.setText("Repair Edit Information");
      grpEditOperations.setLayout(new FillLayout(SWT.HORIZONTAL));

      viewer = new TableViewer(grpEditOperations, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
      createProgElemColumns(parent, viewer);
      final Table table = viewer.getTable();
      table.setHeaderVisible(true);
      table.setLinesVisible(true);

      viewer.setContentProvider(ArrayContentProvider.getInstance());
      menuService.registerContextMenu(viewer.getControl(), POPUPMENU_ID);
      addListener();

   }
   @PostConstruct
   public void createControls(Composite parent, EMenuService menuService) {

   }

   private void addListener() {
      viewer.addSelectionChangedListener(new ISelectionChangedListener() {

         @Override
         public void selectionChanged(SelectionChangedEvent event) {
            // TODO Auto-generated method stub
            IStructuredSelection selection = ( IStructuredSelection )event.getSelection();
            List<?> selectedElements = selection.toList();
            StringBuilder oldStr = new StringBuilder();
            StringBuilder newStr = new StringBuilder();
            Map<Integer, Integer> strMap = new HashMap<Integer, Integer>();
            Map<Integer, Integer> newstrMap = new HashMap<Integer, Integer>();
            for(Object Element: selectedElements) {
               ChangeDistillerElement me = (ChangeDistillerElement) Element;
               String oldFile = me.getOldFile();
               String data = getFileData(oldFile, me);
               int i = me.getClassName().lastIndexOf(".");
               String className = me.getClassName().substring(i+1);
               className = className + ".java";
               String s = String.format("--- line %s, %s-- \n", me.getSourceRangeLine(), className);
               if(data.length()>0) {
                  oldStr.append(s);
                  int v = oldStr.toString().length();
                  oldStr.append(data);
                  strMap.put(v, data.length());
               }
               String newFile = me.getNewFile();
               data = getFileData(newFile, me);
               if(data.length()>0) {
                  newStr.append(s);
                  int v = newStr.toString().length();
                  newStr.append(data);
                  newstrMap.put(v, data.length());
               }
            }
            fileDifferentiator(oldStr.toString(), newStr.toString(), strMap, newstrMap);
         }
      });
   }

   private void fileDifferentiator(String oldText, String newText, Map<Integer, Integer> strMap, Map<Integer, Integer> newstrMap) {
      // TODO Auto-generated method stub
      oldData.setText(oldText);
      for (Map.Entry<Integer, Integer> entry : strMap.entrySet()) {
         styleData(entry.getKey(), entry.getValue());
      }
      newData.setText(newText);
      for (Map.Entry<Integer, Integer> entry : newstrMap.entrySet()) {
         styleNewData(entry.getKey(), entry.getValue());
      }
   }

   private void styleData(int i, int j) {
      StyleRange style = new StyleRange();
      style.start = i;
      style.length = j;
      style.background = oldData.getDisplay().getSystemColor(SWT.COLOR_RED);
      style.foreground = oldData.getDisplay().getSystemColor(SWT.COLOR_WHITE);
      style.borderColor = oldData.getDisplay().getSystemColor(SWT.COLOR_RED);
      style.borderStyle = SWT.BORDER_SOLID;
      oldData.setStyleRange(style);
   }

   private void styleNewData(int i, int j) {
      StyleRange style = new StyleRange();
      style.start = i;
      style.length = j;
      style.background = newData.getDisplay().getSystemColor(SWT.COLOR_BLUE);
      style.foreground = newData.getDisplay().getSystemColor(SWT.COLOR_WHITE);
      style.borderColor = newData.getDisplay().getSystemColor(SWT.COLOR_BLUE);
      style.borderStyle = SWT.BORDER_SOLID;
      newData.setStyleRange(style);
   }

   private String getFileData(String oldFile, ChangeDistillerElement me) {
      // TODO Auto-generated method stub
      String lineNumber = me.getSourceRangeLine();
      String[] lineNumbers = lineNumber.split("~");
      String startValue = lineNumbers[0].strip();
      int start = Integer.parseInt(startValue);
      String endValue = lineNumbers[lineNumbers.length-1].strip();
      int end = Integer.parseInt(endValue);
      StringBuilder sb = new StringBuilder();
      StringBuilder sb1 = new StringBuilder();
      try {
         BufferedReader br = new BufferedReader(new FileReader(oldFile));
         String line = br.readLine();
         int i = 1;
         while (line != null) {
            if(i>=start && i<=end) {
               sb.append(i);
               sb.append("\t");
               sb.append(line);
               sb.append(System.lineSeparator());
            }
            i = i + 1 ;
            line = br.readLine();
         }

         if (sb.toString().contains("//")) {
            sb1.append(sb.toString().split("//")[0]);
         }
         else if (sb.toString().contains("/*")) {
            sb1.append(sb.toString().split("//")[0]);
         } else {
            sb1 = sb;
         }
         br.close();

      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      return sb1.toString();
   }

   public void setInput(Object data) {
      viewer.setInput(data);
   }
   //
   private void createProgElemColumns(final Composite parent, final TableViewer viewer) {
      String[] titles = {"Class", "Method", "Edit Operation", "Line" };
      int[] bounds = { 100, 100, 400, 100};

      TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
      //		col.setLabelProvider(new ColumnLabelProvider() {
      //			@Override
      //			public String getText(Object element) {
      //				ChangeDistillerElement p = (ChangeDistillerElement) element;
      //				return p.getPkgName();
      //			}
      //		});
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ChangeDistillerElement p = (ChangeDistillerElement) element;
            return p.getClassName();
         }
      });

      col = createTableViewerColumn(titles[1], bounds[1], 1);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ChangeDistillerElement p = (ChangeDistillerElement) element;
            return p.getMethodName();
         }
      });

      col = createTableViewerColumn(titles[2], bounds[2], 2);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ChangeDistillerElement p = (ChangeDistillerElement) element;
            return p.getEditOperation();
         }
      });

      //		col = createTableViewerColumn(titles[4], bounds[4], 4);
      //		col.setLabelProvider(new ColumnLabelProvider() {
      //			@Override
      //			public String getText(Object element) {
      //				ChangeDistillerElement p = (ChangeDistillerElement) element;
      //				return p.getEditLabel();
      //			}
      //		});
      //
      //		col = createTableViewerColumn(titles[5], bounds[5], 5);
      //		col.setLabelProvider(new ColumnLabelProvider() {
      //			@Override
      //			public String getText(Object element) {
      //				ChangeDistillerElement p = (ChangeDistillerElement) element;
      //				return String.valueOf(p.getChangedEntityType());
      //			}
      //		});
      //
      //		col = createTableViewerColumn(titles[6], bounds[6], 6);
      //		col.setLabelProvider(new ColumnLabelProvider() {
      //			@Override
      //			public String getText(Object element) {
      //				ChangeDistillerElement p = (ChangeDistillerElement) element;
      //				return p.getChangedEntityName();
      //			}
      //		});

      col = createTableViewerColumn(titles[3], bounds[3], 3);
      col.setLabelProvider(new ColumnLabelProvider() {
         @Override
         public String getText(Object element) {
            ChangeDistillerElement p = (ChangeDistillerElement) element;
            return p.getSourceRangeLine();
         }
      });
   }

   private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
      final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
      final TableColumn column = viewerColumn.getColumn();
      column.setText(title);
      column.setWidth(bound);
      column.setResizable(true);
      column.setMoveable(true);
      return viewerColumn;
   }

   public void clear() {
      ChangeDistillerProgramElement.INSTANCE.clearProgramElements();;
   }

   public void oldFileData(String str) {
      oldData.setText(str);
   }

   public void newFileData(String str) {
      newData.setText(str);
   }

   public void setText(String str) {
      this.styledText.setText(str);
   }

   public void appendText(String str) {
      this.styledText.append(str);
   }
}