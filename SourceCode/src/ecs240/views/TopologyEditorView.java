package ecs240.views;

import java.util.ArrayList;

import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.*;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;

import ecs240.Activator;
import ecs240.datas.Model;
import ecs240.datas.Edge;
import ecs240.datas.Model.ModelChangeEvent;
import ecs240.datas.Node;
import ecs240.datas.Utility;

public class TopologyEditorView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ecs240.views.TopologyEditorView";

	private Display display;
	private FormToolkit toolkit;
	private Form form;

	private Section sourceSection;
	private Composite sourceArea;
	private List sourceList;

	private Section targetSection;
	private Composite targetArea;
	private GC gc;

	private DragSource source;
	private DropTarget target;
	private TextTransfer textTransfer;

	private Label focusingLabel;
	private boolean isDrawingEdge;
	private Label edgeStartLabel;
	private Label edgeEndLabel;
	private Point startPt;
	private Point endPt;
	private Model model;
	ModelChangeEventListener modelListener;

	/**
	 * The constructor.
	 */
	public TopologyEditorView() {
		isDrawingEdge = false;
		model = new Model();
		modelListener = new ModelChangeEventListener();
		model.addModelListener(modelListener);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		display = parent.getDisplay();
		toolkit = new FormToolkit(display);

		// create the underlying form layout
		form = toolkit.createForm(parent);
		GridLayout formlayout = new GridLayout();
		formlayout.numColumns = 2;
		form.getBody().setLayout(formlayout);

		// create source area layout
		sourceSection = toolkit
				.createSection(form.getBody(), Section.TITLE_BAR);
		sourceSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				true));
		sourceSection.setText("Network Component");
		sourceArea = toolkit.createComposite(sourceSection, SWT.BORDER);
		sourceArea.setLayout(new GridLayout());

		// create source list
		sourceList = new List(sourceArea, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);
		sourceList.setItems(new String[] { Utility.SWITCH, Utility.SERVER,
				Utility.CLIENT });
		sourceList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		sourceSection.setClient(sourceArea);

		// create target area layout
		targetSection = toolkit
				.createSection(form.getBody(), Section.TITLE_BAR);
		targetSection
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		targetSection.setText("Topology");
		targetArea = toolkit.createComposite(targetSection, SWT.BORDER);
		targetArea.setLayout(new FormLayout());
		targetSection.setClient(targetArea);
		targetArea.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent e) {
				gc = e.gc;
				redrawLines(gc);
			}
		});

		// add mouse listener
		sourceList.addMouseListener(new SourceListMouseListener());

		// define drag and drop operations for dragList and target area.
		textTransfer = TextTransfer.getInstance();
		// define drag operation in for dragList
		source = new DragSource(sourceList, DND.DROP_COPY);
		source.setTransfer(new Transfer[] { textTransfer });
		source.addDragListener(new SourceListDragListener());// add defined drag
																// listener
		// define drop operation in target area
		target = new DropTarget(targetSection, DND.DROP_COPY | DND.DROP_MOVE
				| DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { textTransfer });
		target.addDropListener(new TargetAreaDropListener());// add drop
																// listener
	}

	class SourceListDragListener implements DragSourceListener {
		public void dragStart(DragSourceEvent event) {
			String selection = sourceList.getSelection()[0];
			if (selection.length() == 0) {
				event.doit = false;
			}
			if (event.detail == DND.DROP_DEFAULT) {
				event.detail = DND.DROP_COPY;
			}
		}

		public void dragSetData(DragSourceEvent event) {
			if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
				String selection = sourceList.getSelection()[0];
				if (selection.length() == 0) {
					event.doit = false;
				} else {
					event.data = selection;
				}
			}
		}

		public void dragFinished(DragSourceEvent event) {

		}
	}

	class SourceListMouseListener implements MouseListener {
		public void mouseDoubleClick(MouseEvent e) {
			// double click on the list item, create a instance on the topology.
			String itemSelected = sourceList.getSelection()[0];
			model.insertNode(itemSelected, 10, 10);
		}

		public void mouseDown(MouseEvent e) {
		}

		public void mouseUp(MouseEvent e) {
		}
	}

	class NetworkNodeDragListener implements DragSourceListener {
		NetworkNodeDragListener() {
		}

		public void dragStart(DragSourceEvent event) {
			if (event.detail == DND.DROP_DEFAULT
					|| event.detail == DND.DROP_NONE) {
				event.detail = DND.DROP_MOVE;
			}
		}

		public void dragSetData(DragSourceEvent event) {
			if (textTransfer.isSupportedType(event.dataType)) {
				if (focusingLabel != null)
					event.data = ((Label) focusingLabel).getText();
			}
		}

		public void dragFinished(DragSourceEvent event) {
			focusingLabel.dispose();
		}
	}

	class NetworkNodeMouseListener implements MouseListener {
		public void mouseDoubleClick(MouseEvent e) {
			if (e.widget instanceof Label) {
				focusingLabel = (Label) e.widget;
				AttributeEditingDialog dialog = new AttributeEditingDialog(
						focusingLabel.getText());
				dialog.open();
			}
		}

		public void mouseDown(MouseEvent e) {
			if (e.widget instanceof Label) {
				focusingLabel = (Label) e.widget;
				if (e.button == 3) {// right button start draw edge
					if (!isDrawingEdge) {
						edgeStartLabel = focusingLabel;
						startPt = display.map(focusingLabel, targetArea, e.x,
								e.y);
						isDrawingEdge = true;
					} else if (edgeStartLabel != focusingLabel) {
						edgeEndLabel = focusingLabel;
						endPt = display
								.map(focusingLabel, targetArea, e.x, e.y);
						model.insertEdge(edgeStartLabel.getText(),
								edgeEndLabel.getText(), startPt, endPt);
						isDrawingEdge = false;
					}

				}
			}
		}

		public void mouseUp(MouseEvent e) {
		}
	}

	class TargetAreaDropListener implements DropTargetListener {
		public void dragEnter(DropTargetEvent event) {
			if (event.detail == DND.DROP_DEFAULT) {
				if ((event.operations & DND.DROP_COPY) != 0) {
					event.detail = DND.DROP_COPY;
				} else if ((event.operations & DND.DROP_MOVE) != 0) {
					event.detail = DND.DROP_MOVE;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
			for (int i = 0; i < event.dataTypes.length; i++) {
				if (textTransfer.isSupportedType(event.dataTypes[i])) {
					event.currentDataType = event.dataTypes[i];
					// 只允许COPY or MOVE
					if (event.detail != DND.DROP_COPY
							&& event.detail != DND.DROP_MOVE) {
						event.detail = DND.DROP_NONE;
					}
					break;
				}
			}
		}

		public void dragOver(DropTargetEvent event) {
			event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
		}

		public void dragOperationChanged(DropTargetEvent event) {
			if (event.detail == DND.DROP_DEFAULT) {
				if ((event.operations & DND.DROP_COPY) != 0) {
					event.detail = DND.DROP_COPY;
				} else if ((event.operations & DND.DROP_MOVE) != 0) {
					event.detail = DND.DROP_MOVE;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}
		}

		public void dragLeave(DropTargetEvent event) {

		}

		public void dropAccept(DropTargetEvent event) {

		}

		public void drop(DropTargetEvent event) {
			if (textTransfer.isSupportedType(event.currentDataType)) {
				String string = (String) (event.data);

				Point pt = display.map(null, targetArea, event.x, event.y);
				model.insertNode(string, pt.x, pt.y);
			}

		}
	}

	public class ModelChangeEventListener implements Listener {

		public void handleEvent(Event event) {
			ModelChangeEvent e = (ModelChangeEvent) event;
			System.out.println("ModelChangeEventListener,e:" + e.eventType);
			switch (e.eventType) {
			case Utility.EVENT_NEW_NODE:
			case Utility.EVENT_UPDATE_NODE: {
				if (e.data instanceof Node) {
					Node nd = (Node) e.data;
					System.out.println("nd, id:" + nd.getNodeID() + ", xy:"
							+ nd.getNodeCoordinates());
					Image image;
					image = getImageByKey(nd.getNodeID());
					Point pt = nd.getNodeCoordinates();
					if (image != null) {
						Label label = new Label(targetArea, SWT.NONE);

						FormData data = new FormData();
						data.top = new FormAttachment(targetArea, pt.y, SWT.TOP);
						data.left = new FormAttachment(targetArea, pt.x,
								SWT.LEFT);
						label.setLayoutData(data);
						label.setText(nd.getNodeID());
						label.setImage(image);
						targetArea.layout(true);

						DragSource dgSrc = new DragSource(label, DND.DROP_MOVE);
						dgSrc.setTransfer(new Transfer[] { textTransfer });
						dgSrc.addDragListener(new NetworkNodeDragListener());
						label.addMouseListener(new NetworkNodeMouseListener());
					}
				}
				break;
			}

			case Utility.EVENT_NEW_EDGE: {
				if (e.data instanceof Edge) {
					Edge edge = (Edge) e.data;
					gc = new GC(targetArea, SWT.NONE);
					drawLine(gc, edge.getStartPoint(), edge.getEndPoint());
					targetArea.redraw();
					gc.dispose();
				}
				break;
			}
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {

	}

	public Image getImageByKey(String key) {
		Image image;
		if (key.length() == 0) {
			return null;
		}
		if (key.startsWith(Utility.SWITCH)) {
			image = Activator.getImage(Utility.SWITCH);
		} else if (key.startsWith(Utility.SERVER)) {
			image = Activator.getImage(Utility.SERVER);
		} else if (key.startsWith(Utility.CLIENT)) {
			image = Activator.getImage(Utility.CLIENT);
		} else {
			System.out.println("Wrong key, return");
			return null;
		}
		return image;
	}

	public void drawLine(GC gc, Point start, Point end) {
		if (gc == null || start == null || end == null) {
			return;
		}
		gc.drawLine(start.x, start.y, end.x, end.y);
	}

	public void redrawLines(GC gc) {
		ArrayList<Edge> edges = model.getEdges();
		for (Edge e : edges) {
			drawLine(gc, e.getStartPoint(), e.getEndPoint());

		}
	}
}