package handlers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import input.IGlobalProperty;
import util.CsvSort;
import util.PrintHelper;
import util.UTFile;
import util.UtilAST;
import view.CryptoMisuseDetection;
import view.CryptoMisuseDetectionTree;

public class RunAllFindMisuseHandlers {

   @Execute
   public void execute(EPartService epartService) {
      Date start = new Date();
      int c1 = 0, c2 = 0, c3 = 0, c4 = 0, c5 = 0, c6=0, c7=0, c8=0;

      List<String> treeList = new ArrayList<>();
      List<String> indicatorList = new ArrayList<>();

      PrintHelper.printDebugMsgH("Run all Misuse Handlers\n");

      ClearViewHandler cvh = new ClearViewHandler();
      cvh.execute(epartService);
      PrintHelper.printDebugMsgH("Run all Misuse Handlers\n");

      //       GenerateFilePathOfTypeHandler gfp = new GenerateFilePathOfTypeHandler();
      //       gfp.execute();

      if (IGlobalProperty.PATTERN1_RUN) {
         FindMisuseECBHandler h1 = new FindMisuseECBHandler();
         h1.execute(epartService, false);
         treeList.addAll(h1.treeList);
         indicatorList.addAll(h1.indList);
         c1 = h1.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA1 ECB Count: " + c1);
         PrintHelper.printDebugMsgT("*** JCA1 ECB Indicator Count: " + h1.indList.size());
      }

      if (IGlobalProperty.PATTERN2_RUN) {
         FindMisuseHashFunctionHandler h2 = new FindMisuseHashFunctionHandler();
         h2.execute(epartService, false);
         treeList.addAll(h2.treeList);
         indicatorList.addAll(h2.indList);
         c2 = h2.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA2 HASH Count: " + c2);
         PrintHelper.printDebugMsgT("*** JCA2 HASH Indicator Count: " + h2.indList.size());
      }

      if (IGlobalProperty.PATTERN3_RUN) {
         FindMisuseConstSecretKeyHandler h3 = new FindMisuseConstSecretKeyHandler();
         h3.execute(epartService, false);
         treeList.addAll(h3.treeList);
         indicatorList.addAll(h3.indList);
         c3 = h3.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA3 SKS Count: " + c3);
         PrintHelper.printDebugMsgT("*** JCA3 SKS Indicator Count: " + h3.indList.size());
      }

      if (IGlobalProperty.PATTERN4_RUN) {
         FindMisusePBEHandler h4 = new FindMisusePBEHandler();
         h4.execute(epartService, false);
         treeList.addAll(h4.treeList);
         indicatorList.addAll(h4.indList);
         c4 = h4.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA4 PBE Count: " + c4);
         PrintHelper.printDebugMsgT("*** JCA4 PBE Indicator Count: " + h4.indList.size());
      }

      if (IGlobalProperty.PATTERN5_RUN) {
         FindMisuseSecureRandom h5 = new FindMisuseSecureRandom();
         h5.execute(epartService, false);
         treeList.addAll(h5.treeList);
         indicatorList.addAll(h5.indList);
         c5 = h5.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA5 SR Count: " + c5);
         PrintHelper.printDebugMsgT("*** JCA5 SR Indicator Count: " + h5.indList.size());
      }

      if (IGlobalProperty.PATTERN6_RUN) {
         FindMisuseKeyGeneratorHandler h6 = new FindMisuseKeyGeneratorHandler();
         h6.execute(epartService, false);
         treeList.addAll(h6.treeList);
         indicatorList.addAll(h6.indList);
         c6 = h6.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA5 SR Count: " + c6);
         PrintHelper.printDebugMsgT("*** JCA5 SR Indicator Count: " + h6.indList.size());
      }
      if (IGlobalProperty.PATTERN7_RUN) {
         FindMisuseSecretKeyFactoryHandler h7 = new FindMisuseSecretKeyFactoryHandler();
         h7.execute(epartService, false);
         treeList.addAll(h7.treeList);
         indicatorList.addAll(h7.indList);
         c7 = h7.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA5 SR Count: " + c7);
         PrintHelper.printDebugMsgT("*** JCA5 SR Indicator Count: " + h7.indList.size());
         FindMisuseInitializationVectorHandler h8 = new FindMisuseInitializationVectorHandler();
         h8.execute(epartService, false);
         treeList.addAll(h8.treeList);
         indicatorList.addAll(h8.indList);
         c8 = h8.detectionCount;
         PrintHelper.printDebugMsgH("*** JCA5 SR Count: " + c8);
         PrintHelper.printDebugMsgT("*** JCA5 SR Indicator Count: " + h8.indList.size());
      }
      if (epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID) != null) {
         Object findPartObjTree = epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID).getObject();
         if (findPartObjTree instanceof CryptoMisuseDetectionTree) {
            CryptoMisuseDetectionTree v = (CryptoMisuseDetectionTree) findPartObjTree;
            v.getTreeViewer().expandAll();
         }
      }
      if (epartService.findPart(CryptoMisuseDetection.VIEW_ID) != null) {
         Object findPartObj = epartService.findPart(CryptoMisuseDetection.VIEW_ID).getObject();
         if (findPartObj instanceof CryptoMisuseDetection) {
            CryptoMisuseDetection v = (CryptoMisuseDetection) findPartObj;
            v.getTreeViewer().expandAll();
         }
      }

      PrintHelper.printDebugMsg("Run all Misuse Handlers Done.");

      /********************************************************************
       * Tree List
       */

      String jcaCounts = c1 + IGlobalProperty.COLUMN_SEPARATOR //
            + c2 + IGlobalProperty.COLUMN_SEPARATOR //
            + c3 + IGlobalProperty.COLUMN_SEPARATOR //
            + c4 + IGlobalProperty.COLUMN_SEPARATOR //
            + c5;

      PrintHelper.printDebugMsgH("JCA Counts: " + jcaCounts);

      String csvFileName = UtilAST.getWorkspaceRootSourcePath() //
            + (UTFile.isMacPlatform() ? IGlobalProperty.JCA_COUNT_FILENAME_MAC : IGlobalProperty.JCA_COUNT_FILENAME);
      PrintHelper.printDebugMsg("Writing output to: " + csvFileName);

      List<List<String>> sortedTreeList = CsvSort.sortTreeList(treeList);
      String header = "Pattern, Group, Indicator Path, Ind-Flag, Ind-Line, Indicator Name, Indicator Values, Root Path, Root-Flag, Root-Line, Root Name, Root Values";

      try {
         UTFile.truncate(csvFileName);
         UTFile.writeFile(csvFileName, header, sortedTreeList);
      } catch (IOException e) {
         e.printStackTrace();
      }

      /********************************************************************
       * Indicator List
       */
      String csvIndFileName = UtilAST.getWorkspaceRootSourcePath() //
            + (UTFile.isMacPlatform() ? IGlobalProperty.JCA_INDICATOR_COUNT_MAC : IGlobalProperty.JCA_INDICATOR_COUNT);
      PrintHelper.printDebugMsg("Writing Indicator output to: " + csvIndFileName);

      List<List<String>> sortedIndList = CsvSort.sortIndicatorList(indicatorList);
      String indHeader = "Pattern, Group, Path, IndName, Args, Line";
      try {
         UTFile.truncate(csvIndFileName);
         UTFile.writeFile(csvIndFileName, indHeader, sortedIndList);
      } catch (IOException e) {
         e.printStackTrace();
      }
      /********************************************************************/

      Date end = new Date();
      long millis = (end.getTime() - start.getTime());
      String duration = DurationFormatUtils.formatDuration(millis, "HH:mm:ss.S");
      PrintHelper.printDebugMsgT("Running All Done in: " + duration);
   }
}
