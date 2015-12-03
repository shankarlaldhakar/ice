/*******************************************************************************
 * Copyright (c) 2015 UT-Battelle, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tony McCrary (tmccrary@l33tlabs.com), Robert Smith
 *******************************************************************************/
package org.eclipse.ice.viz.service.mesh.javafx;

import java.text.DecimalFormat;
import java.util.ArrayList;

import org.eclipse.ice.viz.service.geometry.GeometrySelection;
import org.eclipse.ice.viz.service.geometry.scene.base.GeometryAttachment;
import org.eclipse.ice.viz.service.geometry.scene.base.ICamera;
import org.eclipse.ice.viz.service.geometry.viewer.GeometryViewer;
import org.eclipse.ice.viz.service.javafx.internal.FXContentProvider;
import org.eclipse.ice.viz.service.javafx.internal.model.FXCameraAttachment;
import org.eclipse.ice.viz.service.javafx.internal.model.FXRenderer;
import org.eclipse.ice.viz.service.javafx.internal.scene.TransformGizmo;
import org.eclipse.ice.viz.service.javafx.internal.scene.camera.CameraController;
import org.eclipse.ice.viz.service.javafx.internal.scene.camera.TopDownController;
import org.eclipse.ice.viz.service.mesh.datastructures.FXMeshControllerFactory;
import org.eclipse.ice.viz.service.mesh.datastructures.NekPolygon;
import org.eclipse.ice.viz.service.mesh.datastructures.NekPolygonComponent;
import org.eclipse.ice.viz.service.modeling.AbstractController;
import org.eclipse.ice.viz.service.modeling.AbstractMeshComponent;
import org.eclipse.ice.viz.service.modeling.AbstractView;
import org.eclipse.ice.viz.service.modeling.Edge;
import org.eclipse.ice.viz.service.modeling.FaceEdgeComponent;
import org.eclipse.ice.viz.service.modeling.Vertex;
import org.eclipse.ice.viz.service.modeling.VertexComponent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import javafx.embed.swt.FXCanvas;
import javafx.event.EventHandler;
import javafx.scene.AmbientLight;
import javafx.scene.Camera;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;

/**
 * <p>
 * JavaFX implementation of GeometryViewer.
 * </p>
 * 
 * @author Tony McCrary (tmccrary@l33tlabs.com), Robert Smith
 *
 */
public class FXMeshViewer extends GeometryViewer {

	/** The root JavaFX widget that displays content. */
	private FXCanvas fxCanvas;

	/**
	 * The internally used root that cannot be modified by clients.
	 */
	private Group internalRoot;

	/** The root of the scene as exposed to clients. */
	private Group root;

	/** The active scene displayed to the end user. */
	private Scene scene;

	/**
	 * The content provider that generates JavaFX scene data from the geometry
	 * editor scene model.
	 */
	private FXContentProvider contentProvider;

	/** Default camera controller. */
	private CameraController cameraController;

	/** Default camera. */
	private Camera defaultCamera;

	/**
	 * A handler which places new polygons on the screen based on mouse clicks.
	 */
	private EventHandler<MouseEvent> addHandler;

	/**
	 * A handler which allows the user to select vertices with the mouse.
	 */
	private EventHandler<MouseEvent> editHandler;

	/**
	 * A handler which allows the user to drag selected vertices, having their
	 * movements displayed on the screen and their final position update once
	 * the drag ends
	 */
	private EventHandler<MouseEvent> editDragHandler;

	/**
	 * A handler which moves vertices at the end of a drag action in edit mode
	 */
	private EventHandler<MouseEvent> editMouseUpHandler;

	/**
	 * A list of vertices currently selected by the user, because they were
	 * selected in edit mode or were input vertices which have not yet been
	 * formed into a complete polygon in add mode.
	 */
	private ArrayList<AbstractController> selectedVertices;

	/**
	 * A list of edges input by the user which have not yet been formed into a
	 * complete polygon
	 */
	private ArrayList<AbstractController> tempEdges;

