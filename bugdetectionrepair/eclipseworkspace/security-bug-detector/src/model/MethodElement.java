package model;

import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import graph.model.GNode;

public class MethodElement extends ProgramElement {
   public enum Indicator {
      IND, ROOT, INDROOT, STEP
   };

   private List<?> parameters;
   private IJavaElement javaElement;
   private int lineNumber, argPos, indicatorPos;
   private Indicator indicator;
   private String indicatorClass, indicatorMethod, patternName;
   private String detectionLine, fileName, pkgName, className, repairPath, methodName, projectName;
   private String originalPath, filePath;
   private List<GNode> modelBuilder;
   private List<GNode> treeNode;
   private boolean patternConflict;

   public MethodElement(String name, ProgramElement parent) {
      super(name, parent);
   }

   public void setIndicator(Indicator ind) {
      this.indicator = ind;
   }

   public Indicator getIndicator() {
      return this.indicator;
   }

   public void setParameters(List<?> parameters) {
      this.parameters = parameters;
   }

   public List<?> getParameters() {
      return parameters;
   }

   public String getParameterStr() {
      if (parameters.isEmpty()) {
         return "";
      }
      StringBuilder buf = new StringBuilder();
      for (Object t : parameters) {
         buf.append(t.toString() + ", ");
      }
      return buf.toString().trim().substring(0, buf.toString().trim().length() - 1);
   }

   public String getMethodName() {
      return name;
   }

   public Integer getParameterSize() {
      return parameters.size();
   }

   public IJavaElement getJavaElement() {
      return javaElement;
   }

   public void setJavaElement(IJavaElement javaElem) {
      this.javaElement = javaElem;
   }

   public int getLineNumber() {
      return lineNumber;
   }

   public List<GNode> getGraph() {
      return treeNode;
   }

   public void setGraph(List<GNode> node) {
      this.treeNode = node;
   }

   public void setLineNumber(int lineNumber) {
      this.lineNumber = lineNumber;
   }

   public void setDetectionLine(String detectionLine) {
      this.detectionLine = detectionLine;
   }

   public String getDetectionLine() {
      return this.detectionLine;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public String getFileName() {
      return this.fileName;
   }
   
   public void setProjectName(String projectName) {
      this.projectName = projectName;
   }

   public String getProjectName() {
      return this.projectName;
   }

   public void setPackageName(String pkgName) {
      this.pkgName = pkgName;
   }

   public String getPackageName() {
      return this.pkgName;
   }

   public void setClassName(String className) {
      this.className = className;
   }

   public String getClassName() {
      return this.className;
   }

   public void setClassMethodName(String methodName) {
      this.methodName = methodName;
   }

   public String getClassMethodName() {
      return methodName;
   }

   public void setIndicatorClassName(String indicatorClassName) {
      this.indicatorClass = indicatorClassName;
   }

   public String getIndicatorClassName() {
      return this.indicatorClass;
   }

   public void setIndicatorMethodName(String indicatorMethodName) {
      this.indicatorMethod = indicatorMethodName;
   }

   public String getIndicatorMethodName() {
      return this.indicatorMethod;
   }

   public void setArgPos(int argPos) {
      this.argPos = argPos;
   }

   public int getArgPos() {
      return this.argPos;
   }

   public void setIndicatorPos(int indicatorPos) {
      this.indicatorPos = indicatorPos;
   }

   public int getIndicatorPos() {
      return this.indicatorPos;
   }

   public void setRepairPath(String repairPath) {
      this.repairPath = repairPath;
   }

   public String getRepairPath() {
      return this.repairPath;
   }

   public void setOriginalPath(String originalPath) {
      this.originalPath = originalPath;
   }

   public String getOriginalPath() {
      return this.originalPath;
   }

   public void setFilePath(String filePath) {
      this.filePath = filePath;
   }

   public String getFilePath() {
      return this.filePath;
   }
   
   public void setPatternName(String patternName) {
      this.patternName = patternName;
   }

   public String getPatternName() {
      return this.patternName;
   }

   public void setPatternConflict(boolean patternConflict) {
      this.patternConflict = patternConflict;
   }

   public boolean getPatternConflict() {
      return this.patternConflict;
   }

   public void setModelBuilder(List<GNode> modelBuilder) {
      this.modelBuilder = modelBuilder;
   }

   public List<GNode> getModelBuilder() {
      return this.modelBuilder;
   }


   /**
    * Searches for a child that matches the indicator provided.
    * 
    * @param indicator
    *           - the indicator to search for
    * @return MethodElement <br>
    *         If a match is found, the child is returned as the indicator <br>
    *         If a match is not found, the indicator is added as a child and the indicator is returned
    * 
    */
   public MethodElement getChildMethodElement(MethodElement indicator) {
      for (ProgramElement pe : this.listChildren) {
         MethodElement child = (MethodElement) pe;
         if (child.lineNumber == indicator.lineNumber //
               && child.indicator.equals(indicator.indicator) //
               && child.javaElement.equals(indicator.javaElement) //
               && child.name.contentEquals(indicator.name) && child.parent.equals(indicator.parent))
            return child;
      }
      this.add(indicator);
      return indicator;
   }
}
