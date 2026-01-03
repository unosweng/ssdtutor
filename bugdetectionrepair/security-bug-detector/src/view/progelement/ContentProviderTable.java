package view.progelement;

import org.eclipse.jface.viewers.ITreeContentProvider;

import model.MethodElement;
import view.CryptoMisuseDetectionTree;

public class ContentProviderTable implements ITreeContentProvider {
    public void inputChanged(CryptoMisuseDetectionTree v, Object oldInput, Object newInput) {
    }

    @Override
    public void dispose() {
    }

    @Override
    public Object[] getElements(Object inputElement) {
        return (MethodElement[]) inputElement;
    }

    @Override
    public Object[] getChildren(Object parentElement) {
       MethodElement p = (MethodElement) parentElement;
        if ((p instanceof MethodElement) && !(p instanceof MethodElement)) {
            return p.list();
        }
        return null;
    }

    @Override
    public Object getParent(Object element) {
       MethodElement p = (MethodElement) element;
        return p.getParent();
    }

    @Override
    public boolean hasChildren(Object element) {
       MethodElement p = (MethodElement) element;
        return p.hasChildren();
    }
}
