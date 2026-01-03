/**
 * @(#) ProgramElement.java
 */
package model;

/**
 * @since J2SE-1.8
 */
public class ProjectElement {
	private String pkgName;
	private String prjName;
	private String groupName;
	private String className;
	private String methodName;
	private Integer localVar;
	private Integer FieldVar;

	public ProjectElement(String prjName, String groupName, String pkgName, String className, String methodName, int localVar, int FieldVar) {
		this.pkgName = pkgName;
		this.className = className;
		this.methodName = methodName;
		this.prjName = prjName;
		this.localVar = localVar;
		this.groupName = groupName;
		this.FieldVar = FieldVar; 
	}

	public String getPkgName() {
		return pkgName;
	}

	public void setPkgName(String pkgName) {
		this.pkgName = pkgName;
	}
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	
	public String getPrjName() {
		return prjName;
	}

	public void setPrjName(String prjName) {
		this.prjName = prjName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
	public Integer getLocalVariable() {
		return localVar;
	}

	public void setLocalVariable(Integer localVar) {
		this.localVar = localVar;
	}
	
	public Integer getFieldVariable() {
		return FieldVar;
	}

	public void setFieldVariable(Integer FieldVar) {
		this.FieldVar = FieldVar;
	}

	@Override
	public String toString() {
		return pkgName + "." + className + "." + methodName;
	}
}
