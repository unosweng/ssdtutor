package view.provider;

import org.eclipse.jface.viewers.ITreeContentProvider; 

import model.ModelIndicator;

public class ProviderContentIndicator implements ITreeContentProvider {

   @Override
   public void dispose() {
   }

   @Override
   public Object[] getElements(Object inputElement) {
      return (ModelIndicator[]) inputElement;
   }

   @Override
   public Object[] getChildren(Object parentElement) {
      ModelIndicator p = (ModelIndicator) parentElement;
      return p.list();
   }

   @Override
   public Object getParent(Object element) {
      ModelIndicator p = (ModelIndicator) element;
      return p.getParent();
   }

   @Override
   public boolean hasChildren(Object element) {
      ModelIndicator p = (ModelIndicator) element;
      return p.hasChildren();
   }
}
