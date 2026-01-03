
package handlers;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

public class RunBrowser {
   @Execute
   public void execute() {
      String URL_STR = "http://www.google.com/search?q=";
      IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
      IWebBrowser browser;
      try {
         browser = browserSupport.createBrowser("internalBrowser");
         browser.openURL(new URL(URL_STR));
      } catch (PartInitException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      } catch (MalformedURLException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

}