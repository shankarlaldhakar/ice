/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tony McCrary (tmccrary@l33tlabs.com)
 *******************************************************************************/
package org.eclipse.ice.viz.service.javafx.internal.scene.camera;

import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.geometry.Point3D;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Affine;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;

/**
 * <p>
 * ArcBallController provides 3D editor like features to a camera, by rotating
 * around a point and letting the user zoom in and out.
 * </p>
 */
public class FPSController extends CameraController {

	/** */
	Transform xform;

	/** */
	Affine transform;

	/** */
	private Camera camera;

	/** */
	private Scene scene;

	/** */
	double anchorX;

	/** */
	double anchorY;

	/** */
	double anchorAngle;

	/** */
	private double sphereRadius;

	/** */
	private double height;

	/** */
	private double width;

	/** */
	protected Point3D currentRot;

	/** */
	private Point3D startRot;

	/** */
	protected boolean activeRotation;

	/** */
	private FXCanvas canvas;

	private double mousePosX;

	private double mousePosY;

	private double mouseOldX;

	private double mouseOldY;

	private double mouseDeltaX;

	private double mouseDeltaY;

	private Affine affine;

	private final double NORMAL_SPEED = 60.0d;

	private final double FAST_SPEED = 120.0d;

	private double speed;

	/**
	 * <p>
	 * </p>
	 */
	public FPSController(Camera camera, Scene scene, FXCanvas canvas) {
		this.camera = camera;
		this.scene = scene;
		this.canvas = canvas;

		final Camera finalCamera = camera;

		final Group xform = (Group) camera.getParent();

		Rotate x = new Rotate();
		x.setAxis(Rotate.X_AXIS);

		Rotate y = new Rotate();
		y.setAxis(Rotate.Y_AXIS);

		Rotate z = new Rotate();
		z.setAxis(Rotate.Z_AXIS);

		final Group camGroup = new Group();
		camGroup.getTransforms().setAll(x, y, z);
		camGroup.getChildren().add(finalCamera);

		finalCamera.setTranslateZ(-2000);

		// affine = new Affine();
		// camera.getTransforms().setAll(affine);

		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {

				double speed = NORMAL_SPEED;

				if (event.isShiftDown()) {
					speed = FAST_SPEED;
				}

				KeyCode keyCode = event.getCode();

				Transform worldTransform = xform.getLocalToSceneTransform();
				double zx = worldTransform.getMzx();
				double zy = worldTransform.getMzy();
				double zz = worldTransform.getMzz();

				double xx = worldTransform.getMxx();
				double xy = worldTransform.getMxy();
				double xz = worldTransform.getMxz();

				Point3D zDir = new Point3D(zx, zy, zz).normalize();
				Point3D xDir = new Point3D(xx, xy, xz).normalize();

				if (keyCode == KeyCode.W) {
					Point3D moveVec = zDir.multiply(speed);
					affine.appendTranslation(moveVec.getX(), moveVec.getY(),
							moveVec.getZ());
				} else if (keyCode == KeyCode.S) {
					Point3D moveVec = zDir.multiply(speed);
					Point3D invVec = new Point3D(-moveVec.getX(),
							-moveVec.getY(), -moveVec.getZ());
					affine.appendTranslation(invVec.getX(), invVec.getY(),
							invVec.getZ());
				} else if (keyCode == KeyCode.A) {
					Point3D moveVec = xDir.multiply(speed);
					affine.appendTranslation(-moveVec.getX(), -moveVec.getY(),
							-moveVec.getZ());
				} else if (keyCode == KeyCode.D) {
					Point3D moveVec = xDir.multiply(speed);
					affine.appendTranslation(moveVec.getX(), moveVec.getY(),
							moveVec.getZ());
				}

				if (keyCode == KeyCode.SPACE) {
					finalCamera
							.setTranslateZ(finalCamera.getTranslateZ() - speed);
				} else if (keyCode == KeyCode.C) {
					finalCamera
							.setTranslateZ(finalCamera.getTranslateZ() + speed);
				}
			}
		});

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				mousePosX = arg0.getSceneX();
				mousePosY = arg0.getSceneY();
				mouseOldX = arg0.getSceneX();
				mouseOldY = arg0.getSceneY();
			}
		});

		scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent arg0) {
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = arg0.getSceneX();
				mousePosY = arg0.getSceneY();
				mouseDeltaX = (mousePosX - mouseOldX);
				mouseDeltaY = (mousePosY - mouseOldY);

				double modifier = 1.0;

				if (arg0.isPrimaryButtonDown()) {
					y.setAngle(y.getAngle() - mouseDeltaX);
					x.setAngle(x.getAngle() + mouseDeltaY);
				}
			}

		});

		// Set the bevaior for mouse scroll events
		scene.setOnScroll(new EventHandler<ScrollEvent>() {

			@Override
			public void handle(ScrollEvent event) {

				// Get the current z position and modify it by the amount of
				// scrolling
				double z = finalCamera.getTranslateZ();
				double newZ = z + event.getDeltaY();
				finalCamera.setTranslateZ(newZ);

			}

		});

	}

}