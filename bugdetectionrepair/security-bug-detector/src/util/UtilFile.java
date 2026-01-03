/**
 * @file UtilFile.java
 */
package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import input.IGlobalProperty;
import model.MethodElement;
import analysis.ProgramTreeAnalyzer;

/**
 * @since JavaSE-1.8
 */
public class UtilFile {
   private static StringBuilder sb = new StringBuilder();
   static StringBuilder fileName = new StringBuilder();
   private static HashMap<String, String> tableList = new HashMap<>();
   private static HashMap<String, Integer> conflitClass = new HashMap<>();
   public static String readEntireFile(String filename) throws IOException {
      FileReader in = new FileReader(filename);
      StringBuilder contents = new StringBuilder();
      char[] buffer = new char[4096];
      int read = 0;
      do {
         contents.append(buffer, 0, read);
         read = in.read(buffer);
      } while (read >= 0);
      in.close();
      return contents.toString();
   }

   public static void openEditor(File file) {
      if (file.exists() && file.isFile()) {
         IFileStore fileStore = EFS.getLocalFileSystem().getStore(file.toURI());
         IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
         try {
            IDE.openEditorOnFileStore(page, fileStore);
         } catch (PartInitException e) {
            e.printStackTrace();
         }
      }
   }

   // @Test
   // public void openEditorTest() {
   // openEditor(new File("/lecture-8710-modsw"), 0);
   // openEditor(new File("/Users/mksong/Documents/git-repository/lecture-8710-modsw"), 0);
   // }

   public static void openEditor(File file, int line) {
      IWorkspace workspace = ResourcesPlugin.getWorkspace();
      IPath location = null; 
      java.nio.file.Path realPath = null;

      try {
         realPath = file.toPath().toRealPath(new LinkOption[0]);
         System.out.println("[DBG] simb path: " + file);
         System.out.println("[DBG] real path: " + realPath);
      } catch (IOException e2) {
         e2.printStackTrace();
      }
      location = Path.fromOSString(realPath.toFile().getAbsoluteFile().getAbsolutePath());
      IFile ifile = workspace.getRoot().getFileForLocation(location);

      IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
      HashMap<String, Object> map = new HashMap<String, Object>();
      map.put(IMarker.LINE_NUMBER, line);
      IMarker marker = null;

      try {
         marker = ifile.createMarker(IMarker.TEXT);
         marker.setAttributes(map);
         try {
            IDE.openEditor(page, marker);
         } catch (PartInitException e) {
            e.printStackTrace();
         }
      } catch (CoreException e1) {
         e1.printStackTrace();
      } finally {
         try {
            if (marker != null)
               marker.delete();
         } catch (CoreException e) {
            e.printStackTrace();
         }
      }
   }

   public static void getDirectoryPath(IProject project) {
      // TODO Auto-generated method stub
      int pathLocation = project.getLocation().toFile().getParent().lastIndexOf("/" );
      String dirParent = project.getLocation().toFile().getParent();
      String dirParentName = dirParent.substring(pathLocation + 1, dirParent.length());

      File dir = new File(dirParent);
      List<String> files = Arrays.asList(dir.list());
      files.sort(null);

      int skipIndex = getFileIndex(files, project.getName());
      String dirIndex = Integer.toString(files.indexOf(project.getName()) + 1 - skipIndex);
      String dirName = dirParentName + IGlobalProperty.NAME_SEPARATOR + dirIndex;
      dirName = dirName + IGlobalProperty.NAME_SEPARATOR + project.getName();

      String dirNameRep = dirName + IGlobalProperty.NAME_SEPARATOR + IGlobalProperty.REPAIR;
      String dirNameOrg = dirName + IGlobalProperty.NAME_SEPARATOR + IGlobalProperty.ORIGINAL;

      ProgramTreeAnalyzer.setOriginalPath(dirParent + IGlobalProperty.PATH_SEPARATOR + dirNameOrg);
      ProgramTreeAnalyzer.setRepairPath(dirParent + IGlobalProperty.PATH_SEPARATOR + dirNameRep);
   }

   private static int getFileIndex(List<String> files, String name) {
      int i = 0;
      for (String iRuleFile : files) {
         if(iRuleFile.startsWith(".")){
            i++;
         }
         else if(iRuleFile.contains("original")||iRuleFile.contains("repair")) {
            i++;
         }
         else if(iRuleFile.equals(name)){
            return i;
         }
      }
      return 0;
   }

   public static String getProjectFile(String fileName) {
      String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<projectDescription>\n"
            + String.format("\t<name>%s</name>\n", fileName)
            + "\t<comment></comment>\n"
            + "\t<projects></projects>\n"
            + "\t<buildSpec>\n"
            + "\t\t<buildCommand>\n"
            + "\t\t\t<name>org.eclipse.jdt.core.javabuilder</name>\n"
            + "\t\t\t<arguments>\n"
            + "\t\t\t</arguments>\n"
            + "\t\t</buildCommand>\n"
            + "\t</buildSpec>\n"
            + "\t<natures>\n"
            + "\t\t<nature>org.eclipse.jdt.core.javanature</nature>\n"
            + " \t</natures>\n"
            + "</projectDescription>\n";
      return text;
   }
   //	private static String createFileDirectory(String fileName, String absPath) {
   //		String dirPath = absPath + IGlobalProperty.PATH_SEPARATOR + fileName;
   //		File dir = new File(dirPath);
   //		if (!dir.exists()){
   //			dir.mkdirs();
   //		}
   //		return dirPath;
   //	}
   //	
   //	public static void copyFiles(String newPath, ICompilationUnit iUnit) {
   //		File dir = new File(newPath);
   //		File dirPath = new File(dir.getParent());
   //		if (!dirPath.exists()){
   //			dirPath.mkdirs();
   //		}
   //
   //		File file = new File(newPath);
   //		if (!file.exists()){
   //			try {
   //				Files.copy(iUnit.getResource().getLocation().toFile().toPath(), dir.toPath());
   //			} catch (IOException e) {
   //				// TODO Auto-generated catch block
   //				e.printStackTrace();
   //			}
   //		}
   ////		FileUtils.copyDirectory(dir, dir);
   //	}
   //	
   public static boolean checkPatternConflict(MethodElement me) {
      sb.append(me.getLineNumber());
      sb.append(me.getFilePath());
      String matchValue = tableList.get(sb.toString());
      boolean hasPatternConflict = false;
      if (matchValue != null && !(matchValue.equals(me.getIndicatorClassName()))){
         if(conflitClass.get(matchValue) == null) {
            conflitClass.put(matchValue, conflitClass.size() + 1);
         }
         RepairHelper.setConflictId(conflitClass);
         hasPatternConflict = true;
      }
      else {
         tableList.put(sb.toString(), me.getIndicatorClassName());
      }
      sb = new StringBuilder();
      return hasPatternConflict;
   }
}
