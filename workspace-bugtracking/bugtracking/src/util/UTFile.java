/**
 */
package util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

/**
 * @author
 * @date
 * @since Java SE 8
 */
public class UTFile {
   private static final int BUF_SIZE = 8192;
   public static final String COLUMN_SEPARATOR = ",";
   public static final String UTF_8 = "UTF-8";
   public static final String DEFAULT_FILE_ENCODING = "ISO-88659-1";

   public static boolean isMacPlatform() {
      return System.getProperty("os.name").toLowerCase().contains("mac");
   }

   /**
    * Copy to.
    * 
    * @param is
    *           the is
    * @param os
    *           the os
    * @return the long
    */
   public static long copyTo(InputStream is, OutputStream os) {
      byte[] buf = new byte[BUF_SIZE];
      long tot = 0;
      int len = 0;
      try {
         while (-1 != (len = is.read(buf))) {
            os.write(buf, 0, len);
            tot += len;
         }
      } catch (IOException ioe) {
         throw new RuntimeException("error - ", ioe);
      }
      return tot;
   }

   public static void deleteFile(String f) {
      File file = new File(f);
      if (file.exists()) {
         file.delete();
      }
   }

   /**
    * Read2lines from file.
    * 
    * @param fileName
    *           the file name
    * @param list1
    *           the list1
    * @param list2
    *           the list2
    */
   public void read2linesFromFile(String fileName, List<String> list1, List<String> list2) {

      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line1 = null, line2 = null;

         while ((line1 = in.readLine()) != null) {
            line1 = line1.trim();

            if (line1.isEmpty()) {
               continue;
            }
            else {
               line2 = in.readLine();
               line2 = line2.trim();
            }
            list1.add(line1);
            list2.add(line2);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }

   /**
    * Read file in list buffer.
    * 
    * @param fileName
    *           the file name
    * @param buffer
    *           the buffer
    */
   public void readFileInListBuffer(String fileName, List<String> buffer) {
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line = null;
         boolean isComment = false;

         while ((line = in.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("//")) {
               continue;
            }
            if (isComment) {
               if (line.endsWith("*/"))
                  isComment = false;
               continue;
            }
            if (line.startsWith("/*")) {
               isComment = true;
            }
            else if (line.isEmpty()) {
               buffer.add("");
            }
            else {
               buffer.add(line);
            }
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }

   public static void readFile(String fileName, List<String> buffer) {
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line = null;
         while ((line = in.readLine()) != null) {
            buffer.add(line.trim());
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
   }

   /**
    * Read reverse by token.
    * 
    * @param file
    *           the file
    * @param token
    *           the token
    * @param bgn
    *           the bgn
    * @param end
    *           the end
    * @return the string
    */
   public String readReverseByToken(File file, String token, String bgn, String end) {
      BufferedReader in = null;
      String result = null;
      try {
         in = new BufferedReader(new InputStreamReader(new ReverseLineInputStream(file)));

         while (true) {
            String line = in.readLine();
            if (line == null) {
               break;
            }
            else if (line.contains("Command-line arguments") && line.contains(token)) {

               // DBG__________________________________________------------------------
               // [DBG] FOUND: Command-line arguments: -product org.eclipse.sdk.ide -data
               // /Users/mksong/workspaceCHIME/../runtime-ChimeUI
               // -dev file:/Users/mksong/workspaceCHIME/.metadata/.plugins/org.eclipse.pde.core/ChimeUI/dev.properties
               // -os macosx -ws cocoa -arch x86_64 -consoleLog
               // DBG__________________________________________------------------------
               // [DBG] Users/mksong/workspaceCHIME/
               // DBG__________________________________________------------------------

               int idx_token = line.indexOf(token);
               int idx_bgn_path = line.indexOf(bgn, idx_token);
               int idx_end_path = line.indexOf(end, idx_bgn_path);

               if (System.getProperty("os.name").startsWith("Windows")) {
                  result = line.substring(idx_bgn_path + 1, idx_end_path);
               }
               result = line.substring(idx_bgn_path, idx_end_path);

               break;
            }
         }

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null)
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
      }
      return result;
   }

   class ReverseLineInputStream extends InputStream {
      RandomAccessFile in;
      long currentLineStart = -1;
      long currentLineEnd = -1;
      long currentPos = -1;
      long lastPosInFile = -1;

      /**
       * Instantiates a new reverse line input stream.
       * 
       * @param file
       *           the file
       * @throws FileNotFoundException
       *            the file not found exception
       */
      public ReverseLineInputStream(File file) throws FileNotFoundException {
         in = new RandomAccessFile(file, "r");
         currentLineStart = file.length();
         currentLineEnd = file.length();
         lastPosInFile = file.length() - 1;
         currentPos = currentLineEnd;
      }

      /**
       * Find prev line.
       * 
       * @throws IOException
       *            Signals that an I/O exception has occurred.
       */
      public void findPrevLine() throws IOException {

         currentLineEnd = currentLineStart;

         if (currentLineEnd == 0) {
            currentLineEnd = -1;
            currentLineStart = -1;
            currentPos = -1;
            return;
         }

         long filePointer = currentLineStart - 1;

         while (true) {
            filePointer--;

            if (filePointer < 0) {
               break;
            }

            in.seek(filePointer);
            int readByte = in.readByte();

            if (readByte == 0xA && filePointer != lastPosInFile) {
               break;
            }
         }
         currentLineStart = filePointer + 1;
         currentPos = currentLineStart;
      }

      /*
       * (non-Javadoc)
       * 
       * @see java.io.InputStream#read()
       */
      @Override
      public int read() throws IOException {

         if (currentPos < currentLineEnd) {
            in.seek(currentPos++);
            int readByte = in.readByte();
            return readByte;

         }
         else if (currentPos < 0) {
            return -1;
         }
         else {
            findPrevLine();
            return read();
         }
      }
   }

   /**
    * Read file.
    * 
    * @param fileName
    *           the file name
    * @return the string
    */
   public static String readFile(String fileName) {
      BufferedReader in = null;
      StringBuilder sb = new StringBuilder();
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line;
         while ((line = in.readLine()) != null) {
            sb.append(line);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return sb.toString();
   }

   /**
    * Read file.
    * 
    * @param fileName
    *           the file name
    * @return the string
    */
   public static String readFileWithNewLine(String fileName) {
      BufferedReader in = null;
      StringBuilder sb = new StringBuilder();
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line;
         while ((line = in.readLine()) != null) {
            sb.append(line);
            sb.append(System.getProperty("line.separator"));
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return sb.toString();
   }

   /**
    * Read file to list.
    * 
    * @param fileName
    *           the file name
    * @return the list
    */
   public static List<String> readFileToList(String fileName) {
      List<String> list = new ArrayList<String>();
      BufferedReader in = null;
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line;
         while ((line = in.readLine()) != null) {
            list.add(line);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return list;
   }

   /**
    * Read entire file.
    * 
    * @param filename
    *           the filename
    * @return the string
    * @throws IOException
    *            Signals that an I/O exception has occurred.
    */
   public static String readEntireFile(String filename) throws IOException {
      FileReader in = new FileReader(filename);
      StringBuilder contents = new StringBuilder();
      char[] buffer = new char[4096];
      int read = 0;
      do {
         contents.append(buffer, 0, read);
         read = in.read(buffer);
      }
      while (read >= 0);
      in.close();
      return contents.toString();
   }

   /**
    * Read file without space.
    * 
    * @param fileName
    *           the file name
    * @return the string
    */
   public static String readFileWithoutSpace(String fileName) {
      BufferedReader in = null;
      String line;
      StringBuilder buf = new StringBuilder();
      try {
         in = new BufferedReader(new FileReader(fileName));
         while ((line = in.readLine()) != null) {
            if (line.trim().startsWith("#")) {
               continue;
            }
            buf.append(line.trim());
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return buf.toString();
   }

   public static void createDir(String dirName) {
      File f = new File(dirName);
      if (f.mkdirs()) {
         System.out.println("[DBG] DIR CREATED: " + f.getAbsolutePath());
      }
   }

   /**
    * Gets the contents.
    * 
    * @param fileName
    *           the file name
    * @return the contents
    */
   public static String getContents(String fileName) {
      String L = System.getProperty("line.separator");
      BufferedReader in = null;
      StringBuilder buf = new StringBuilder();
      try {
         in = new BufferedReader(new FileReader(fileName));
         String line;
         while ((line = in.readLine()) != null) {
            buf.append(line + L);
         }
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (in != null) {
            try {
               in.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return buf.toString();
   }

   /**
    * Gets the dir name.
    * 
    * @param fileName
    *           the file name
    * @return the dir name
    */
   public String getDirName(String fileName) {
      String S = System.getProperty("file.separator");
      int idx = fileName.lastIndexOf(S);
      if (idx == -1) {
         idx = fileName.lastIndexOf("/");
         if (idx == -1) {
            idx = fileName.lastIndexOf("\\");
         }
      }
      return fileName.substring(0, idx);
   }

   /**
    * Gets the attributes.
    * 
    * @param pathStr
    *           the path str
    * @return the attributes
    * @throws IOException
    *            Signals that an I/O exception has occurred.
    */
   public static long getLastModifiedTime(String pathStr) throws IOException {
      Path p = Paths.get(pathStr);
      BasicFileAttributes view = Files.getFileAttributeView(p, BasicFileAttributeView.class).readAttributes();
      System.out.println(view.creationTime() + ", " + view.lastModifiedTime());
      return view.lastModifiedTime().toMillis();
   }

   public static String getRuntimeDirPath() {
      String keyRTimeDir = System.getProperty("osgi.instance.area");
      String pathRTimeDir = null;
      pathRTimeDir = keyRTimeDir.split("file:/")[1];
      if (System.getProperty("os.name").toLowerCase().contains("mac")) {
         pathRTimeDir = keyRTimeDir.split(":")[1];
      }
      return pathRTimeDir;
   }

   /**
    * Gets the short file name.
    * 
    * @param fileName
    *           the file name
    * @return the short file name
    */
   public static String getShortFileName(String fileName) {
      String S = System.getProperty("file.separator");
      int idx = fileName.lastIndexOf(S);
      if (idx == -1) {
         idx = fileName.lastIndexOf("/");
         if (idx == -1) {
            idx = fileName.lastIndexOf("\\");
         }
      }
      return fileName.substring(idx + 1);
   }

   /**
    * Gets the string from input stream.
    * 
    * @param is
    *           the is
    * @return the string from input stream
    */
   public static String getStringFromInputStream(InputStream is) {
      BufferedReader br = null;
      StringBuilder sb = new StringBuilder();

      String line;
      try {
         br = new BufferedReader(new InputStreamReader(is, DEFAULT_FILE_ENCODING));
         // char[] cbuf = new char[2];
         // br.read(cbuf, 0, 2); // avoid special characters like "??"
         while ((line = br.readLine()) != null) {
            sb.append(line + '\n');
         }

      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (br != null) {
            try {
               br.close();
            } catch (IOException e) {
               e.printStackTrace();
            }
         }
      }
      return sb.toString();
   }

   public static List<Path> getFileList(String dir, String filter) {
      File[] javaFiles = new File(dir).listFiles(new FilenameFilter() {
         @Override
         public boolean accept(File dir, String name) {
            return name.toLowerCase().endsWith(filter);
         }
      });
      List<Path> listPath = new ArrayList<>();
      for (int i = 0; i < javaFiles.length; i++) {
         listPath.add(javaFiles[i].toPath());
      }
      return listPath;
   }

   /**
    * @ pattern "*.java"
    */
   public static List<Path> getFileListRecursive(String dir, String pattern) {
      List<Path> list = new ArrayList<Path>();
      walkDir(dir, list, pattern);
      return list;
   }

   static void walkDir(String dir, List<Path> list, String pattern) {
      Path startingDir = Paths.get(dir);
      UTFileFinder walkFiles = new UTFileFinder(pattern, list);
      try {
         Files.walkFileTree(startingDir, walkFiles);
      } catch (Exception e) {
         e.printStackTrace();
      }
   }

   public static void getPathList(String path_DEFAULT, String path_SELECTED, String path_SAMPLE, List<Path> listMerged, String[] fileArray) {
      List<Path> javaFilesDirDEFAULT = getFileList(path_DEFAULT, ".java");
      List<Path> javaFilesDirSELECTED = getFileList(path_SELECTED, ".java");
      List<Path> javaFilesDirSAMPLE = getFileList(path_SAMPLE, ".java");

      for (int i = 0; i < fileArray.length; i++) {
         String iFile = fileArray[i];
         Path iPathDefault = new File(path_DEFAULT + iFile).toPath();
         Path iPathSelected = new File(path_SELECTED + iFile).toPath();
         Path iPathSample = new File(path_SAMPLE + iFile).toPath();

         javaFilesDirDEFAULT.remove(iPathDefault);
         javaFilesDirSELECTED.remove(iPathSelected);
         javaFilesDirSAMPLE.remove(iPathSample);

      }

      listMerged.addAll(javaFilesDirDEFAULT);
      listMerged.addAll(javaFilesDirSELECTED);
      listMerged.addAll(javaFilesDirSAMPLE);
   }

   public static void makeDir(String file) {
      Path path = Paths.get(file);
      if (Files.exists(path)) {
         return;
      }
      try {
         Files.createDirectories(path);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   public static void write(String file, String buf) {
      PrintWriter writer = null;
      try {
         writer = new PrintWriter(file, UTF_8);
         writer.print(buf);
         writer.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      } finally {
         if (writer != null) {
            writer.close();
         }
      }
   }

   /**
    * Write file.
    * 
    * @param f
    *           the batchfile
    * @param outputList
    *           the output list
    */
   public static void writeFile(String f, List<String> outputList) {
      String fileName = f;
      final int MAXFILENAMELENGTH = 250;
      if (f.length() > MAXFILENAMELENGTH) {
         int lastIndexOfPeriod = f.lastIndexOf(".");
         String suffix = f.substring(lastIndexOfPeriod);
         fileName = f.substring(0, MAXFILENAMELENGTH - 20) + "-shorten" + suffix;
      }

      PrintWriter writer = null;
      try {
         writer = new PrintWriter(new FileOutputStream(new File(fileName), true));
         for (int i = 0; i < outputList.size(); i++) {
            String elem = outputList.get(i);
            writer.println(elem);
         }
         writer.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } finally {
         if (writer != null) {
            writer.close();
         }
      }
   }

   /**
    * @param f
    * @param header
    * @param lines
    * @return
    */
   public static void writeFile(String f, String header, List<List<String>> lines) {
      String fileName = f;
      final int MAXFILENAMELENGTH = 250;
      if (f.length() > MAXFILENAMELENGTH) {
         int lastIndexOfPeriod = f.lastIndexOf(".");
         String suffix = f.substring(lastIndexOfPeriod);
         fileName = f.substring(0, MAXFILENAMELENGTH - 20) + "-shorten" + suffix;
      }

      PrintWriter writer = null;
      try {
         writer = new PrintWriter(new FileOutputStream(new File(fileName), true));
         writer.write(header + "\n");

         for (List<String> list : lines) {
            for (int i = 0; i < list.size(); i++) {
               writer.write(list.get(i));
               if (i < list.size() - 1) {
                  writer.write(COLUMN_SEPARATOR);
               }
            }
            writer.write("\n");
         }
         writer.close();

      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } finally {
         if (writer != null) {
            writer.close();
         }
      }
   }

   public static void writeURL(String url, String fileName) throws Exception {
      BufferedInputStream in = new BufferedInputStream(new URL(url).openStream()); //
      FileOutputStream fileOutputStream = new FileOutputStream(fileName);
      int BUF_SZ = 102400;
      byte dataBuffer[] = new byte[BUF_SZ];
      int bytesRead;
      while ((bytesRead = in.read(dataBuffer, 0, BUF_SZ)) != -1) {
         fileOutputStream.write(dataBuffer, 0, bytesRead);
      }
      fileOutputStream.close();
      in.close();
   }

   @Test
   public void testWriteFile() {
      String input = "123456789012345678901234567890123456789012345678901234567890.txt";
      List<String> outputList = new ArrayList<String>();
      outputList.add("abc");
      outputList.add("def");
      writeFile(input, outputList);
   }

   public static <T> void write(String fileName, List<T[]> listList) {
      StringBuilder buf = new StringBuilder();
      PrintWriter writer = null;
      int i = 0;
      try {
         writer = new PrintWriter(fileName, DEFAULT_FILE_ENCODING);
         for (i = 0; i < listList.size(); i++) {
            T[] elem = listList.get(i);
            StringBuilder aLine = new StringBuilder();
            for (int j = 0; j < elem.length; j++) {
               if (j > 0) {
                  aLine.append(COLUMN_SEPARATOR);
               }
               aLine.append(elem[j]);
            }
            buf.append(aLine + System.lineSeparator());
         }
         writer.print(buf.toString());
         writer.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (Exception e) {
         e.printStackTrace();
      } finally {
         if (writer != null) {
            writer.close();
         }
      }
   }

   public static void truncate(String csvFileName) throws IOException {
      new FileOutputStream(csvFileName).close();

   }
}
