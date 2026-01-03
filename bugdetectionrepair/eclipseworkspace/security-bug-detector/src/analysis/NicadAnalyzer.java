package analysis;

import java.util.Date;
import java.util.HashSet;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import input.IGlobalProperty;
import util.PrintHelper;
import view.CryptoMisuseDetectionTree;
import visitor.DetectionASTVisitor;

public class NicadAnalyzer {
   private DetectionASTVisitor detector;
   private int pattern;
   private HashSet<String> treeList = new HashSet<>();
   private HashSet<String> indicatorList = new HashSet<>();

   public HashSet<String> getTreeList() {
      return treeList;
   }

   public HashSet<String> getIndicatorList() {
      return indicatorList;
   }

   public NicadAnalyzer(DetectionASTVisitor detector, int pattern) {
      this.detector = detector;
      this.pattern = pattern;
   }

   public int go(EPartService epartService, boolean clear) {
      int count = 0;
      String p = "(P" + this.pattern + ") ";
      PrintHelper.printDebugMsgH(p + "Detect " + this.detector.message);
      Date start = new Date();
      if (epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID) != null) {
         Object findPartObjTree = epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID).getObject();
         if (findPartObjTree instanceof CryptoMisuseDetectionTree) {
            CryptoMisuseDetectionTree v = (CryptoMisuseDetectionTree) findPartObjTree;
            if (clear) {
               v.clearTreeViewer();
            }

            v.getTreeList().clear();
            v.getIndicatorList().clear();
            NicadTreeAnalyzer analyzer = new NicadTreeAnalyzer(v, this.detector);
            analyzer.analyze();

            count = v.getTreeList().size();
            if (count > 0) {
               PrintHelper.printDebugMsgH("Tree Lists: " + count);

               for (String item : v.getTreeList()) {
                  item = pattern + IGlobalProperty.COLUMN_SEPARATOR + item;
                  treeList.add(item);
                  PrintHelper.printDebugMsg(item);
               }
            }

            // indicator list
            int ic = v.getIndicatorList().size();
            if (ic > 0) {
               PrintHelper.printDebugMsgH("Indicator Lists: " + ic);
               for (String item : v.getIndicatorList()) {
                  item = pattern + IGlobalProperty.COLUMN_SEPARATOR + item;
                  indicatorList.add(item);
                  PrintHelper.printDebugMsg(item);
               }
            }

            if (clear) {
               v.getTreeViewer().expandAll();
            }
         }
      }
      Date end = new Date();
      long millis = (end.getTime() - start.getTime());
      String duration = DurationFormatUtils.formatDuration(millis, "HH:mm:ss.S");
      PrintHelper.printDebugMsgT(p + "Done in: " + duration);
      return count;
   }
}
