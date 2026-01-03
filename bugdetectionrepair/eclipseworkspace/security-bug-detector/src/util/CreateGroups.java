package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import input.IGlobalProperty;

public class CreateGroups {
   private String clocLocation = "/opt/homebrew/bin/cloc";
   private int LOC = 900000;
   private int SRC_FILE = 50;

   public CreateGroups() {
      // TODO Auto-generated constructor stub
   }

   public StringBuilder writeFileInformation(IProject[] projects) {
      StringBuilder sb = new StringBuilder();
      try {
         int i = 1;
         int lineOfCode = 0;
         int src_val = 0;
         int total = 0;
         int projectCount = 0;
         for (IProject project : projects) {
            if (!project.isOpen() || !project.isNatureEnabled(IGlobalProperty.JAVANATURE)) { // Check if we have a Java project.
               continue;
            }
            List<String> cryptoPaths = UtilAST.getCryptoFilePaths(project);
            System.out.println(project.getName());
//            if(lineOfCode<=0 || src_val<=0) {
//               String group = "Group" + Integer.toString(i);
//               sb.append(group);
//               sb.append("\n");
//               i++;
//               lineOfCode = LOC;
//               src_val = SRC_FILE;
//            }
            List<String> parameters = new ArrayList<String>();
            parameters.add(clocLocation);
            parameters.add(project.getLocation().toFile().getAbsolutePath());
//            
//            sb.append(project.getName());
            total = total + cryptoPaths.size();
            System.out.println(total);
            if(total > SRC_FILE || lineOfCode<=0 ) {
               String group = "Group" + Integer.toString(i);
               sb.append("\n");
               sb.append(group);
               sb.append("\n");
               sb.append(project.getName());
               sb.append(",");
               i++;
               lineOfCode = LOC;
               src_val = SRC_FILE;
               total = cryptoPaths.size();
            } else {
               sb.append(project.getName());
               sb.append(",");
            }
            src_val = src_val - cryptoPaths.size();
            try {
               Process p = new ProcessBuilder(parameters).start();
               p.waitFor();
               BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
               String line;
               while ((line = stdout.readLine()) != null) {
                  if(line.contains("Java")) {
                     String[] arr = line.split(" ");
                     lineOfCode = lineOfCode - Integer.valueOf(arr[arr.length-1]);
                     break;
                  }
               }
               p.waitFor();
            } catch (IOException | InterruptedException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }
//            if(lineOfCode<=0 || total > SRC_FILE) {
//               sb.append("\n");
//            } else {
//               sb.append(",");
//            }
            projectCount = projectCount + 1;
            project.clearCachedDynamicReferences();
            project.clearHistory(null);
//            project.close(null);
         }
         String count = Integer.toString(projectCount) + "\n";
         sb.insert(0, count);
      }catch (CoreException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      return sb;
   }

}
