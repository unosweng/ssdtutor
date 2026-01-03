/*
 * @(#) View.java
 *
 */
package view;

import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.zest.core.viewers.GraphViewer;
import org.eclipse.zest.core.widgets.Graph;
import org.eclipse.zest.core.widgets.GraphItem;
import org.eclipse.zest.layouts.LayoutStyles;
import org.eclipse.zest.layouts.algorithms.RadialLayoutAlgorithm;
import org.eclipse.zest.layouts.algorithms.TreeLayoutAlgorithm;
import graph.model.GNode;
import graph.provider.GLabelProvider;
import graph.provider.GNodeContentProvider;

public class ZestViewer {
	public static final String SIMPLEZESTVIEW = "security-bug-detector.partdescriptor.zestviewer";
	public final static String POPUPMENU = "security-bug-detector.popupmenu.zestmenu";
	private GraphViewer        gViewer;
	private int                layout         = 0;

	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService) {
		gViewer = new GraphViewer(parent, SWT.BORDER);
		menuService.registerContextMenu(gViewer.getControl(), POPUPMENU);
		gViewer.setContentProvider(new GNodeContentProvider());
		gViewer.setLabelProvider(new GLabelProvider());
		gViewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		gViewer.applyLayout();
		addListener();
	}

	public void update(List<GNode> modelBuilder) {
		gViewer.setInput(modelBuilder);
		if (layout % 2 == 0)
			gViewer.setLayoutAlgorithm(new TreeLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		else
			gViewer.setLayoutAlgorithm(new RadialLayoutAlgorithm(LayoutStyles.NO_LAYOUT_NODE_RESIZING), true);
		layout++;
	}

	public void clear() {
		gViewer.setInput("");
	}

	@Focus
	public void setFocus() {
		this.gViewer.getGraphControl().setFocus();
	}
	
	private void addListener() {
		Graph graph = (Graph) gViewer.getControl();
		graph.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				GraphItem item = (GraphItem) event.item;
				GNode me = (GNode) item.getData();
				openInEditor(me);
			}
		});
	}
	
	public static void openInEditor(GNode me) {
		IJavaElement javaElement = me.getJavaElement();
		int lineNumber = me.getLineNumber();
		try {
			IEditorPart editorPart = JavaUI.openInEditor(javaElement, true, true);
			if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
				return;
			}

			ITextEditor editor = (ITextEditor) editorPart;
			IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
			if (document != null) {
				IRegion lineInfo = null;
				lineInfo = document.getLineInformation(lineNumber - 1);
				if (lineInfo != null) {
					editor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
