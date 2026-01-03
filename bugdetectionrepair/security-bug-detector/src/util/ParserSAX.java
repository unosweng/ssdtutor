package util;
//
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import input.IGlobalProperty;

public class ParserSAX implements IGlobalProperty {
   Map<String, Map<Integer, Map<String, Map<String, String>>>> rule = new HashMap<>();
   Map<String, Map<Integer, String>> repairRule = new HashMap<>();
   Map<String, Map<Integer,Map<String, String>>> searchRules = new HashMap<>();
   Map<String, Map<String, String>> classRules = new HashMap<>();
   Map<String, Map<String, String>> repairRules = new HashMap<>();
   Map<String, String> className = new HashMap<>();
   private static ParserSAX instance = new ParserSAX();
   private String indicatorClass, message, indicatorMethod, referenceClass;
   private List<Map<String, String>> menus = new ArrayList<Map<String, String>>();

   //make the constructor private so that this class cannot be
   //instantiated
   private ParserSAX(){}

   public static ParserSAX getInstance() {
      return instance;
   }

   public static ParserSAX getInstance(String indicator) {
      System.out.println(indicator);
      instance.checkFile(indicator);
      return instance;
   }

   public static ParserSAX getInstance(int classId) {
      instance.getClassName(classId);
      return instance;
   }

   public static ParserSAX getRepairInstance(String indicator) {
      instance.checkRepairFile(indicator);
      return instance;
   }

   public static ParserSAX getRepairReference(String indicator) {
      instance.getReferenceClassName(indicator);
      return instance;
   }

   public static ParserSAX getRepairMenu(String indicator) {
      instance.repairMenu(indicator);
      return instance;
   }
   //
   private void getClassName(int classId) {
      // TODO Auto-generated method stub
      instance.checkFile("classRules");
      Map<String, String> classRule = classRules.get(Integer.toString(classId));
      indicatorClass = classRule.get("className");
      message = classRule.get("message");
   }

   private void getReferenceClassName(String indicator) {
      // TODO Auto-generated method stub
      instance.checkRepairFile(indicator);
      referenceClass = className.get("className");
   }

   private void repairMenu(String indicator) {
      // TODO Auto-generated method stub
      instance.checkRepairFile("repairRules");
      menus = new ArrayList<Map<String, String>>();
      for (Entry<String, Map<String, String>> entry : repairRules.entrySet()) {
         Map<String, String> value = entry.getValue();
         if((value.size()>0) && (value.get("className").toUpperCase().equals(indicator.toUpperCase()))) {
            menus.add(value);
         }
      }
   }