	/**
	 * The factory responsible for creating views/controllers for new model
	 * components.
	 */
	private FXMeshControllerFactory factory;

	/**
	 * The mouse's last recorded x position
	 */
	private double mouseOldX;

	/**
	 * The mouse's last recorded y position.
	 */
	private double mouseOldY;

	/**
	 * The mouse's current x position
	 */
	private double mousePosX;

	/**
	 * The mouse's current y position.
	 */
	private double mousePosY;

	/**
	 * A JavaFX Text shape displaying the mouse cursor's current x and y
	 * coordinates.
	 */
	private Text cursorPosition;

	/**
	 * A root model part in which temporary vertices and edges will be held
	 * before being added to the mesh permanently. These are maintained
	 * separately so that such parts will not appear in the tree view until
	 * their parent polygon is completed.
	 */
	private AbstractController tempRoot = new AbstractController(
			new AbstractMeshComponent(), new AbstractView());

	/**
	 * A list of displayed circles to show the user the location that selectice
	 * vertices are being dragged to.
	 */
	private ArrayList<Sphere> vertexMarkers;

	/**
	 * The gizmo containing the axis.
	 */
	private TransformGizmo gizmo;

	/**
	 * The manager for attachments to the renderer
	 */
	private FXMeshAttachmentManager attachmentManager;

	/**
	 * Whether or not a drag mouse motion is in progress.
	 */
	boolean dragStarted = false;

	/**
	 * An ordered list of each selected vertex's x coordinate relative to the
	 * vertex being dragged
	 */
	ArrayList<Double> relativeXCords = new ArrayList<Double>();

	/**
	 * An ordered list of each selected vertex's y coordinate relative to the
	 * vertex being dragged
	 */
	ArrayList<Double> relativeYCords = new ArrayList<Double>();

	/**
	 * The next unused number to assign as a Vertex's ID.
	 */
	int nextVertexID = 1;

	/**
	 * The next unused number to assign as an Edge's ID.
	 */
	int nextEdgeID = 1;

	/**
	 * The next unused number to assign as a Polygon's ID.
	 */
	int nextPolygonID = 1;

