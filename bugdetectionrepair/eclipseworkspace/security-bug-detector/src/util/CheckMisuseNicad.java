package util;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import view.CryptoMisuseDetectionTree;

public class CheckMisuseNicad {
	public CheckMisuseNicad() {
		// TODO Auto-generated constructor stub
	}

	@Execute
	public String execute(EPartService eService) {
		MPart findPart1 = eService.findPart(CryptoMisuseDetectionTree.VIEW_ID);
		Object findPartObj1 = findPart1.getObject();		

		if (findPartObj1 instanceof CryptoMisuseDetectionTree) {
			CryptoMisuseDetectionTree v1 = (CryptoMisuseDetectionTree) findPartObj1;
			String treedata = v1.getMisusePattern();
			System.out.println(treedata);
			if(treedata.toLowerCase().contains("cipher")) {
				return "Pattern1";
			}
			if(treedata.toLowerCase().contains("messagedigest")) {
				return "Pattern2";
			}
			if(treedata.toLowerCase().contains("secretkeyspec")) {
				return "Pattern3";
			}
			if(treedata.toLowerCase().contains("pbeparameterspec")) {
				return "Pattern4";
			}
			if(treedata.toLowerCase().contains("securerandom")) {
				return "Pattern5";
			}
			if(treedata.toLowerCase().contains("keygenerator")) {
				return "Pattern6";
			}
			if(treedata.toLowerCase().contains("secretkeyfactory")) {
				return "Pattern7";
			}
			if(treedata.toLowerCase().contains("ivparameterspec")) {
				return "Pattern8";
			}
		}
		return "";
	}
}
