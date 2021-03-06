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
package org.eclipse.nebula.widgets.nattable.resize.command;

import org.eclipse.nebula.widgets.nattable.command.AbstractLayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;

public class MultiColumnResizeCommandHandler extends AbstractLayerCommandHandler<MultiColumnResizeCommand> {

	private final DataLayer dataLayer;

	public MultiColumnResizeCommandHandler(DataLayer dataLayer) {
		this.dataLayer = dataLayer;
	}
	
	public Class<MultiColumnResizeCommand> getCommandClass() {
		return MultiColumnResizeCommand.class;
	}

	@Override
	protected boolean doCommand(MultiColumnResizeCommand command) {
		for (int columnPosition : command.getColumnPositions()) {
			dataLayer.setColumnWidthByPosition(columnPosition, command.getColumnWidth(columnPosition));
		}
		return true;
	}

}
