package view.progelement;

import org.eclipse.jface.viewers.ITreeContentProvider;

import model.ProgramElement;
import view.CryptoMisuseDetection;

public class ContentProviderProgElem implements ITreeContentProvider {
   public void inputChanged(CryptoMisuseDetection v, Object oldInput, Object newInput) {
   }

   @Override
   public void dispose() {
   }

   @Override
   public Object[] getElements(Object inputElement) {
      return (ProgramElement[]) inputElement;
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      ProgramElement p = (ProgramElement) parentElement;
      return p.list();
   }

   @Override
   public Object getParent(Object element) {
      ProgramElement p = (ProgramElement) element;
      return p.getParent();
   }

   @Override
   public boolean hasChildren(Object element) {
      ProgramElement p = (ProgramElement) element;
      return p.hasChildren();
   }
}
