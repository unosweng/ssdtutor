
package handlers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import view.Diff2htmlViewer;
import view.CryptoMisuseDetectionTree;

public class Diff2HtmlLineHandler {
	List<String> files = new ArrayList<String>();
	private String nodeLocation = "/opt/homebrew/bin/node";
	private String diff2htmlLocation = "/opt/homebrew/bin/diff2html";
	private String PATH_SEPERATOR = "/";
	@Inject
	private EPartService epartService;
	@Execute
	public void execute() {
		MPart findPart = epartService.findPart(Diff2htmlViewer.VIEW_ID);
		Object findPartObj = findPart.getObject();

		MPart findPart1 = epartService.findPart(CryptoMisuseDetectionTree.VIEW_ID);
		Object findPartObj1 = findPart1.getObject();		

		if (findPartObj1 instanceof CryptoMisuseDetectionTree) {
			CryptoMisuseDetectionTree v1 = (CryptoMisuseDetectionTree) findPartObj1;
			files = v1.getTableData();
		}
		if (findPartObj instanceof Diff2htmlViewer) {
			Diff2htmlViewer v = (Diff2htmlViewer) findPartObj;
			String oldFile = files.get(0);
			String newFile = files.get(1);
			String subfiles = files.get(2);
			String fileLocation = subfiles.substring(1);
			fileLocation = fileLocation.replace("/", ".");
			String htmlDirPath = files.get(3)+ PATH_SEPERATOR + "html";
			File htmlDir = new File(htmlDirPath);
			if (!htmlDir.exists()) {
				htmlDir.mkdirs();
			}
			fileLocation = fileLocation.replace("java", "html");
			String htmlFilePath = htmlDirPath + PATH_SEPERATOR + fileLocation;
			createHTML(oldFile, newFile, htmlFilePath, v);
		}
	}

	public void createHTML(String oldFile, String newFile, String htmlFilePath, Diff2htmlViewer v) {
		// TODO Auto-generated method stub
		List<String> parameters = new ArrayList<String>();
		parameters.add(nodeLocation);
		parameters.add(diff2htmlLocation);
		parameters.add("-s");
		parameters.add("line");
		parameters.add(oldFile);
		parameters.add(newFile);
		parameters.add("-F");
		parameters.add(htmlFilePath);
		try {
			Process pb = new ProcessBuilder(parameters).start();
			pb.waitFor();
			FileReader fr = new FileReader(htmlFilePath);
			BufferedReader br= new BufferedReader(fr);
			StringBuilder htmlContent=new StringBuilder();
			String s;
			while((s=br.readLine())!=null)
			{
				htmlContent.append(s);
			}
			v.setText(htmlContent.toString());
			br.close();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