   public void checkFile(String indicatorClass) {
      System.out.println("[DBG] Loading security rule files...");
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject iProject : projects) {
         if (!iProject.getName().equals("security_rules")) {
            continue;
         }
         String pathSecRules = iProject.getLocation().toFile().getAbsolutePath() + File.separator + PATH_SEC_RULE_FILES;
         File inputFile = new File(pathSecRules);
         for (File iRuleFile : inputFile.listFiles()) {
            if (!iRuleFile.isDirectory()) {
               System.out.println("[DBG] " + iRuleFile.getName());
               String filename = iRuleFile.getName().substring(0, iRuleFile.getName().lastIndexOf("."));
               if (indicatorClass.toUpperCase().equals(filename.toUpperCase())) {
                  System.out.println(iRuleFile);
                  ParseXMLFile(iRuleFile);
               }
            }
         }
      }
   }

   public void checkRepairFile(String indicatorClass) {
      System.out.println("[DBG] Loading security rule files...");
      repairRule = new HashMap<>();
      IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
      for (IProject iProject : projects) {
         if (!iProject.getName().equals("security_rules")) {
            continue;
         }
         String pathSecRules = iProject.getLocation().toFile().getAbsolutePath() + File.separator + PATH_SEC_RULE_FILES;
         File inputFile = new File(pathSecRules);
         for (File iRuleFile : inputFile.listFiles()) {
            if (!iRuleFile.getName().equals("repair")) {
               continue;
            }
            for (File iRuleFiles : iRuleFile.listFiles()) {
               if (!iRuleFiles.isDirectory()) {
                  System.out.println("[DBG] " + iRuleFiles.getName());
                  String filename = iRuleFiles.getName().substring(0, iRuleFiles.getName().lastIndexOf("."));
                  if (indicatorClass.toUpperCase().equals(filename.toUpperCase())) {
                     ParseXMLFile(iRuleFiles);
                  } else if(filename.toUpperCase().contains(indicatorClass.toUpperCase())) {
                     ParseXMLFile(iRuleFiles);
                  }
               }
            }
         }
      }
   }
   //
   public void ParseXMLFile(File file) {
      try {
         SAXParserFactory factory = SAXParserFactory.newInstance();
         SAXParser saxParser = factory.newSAXParser();
         UserHandler userhandler = new UserHandler();
         saxParser.parse(file, userhandler);
      } catch (ParserConfigurationException | SAXException | IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public Map<String, Map<Integer, Map<String, Map<String, String>>>> getRule(){
      return rule;
   }

   public Map<String, Map<Integer,Map<String, String>>> getSearchType() {
      // TODO Auto-generated method stub
      return searchRules;
   }

   public String getIndicatorClass() {
      // TODO Auto-generated method stub
      return indicatorClass;
   }

   public String getIndicatorMethod() {
      // TODO Auto-generated method stub
      return indicatorMethod;
   }

   public String getMessage() {
      // TODO Auto-generated method stub
      return message;
   }

   public List<Map<String, String>> getRepairMenu() {
      // TODO Auto-generated method stub
      return menus;
   }

   public String getReferenceClass() {
      // TODO Auto-generated method stub
      return referenceClass;
   }

   public Map<String, Map<Integer, String>> getRepairRule() {
      // TODO Auto-generated method stub
      return repairRule;
   }

   class UserHandler extends DefaultHandler {
      private static final String RULES = "rules";
      private static final String RULE = "rule";
      private static final String CLASS = "class";
      private static final String VALUE = "value";
      private static final String ARGUMENT = "argument";
      private String methodName, repairValue;
      private Integer argumentIndex;
      Map<Integer, Map<String, Map<String, String>>> arguments = new HashMap<>();
      Map<Integer,Map<String, String>> searchArguments = new HashMap<>();
      Map<String, Map<String, String>> values = new HashMap<>();
      Map<String, String> attributes = new HashMap<>();
      Map<String, String> searchRule = new HashMap<>();
      Map<Integer, String> repairArguments = new HashMap<>();
      String searchType;
      boolean isMethod;

      @Override
      public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
         switch (qName) {
         case RULES:
            className.put("className", atts.getValue("class"));
            break;
         case RULE:
            methodName = atts.getValue("method");
            for (int i = 1; i < atts.getLength(); i++) {
               attributes.put(atts.getQName(i), atts.getValue(i));
            }
            repairRules.put(atts.getValue(0).toUpperCase(), attributes);
            attributes = new HashMap<>();
            break;
         case CLASS:
            for (int i = 1; i < atts.getLength(); i++) {
               attributes.put(atts.getQName(i), atts.getValue(i));
            }
            classRules.put(atts.getValue(0).toUpperCase(), attributes);
            attributes = new HashMap<>();
            break;
         case ARGUMENT:
            argumentIndex = Integer.valueOf(atts.getValue("index"));
            if (atts.getValue("searchType") != null){
               searchRule.put("searchType", atts.getValue("searchType").toUpperCase());
               searchArguments.put(argumentIndex, searchRule);
               searchRule = new HashMap<>();
            }
            break;
         case VALUE:
            if(atts.getQName(0).toString().equals("reference")) {
               for (int i = 0; i < atts.getLength(); i++) {
                  attributes.put(atts.getQName(i), atts.getValue(i));
               }
            }
            else {
               for (int i = 1; i < atts.getLength(); i++) {
                  attributes.put(atts.getQName(i), atts.getValue(i));
               }
            }
            values.put(atts.getValue(0).toUpperCase(), attributes);
            repairValue = atts.getValue(0).toUpperCase();
            attributes = new HashMap<>();
            break;
         }
      }

      @Override
      public void endElement(String uri, String localName, String qName) throws SAXException {
         switch (qName) {
         case RULE:
            rule.put(methodName, arguments);
            repairRule.put(methodName, repairArguments);
            searchRules.put(methodName, searchArguments);
            searchArguments = new HashMap<>();
            arguments = new HashMap<>();
            repairArguments = new HashMap<>();
            break;
         case ARGUMENT:
            arguments.put(argumentIndex, values);
            repairArguments.put(argumentIndex, repairValue);
            values = new HashMap<>();
            repairValue = null;
            break;
         }
      }
   }
}