	/**
	 * <p>
	 * Creates a JavaFX GeometryViewer.
	 * </p>
	 * 
	 * @param parent
	 */
	public FXMeshViewer(Composite parent) {
		super(parent);

		// Initialize the class variables
		renderer = new FXRenderer();

		attachmentManager = new FXMeshAttachmentManager();
		renderer.register(GeometryAttachment.class, attachmentManager);

		factory = new FXMeshControllerFactory();
		selectedVertices = new ArrayList<AbstractController>();
		tempEdges = new ArrayList<AbstractController>();
		vertexMarkers = new ArrayList<Sphere>();

		// Create the handler for add mode
		addHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				// Get the user's selection
				PickResult pickResult = event.getPickResult();
				Node intersectedNode = pickResult.getIntersectedNode();

				// Whether or not a new vertex has been added
				boolean changed = false;

				// If the user didn't select a shape, add a new shape where they
				// clicked
				if (intersectedNode instanceof Box) {

					// Create a new vertex at that point
					VertexComponent tempComponent = new VertexComponent(
							event.getX(), event.getY(), 0);
					tempComponent.setProperty("Constructing", "True");
					Vertex tempVertex = (Vertex) factory
							.createController(tempComponent);

					// Set the vertex's name and ID
					tempVertex.setProperty("Name", "Vertex");
					tempVertex.setProperty("Id", String.valueOf(nextVertexID));
					nextVertexID++;

					// Add the new vertex to the list
					selectedVertices.add(tempVertex);

					// Add it to the temp root
					tempRoot.addEntity(tempVertex);

					// Add the temp root to the attachment
					attachmentManager.getAttachments().get(0)
							.addGeometry(tempRoot);

					tempVertex.refresh();
					changed = true;
				}

				// If the user clicked a shape, try to add it to a polygon
				else if (intersectedNode instanceof Shape3D) {

					// Resolve the parent
					Group nodeParent = (Group) intersectedNode.getParent();

					// Resolve the shape
					AbstractController modelShape = (AbstractController) nodeParent
							.getProperties().get(AbstractController.class);

					// If the vertex is already in the polygon currently being
					// constructed, ignore it
					if (selectedVertices.contains(modelShape)) {
						return;
					}

					// If the selected shape is a vertex, add it to the list
					if (modelShape instanceof Vertex) {
						selectedVertices.add(modelShape);
						changed = true;

						// Change the vertex's color to show that it is part of
						// the new polygon
						modelShape.setProperty("Constructing", "True");
					}

				}

				// If a new vertex was added, then construct edges/polygons as
				// needed
				if (changed) {

					// The number of vertices in the polygon under construction
					int numVertices = selectedVertices.size();

					// If this is not the first vertex, create an edge between
					// it and the last one
					if (numVertices > 1) {
						FaceEdgeComponent tempComponent = new FaceEdgeComponent(
								(Vertex) selectedVertices.get(numVertices - 2),
								(Vertex) selectedVertices.get(numVertices - 1));
						tempComponent.setProperty("Constructing", "True");
						Edge tempEdge = (Edge) factory
								.createController(tempComponent);

						// Set the edge's name and ID
						tempEdge.setProperty("Name", "Edge");
						tempEdge.setProperty("Id", String.valueOf(nextEdgeID));
						nextEdgeID++;

						// Set the mouse to ignore edges. Only Vertices and
						// empty space may be selected.
						((Group) tempEdge.getRepresentation())
								.setMouseTransparent(true);

						// Add the edge to the list
						tempEdges.add(tempEdge);

						// Add it to the temp root
						tempRoot.addEntity(tempEdge);

						// Refresh the edge
						tempEdge.refresh();
					}

					// If this was the fourth vertex, the quadrilateral is done
					// so finish up the polygon
					if (numVertices == 4) {

						// Create an edge between the last vertex and the first
						FaceEdgeComponent tempComponent = new FaceEdgeComponent(
								(Vertex) selectedVertices.get(numVertices - 1),
								(Vertex) selectedVertices.get(0));
						tempComponent.setProperty("Constructing", "True");
						Edge tempEdge = (Edge) factory
								.createController(tempComponent);

						// Set the edge's name and ID
						tempEdge.setProperty("Name", "Edge");
						tempEdge.setProperty("Id", String.valueOf(nextEdgeID));
						nextEdgeID++;

						tempEdges.add(tempEdge);

						// Create a face out of all the edges
						NekPolygonComponent faceComponent = new NekPolygonComponent();
						NekPolygon newFace = (NekPolygon) factory
								.createController(faceComponent);

						// Set the polygon's name and ID
						newFace.setProperty("Name", "Polygon");
						newFace.setProperty("Id",
								String.valueOf(nextPolygonID));
						nextPolygonID++;

						for (AbstractController edge : tempEdges) {
							newFace.addEntity(edge);

							// Remove the edge from the temporary root
							tempRoot.removeEntity(edge);
						}

						// Remove the vertices from the temporary root
						for (AbstractController vertex : selectedVertices) {
							tempRoot.removeEntity(vertex);
						}

						// Set the new polygon to the default color
						newFace.setProperty("Constructing", "False");

						// Add the new polygon to the mesh permanently
						attachmentManager.getAttachments().get(0).getRoot()
								.addEntity(newFace);

						// Empty the lists of temporary constructs
						selectedVertices = new ArrayList<AbstractController>();
						tempEdges = new ArrayList<AbstractController>();

					}
				}
			}
		};

		// Start with the add mode by default
		scene.setOnMouseClicked(addHandler);

		// Create the handler for edit mode
		editHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				// Get the mouse position
				mousePosX = event.getSceneX();
				mousePosY = event.getSceneY();
				mouseOldX = event.getSceneX();
				mouseOldY = event.getSceneY();

				// Get the user's selection
				PickResult pickResult = event.getPickResult();
				Node intersectedNode = pickResult.getIntersectedNode();

				if (intersectedNode instanceof Shape3D) {
					// Resolve the parent
					Group nodeParent = (Group) intersectedNode.getParent();

					// Resolve the shape
					AbstractController modelShape = (AbstractController) nodeParent
							.getProperties().get(AbstractController.class);

					// If the user clicked a vertex, handle it
					if (modelShape instanceof Vertex) {

						// If shift is down, add the vertex to the selection
						if (event.isShiftDown()) {
							selectedVertices.add(modelShape);
							modelShape.setProperty("Selected", "True");
						}

						// If shift is not down and control is, either add the
						// vertex to the selection if it is not present already
						// or remove it if it is.
						else if (event.isControlDown()) {
							if (selectedVertices.contains(modelShape)) {
								selectedVertices.remove(modelShape);
								modelShape.setProperty("Selected", "False");
							}

							else {
								selectedVertices.add(modelShape);
								modelShape.setProperty("Selected", "True");
							}
						}

						// If nothing is pressed, select that vertex and nothing
						// else
						else {
							for (AbstractController vertex : selectedVertices) {
								vertex.setProperty("Selected", "False");
							}
							selectedVertices.clear();

							selectedVertices.add(modelShape);
							modelShape.setProperty("Selected", "True");
						}
					}
				}
			}
		};

		editDragHandler = new EventHandler<MouseEvent>() {

			// The marker the user is dragging with the mouse
			Sphere dragMarker;

			@Override
			public void handle(MouseEvent event) {

				// Get the mouse position
				mouseOldX = mousePosX;
				mouseOldY = mousePosY;
				mousePosX = event.getX();
				mousePosY = event.getY();

				// Get the user's selection
				PickResult pickResult = event.getPickResult();
				Node intersectedNode = pickResult.getIntersectedNode();

				// If the user is not dragging a shape, ignore the motion
				if (intersectedNode instanceof Shape3D || dragStarted) {

					// The drag has started, so continue dragging even if the
					// mouse has moved off a shape
					dragStarted = true;

					// Resolve the parent
					Group nodeParent = (Group) intersectedNode.getParent();

					// Resolve the shape
					AbstractController modelShape = (AbstractController) nodeParent
							.getProperties().get(AbstractController.class);

					// If the user has selected a vertex, drag it
					if (selectedVertices.contains(modelShape) || dragStarted) {

						// If the vertex markers have not yet been made,
						// create them
						if (vertexMarkers.isEmpty()) {

							// Get the location of the vertex which was clicked
							double[] cursorLocation = ((Vertex) modelShape)
									.getTranslation();

							for (AbstractController vertex : selectedVertices) {

								// Create the circle
								Sphere marker = new Sphere(1);
								// marker.setScaleZ(.25d);

								// Place it at the vertex's position
								double[] position = ((Vertex) vertex)
										.getTranslation();
								marker.setTranslateX(position[0]);
								marker.setTranslateY(position[1]);

								// Add it to the list
								vertexMarkers.add(marker);

								// Get the relative position of this vertex from
								// the vertex being dragged
								relativeXCords
										.add(position[0] - cursorLocation[0]);
								relativeYCords
										.add(position[1] - cursorLocation[1]);

								// If this is the vertex on which the user
								// started the drag, its marker will be the
								// target for the drag action
								if (vertex == modelShape) {
									dragMarker = marker;
								}

								attachmentManager.getAttachments().get(0)
										.getFxNode().getChildren().add(marker);

							}
						}

						// Move each vertex
						for (int i = 0; i < vertexMarkers.size(); i++) {

							// Get the vertex marker for this index
							Sphere marker = vertexMarkers.get(i);

							// Move the vertex to the mouse's current
							// position, offset by the original distance
							// between the vertices.
							marker.setTranslateX(
									relativeXCords.get(i) + mousePosX);
							marker.setTranslateY(
									relativeYCords.get(i) + mousePosY);
						}

					}

				}

			};

		};

		editMouseUpHandler = new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				// Move the selected vertices at the end of a drag, ignoring
				// other clicks
				if (dragStarted) {
					dragStarted = false;

					// Get the mouse position
					mouseOldX = mousePosX;
					mouseOldY = mousePosY;
					mousePosX = event.getX();
					mousePosY = event.getY();

					for (int i = 0; i < selectedVertices.size(); i++) {

						// Get the vertex
						Vertex vertex = (Vertex) selectedVertices.get(i);

						// Update its position
						vertex.updateLocation(relativeXCords.get(i) + mousePosX,
								relativeYCords.get(i) + mousePosY, 0);

						// Remove the markers from the scene
						for (Sphere marker : vertexMarkers) {
							attachmentManager.getAttachments().get(0)
									.getFxNode().getChildren().remove(marker);
						}

					}

					// Empty the lists of markers and coordinates
					vertexMarkers.clear();
					relativeXCords.clear();
					relativeYCords.clear();
				}

			}

		};

		scene.setOnMouseMoved(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {

				DecimalFormat format = new DecimalFormat("#.##");
				cursorPosition.setText(
						"Cursor position (x,y): (" + format.format(me.getX())
								+ "," + format.format(me.getY()) + ")");
				cursorPosition.setTranslateZ(-5);

				// cursorPosition.setTranslateX(-0.1 * scene.getX());
				// cursorPosition.setTranslateY(0.1 * scene.getY());

				// cursorPosition.getTransforms()
				// .setAll(defaultCamera.getTransforms());
				// cursorPosition
				// .setTranslateZ(cursorPosition.getTranslateX() - 5);

			}
		});
	}

	/**
	 * <p>
	 * Creates an FXCanvas control and initializes a default empty JavaFX scene.
	 * </p>
	 */
	@Override
	protected void createControl(Composite parent) {
		contentProvider = new FXContentProvider();

		fxCanvas = new FXCanvas(parent, SWT.NONE);

		// Create the root nodes
		internalRoot = new Group();
		root = new Group();

		internalRoot.getChildren().add(root);

		setupSceneInternals(internalRoot);

		// Add the HUD text controls to the scene
		cursorPosition = new Text();
		// root.getChildren().add(cursorPosition);

		scene = new Scene(internalRoot, 100, 100, true);

		// PerspectiveCamera hudCam = new PerspectiveCamera();
		// hudCam.setTranslateZ(-100);
		//
		// Pane pane = new Pane();
		//
		// Label label = new Label();
		// label.setText("hello world");
		// pane.getChildren().add(label);

		// Group hudRoot = new Group();
		// HUD = new SubScene(hudRoot, 100, 100, true,
		// SceneAntialiasing.BALANCED);
		// HUD.setFill(Color.TRANSPARENT);
		// hudRoot.getChildren().add(cursorPosition);
		// HUD.setCamera(hudCam);
		// internalRoot.getChildren().add(pane);

		// Set the scene's background color
		scene.setFill(Color.rgb(24, 30, 31));

		// Setup camera and input
		createDefaultCamera(internalRoot);
		wireSelectionHandling();

		// Get the current key handler from the camera
		final EventHandler<? super KeyEvent> handler = scene.getOnKeyPressed();

		// Set a handler for clearing the current selection
		scene.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {

				// If Escape is pressed, any polygon under construction will be
				// removed
				if (event.getCode() == KeyCode.ESCAPE) {

					clearSelection();
				}

				// If another key was pressed, invoke the camera's key handler
				else {
					handler.handle(event);
				}
			}
		});

		fxCanvas.setScene(scene);
	}

	/**
	 * <p>
	 * Creates the current geometry editor camera.
	 * </p>
	 * 
	 * @param parent
	 *            the parent to create the camera on
	 * 
	 */
	private void createDefaultCamera(Group parent) {
		PerspectiveCamera perspCamera = new PerspectiveCamera(true);
		perspCamera.setNearClip(0.1);
		perspCamera.setFarClip(4000.0);
		perspCamera.setFieldOfView(35);
		perspCamera.setTranslateX(0);
		perspCamera.setTranslateY(0);

		parent.getChildren().add(perspCamera);

		// Hacked in camera (for now)
		FXCameraAttachment cameraAttachment = new FXCameraAttachment(
				perspCamera);
		setCamera(cameraAttachment);
	}

	/**
	 * <p>
	 * Hooks up JavaFX picking with JFace selections.
	 * </p>
	 */
	private void wireSelectionHandling() {

		scene.setOnMousePressed(new EventHandler<MouseEvent>() {

			Group lastSelection = null;

			@Override
			public void handle(MouseEvent event) {
				// Pick
				PickResult pickResult = event.getPickResult();

				Node intersectedNode = pickResult.getIntersectedNode();

				if (intersectedNode == null) {
					return;
				}

				if (!(intersectedNode instanceof Shape3D)) {
					return;
				}

				// Resolve the parent
				Group nodeParent = (Group) intersectedNode.getParent();

				if (nodeParent == lastSelection) {
					return;
				}

				// Resolve the shape
				AbstractController modelShape = (AbstractController) nodeParent
						.getProperties().get(AbstractController.class);

				if (modelShape == null) {
					return;
				}

				// Create and set the viewer selection
				// (event gets fired in parent class)
				GeometrySelection selection = new GeometrySelection(modelShape);

				setSelection(selection);

				// nodeParent.setSelected(true);

				// if (lastSelection != null) {
				// lastSelection.setSelected(false);
				// }

				lastSelection = nodeParent;
			}
		});

	}

	/**
	 * <p>
	 * Creates scene elements that aren't meant to be manipulated by the user
	 * (markers, camera, etc.)
	 * </p>
	 */
	private void setupSceneInternals(Group parent) {
		// Create scene plane for frame of reference.
		Box box = new Box(1000, 0, 1000);
		box.setMouseTransparent(true);
		box.setDrawMode(DrawMode.LINE);
		box.setMaterial(new PhongMaterial(Color.ANTIQUEWHITE));

		AmbientLight ambientLight = new AmbientLight(Color.rgb(100, 100, 100));

		PointLight light1 = new PointLight(Color.ANTIQUEWHITE);
		light1.setMouseTransparent(true);
		light1.setTranslateY(-350);

		PointLight light2 = new PointLight(Color.ANTIQUEWHITE);
		light2.setMouseTransparent(true);
		light2.setTranslateZ(350);

		PointLight light3 = new PointLight(Color.ANTIQUEWHITE);
		light3.setMouseTransparent(true);
		light3.setTranslateZ(-350);

		PointLight light4 = new PointLight(Color.ANTIQUEWHITE);
		light4.setMouseTransparent(true);
		light4.setTranslateZ(350);

		gizmo = new TransformGizmo(1000);
		gizmo.showHandles(false);

		parent.getChildren().addAll(gizmo, box, light1, light2, light3, light4,
				ambientLight);

	}

	/**
	 * <p>
	 * Handles recreating the scene when the input changes.
	 * </p>
	 * 
	 * @see Viewer#inputChanged(Object, Object)
	 */
	@Override
	protected void inputChanged(Object oldInput, Object newInput) {
		contentProvider.inputChanged(this, newInput, input);
	}

	/**
	 * @see GeometryViewer#updateCamera(ICamera)
	 */
	@Override
	protected void updateCamera(ICamera camera) {
		if (!(camera instanceof FXCameraAttachment)) {
			throw new IllegalArgumentException(
					"Invalid camera attached to Mesh Viewer.");
		}

		FXCameraAttachment attachment = (FXCameraAttachment) camera;
		Camera fxCamera = attachment.getFxCamera();

		if (fxCamera == null) {
			throw new NullPointerException(
					"No camera was attached to Mesh Viewer");
		}

		cameraController = new TopDownController(fxCamera, scene, fxCanvas);

		scene.setCamera(fxCamera);

		defaultCamera = fxCamera;

		// ((TopDownController) cameraController).fixToCamera(cursorPosition);

	}

	/**
	 * @see Viewer#getClass()
	 */
	@Override
	public Control getControl() {
		return fxCanvas;
	}

	/**
	 * @see Viewer#refresh()
	 */
	@Override
	public void refresh() {

	}

	/**
	 * 
	 * @return
	 */
	public FXCanvas getFxCanvas() {
		return fxCanvas;
	}

	/**
	 * 
	 * @return
	 */
	@Override
	public Group getRoot() {
		return root;
	}

	/**
	 * 
	 * @return
	 */
	public Scene getScene() {
		return scene;
	}

	/**
	 * 
	 * @return
	 */
	public FXContentProvider getContentProvider() {
		return contentProvider;
	}

	/**
	 * 
	 * @param contentProvider
	 */
	public void setContentProvider(FXContentProvider contentProvider) {
		this.contentProvider = contentProvider;
	}

	public CameraController getCameraController() {
		return cameraController;
	}

	public Camera getDefaultCamera() {
		return defaultCamera;
	}

	/**
	 * Provide a handler defining the editor's behavior upon mouse use to the
	 * viewer.
	 * 
	 * @param handler
	 *            The EventHandler which will process mouse input for this
	 *            viewer.
	 */
	public void setEditSelectionHandeling(boolean edit) {

		// If the user is switching to edit mode, register the edit handlers
		// with the scene
		if (edit) {
			scene.setOnMouseClicked(editHandler);
			scene.setOnMouseDragged(editDragHandler);
			scene.setOnMouseReleased(editMouseUpHandler);

		} else {

			// If the user is switching to add mode, register the add handler
			// and remove the edit drag handler, as add mode has no
			// functionality for mouse drag
			scene.setOnMouseClicked(addHandler);
			scene.setOnMouseDragged(null);
			scene.setOnMouseReleased(editMouseUpHandler);
		}

		// Don't maintain selections between different modes
		clearSelection();
	}

	/**
	 * Sets the viewer's HUD, which displays the camera center and mouse cursor
	 * positions, to be visible or invisible.
	 * 
	 * @param visible
	 *            Whether or not the viewer should display the HUD.
	 */
	public void setHUDVisible(boolean visible) {
		cursorPosition.setVisible(visible);
	}

	/**
	 * Checks whether the viewer's HUD is visible.
	 * 
	 * @return True if the HUD is being displayed, false if it is not.
	 */
	public boolean isHUDVisible() {
		return cursorPosition.isVisible();
	}

	/**
	 * Sets the editor's axis display's visibility.
	 * 
	 * @param visible
	 *            Whether or not the editor should display its axis.
	 */
	public void setAxisVisible(boolean visible) {
		gizmo.setVisible(visible);
	}

	/**
	 * Checks whether the viewer has visible axis.
	 * 
	 * @return True if the axis are displayed in the viewer, false otherwise
	 */
	public boolean getAxisVisible() {
		return gizmo.isVisible();
	}

	/**
	 * Remove the selected property from all selected parts and reset the
	 * selection lists
	 */
	private void clearSelection() {

		// Remove the temporary vertices from the scene
		for (AbstractController vertex : selectedVertices) {
			tempRoot.removeEntity(vertex);
			vertex.setProperty("Selected", "False");
			vertex.setProperty("Constructing", "False");
			vertex.refresh();
		}

		// Remove the temporary edges from the scene and set them to
		// be unselected
		for (AbstractController edge : tempEdges) {
			tempRoot.removeEntity(edge);
			edge.setProperty("Selected", "False");
		}

		// Empty the lists
		selectedVertices.clear();
		tempEdges.clear();
	}

}