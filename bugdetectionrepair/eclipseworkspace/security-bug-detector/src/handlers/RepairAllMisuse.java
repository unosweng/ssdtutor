
package handlers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import model.MethodElement;
import util.ParserSAX;
import util.RepairHelper;

import view.MisuseViewer;

public class RepairAllMisuse {
   String URI = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
   String xmlURL = String.format("%s/misuse.xml", URI);

   @Execute
   public void execute() {
      File misuseF = new File(xmlURL);
      //      ParseXMLFile(misuseF);
      if(misuseF.exists()) {
         try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            File misuseFile = new File(xmlURL);
            RepairHelper rHelper = new RepairHelper();
            if(misuseFile.exists()){
               Document doc = docBuilder.parse(xmlURL);
               NodeList items = doc.getElementsByTagName("WarningInstance");
               for(int i=0; i<items.getLength(); i++) {
                  Node node = items.item(i);
                  if (node.getNodeType() == Node.ELEMENT_NODE) {
                     Element element = (Element) node;
                     String projectName = element.getElementsByTagName("Project").item(0).getTextContent();
                     NodeList val = element.getElementsByTagName("Root");
                     for(int j=0; j<val.getLength(); j++) {
                        Node sub = val.item(j);
                        if (sub.getNodeType() == Node.ELEMENT_NODE) {
                           Element elem = (Element) sub;
                           String path = elem.getElementsByTagName("FilePath").item(0).getTextContent();
                           System.out.println(path);
                           String arg = elem.getElementsByTagName("ArgPos").item(0).getTextContent();
                           int argPos = Integer.valueOf(arg);
                           String className = elem.getElementsByTagName("IndClass").item(0).getTextContent();
                           String methodName = elem.getElementsByTagName("IndMethod").item(0).getTextContent();
                           String line = elem.getElementsByTagName("LineNumber").item(0).getTextContent();
                           String param = elem.getElementsByTagName("Parameters").item(0).getTextContent();
                           String repairPath = elem.getElementsByTagName("RepairPath").item(0).getTextContent();
                           String orgPath = elem.getElementsByTagName("OriginalPath").item(0).getTextContent();
                           String patternConflict = elem.getElementsByTagName("PatternConflict").item(0).getTextContent();
                           System.out.println(repairPath);
                           System.out.println(orgPath);
                           List<?> parameters = Arrays.asList(
                                 param.substring(1, param.length() - 1).split(",")
                                 );
                           int lineNumber = Integer.valueOf(line);
                           IPath filePath = new Path(path);
                           IFile files = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(filePath);
                           List<ICompilationUnit> iUnits = new ArrayList<>();
                           ICompilationUnit iUnit = JavaCore.createCompilationUnitFrom(files);
                           iUnits.add(iUnit);
                           try {
                              MethodElement me = new MethodElement(null, null);
                              me.setArgPos(argPos);
                              me.setLineNumber(lineNumber);
                              me.setIndicatorClassName(className);
                              me.setIndicatorMethodName(methodName);
                              me.setParameters(parameters);
                              me.setRepairPath(repairPath);
                              me.setOriginalPath(orgPath);
                              me.setPatternConflict(Boolean.valueOf(patternConflict));
                              ParserSAX parser = ParserSAX.getRepairMenu(me.getIndicatorClassName());
                              List<Map<String, String>> menuList = parser.getRepairMenu();
                              for (int k=0; k<menuList.size(); k++) {
                                 Map<String, String> menuItem = menuList.get(k);
                                 int patternID = Integer.valueOf(menuItem.get("case"));
                                 if((menuItem.get("argPos")!=null) && !(menuItem.get("argPos").equals(String.valueOf(me.getIndicatorPos())))) {
                                    continue;
                                 }
                                 else {
                                    rHelper.repair(patternID, path, projectName, me);
                                 }
                              }
                              
                           } catch (Exception e) {
                              // TODO Auto-generated catch block
                              e.printStackTrace();
                           }
                        }
                     }
                  }
               }
            }
         } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
         }

      }
   }

}