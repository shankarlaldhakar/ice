/*******************************************************************************
 * Copyright (c) 2016 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.javafx.mesh.datatypes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.ice.viz.service.modeling.AbstractController;
import org.eclipse.ice.viz.service.modeling.AbstractMesh;
import org.eclipse.ice.viz.service.modeling.AbstractView;
import org.eclipse.ice.viz.service.modeling.VertexMesh;
import org.junit.Test;

/**
 * A class to test the functionality of the FXVertexView
 * 
 * @author Robert Smith
 *
 */
public class FXVertexControllerTester {

	/**
	 * Checks that the controller correctly triggers refreshes in its view
	 */
	@Test
	public void checkRefresh() {

		// Create a vertex
		VertexMesh mesh = new VertexMesh();
		TestVertexView view = new TestVertexView(mesh);
		FXVertexController vertex = new FXVertexController(mesh, view);

		// Reset the view's refreshed state
		view.wasRefreshed();

		// The view should be refreshed when a property is changed
		mesh.setProperty("Test", "Property");
		assertTrue(view.wasRefreshed());

		// The view should be refreshed when the part is selected
		mesh.setProperty("Selected", "True");
		assertTrue(view.wasRefreshed());

		// The view should not be refreshed when a child is added
		mesh.addEntity(
				new AbstractController(new AbstractMesh(), new AbstractView()));
		assertFalse(view.wasRefreshed());
	}

	/**
	 * An extension of FXVertexView which keeps track of whether it has been
	 * refreshed for testing purposes.
	 * 
	 * @author Robert Smith
	 *
	 */
	private class TestVertexView extends FXVertexView {

		/**
		 * Whether the view has been refreshed since the last time it was
		 * checked.
		 */
		boolean refreshed = false;

		/**
		 * The default constructor.
		 * 
		 * @param model
		 *            The model that this view will represent.
		 */
		public TestVertexView(VertexMesh model) {
			super(model);
		}

		/**
		 * Check if the view was refreshed, then return the view to its original
		 * unrefreshed state.
		 * 
		 * @return True if refresh() was called since the last time this
		 *         function was invoked. Otherwise, false.
		 */
		public boolean wasRefreshed() {
			boolean temp = refreshed;
			refreshed = false;
			return temp;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ice.viz.service.javafx.mesh.datatypes.FXVertexView#
		 * refresh(org.eclipse.ice.viz.service.modeling.AbstractMesh)
		 */
		@Override
		public void refresh(AbstractMesh model) {
			refreshed = true;
		}

	}
}
