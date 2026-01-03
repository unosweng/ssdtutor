package view.progelement;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import model.MethodElement;

public class LabelProviderLineNumberTable extends ColumnLabelProvider {
//  protected String getCellText(ViewerCell cell) {
//        Person person = (Person) cell.getElement();
//        String cellText = person.getLastName();
//        return cellText;
//     }
    @Override
    public String getText(Object element) {
        MethodElement p = (MethodElement) element;
        System.out.println("here");
        System.out.println(p.getLineNumber());
        return p.getMethodName();
    }
}