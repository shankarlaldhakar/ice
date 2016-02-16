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
package org.eclipse.ice.viz.service.javafx.geometry.plant.test;

import static org.junit.Assert.assertTrue;

import org.eclipse.ice.viz.service.geometry.reactor.PipeMesh;
import org.eclipse.ice.viz.service.geometry.reactor.ReactorController;
import org.eclipse.ice.viz.service.geometry.reactor.ReactorMesh;
import org.eclipse.ice.viz.service.javafx.geometry.plant.FXPipeController;
import org.eclipse.ice.viz.service.javafx.geometry.plant.FXPipeView;
import org.eclipse.ice.viz.service.javafx.geometry.plant.FXReactorView;
import org.junit.Test;

import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;

/**
 * A class to test the functionality of the FXReactorView
 * 
 * @author Robert Smith
 *
 */
public class FXReactorViewTester {

	/**
	 * Check that the view sets its shapes to the proper drawmode for
	 * wireframes.
	 */
	@Test
	public void checkWireFrame() {

		// Create a reactor
		ReactorMesh mesh = new ReactorMesh();
		ReactorController reactor = new ReactorController(mesh,
				new FXReactorView(mesh));

		// Create a pipe
		PipeMesh pipeMesh = new PipeMesh();
		pipeMesh.setLength(100);
		pipeMesh.setInnerRadius(5);
		pipeMesh.setRadius(5);
		pipeMesh.setAxialSamples(3);
		pipeMesh.setProperty("Core Channel", "True");
		FXPipeView pipeView = new FXPipeView(pipeMesh);
		FXPipeController pipe = new FXPipeController(pipeMesh, pipeView);

		// Add the pipe to the reactor
		reactor.addEntityByCategory(pipe, "Core Channels");

		// Get the four shapes that make up the reactor's representation
		ObservableList<Node> children = ((Group) ((Group) reactor
				.getRepresentation()).getChildren().get(0)).getChildren();
		assertTrue(children.size() == 4);

		// Check that each shape is drawn normally
		for (Node child : children) {
			if (child instanceof Shape3D) {
				assertTrue(((Shape3D) child).getDrawMode() == DrawMode.FILL);
			} else {
				assertTrue(((MeshView) child).getDrawMode() == DrawMode.FILL);
			}
		}

		// Set the reactor to draw in wireframe mode
		reactor.setWireFrameMode(true);

		// Get the current list of children
		children = ((Group) ((Group) reactor.getRepresentation()).getChildren()
				.get(0)).getChildren();
		assertTrue(children.size() == 4);

		// Check that the children are drawn in wireframe mode
		for (Node child : children) {
			if (child instanceof Shape3D) {
				assertTrue(((Shape3D) child).getDrawMode() == DrawMode.LINE);
			} else {
				assertTrue(((MeshView) child).getDrawMode() == DrawMode.LINE);
			}
		}

		// Set the reactor back to normal mode
		reactor.setWireFrameMode(false);

		// Get the current list of children
		children = ((Group) ((Group) reactor.getRepresentation()).getChildren()
				.get(0)).getChildren();
		assertTrue(children.size() == 4);

		// Check that the children are drawn normally again
		for (Node child : children) {
			if (child instanceof Shape3D) {
				assertTrue(((Shape3D) child).getDrawMode() == DrawMode.FILL);
			} else {
				assertTrue(((MeshView) child).getDrawMode() == DrawMode.FILL);
			}
		}

	}
}
