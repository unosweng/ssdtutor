package handlers;

import java.util.HashSet;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import analysis.ExecuteAnalysis;
import visitor.FindMisusePBEDetecter;

public class FindMisusePBEHandler {
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
      FindMisusePBEDetecter detector = new FindMisusePBEDetecter();
      ExecuteAnalysis ea = new ExecuteAnalysis(detector, 4);
      detectionCount = ea.go(epartService, this.clear);
      treeList = ea.getTreeList();
      indList = ea.getIndicatorList();
   }
}