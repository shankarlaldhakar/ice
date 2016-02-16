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
import org.eclipse.ice.viz.service.modeling.EdgeMesh;
import org.junit.Test;

/**
 * A class for testing the functionality of the FXEdgeController
 * 
 * @author Robert Smith
 *
 */
public class FXEdgeControllerTester {

	/**
	 * Check that the controller propagates properties changes to its children
	 * correctly.
	 */
	@Test
	public void checkProperties() {

		// Create an edge for testing
		EdgeMesh mesh = new EdgeMesh();
		FXEdgeController edge = new FXEdgeController(mesh, new AbstractView());

		// Give a child entity to the edge
		AbstractController child = new AbstractController(new AbstractMesh(),
				new AbstractView());
		edge.addEntityByCategory(child, "Vertices");

		// Set the Constructing property. This change should be mirrored in the
		// edge's children.
		edge.setProperty("Constructing", "True");
		assertTrue("True".equals(child.getProperty("Constructing")));

		// Set the Selected property. This changed should be mirrored in the
		// edge's children
		edge.setProperty("Selected", "True");
		assertTrue("True".equals(child.getProperty("Selected")));

		// Set a different property. This change should not be reflected in the
		// child
		edge.setProperty("Test", "Property");
		assertFalse("Property".equals(child.getProperty("Test")));
	}
}
