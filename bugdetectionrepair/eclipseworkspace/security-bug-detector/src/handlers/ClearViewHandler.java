/**
 */
package handlers;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import view.CryptoMisuseDetection;
import view.CryptoMisuseDetectionTree;

/**
 * @author
 * @date
 * @since J2SE-1.8
 */
public class ClearViewHandler {   
	@Execute
	public void execute(EPartService epartService) {
		if (epartService.findPart(CryptoMisuseDetection.VIEW_ID) != null) {
			Object findPartObj = epartService.findPart(CryptoMisuseDetection.VIEW_ID).getObject();

			if (findPartObj instanceof CryptoMisuseDetection) {
				CryptoMisuseDetection v = (CryptoMisuseDetection) findPartObj;
				v.setText("");
				v.clearTreeViewer();
			}
		}
		if (epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID) != null) {
			Object findPartObjTree = epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID).getObject();

			if (findPartObjTree instanceof CryptoMisuseDetectionTree) {
				CryptoMisuseDetectionTree v = (CryptoMisuseDetectionTree) findPartObjTree;
				v.clearTreeViewer();
			}
		}
	}

}