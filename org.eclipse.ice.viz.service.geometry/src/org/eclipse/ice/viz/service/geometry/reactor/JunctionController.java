/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.geometry.reactor;

import org.eclipse.ice.viz.service.modeling.AbstractController;
import org.eclipse.ice.viz.service.modeling.AbstractView;
import org.eclipse.ice.viz.service.modeling.IWireFramePart;

/**
 * A Junction part for the Reactor Analyzer.
 * 
 * @author Robert Smith
 *
 */
public class JunctionController extends AbstractController
		implements IWireFramePart {

	/**
	 * The nullary constructor
	 */
	public JunctionController() {
		super();
	}

	/**
	 * The default constructor
	 * 
	 * @param model
	 * @param view
	 */
	public JunctionController(JunctionMesh model, AbstractView view) {
		super(model, view);
	}

	/**
	 * Gets the center of the box representing the Junction
	 * 
	 * @return An array of the coordinates of the junciton's center, in the
	 *         order x, y, z
	 */
	public double[] getCenter() {
		return ((JunctionView) view).getCenter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ice.viz.service.reactor.javafx.datatypes.WireFramePart#
	 * setWireFrameMode(boolean)
	 */
	@Override
	public void setWireFrameMode(boolean on) {
		((IWireFramePart) view).setWireFrameMode(on);
	}

}