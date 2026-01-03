package util;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import model.MethodElement;
import model.ProgElementModelProvider;
import model.ProgramElement;

public class TableViewerHelper {
   static List<Object> data = new ArrayList<Object>();
   private static String menu;
   static StringBuilder fileName = new StringBuilder();

   public static List<Object> TableViewerData(MethodElement m) {
      data.clear();
      CheckMethodChild(m);
      return data;
   }

   private static void CheckMethodChild(MethodElement m) {
      // TODO Auto-generated method stub
      for (ProgramElement child:m.list()) {
         MethodElement m1 = (MethodElement) child;
         String detectionLine;
         boolean hasPatternConflict = UtilFile.checkPatternConflict(m1);	
         RepairHelper.setpatternConflict(hasPatternConflict);
         detectionLine = getDetectionLine(m1);
         boolean hasChanged = checkChangeInLineNumber(m1, detectionLine);
         if (hasChanged){
            m1.setLineNumber(m1.getLineNumber() + 1);
            detectionLine = getDetectionLine(m1);
         }
         m1.setDetectionLine(detectionLine);
         data.add(m1);
         if (m1.hasChildren()) {
            CheckMethodChild(m1);
         }
      }

   }

   private static boolean checkChangeInLineNumber(MethodElement me, String detection) {
      if (detection.length()==0) {
         return true;
      }
      if (me.getDetectionLine().contains("=")) {
         if (!me.getDetectionLine().split("=")[0].equals(detection.split("=")[0])) {
            return true;
         }
      }
      //      else if(!me.getDetectionLine().startsWith(detection.split(" ")[0])) {
      //         return true;
      //      }
      return false;
   }


   public static String getDetectionLine(MethodElement m1) {
      // TODO Auto-generated method stub
      IJavaElement javaElement = m1.getJavaElement();
      int lineNumber = m1.getLineNumber();
      String detection = null;
      try {
         IEditorPart editorPart = JavaUI.openInEditor(javaElement, true, true);
         if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
            return null;
         }
         ITextEditor editor = (ITextEditor) editorPart;
         IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
         if (document != null) {
            IRegion lineInfo = null;
            lineInfo = document.getLineInformation(lineNumber - 1);
            detection = document.get(lineInfo.getOffset(), lineInfo.getLength());
         }
      } catch (Exception e) {
         e.printStackTrace();
      }
      return detection.strip();
   }

   public static void createContextMenu(TableViewer view, MethodElement me) {
      Menu contextMenu = new Menu(view.getTable());
      view.getTable().setMenu(contextMenu);
      createMenuItems(view, contextMenu, me);
   }

   private static void createMenuItems(TableViewer view, Menu parent, MethodElement me) {
      // NOTE: order of declaration determines order of appearance in menu
      final MenuItem menuItemClear = new MenuItem(parent, SWT.PUSH);
      menuItemClear.setText("Clear");
      menuItemClear.addSelectionListener(new SelectionAdapter() {
         @Override
         public void widgetSelected(SelectionEvent e) {
            clearTableView(view);
         }
      });

      new MenuItem(parent, SWT.SEPARATOR);

      final Menu repairMenu = new Menu(parent);
      ParserSAX parser = ParserSAX.getRepairMenu(me.getIndicatorClassName());
      List<Map<String, String>> menuList = parser.getRepairMenu();
      for (int i=0; i<menuList.size(); i++) {
         Map<String, String> menuItem = menuList.get(i);
         if((menuItem.get("argPos")!=null) && !(menuItem.get("argPos").equals(String.valueOf(me.getIndicatorPos())))) {
            continue;
         }
         else {
            menu = menuItem.get("repairMenu");
            int caseId = Integer.valueOf(menuItem.get("case"));
            final MenuItem repair1 = new MenuItem(repairMenu, SWT.PUSH);
            repair1.setText(menu);
            repair1.addSelectionListener(new SelectionAdapter() {
               @Override
               public void widgetSelected(SelectionEvent e) {
                  TableItem[] selection = view.getTable().getSelection();
                  if (selection != null && selection.length > 0) {
                     Object data = selection[0].getData();
                     if (data instanceof MethodElement) {
                        long start = System.currentTimeMillis();
                        MethodElement me = (MethodElement) data;
                        TreeViewerHelper.openInEditor(me);
                        RepairHelper repairHelper = new RepairHelper(me);
                        System.out.println(caseId);
                        try {
                           repairHelper.repair(caseId);
                        } catch (Exception e1) {
                           e1.printStackTrace();
                        }
                        long end = System.currentTimeMillis();
                        System.out.println(end-start);
                     }
                  }

               }
            });
         }

      }

      final MenuItem repair = new MenuItem(parent, SWT.CASCADE);
      repair.setText("Repair");
      repair.setMenu(repairMenu);

   }

   public static void clearTableView(TableViewer view) {
      ProgElementModelProvider.INSTANCE.clearProgramElements();
      view.getTable().clearAll();
   }

   public static String getClassNameFromJavaFile(String javafileName) {
      int indexOfDot = javafileName.lastIndexOf('.');
      if (indexOfDot < 0) {
         return null;
      }
      return javafileName.substring(0, indexOfDot);
   }
}
