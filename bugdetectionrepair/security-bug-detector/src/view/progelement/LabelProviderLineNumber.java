package view.progelement;

import org.eclipse.jface.viewers.StyledString;

import model.MethodElement;

public class LabelProviderLineNumber extends LabelProviderProgElem {

    @Override
    public StyledString getStyledText(Object element) {
        if (element instanceof MethodElement) {
            MethodElement me = (MethodElement) element;
            int lineNum = me.getLineNumber();
            if (lineNum > 0) {
                String line = String.valueOf(lineNum);
                return new StyledString(line);
            }
        }
        return new StyledString(""); // super.getStyledText(element);
    }
}
