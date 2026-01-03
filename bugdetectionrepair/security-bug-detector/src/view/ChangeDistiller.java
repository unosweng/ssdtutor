package view;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import analysis.FileAnalyzer;

import org.eclipse.e4.ui.workbench.modeling.EPartService;

//import analysis.FileAnalyzer;
//import model.ModelProvider;
import model.ChangeDistillerProgramElement;
import model.ChangeDistillerElement;
//import handler.Diff2htmlHandler;

public class ChangeDistiller {

	public static final String VIEW_ID = "security-bug-detector.partdescriptor.changedistiller";
	public final static String POPUPMENU_ID = "security-bug-detector.popupmenu.mypopupmenu";
	private TableViewer viewer;
	private StyledText oldData;
	private StyledText newData;
	private StyledText styledText = null;

	@Inject
	EPartService epartService;
	@Inject
	public ChangeDistiller() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {

	}

	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout());
		oldData = new StyledText(container, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		newData = new StyledText(container,SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);

		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createProgElemColumns(parent, viewer);
		final Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		viewer.setContentProvider(ArrayContentProvider.getInstance());
		menuService.registerContextMenu(viewer.getControl(), POPUPMENU_ID);
		addListener();
	}

	private void addListener() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				Object firstElement = selection.getFirstElement();
				ChangeDistillerElement me = (ChangeDistillerElement) firstElement;
				fileDifferentiator(me);
			}
		});
	}

	private void fileDifferentiator(ChangeDistillerElement me) {
		// TODO Auto-generated method stub
		String oldFile = me.getOldFile();
		String data = getFileData(oldFile, me);
		oldData.setText(data);
		StyleRange style1 = new StyleRange();
		style1.start = 0;
		style1.length = data.length();
		style1.background = oldData.getDisplay().getSystemColor(SWT.COLOR_RED);
		style1.foreground = oldData.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		style1.borderColor = oldData.getDisplay().getSystemColor(SWT.COLOR_RED);
		style1.borderStyle = SWT.BORDER_SOLID;
		oldData.setStyleRange(style1);
		String newFile = me.getNewFile();
		data = getFileData(newFile, me);
		newData.setText(data);
		StyleRange style2 = new StyleRange();
		style2.start = 0;
		style2.length = data.length();
		style2.background = newData.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		style2.foreground = newData.getDisplay().getSystemColor(SWT.COLOR_WHITE);
		style2.borderColor = newData.getDisplay().getSystemColor(SWT.COLOR_BLUE);
		style2.borderStyle = SWT.BORDER_SOLID;
		newData.setStyleRange(style2);
	}

	private String getFileData(String oldFile, ChangeDistillerElement me) {
		// TODO Auto-generated method stub
		String lineNumber = me.getSourceRangeLine();
		String[] lineNumbers = lineNumber.split("~");
		String startValue = lineNumbers[0].strip();
		int start = Integer.parseInt(startValue);
		String endValue = lineNumbers[lineNumbers.length-1].strip();
		int end = Integer.parseInt(endValue);
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader br = new BufferedReader(new FileReader(oldFile));
			String line = br.readLine();
			int i = 1;
			while (line != null) {
				if(i>=start && i<=end) {
					sb.append(i);
					sb.append("\t");
					sb.append(line);
					sb.append(System.lineSeparator());
				}
				i = i + 1 ;
				line = br.readLine();
			}
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}

	public void setInput(Object data) {
		viewer.setInput(data);
	}

	private void createProgElemColumns(final Composite parent, final TableViewer viewer) {
		String[] titles = { "Package name", "Class name", "Method Name", "Edit Operation", "Edit Label",
				"ChangedEntity Type", "ChangedEntity Name", "Source Range Line" };
		int[] bounds = { 100, 100, 100, 100, 100, 100, 100, 100};

		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getPkgName();
			}
		});

		col = createTableViewerColumn(titles[1], bounds[1], 1);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getClassName();
			}
		});

		col = createTableViewerColumn(titles[2], bounds[2], 2);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getMethodName();
			}
		});

		col = createTableViewerColumn(titles[3], bounds[3], 3);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getEditOperation();
			}
		});

		col = createTableViewerColumn(titles[4], bounds[4], 4);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getEditLabel();
			}
		});

		col = createTableViewerColumn(titles[5], bounds[5], 5);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return String.valueOf(p.getChangedEntityType());
			}
		});

		col = createTableViewerColumn(titles[6], bounds[6], 6);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getChangedEntityName();
			}
		});

		col = createTableViewerColumn(titles[7], bounds[7], 7);
		col.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ChangeDistillerElement p = (ChangeDistillerElement) element;
				return p.getSourceRangeLine();
			}
		});
	}

	private TableViewerColumn createTableViewerColumn(String title, int bound, final int colNumber) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	public void clear() {
		ChangeDistillerProgramElement.INSTANCE.clearProgramElements();;
	}

	public void oldFileData(String str) {
		oldData.setText(str);
	}

	public void newFileData(String str) {
		newData.setText(str);
	}

	public void setText(String str) {
		this.styledText.setText(str);
	}

	public void appendText(String str) {
		this.styledText.append(str);
	}
}