/*
 * @(#) ZestLabelProvider.java
 *
 */
package graph.provider;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Color;
import org.eclipse.zest.core.viewers.EntityConnectionData;
import org.eclipse.zest.core.viewers.IEntityConnectionStyleProvider;
import org.eclipse.zest.core.viewers.IEntityStyleProvider;
import org.eclipse.zest.core.widgets.ZestStyles;

import graph.builder.GModelBuilder;
import graph.model.GNode;
import graph.model.node.GIndicatorNode;
import graph.model.node.GPathNode;
import graph.model.node.GRootNode;

public class GLabelProvider extends LabelProvider implements IEntityStyleProvider, IEntityConnectionStyleProvider {
	@Override
	public String getText(Object element) {
		// Create a label for node.
		if (element instanceof GNode) {
			GNode myNode = (GNode) element;
			return myNode.getName();
		}
		// Create a label for connection.
		if (element instanceof EntityConnectionData) {
			EntityConnectionData eCon = (EntityConnectionData) element;
			if (eCon.source instanceof GNode) {
				return GModelBuilder.instance().getConnectionLabel( //
						((GNode) eCon.source).getId(), //
						((GNode) eCon.dest).getId());
			}
		}
		return "";
	}

	@Override
	public Color getBackgroundColour(Object o) {
		return getNodeColor(o);
	}

	private Color getNodeColor(Object o) {
		if (o instanceof GPathNode) {
			return ColorConstants.darkGreen;
		}
		if (o instanceof GRootNode) {
			return ColorConstants.darkBlue;
		}
		if (o instanceof GIndicatorNode) {
			return ColorConstants.red;
		}
		return ColorConstants.blue;
	}

	@Override
	public int getConnectionStyle(Object src, Object dest) {
		if (src instanceof GPathNode && dest instanceof GPathNode) {
			return ZestStyles.CONNECTIONS_SOLID;
		}
		return ZestStyles.CONNECTIONS_DOT;
	}

	@Override
	public Color getNodeHighlightColor(Object entity) {
		return ColorConstants.darkGray;
	}
	
	@Override
	public Color getBorderColor(Object entity) {
		return null;
	}

	@Override
	public Color getBorderHighlightColor(Object entity) {
		return ColorConstants.red;
	}

	@Override
	public int getBorderWidth(Object entity) {
		return 0;
	}
	
	@Override
	public Color getColor(Object src, Object dest) {
		return ColorConstants.black;
	}
	
	@Override
	public Color getHighlightColor(Object src, Object dest) {
		return ColorConstants.black;
	}

	public int getLineWidth(Object src, Object dest) {
		return 0;
	}

	@Override
	public Color getForegroundColour(Object entity) {
		return ColorConstants.white;
	}

	@Override
	public IFigure getTooltip(Object entity) {
		return null;
	}

	@Override
	public boolean fisheyeNode(Object entity) {
		return false;
	}
}
