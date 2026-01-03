
package handlers;

import java.io.File;
import java.io.FileWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import analysis.ProjectAnalyzer;
import input.IGlobalProperty;
import model.BatchProcessing;
import model.ChangeDistillerProgramElement;
import model.MethodElement;
import model.ProgElementModelProvider;
import model.ProgramElement;
import util.PrintHelper;
import util.TableViewerHelper;
import util.UtilAST;
import util.CreateGroups;
import visitor.DetectionASTVisitor;
import visitor.FindMisuseECBDetector;
import util.UtilFile;
import view.MisuseViewer;
import view.CryptoMisuseDetectionTree;
import visitor.FindMisuseConstSecretKeyDetector;
import visitor.FindMisuseHashFunDetector;
import visitor.FindMisuseInitializationVector;
import visitor.FindMisuseKeyGenerator;
import visitor.FindMisusePBEDetecter;
import visitor.FindMisuseSecretKeyFactory;
import visitor.FindMisuseSecureRandomDetecter;
import visitor.FindMisuse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class RunFindAllCryptoAPIMisuseHandlers {

   private Document doc;
   int i;
   private List<String> cryptoPaths;
   private static String repairPath;
   private static String originalPath;
   private static String rootPath;
   protected DetectionASTVisitor dec;
   protected FindMisuse fMisuse;
   protected String URI = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
   int j;

   @Execute
   public void execute(EPartService epartService) throws IOException, CoreException, SAXException {
      Date start = new Date();
      PrintHelper.printDebugMsgH("Run all Misuse Handlers\n");
      i = 1;
      String fileInfoURL = String.format("%s/fileInfo.csv",URI);
      String misuseFileURL = String.format("%s/misuse.csv", URI);
      String progressFileURL = String.format("%s/progressFile.csv", URI);
      String xmlURL = String.format("%s/misuse.xml", URI);
      Shell shell = null;
      FileWriter writer = null;
      Element root = null;
      try {
         DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
         File misuseFile = new File(xmlURL);
         if(misuseFile.exists()){
            doc = docBuilder.parse(xmlURL);
            root = doc.getDocumentElement();
            i = doc.getElementsByTagName("WarningInstance").getLength() + 1;
         }
         else {
            doc = docBuilder.newDocument();
            root = doc.createElement("Warnings");
            doc.appendChild(root);
         }
      } catch (ParserConfigurationException e1) {
         // TODO Auto-generated catch block
         e1.printStackTrace();
      }
      File misuseFile = new File(misuseFileURL);
      StringBuilder mD = new StringBuilder();
      if(misuseFile.exists()) {
         Scanner misuseData = new Scanner(new File(misuseFileURL));
         misuseData.nextLine();
         while (misuseData.hasNextLine()) {
            mD.append(misuseData.nextLine());
            mD.append("\n");
         }
         misuseFile.delete();
      }
      misuseFile.createNewFile();
      writer = new FileWriter(misuseFile);
      writer.append("WarningInstanceID");
      writer.append(",");
      writer.append("Date");
      writer.append(",");
      writer.append("WarningType");
      writer.append(",");
      writer.append("Project");
      writer.append(",");
      writer.append("Indicator");
      writer.append(",");
      writer.append("IndClass");
      writer.append(",");
      writer.append("IndMethod");
      writer.append(",");
      writer.append("IndField");
      writer.append(",");
      writer.append("IndFilePath");
      writer.append(",");
      writer.append("IndLineNumber");
      writer.append(",");
      writer.append("RootCause");
      writer.append(",");
      writer.append("RootClass");
      writer.append(",");
      writer.append("RootMethod");
      writer.append(",");
      writer.append("RootField");
      writer.append(",");
      writer.append("RootFilePath");
      writer.append(",");
      writer.append("RootLineNumber");
      writer.append("\n");
      writer.append(mD.toString());     
      File projectInfoFIle = new File(fileInfoURL);
      if(!projectInfoFIle.exists()) {
         FileWriter pFile = new FileWriter(projectInfoFIle);
         CreateGroups createGroups = new CreateGroups();
         IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
         StringBuilder sb = createGroups.writeFileInformation(projects);
         sb.setLength(sb.length() - 1);
         pFile.append(sb.toString());
         pFile.flush();
         pFile.close();
         sb = null;
         System.gc();
      }
      File progressFile = new File(progressFileURL);
      List<String> existingGoups = new ArrayList<String>();
      if(progressFile.exists()) {
         Scanner groups = new Scanner(new File(progressFileURL));
         while (groups.hasNextLine()) {
            existingGoups.add(groups.nextLine());
         }
      }
      Scanner line = new Scanner(new File(fileInfoURL));
      String files = null;
      String group = null;
      int completedProjectLength = 0;
      int projectLength = 0;
      if(line.hasNextLine()) {
         projectLength = Integer.valueOf(line.nextLine());
      }
      line.nextLine();
      while (line.hasNextLine()) {
         String values = line.nextLine();
         if(values.contains("Group")) {
            if (existingGoups.contains(values)) {
               String v = line.nextLine();
               completedProjectLength = completedProjectLength + v.split(",").length;
            } else {
               group = values;
            }
         } else {
            files = values;
            existingGoups.add(group);  
            break;
         }
      }
      FileWriter pFile = new FileWriter(progressFile);
      for(String groupTitle:existingGoups) {
         pFile.append(groupTitle);
         pFile.append("\n");
      }
      pFile.flush();
      pFile.close();
      for(String filePath: files.split(",")) {
         j = completedProjectLength + 1;
         System.out.println(filePath);
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(filePath);
         FindMisuseECBDetector detector1 = new FindMisuseECBDetector();
         go(detector1, project);
         getProgramElement(writer, root, 1);
         checkRAM();
         FindMisuseHashFunDetector detector2 = new FindMisuseHashFunDetector();
         go(detector2, project);
         getProgramElement(writer, root, 2);
         checkRAM();
         FindMisuseConstSecretKeyDetector detector3 = new FindMisuseConstSecretKeyDetector();
         go(detector3, project);
         getProgramElement(writer, root, 3);
         checkRAM();
         FindMisusePBEDetecter detector4 = new FindMisusePBEDetecter();
         go(detector4, project);
         getProgramElement(writer, root, 4);
         checkRAM();
         FindMisuseSecureRandomDetecter detector5 = new FindMisuseSecureRandomDetecter();
         go(detector5, project);
         getProgramElement(writer, root, 5);
         checkRAM();
         FindMisuseKeyGenerator detector6 = new FindMisuseKeyGenerator();
         go(detector6, project);
         getProgramElement(writer, root, 6);
         checkRAM();
         FindMisuseSecretKeyFactory detector7 = new FindMisuseSecretKeyFactory();
         go(detector7, project);
         getProgramElement(writer, root, 7);
         checkRAM();
         FindMisuseInitializationVector detector8 = new FindMisuseInitializationVector();
         go(detector8, project);
         getProgramElement(writer, root, 8);
         checkRAM();
         completedProjectLength += 1;
      }
      System.gc();
      Runtime.getRuntime().gc();
      writer.flush();
      writer.close();
      Date end = new Date();
      long millis = (end.getTime() - start.getTime());
      String duration = DurationFormatUtils.formatDuration(millis, "HH:mm:ss.S");
      PrintHelper.printDebugMsgT("Done in: " + duration);
      if(completedProjectLength==projectLength) {
         String information = String.format("Completed");
         MessageDialog.openInformation(shell, "Update Information", information);
         File misuseF = new File(xmlURL);
         StringBuilder sb = new StringBuilder();
         sb.append("<xmp>");
         sb.append("\n");
         if(misuseF.exists()) {
            Scanner groups = new Scanner(new File(xmlURL));
            while (groups.hasNextLine()) {
               sb.append(groups.nextLine());
               sb.append("\n");
            }
         }
         sb.append("</xmp>");
         MPart findPart = epartService.findPart(MisuseViewer.VIEW_ID);
         Object findPartObj = findPart.getObject();
         if (findPartObj instanceof MisuseViewer) {
            MisuseViewer v = (MisuseViewer) findPartObj;
            v.setText(sb.toString());
         }
      } else {
         String information = String.format("%d out of %d completed. Please run the Find All Crypto API Misuse again.", completedProjectLength, projectLength);
         MessageDialog.openInformation(shell, "Update Information", information);
      }
   }

   public void checkRAM() {
      long totMem = Runtime.getRuntime().totalMemory();
      long maxMem = Runtime.getRuntime().maxMemory();
      System.gc();
      System.out.println("Total Memory (in bytes): " + totMem);
      System.out.println("Free Memory (in bytes): " + maxMem);
   }


   public void go(DetectionASTVisitor detector, IProject project) {
      analyze(detector, project);
   }

   public void analyze(DetectionASTVisitor detector, IProject project) {
      if (detector == null || project == null) {
         PrintHelper.printDebugMsgH("ERROR - detector is null");
         return;
      }
      try {
         String group = UtilAST.getGroupCategory(project);
         detector.setProjectName(project.getName());
         detector.setGroup(group);
         cryptoPaths = UtilAST.getCryptoFilePaths(project);
         UtilFile.getDirectoryPath(project, j);
         if (cryptoPaths.size() > 0) {
            File f = new File(originalPath);
            if (!f.exists()) {
               try {
                  FileUtils.copyDirectory(project.getLocation().toFile(), f);
                  String projectFilePath = originalPath + IGlobalProperty.PATH_SEPARATOR + ".project";
                  File projectFile = new File(projectFilePath);
                  projectFile.delete();
                  File newProject = new File(projectFilePath);
                  FileWriter f2 = new FileWriter(newProject, false);
                  String[] subString = originalPath.split("/");
                  String projectContent = UtilFile.getProjectFile(subString[subString.length-1]);
                  f2.write(projectContent);
                  f2.close();
               } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            File repairFile = new File(repairPath);
            if (!repairFile.exists()) {
               try {
                  FileUtils.copyDirectory(f, repairFile);
                  String projectFilePath = repairPath + IGlobalProperty.PATH_SEPARATOR + ".project";
                  File projectFile = new File(projectFilePath);
                  projectFile.delete();
                  File newProject = new File(projectFilePath);
                  FileWriter f2 = new FileWriter(newProject, false);
                  String[] subString = repairPath.split("/");
                  String projectContent = UtilFile.getProjectFile(subString[subString.length-1]);
                  f2.write(projectContent);
                  f2.close();
               } catch (IOException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }
            }
            analyzePackages(JavaCore.create(project).getPackageFragments(), detector);
         } else {
            PrintHelper.printDebugMsg("SKIP: " + project.getName() + " -> DETECTOR file is empty");
         }
      } catch (JavaModelException e) {
         e.printStackTrace();
      } catch (CoreException e) {
         e.printStackTrace();
      }
   }

   protected void analyzePackages(IPackageFragment[] packages, DetectionASTVisitor visitor) throws CoreException, JavaModelException {
      // =============================================================
      // 2nd step: Packages
      // =============================================================
      for (IPackageFragment iPackage : packages) {
         if (iPackage.getKind() == IPackageFragmentRoot.K_SOURCE) {
            if (iPackage.getCompilationUnits().length < 1) {
               continue;
            }
            visitor.setiPackages(packages);
            visitor.setPackageName(iPackage.getElementName());
            analyzeCompilationUnit(iPackage.getCompilationUnits(), visitor);
         }
      }
   }

   private void analyzeCompilationUnit(ICompilationUnit[] iCompilationUnits, DetectionASTVisitor visitor) throws JavaModelException {
      // =============================================================
      // 3rd step: ICompilationUnits
      // =============================================================
      for (ICompilationUnit iUnit : iCompilationUnits) {
         String iUnitPath = iUnit.getResource().getLocation().toFile().getPath();
         String javaFilePath = iUnit.getResource().getLocation().toFile().getAbsolutePath();
         String className = TableViewerHelper.getClassNameFromJavaFile(iUnit.getElementName());
         if (cryptoPaths.contains(javaFilePath)) {
            PrintHelper.printDebugMsg("Running Detector on: " + javaFilePath);
            visitor.setJavaFilePath(javaFilePath);
            visitor.setFileName(iUnit.getElementName());
            visitor.setClassName(className);
            visitor.setRepairPath(getRepairPath());
            visitor.setFilePath(iUnitPath);
            visitor.setOriginalPath(getOriginalPath());
            CompilationUnit comUnit = UtilAST.parse(iUnit);
            comUnit.accept(visitor);
            comUnit = null;
            System.gc();
         }
      }
   }

   private void getProgramElement(FileWriter writer, Element root, int caseId) throws IOException {
      List<MethodElement> misuse = new ArrayList<MethodElement>();

      if(BatchProcessing.INSTANCE.getMisuse().size() > 0) {
         for(MethodElement me:BatchProcessing.INSTANCE.getMisuse()) {
            if(me.getListChildren().size()>0) {
               for(int k =0; k<me.getListChildren().size(); k++) {
                  misuse.add(me);
                  MethodElement me1 = (MethodElement) me.getListChildren().get(k);
                  misuse.add(me1);
                  createXML(misuse, me.getProjectName(), me.getPatternName(), root);
                  createCSV(misuse, me.getProjectName(), me.getPatternName(), writer);
                  misuse.clear();
                  i = i + 1;
               }
            }
            else {
               misuse.add(me);
               createXML(misuse, me.getProjectName(), me.getPatternName(), root);
               createCSV(misuse, me.getProjectName(), me.getPatternName(), writer);
               misuse.clear();
               i = i + 1;
            }
//            for(int k =0; k<me.getListChildren().size(); k++) {
//               MethodElement me1 = (MethodElement) me.getListChildren().get(k);
//               misuse.add(me1);
//               System.out.println(me1.getParameters());
//               System.out.println(me1.getArgPos());
//            }
            
         }
      }
      BatchProcessing.INSTANCE.clearMisuse();
      ProgElementModelProvider.INSTANCE.clearProgramElements();
   }

   private void createCSV(List<MethodElement> misuses, String fileName, String patternName, FileWriter writer) throws IOException {
      writer.append(Integer.toString(i));
      writer.append(",");
      writer.append(new Date().toString());
      writer.append(",");
      writer.append(patternName);
      writer.append(",");
      writer.append(fileName);
      writer.append(",");
      for(MethodElement misuse: misuses) {
         if(misuse.getIndicator().toString().toLowerCase().equals("ind")){
            writer.append(misuse.getIndicatorClassName());
            writer.append(",");
            writer.append(misuse.getClassName());
            writer.append(",");
            writer.append(misuse.getClassMethodName());
            writer.append(",");
            writer.append(misuse.getName());
            writer.append(",");
            writer.append(misuse.getJavaElement().getResource().getLocation().toFile().getAbsolutePath());
            writer.append(",");
            writer.append(Integer.toString(misuse.getLineNumber()));
            writer.append(",");
         } if(misuse.getIndicator().toString().toLowerCase().equals("root")) {
            writer.append(misuse.getIndicatorClassName());
            writer.append(",");
            writer.append(misuse.getClassName());
            writer.append(",");
            writer.append(misuse.getClassMethodName());
            writer.append(",");
            writer.append(misuse.getName());
            writer.append(",");
            writer.append(misuse.getJavaElement().getResource().getLocation().toFile().getAbsolutePath());
            writer.append(",");
            writer.append(Integer.toString(misuse.getLineNumber()));
            writer.append("\n");
         } if(misuse.getIndicator().toString().toLowerCase().equals("indroot")) {
            writer.append("");
            writer.append(",");
            writer.append("");
            writer.append(",");
            writer.append("");
            writer.append(",");
            writer.append("");
            writer.append(",");
            writer.append("");
            writer.append(",");
            writer.append("");
            writer.append(",");
            writer.append(misuse.getIndicatorClassName());
            writer.append(",");
            writer.append(misuse.getClassName());
            writer.append(",");
            writer.append(misuse.getClassMethodName());
            writer.append(",");
            writer.append(misuse.getName());
            writer.append(",");
            writer.append(misuse.getJavaElement().getResource().getLocation().toFile().getAbsolutePath());
            writer.append(",");
            writer.append(Integer.toString(misuse.getLineNumber()));
            writer.append("\n");
         }
      }
   }

   private void createXML(List<MethodElement> misuses, String fileName, String patternName, Element root) {
      Element rootElement = doc.createElement("WarningInstance");
      root.appendChild(rootElement);
      rootElement.setAttribute("ID", Integer.toString(i));
      Element date = doc.createElement("Date");
      date.setTextContent(new Date().toString());
      rootElement.appendChild(date);
      Element pattern = doc.createElement("WarningType");
      pattern.setTextContent(patternName);
      rootElement.appendChild(pattern);
      Element project = doc.createElement("Project");
      project.setTextContent(fileName);
      rootElement.appendChild(project);


      for(MethodElement misuse: misuses) {
         if(misuse.getIndicator().toString().toLowerCase().equals("ind")){
            Element indicator = doc.createElement("Indicator");
            Element className = doc.createElement("Class");
            className.setTextContent(misuse.getClassName());
            indicator.appendChild(className);
            Element method = doc.createElement("Method");
            method.setTextContent(misuse.getClassMethodName());
            indicator.appendChild(method);
            Element field = doc.createElement("Field");
            field.setTextContent(misuse.getName());
            indicator.appendChild(field);
            Element filepath = doc.createElement("FilePath");
            filepath.setTextContent(misuse.getJavaElement().getResource().getLocation().toFile().getAbsolutePath());
            indicator.appendChild(filepath);
            Element lineNumber = doc.createElement("LineNumber");
            lineNumber.setTextContent(Integer.toString(misuse.getLineNumber()));
            indicator.appendChild(lineNumber);
            Element argPos = doc.createElement("ArgPos");
            argPos.setTextContent(Integer.toString(misuse.getArgPos()));
            indicator.appendChild(argPos);
            Element indClass = doc.createElement("IndClass");
            indClass.setTextContent(misuse.getIndicatorClassName());
            indicator.appendChild(indClass);
            Element indMethod = doc.createElement("IndMethod");
            indMethod.setTextContent(misuse.getIndicatorMethodName());
            indicator.appendChild(indMethod);
            Element param = doc.createElement("Parameters");
            param.setTextContent(misuse.getParameters().toString());
            indicator.appendChild(param);
            rootElement.appendChild(indicator);
         }
         if(misuse.getIndicator().toString().toLowerCase().contains("root")) {
            Element rootElem = doc.createElement("Root");
            Element className = doc.createElement("Class");
            className.setTextContent(misuse.getClassName());
            rootElem.appendChild(className);
            Element method = doc.createElement("Method");
            method.setTextContent(misuse.getClassMethodName());
            rootElem.appendChild(method);
            Element field = doc.createElement("Field");
            field.setTextContent(misuse.getName());
            rootElem.appendChild(field);
            Element filepath = doc.createElement("FilePath");
            filepath.setTextContent(misuse.getJavaElement().getResource().getLocation().toFile().getAbsolutePath());
            rootElem.appendChild(filepath);
            Element lineNumber = doc.createElement("LineNumber");
            lineNumber.setTextContent(Integer.toString(misuse.getLineNumber()));
            rootElem.appendChild(lineNumber);
            Element argPos = doc.createElement("ArgPos");
            argPos.setTextContent(Integer.toString(misuse.getArgPos()));
            rootElem.appendChild(argPos);
            Element indClass = doc.createElement("IndClass");
            indClass.setTextContent(misuse.getIndicatorClassName());
            rootElem.appendChild(indClass);
            Element indMethod = doc.createElement("IndMethod");
            indMethod.setTextContent(misuse.getIndicatorMethodName());
            rootElem.appendChild(indMethod);
            Element param = doc.createElement("Parameters");
            param.setTextContent(misuse.getParameters().toString());
            rootElem.appendChild(param);
            Element repairPath = doc.createElement("RepairPath");
            repairPath.setTextContent(misuse.getRepairPath());
            rootElem.appendChild(repairPath);
            Element originalPath = doc.createElement("OriginalPath");
            originalPath.setTextContent(misuse.getOriginalPath());
            rootElem.appendChild(originalPath);
            Element patternConflict = doc.createElement("PatternConflict");
            patternConflict.setTextContent(String.valueOf(misuse.getPatternConflict()));
            rootElem.appendChild(patternConflict);
            rootElement.appendChild(rootElem);
         }
      }
      String xmlURL = String.format("%s/misuse.xml", URI);
      try (FileOutputStream output =
            new FileOutputStream(xmlURL)) {
         writeXml(doc, output);
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   private static void writeXml(Document doc, OutputStream output){

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      try {
         Transformer transformer = transformerFactory.newTransformer();
         transformer.setOutputProperty(OutputKeys.INDENT, "yes");
         DOMSource source = new DOMSource(doc);
         StreamResult result = new StreamResult(output);
         transformer.transform(source, result);
      } catch (TransformerException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public String getRepairPath() {
      return repairPath;
   }

   public static void setRepairPath(String string) {
      repairPath = string;
   }

   public String getOriginalPath() {
      return originalPath;
   }

   public static void setOriginalPath(String string) {
      originalPath = string;
   }

   public String getRootPath() {
      return rootPath;
   }

   public static void setRootPath(String string) {
      rootPath = string;
   }
}