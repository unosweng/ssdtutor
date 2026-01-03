package handlers;

import java.util.List;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import graph.model.GNode;
import view.CryptoMisuseDetectionTree;
import view.ZestViewer;
import graph.builder.GModelBuilder;

public class ZestViewHandler {
	@Execute
	public void execute(EPartService eService) {
		System.out.println("Graph Handler!!!");
		MPart findPart1 = eService.findPart(CryptoMisuseDetectionTree.VIEW_ID);
		Object findPartObj1 = findPart1.getObject();
		List<GNode> treeData = null;

		if (findPartObj1 instanceof CryptoMisuseDetectionTree) {
			CryptoMisuseDetectionTree v = (CryptoMisuseDetectionTree) findPartObj1;
			treeData = v.treeNodes();;
			GModelBuilder gModelBuilder = new GModelBuilder();
			gModelBuilder.setNodes();
		}
		MPart findPart = eService.findPart(ZestViewer.SIMPLEZESTVIEW);
		if (findPart != null && findPart.getObject() instanceof ZestViewer) {
			((ZestViewer) findPart.getObject()).clear();
			((ZestViewer) findPart.getObject()).update(treeData);
		}
	}
}