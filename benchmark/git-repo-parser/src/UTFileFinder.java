/**
 */


import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class UTFileFinder extends SimpleFileVisitor<Path> {

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
}
