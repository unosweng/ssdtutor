/**
 * @file UtilFile.java
 */
package util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.LinkOption;
import java.util.HashMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

/**
 * @since JavaSE-1.8
 */
public class UtilFile {
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
}
