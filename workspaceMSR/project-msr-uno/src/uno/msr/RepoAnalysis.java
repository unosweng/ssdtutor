package uno.msr;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.analysis.ASCIIFoldingFilter;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PorterStemFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
<row 
Id="1" 
PostTypeId="1" 
AcceptedAnswerId="51" 
CreationDate="2016-01-12T18:45:19.963" 
Score="10" 
ViewCount="306" 
Body="&lt;p&gt;When I've printed an object I've had to choose between high resolution and quick prints.  What techniques or technologies can I use or deploy to speed up my high resolution prints?&lt;/p&gt;&#xA;" 
OwnerUserId="16" 
LastActivityDate="2017-10-31T02:31:08.560" 
Title="How to obtain high resolution prints in a shorter period of time?" 
Tags="&lt;resolution&gt;&lt;speed&gt;&lt;quality&gt;" 
AnswerCount="2" 
CommentCount="6" 
/>
*/

public class RepoAnalysis {
   public static final String _root = "/opt/workspaceMSR/"; // Change to your system path

   public static final String _all_packages = _root + "allpackages.txt";
   public static final String _all_classes = _root + "allclasses.txt";
   public static final String _all_methods = _root + "allmethods.txt";

   public static final String _repo_path = _root + "input-posts";
   public static final String _repo_post_file = "Posts.xml";
   public static final String _repo_category = _root + "repo/repo_category_filtered.txt";

   private static final String OUTPUT_FILE_BYTOPIC_CSV = _root + "bytopic.csv";

   public static List<String> _categories;

   public static void main(String[] args) {
      try {
         byTopic();
      } catch (ParserConfigurationException | SAXException | IOException e) {
         e.printStackTrace();
      }

      // try {
      // byAPI();
      // } catch (ParserConfigurationException | SAXException | IOException e) {
      // e.printStackTrace();
      // }
   }

