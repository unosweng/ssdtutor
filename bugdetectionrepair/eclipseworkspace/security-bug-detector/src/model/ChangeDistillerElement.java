package model;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;

public class ChangeDistillerElement {
    private String      pkgName;
    private String      className;
    private String      methodName;
    private String      editLabel;
    private String      editOperation;
    private String      oldFile;
    private String      newFile;
    private EntityType  changedEntityType;
    private String changedEntityName;
    private String sourceRangeLine;

    public ChangeDistillerElement() {
    }

    public ChangeDistillerElement(String oldFile, String newFile, String pkgName, String className, String methodName,  String editOperation, String editLabel, EntityType changedEntityType, String changedEntityName, String sourceRangeLine) {
        this.oldFile = oldFile;
        this.newFile = newFile;
        this.pkgName = pkgName;
        this.className = className;
        this.methodName = methodName;
        this.editOperation = editOperation;
        this.editLabel = editLabel;
        this.changedEntityType = changedEntityType;
        this.changedEntityName = changedEntityName;
        this.sourceRangeLine = sourceRangeLine;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    public String getNewFile() {
        return newFile;
    }

    public void setNewFile(String newFile) {
        this.newFile = newFile;
    }
    
    public String getOldFile() {
        return oldFile;
    }

    public void setOldFile(String oldFile) {
        this.oldFile = oldFile;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getEditLabel() {
        return editLabel;
    }
    
    public void setEditOperation(String editOperation) {
        this.editOperation = editOperation;
    }

    public String getEditOperation() {
        return editOperation;
    }
    
    public void setEditLabel(String editLabel) {
        this.editLabel = editLabel;
    }

    public EntityType getChangedEntityType() {
        return changedEntityType;
    }

    public void setChangedEntityType(EntityType changedEntityType) {
        this.changedEntityType = changedEntityType;
    }
    
    public String getChangedEntityName() {
        return changedEntityName;
    }

    public void SetChangedEntityName(String changedEntityName) {
        this.changedEntityName = changedEntityName;
    }
    
    public String getSourceRangeLine() {
        return sourceRangeLine;
    }

    public void getSourceRangeLine(String sourceRangeLine) {
        this.sourceRangeLine = sourceRangeLine;
    }

    @Override
    public String toString() {
        return pkgName + "." + className + "." + methodName;
    }
}
