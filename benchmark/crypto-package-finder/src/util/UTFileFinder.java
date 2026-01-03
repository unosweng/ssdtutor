/**
 */
package util;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import input.IGlobalProperty;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class UTFileFinder extends SimpleFileVisitor<Path> implements IGlobalProperty {

   private final PathMatcher matcher;
   private int numMatches = 0;
   private List<Path> list;

   UTFileFinder(String pattern, List<Path> list) {
      matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
      this.list = list;
   }

   void find(Path file) {
      Path name = file.getFileName();
      if (name != null && matcher.matches(name)) {
         numMatches++;
         list.add(file);
      }
   }

   void done() {
      System.out.println("Matched: " + numMatches);
   }

   @Override
   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      find(file);
      return FileVisitResult.CONTINUE;
   }

   @Override
   public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
      find(dir);
      return FileVisitResult.CONTINUE;
   }

   @Override
   public FileVisitResult visitFileFailed(Path file, IOException exc) {
      System.err.println(exc);
      return FileVisitResult.CONTINUE;
   }

   public static void main(String[] args) {
      List<Path> list = new ArrayList<Path>();
      
      Path startingDir = Paths.get(TARGET_GIT_DIR);
      UTFileFinder finder = new UTFileFinder("*.java", list);
      try {
         Files.walkFileTree(startingDir, finder);
      } catch (IOException e) {
         e.printStackTrace();
      }
      finder.done();
   }
}
