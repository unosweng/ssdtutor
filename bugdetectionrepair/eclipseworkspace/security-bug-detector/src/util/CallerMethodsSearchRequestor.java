/**
 */
package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchRequestor;

/**
 * @since JDK1.8
 */
public class CallerMethodsSearchRequestor extends SearchRequestor {
    private List<IMethod> callerMethods = null;

    public CallerMethodsSearchRequestor() {
        super();
        this.callerMethods = new ArrayList<IMethod>();
    }

    public List<IMethod> getCallerMethods() {
        return this.callerMethods;
    }

    public List<IMethod> getUniqueCallerMethods() {
        List<IMethod> newCallerMethods = new ArrayList<>();
        Set<IMethod> uniqueCallerMethods = new HashSet<IMethod>(this.callerMethods);
        newCallerMethods.addAll(uniqueCallerMethods);
        return newCallerMethods;
    }

    @Override
    public void acceptSearchMatch(SearchMatch match) throws CoreException {
        if (match instanceof MethodReferenceMatch) {
            MethodReferenceMatch methodMatch = (MethodReferenceMatch) match;
            if (methodMatch.getElement() instanceof IMethod) { // do not regard BinaryType objects here
                IMethod element = (IMethod) methodMatch.getElement();
                this.callerMethods.add(element);
            }
        }
    }
}