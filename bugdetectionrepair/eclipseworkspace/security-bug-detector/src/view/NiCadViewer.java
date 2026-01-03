 
package view;

import javax.inject.Inject;
import javax.annotation.PostConstruct;

import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public class NiCadViewer {
	public static final String VIEW_ID = "security-bug-detector.partdescriptor.nicadview";
	public final static String POPUPMENU = "security-bug-detector.popupmenu.nicadmenu";

	private StyledText styledText = null;
	
	Browser browser;
	@Inject
	EPartService epartService;
	
	@Inject
	public NiCadViewer() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		
	}
	
	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService) {
		browser = new Browser(parent, SWT.NONE);
		menuService.registerContextMenu(browser, POPUPMENU);
	}


	public void setText(String str) {
		browser.setText(str);
	}

	public void appendText(String str) {
		this.styledText.append(str);
	}
	
	public void clear() {
		browser.setText("");
	}
}