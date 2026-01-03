package handlers;

import java.io.IOException;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import view.NiCadViewer;

public class NiCadHandler {
	@Execute
	public void execute(EPartService eService) throws IOException {
		System.out.println("NiCad Handler!!!");
		MPart findPart = eService.findPart(NiCadViewer.VIEW_ID);
		if (findPart != null && findPart.getObject() instanceof NiCadViewer) {
			((NiCadViewer) findPart.getObject()).clear();
		}
	}
}
