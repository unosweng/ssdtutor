package view.progelement;

import org.eclipse.jface.viewers.ITreeContentProvider;

import model.MethodElement;
import model.ProgramElement;
import view.CryptoMisuseDetectionTree;

public class ContentProviderTree implements ITreeContentProvider {
	public void inputChanged(CryptoMisuseDetectionTree v, Object oldInput, Object newInput) {
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
		if ((p instanceof ProgramElement) && !(p instanceof MethodElement)) {
			return p.list();
		}
		return null;
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
