/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.layer.stack;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;


public class DefaultBodyLayerStack extends AbstractIndexLayerTransform {

	private final ColumnReorderLayer columnReorderLayer;
	private final ColumnHideShowLayer columnHideShowLayer;
	private final SelectionLayer selectionLayer;
	private final ViewportLayer viewportLayer;

	public DefaultBodyLayerStack(IUniqueIndexLayer underlyingLayer) {
		columnReorderLayer = new ColumnReorderLayer(underlyingLayer);
		columnHideShowLayer = new ColumnHideShowLayer(columnReorderLayer);
		selectionLayer = new SelectionLayer(columnHideShowLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		setUnderlyingLayer(viewportLayer);

		registerCommandHandler(new CopyDataCommandHandler(selectionLayer));
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public ColumnReorderLayer getColumnReorderLayer() {
		return columnReorderLayer;
	}

	public ColumnHideShowLayer getColumnHideShowLayer() {
		return columnHideShowLayer;
	}

	public SelectionLayer getSelectionLayer() {
		return selectionLayer;
	}

	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

}
