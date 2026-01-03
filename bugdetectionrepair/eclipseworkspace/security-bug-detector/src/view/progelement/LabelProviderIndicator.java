package view.progelement;

import org.eclipse.jface.viewers.StyledString;

import model.MethodElement;

public class LabelProviderIndicator extends LabelProviderProgElem {

    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof MethodElement) {
            MethodElement me = (MethodElement) element;
            MethodElement.Indicator ind = me.getIndicator();
            if (ind != null) {
                return new StyledString(ind.name());
            }
        }
        return new StyledString(""); // super.getStyledText(element);
    }
}
