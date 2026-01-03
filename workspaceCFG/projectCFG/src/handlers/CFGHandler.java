
package handlers;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import analysis.ProjectAnalyzer;

public class CFGHandler {
   @Execute
   public void execute(EPartService epartService) throws CoreException {
      ProjectAnalyzer analyzer = new ProjectAnalyzer();
      analyzer.analyze();
   }
}