   public static void byTopic() throws ParserConfigurationException, SAXException, IOException {
      // String[] c = readFile(_repo_category).split("\n");
      // _categories = Arrays.asList(c);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      StringBuffer sb = new StringBuffer();
      sb.append("category,total_count,api_count,crypto_count,des_count,aes_count,rsa_count," //
            + "hash_count,sha_count,md_count,hmac_count,bpe_count").append("\n");
      File root = new File(_repo_path);
      File[] dirs = root.listFiles();
      for (File dir : dirs) {
         // if (!(dir.getName().startsWith("crypto."))) {
         // continue;
         // }

         // if (!_categories.contains(dir.getName())) {
         // continue;
         // }

         String file = dir.getAbsolutePath() + File.separator + _repo_post_file;
         if (!new File(file).exists()) {
            continue;
         }
         System.out.println(file);
         int count_total = 0;
         int count_api = 0;
         int count_crypto = 0;
         int count_des = 0;
         int count_aes = 0;
         int count_rsa = 0;
         int count_hash = 0;
         int count_sha = 0;
         int count_md = 0;
         int count_hmac = 0;
         int count_bpe = 0;
         try {
            Document document = (Document) builder.parse(new File(file));
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
               Node node = nodeList.item(i);

               if (node.getNodeType() == Node.ELEMENT_NODE) {
                  NamedNodeMap map = node.getAttributes();
                  // String id = map.getNamedItem("Id").getNodeValue();
                  String body = map.getNamedItem("Body").getNodeValue();
                  body = body.trim().replace("\n", ". ").replace("\r", "").replaceAll("\\<.*?\\>", "");
                  count_total++;

                  List<Keyword> keywords = guessFromString(body);
                  int tmp_count_api = 0;
                  int tmp_count_des = 0;
                  int tmp_count_aes = 0;
                  int tmp_count_rsa = 0;
                  int tmp_count_hash = 0;
                  int tmp_count_sha = 0;
                  int tmp_count_md = 0;
                  int tmp_count_hmac = 0;
                  int tmp_count_bpe = 0;
                  for (Keyword keyword : keywords) {
                     if (keyword.getStem().equals("api") || keyword.getStem().equals("implement") || keyword.getStem().equals("librari")) {
                        tmp_count_api++;
                     }
                     if (keyword.getStem().equals("des") || keyword.getStem().equals("de")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_des++;
                     }
                     if (keyword.getStem().equals("aes") || keyword.getStem().equals("ae")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_aes++;
                     }
                     if (keyword.getStem().equals("rsa")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_rsa++;
                     }
                     if (keyword.getStem().equals("hash")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_hash++;
                     }
                     if (keyword.getStem().equals("sha")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_sha++;
                     }
                     if (keyword.getStem().equals("md")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_md++;
                     }
                     if (keyword.getStem().equals("hmac")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_hmac++;
                     }
                     if (keyword.getStem().equals("pbkdf") || keyword.getStem().equals("pbe")) {
                        // System.out.println(id + ":" + keyword.getStem() + "(" + keyword.getFrequency() + ");");
                        tmp_count_bpe++;
                     }
                  }
                  if (tmp_count_api > 0) {
                     count_api++;

                     if (tmp_count_des > 0 || tmp_count_aes > 0 || tmp_count_rsa > 0 || tmp_count_hash > 0 || tmp_count_sha > 0 || tmp_count_md > 0 || tmp_count_hmac > 0 || tmp_count_bpe > 0) {
                        count_crypto++;
                     }

                     if (tmp_count_des > 0) {
                        count_des++;
                     }
                     if (tmp_count_aes > 0) {
                        count_aes++;
                     }
                     if (tmp_count_rsa > 0) {
                        count_rsa++;
                     }
                     if (tmp_count_hash > 0) {
                        count_hash++;
                     }
                     if (tmp_count_sha > 0) {
                        count_sha++;
                     }
                     if (tmp_count_md > 0) {
                        count_md++;
                     }
                     if (tmp_count_hmac > 0) {
                        count_hmac++;
                     }
                     if (tmp_count_bpe > 0) {
                        count_bpe++;
                     }
                  }
               }
            }
            sb.append(dir.getAbsolutePath()).append(",").append(count_total).append(",").append(count_api).append(",").append(count_crypto).append(",").append(count_des).append(",").append(count_aes).append(",").append(count_rsa).append(",").append(count_hash).append(",").append(count_sha).append(",").append(count_md).append(",").append(count_hmac).append(",").append(count_bpe).append("\n");
            writeFile(OUTPUT_FILE_BYTOPIC_CSV, sb.toString());
            System.out.println("[DBG] written " + OUTPUT_FILE_BYTOPIC_CSV);
         } catch (Exception e) {
            //
         }
         break;
      }
   }

   public static void byAPI() throws ParserConfigurationException, SAXException, IOException {
      String[] keywords_class = readFile(_all_classes).split("\n");
      String[] keywords_package = new String[keywords_class.length];
      String[] keywords_klass = new String[keywords_class.length];
      for (int w = 0; w < keywords_class.length; w++) {
         keywords_package[w] = keywords_class[w].substring(0, keywords_class[w].lastIndexOf("."));
         keywords_klass[w] = keywords_class[w].substring(keywords_class[w].lastIndexOf(".") + 1);
      }

      String[] keywords_method = readFile(_all_methods).split("\n");
      String[] keywords_mklass = new String[keywords_method.length];
      String[] keywords_mmtd = new String[keywords_method.length];
      for (int w = 0; w < keywords_method.length; w++) {
         keywords_mklass[w] = keywords_method[w].substring(0, keywords_method[w].lastIndexOf("."));
         keywords_mmtd[w] = keywords_method[w].substring(keywords_method[w].lastIndexOf(".") + 1);
      }

      String[] c = readFile(_repo_category).split("\n");
      _categories = Arrays.asList(c);

      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();

      StringBuffer sb_class = new StringBuffer();
      sb_class.append("category,total_count,class_count,").append(String.join(",", keywords_class)).append("\n");

      StringBuffer sb_method = new StringBuffer();
      sb_method.append("category,total_count,method_count,").append(String.join(",", keywords_method)).append("\n");

      File root = new File(_repo_path);
      File[] dirs = root.listFiles();
      for (File dir : dirs) {
         // if (!(dir.getName().startsWith("drupal."))) {
         // continue;
         // }

         if (!_categories.contains(dir.getName())) {
            continue;
         }

         String file = dir.getAbsolutePath() + File.separator + _repo_post_file;
         if (!new File(file).exists()) {
            continue;
         }
         System.out.println(file);

         int count_total = 0;

         int count_api_class = 0;
         int[] count_keywords_class = new int[keywords_class.length];
         for (int i = 0; i < count_keywords_class.length; i++) {
            count_keywords_class[i] = 0;
         }

         int count_api_method = 0;
         int[] count_keywords_method = new int[keywords_method.length];
         for (int i = 0; i < count_keywords_method.length; i++) {
            count_keywords_method[i] = 0;
         }

         try {
            Document document = (Document) builder.parse(new File(file));
            NodeList nodeList = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
               Node node = nodeList.item(i);

               if (node.getNodeType() == Node.ELEMENT_NODE) {
                  NamedNodeMap map = node.getAttributes();
                  // String id = map.getNamedItem("Id").getNodeValue();
                  String body = map.getNamedItem("Body").getNodeValue();
                  body = body.trim().replace("\n", ". ").replace("\r", "").replaceAll("\\<.*?\\>", "");
                  count_total++;

                  int count_class = 0;
                  for (int k = 0; k < keywords_class.length; k++) {
                     if (body.contains(keywords_class[k])) {
                        count_keywords_class[k]++;
                        count_class++;
                     }
                     else {
                        if (body.contains(keywords_package[k]) || body.contains(keywords_klass[k])) {
                           count_keywords_class[k]++;
                           count_class++;
                        }
                     }
                  }
                  if (count_class > 0) {
                     count_api_class++;
                  }

                  int count_method = 0;
                  for (int k = 0; k < keywords_method.length; k++) {
                     if (body.contains(keywords_method[k])) {
                        count_keywords_method[k]++;
                        count_method++;
                     }
                     else {
                        if (body.contains(keywords_mklass[k]) || body.contains(keywords_mmtd[k])) {
                           count_keywords_method[k]++;
                           count_method++;
                        }
                     }
                  }
                  if (count_method > 0) {
                     count_api_method++;
                  }
               }
            }
            sb_class.append(dir.getAbsolutePath()).append(",").append(count_total).append(",").append(count_api_class).append(",").append(Arrays.toString(count_keywords_class).replace("[", "").replace("]", "").replace(" ", "")).append("\n");
            writeFile(_root + "byclass.csv", sb_class.toString());
            sb_method.append(dir.getAbsolutePath()).append(",").append(count_total).append(",").append(count_api_method).append(",").append(Arrays.toString(count_keywords_method).replace("[", "").replace("]", "").replace(" ", "")).append("\n");
            writeFile(_root + "bymethod.csv", sb_method.toString());
         } catch (Exception e) {

         }
      }
   }

   public static boolean isAPI(List<Keyword> keywords) {
      for (Keyword keyword : keywords) {
         if (keyword.getStem().equals("api") || keyword.getStem().equals("implement") || keyword.getStem().equals("librari")) {
            return true;
         }
      }
      return false;
   }

   public static String readFile(String filepath) {
      File file = new File(filepath);
      if (!file.exists()) {
         return null;
      }
      Long filelength = file.length();
      byte[] filecontent = new byte[filelength.intValue()];
      try {
         FileInputStream in = new FileInputStream(file);
         in.read(filecontent);
         in.close();
      } catch (FileNotFoundException e) {
         e.printStackTrace();
      } catch (IOException e) {
         e.printStackTrace();
      }
      try {
         return new String(filecontent, "ISO-8859-1");
      } catch (UnsupportedEncodingException e) {
         System.err.println("The OS does not support ISO-8859-1.");
         e.printStackTrace();
         return null;
      }
   }

   public static void writeFile(String filepath, String content) {
      try {
         FileWriter myWriter = new FileWriter(filepath);
         myWriter.write(content);
         myWriter.close();
      } catch (IOException e) {
         // System.out.println("----" + e.getMessage());
      }
   }

   public static List<Keyword> guessFromString(String input) throws IOException {
      TokenStream tokenStream = null;
      try {
         // hack to keep dashed words (e.g. "non-specific" rather than "non" and "specific")
         input = input.replaceAll("-+", "-0");
         // replace any punctuation char but apostrophes and dashes by a space
         input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
         // replace most common english contractions
         input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

         // tokenize input
         tokenStream = new ClassicTokenizer(Version.LUCENE_36, new StringReader(input));
         // to lowercase
         tokenStream = new LowerCaseFilter(Version.LUCENE_36, tokenStream);
         // remove dots from acronyms (and "'s" but already done manually above)
         tokenStream = new ClassicFilter(tokenStream);
         // convert any char to ASCII
         tokenStream = new ASCIIFoldingFilter(tokenStream);
         // remove english stop words
         tokenStream = new StopFilter(Version.LUCENE_36, tokenStream, EnglishAnalyzer.getDefaultStopSet());

         List<Keyword> keywords = new LinkedList<Keyword>();
         CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
         tokenStream.reset();
         while (tokenStream.incrementToken()) {
            String term = token.toString();
            // stem each term
            String stem = stem(term);
            if (stem != null) {
               // create the keyword or get the existing one if any
               Keyword keyword = find(keywords, new Keyword(stem.replaceAll("-0", "-")));
               // add its corresponding initial token
               keyword.add(term.replaceAll("-0", "-"));
            }
         }

         // reverse sort by frequency
         Collections.sort(keywords);
         return keywords;
      } finally {
         if (tokenStream != null) {
            tokenStream.close();
         }
      }
   }

   public static String stem(String term) throws IOException {
      TokenStream tokenStream = null;
      try {
         // tokenize
         tokenStream = new ClassicTokenizer(Version.LUCENE_36, new StringReader(term));
         // stem
         tokenStream = new PorterStemFilter(tokenStream);

         // add each token in a set, so that duplicates are removed
         Set<String> stems = new HashSet<String>();
         CharTermAttribute token = tokenStream.getAttribute(CharTermAttribute.class);
         tokenStream.reset();
         while (tokenStream.incrementToken()) {
            stems.add(token.toString());
         }

         // if no stem or 2+ stems have been found, return null
         if (stems.size() != 1) {
            return null;
         }
         String stem = stems.iterator().next();
         // if the stem has non-alphanumerical chars, return null
         if (!stem.matches("[a-zA-Z0-9-]+")) {
            return null;
         }

         return stem;
      } finally {
         if (tokenStream != null) {
            tokenStream.close();
         }
      }
   }

   public static <T> T find(Collection<T> collection, T example) {
      for (T element : collection) {
         if (element.equals(example)) {
            return element;
         }
      }
      collection.add(example);
      return example;
   }
}
