package uno.msr;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class MethodVisitor extends ASTVisitor {

	public String _path;

	public String _package;

	public String _class;

	public Map<String, ArrayList<String>> _type_methods;

	public MethodVisitor(String path, String pack) {
		this._path = path.replace(LibExtractor._root, "");
		this._package = pack;
		this._type_methods = new HashMap<String, ArrayList<String>>();
	}

	@Override
	public boolean visit(TypeDeclaration typeNode) {
		String typeName = typeNode.getName().toString();
		if (this._path.contains(typeName)) {
			this._class = typeName;
		}
		ArrayList<String> methods = new ArrayList<String>();
		this._type_methods.put(typeName, methods);

		MethodDeclaration[] notes = typeNode.getMethods();
		for (MethodDeclaration note : notes) {
			if (Modifier.isPublic(note.getModifiers())) {
				this._type_methods.get(typeName).add(note.getName().toString());
			}
		}
		return super.visit(typeNode);
	}

	public boolean visit_old(TypeDeclaration typeNode) {
		System.out.println("------------------self-----------");
		System.out.println(typeNode.getName());

		System.out.println("------------------parent-----------");
		if (typeNode.getSuperclassType() != null) {
			System.out.println(typeNode.getSuperclassType().getClass());
		}
		System.out.println(typeNode.superInterfaceTypes());
        List<?> list = typeNode.superInterfaceTypes();
		for (Object o : list) {
			System.out.println(((Type) o).resolveBinding());
		}

		System.out.println("------------------methods-----------");
		MethodDeclaration[] notes = typeNode.getMethods();
		for (MethodDeclaration note : notes) {
			int modifiers = note.getModifiers();
			if (Modifier.isPublic(modifiers)) {
				System.out.println("modifier: " + modifiers); // 9 = public static, 1=public,
				System.out.println("name: " + note.getName());
				System.out.println("parameter: " + note.parameters());
				System.out.println("return type: " + note.getReturnType2());
				System.out.println("-------");
			}
		}

		System.out.println("------------------fields-----------");
		FieldDeclaration[] nodes = typeNode.getFields();
		for (FieldDeclaration node : nodes) {
			System.out.println(node.getModifiers());// 25 = public static final
			System.out.println(node.fragments());
			for (Object f : node.fragments()) {
				System.out.println(f);
				String s = f.toString();
				if (s.contains("=")) {
					String[] arr = s.split("=");
					s = arr[0];
				} else {
				}
			}
			System.out.println("-------");
		}

		return super.visit(typeNode);
	}

	public boolean visit_old(MethodDeclaration node) {
		int modifiers = node.getModifiers();
		if (Modifier.isPublic(modifiers)) {

		}
		return super.visit(node);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (String type : this._type_methods.keySet()) {
			ArrayList<String> methods = this._type_methods.get(type);
			for (String method : methods) {
				sb.append(this._path).append(",").append(this._package).append(",").append(this._class).append(",").append(type).append(",").append(method).append("\n");
			}
		}
		return sb.toString();
	}

	public void print() {
		System.out.println(toString());
	}
}
