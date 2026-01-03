 
package handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import view.MisuseViewer;

public class MisuseViewerHandler {
   String URI = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
   String xmlURL = String.format("%s/misuse.xml", URI);
   
   @Execute
   public void execute(EPartService epartService) {
      File misuseF = new File(xmlURL);
      StringBuilder sb = new StringBuilder();
      sb.append("<xmp>");
      sb.append("\n");
      if(misuseF.exists()) {
         try {
            Scanner groups = new Scanner(new File(xmlURL));
            while (groups.hasNextLine()) {
               sb.append(groups.nextLine());
               sb.append("\n");
            }
         } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }
         
      }
      sb.append("</xmp>");
      MPart findPart = epartService.findPart(MisuseViewer.VIEW_ID);
      Object findPartObj = findPart.getObject();
      if (findPartObj instanceof MisuseViewer) {
         MisuseViewer v = (MisuseViewer) findPartObj;
         v.setText(sb.toString());
      }
   }
		
}