
package handlers;

import java.util.HashSet;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import analysis.ExecuteAnalysis;
import visitor.FindMisuseInitializationVector;

public class FindMisuseInitializationVectorHandler {
   boolean clear = true;
   int detectionCount = 0;
   HashSet<String> treeList;
   HashSet<String> indList;

   public int getDetectionCount() {
      return detectionCount;
   }

   public void execute(EPartService epartService, boolean clear) {
      this.clear = clear;
      this.execute(epartService);
   }

   @Execute
   public void execute(EPartService epartService) {
      FindMisuseInitializationVector detector = new FindMisuseInitializationVector();
      ExecuteAnalysis ea = new ExecuteAnalysis(detector, 8);
      detectionCount = ea.go(epartService, this.clear);
      treeList = ea.getTreeList();
      indList = ea.getIndicatorList();
   }

}
