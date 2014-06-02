package ecs240.views;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

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
	private Button button0;
	private Button button1;
	private Button button2;
	private Button button3;
	private Button button4;
	private String policySelected;
	private String appSelected;
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
	private PyreticThread pyreticthread;
	private MininetThread mininetthread;
	ModelChangeEventListener modelListener;

	/**
	 * The constructor.
	 */
	public TopologyEditorView() {
		isDrawingEdge = false;
		model = Activator.getModel();
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

		sourceList = new List(sourceArea, SWT.SINGLE | SWT.BORDER
				| SWT.V_SCROLL);
		sourceList.setItems(new String[] { Utility.SWITCH, Utility.HOST });
		sourceList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 1));
		sourceSection.setClient(sourceArea);

		button0 = toolkit.createButton(sourceArea, "Load Topo", SWT.BORDER);
		button0.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button0.addSelectionListener(new LoadTopoButtonSelection());

		button1 = toolkit.createButton(sourceArea, "Choose Policy", SWT.BORDER);
		button1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button1.addSelectionListener(new PolicyButtonSelection());

		button2 = toolkit.createButton(sourceArea, "Choose Application",
				SWT.BORDER);
		button2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button2.addSelectionListener(new ApplicationyButtonSelection());

		button3 = toolkit.createButton(sourceArea, "Run", SWT.BORDER);
		button3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button3.addSelectionListener(new RunButtonSelection());

		button4 = toolkit.createButton(sourceArea, "Stop", SWT.BORDER);
		button4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		button4.addSelectionListener(new StopButtonSelection());

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
				reDraw(gc);
			}
		});
		targetArea.addKeyListener(new NetworkNodeKeyListener());

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
			focusingLabel = null;
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

	class NetworkNodeKeyListener implements KeyListener {
		public void keyPressed(KeyEvent e) {
			if (e.character == SWT.DEL) {
				if (focusingLabel != null) {
					model.deleteNode(focusingLabel.getText());
					focusingLabel.dispose();
					focusingLabel = null;
				}
			}
		}

		public void keyReleased(KeyEvent e) {

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
			switch (e.eventType) {
			case Utility.EVENT_NEW_NODE:
			case Utility.EVENT_UPDATE_NODE:
			case Utility.EVENT_RELOAD:
			case Utility.EVENT_DEL_NODE:
			case Utility.EVENT_NEW_EDGE: {
				for (Control control : targetArea.getChildren()) {
					control.dispose();
				}
				targetArea.redraw();
				break;
			}
			// if (e.data instanceof Node) {
			// Node nd = (Node) e.data;
			// // System.out.println("nd, id:" + nd.getNodeID() + ", xy:"
			// // + nd.getNodeCoordinates());
			// Image image;
			// image = getImageByKey(nd.getNodeID());
			// Point pt = nd.getNodeCoordinates();
			// if (image != null) {
			// Label label = new Label(targetArea, SWT.NONE);
			//
			// FormData data = new FormData();
			// data.top = new FormAttachment(targetArea, pt.y, SWT.TOP);
			// data.left = new FormAttachment(targetArea, pt.x,
			// SWT.LEFT);
			// label.setLayoutData(data);
			// label.setText(nd.getNodeID());
			// label.setImage(image);
			// targetArea.layout(true);
			//
			// DragSource dgSrc = new DragSource(label, DND.DROP_MOVE);
			// dgSrc.setTransfer(new Transfer[] { textTransfer });
			// dgSrc.addDragListener(new NetworkNodeDragListener());
			// label.addMouseListener(new NetworkNodeMouseListener());
			// label.addKeyListener(new NetworkNodeKeyListener());
			// targetArea.redraw();
			// }
			// }
			// break;
			// }

			// case Utility.EVENT_NEW_EDGE: {
			// if (e.data instanceof Edge) {
			// Edge edge = (Edge) e.data;
			// gc = new GC(targetArea, SWT.NONE);
			// drawLine(gc, edge.getStartPoint(), edge.getEndPoint());
			// targetArea.redraw();
			// gc.dispose();
			// }
			// break;
			// }
			// case Utility.EVENT_DEL_NODE: {
			// targetArea.redraw();
			// break;
			// }
			// case Utility.EVENT_RELOAD: {
			// System.out.println("reload");
			// for (Control control : targetArea.getChildren()) {
			// System.out.println(control);
			// control.dispose();
			// }
			// targetArea.redraw();
			// break;
			// }
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
		} else if (key.startsWith(Utility.HOST)) {
			image = Activator.getImage(Utility.HOST);
		} else if (key.startsWith(Utility.CONTROLLER)) {
			image = Activator.getImage(Utility.CONTROLLER);
		} else {
			// System.out.println("Wrong key, return");
			return null;
		}
		return image;
	}

	public void drawLine(GC gc, Point start, Point end) {
		if (gc == null || start == null || end == null) {
			return;
		}
		gc.drawLine(start.x + 15, start.y + 15, end.x + 15, end.y + 15);
	}

	public void reDraw(GC gc) {
		Hashtable<String, Node> nds = model.getNodes();
		for (Iterator<String> it = nds.keySet().iterator(); it.hasNext();) {
			Node nd = (Node) nds.get(it.next());
			Image image;
			image = getImageByKey(nd.getNodeID());
			Point pt = nd.getNodeCoordinates();
			if (image != null) {
				Label label = new Label(targetArea, SWT.NONE);

				FormData data = new FormData();
				data.top = new FormAttachment(targetArea, pt.y, SWT.TOP);
				data.left = new FormAttachment(targetArea, pt.x, SWT.LEFT);
				label.setLayoutData(data);
				label.setText(nd.getNodeID());
				label.setImage(image);
				targetArea.layout(true);

				DragSource dgSrc = new DragSource(label, DND.DROP_MOVE);
				dgSrc.setTransfer(new Transfer[] { textTransfer });
				dgSrc.addDragListener(new NetworkNodeDragListener());
				label.addMouseListener(new NetworkNodeMouseListener());
				label.addKeyListener(new NetworkNodeKeyListener());
				// targetArea.redraw();
			}
		}
		ArrayList<Edge> edges = model.getEdges();
		for (Edge e : edges) {
			drawLine(gc, e.getStartPoint(), e.getEndPoint());
		}
	}

	public class LoadTopoButtonSelection implements SelectionListener {
		public void widgetSelected(SelectionEvent event) {
			String loc = Activator.getDefault().getBundle().getLocation();
			String dir = loc.substring(loc.lastIndexOf(':') + 1)
					+ "src/ecs240/views";
			FileDialog dialog = new FileDialog(sourceArea.getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String[] { "*.txt" });
			dialog.setFilterPath(dir);
			String str = dialog.open();
			model.loadFromFile(str);
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			this.widgetSelected(event);
		}
	}

	public class PolicyButtonSelection implements SelectionListener {

		public void widgetSelected(SelectionEvent event) {
			FileDialog dialog = new FileDialog(sourceArea.getShell(), SWT.OPEN);
			policySelected = null;
			dialog.setFilterExtensions(new String[] { "*.py" });
			dialog.setFilterPath(System.getenv("HOME")
					+ "/pyretic/pyretic/modules");
			dialog.open();
			String str = dialog.getFileName();
			if (str.endsWith(".py")) {
				policySelected = "pyretic.modules."
						+ str.substring(0, str.length() - 3);
				System.out.println(policySelected);
			}
		}

		public void widgetDefaultSelected(SelectionEvent event) {
			this.widgetSelected(event);
		}

	}

	public class ApplicationyButtonSelection implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent event) {
			this.widgetSelected(event);
		}

		public void widgetSelected(SelectionEvent arg0) {
			AppSelectionDialog dialog = new AppSelectionDialog();

			if (dialog.open() == 1)
				appSelected = dialog.getApp();
			System.out.println("appselected " + appSelected);
		}
	}

	public class RunButtonSelection implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent event) {
			this.widgetSelected(event);
		}

		public void widgetSelected(SelectionEvent arg0) {
			model.dumpToFile();
			try {
				if (policySelected != null) {
					System.out.println("******Starting Pyretics");
					Thread pythread;
					pyreticthread = new PyreticThread(policySelected);
					pythread = new Thread(pyreticthread, "PyreticThread");
					pythread.start();
				}
				System.out.println("*****Starting Mininet");
				Thread mnthread;
				if (appSelected != null) {
					mininetthread = new MininetThread(appSelected);
				} else {
					mininetthread = new MininetThread();
				}
				mnthread = new Thread(mininetthread, "MininetThread");
				mnthread.start();
				button3.setEnabled(false);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public class StopButtonSelection implements SelectionListener {
		public void widgetDefaultSelected(SelectionEvent event) {
			this.widgetSelected(event);
		}

		public void widgetSelected(SelectionEvent arg0) {
			try {
				// if (pyreticthread != null) {
				// System.out.println("****** Stopping Pyretic");
				// pyreticthread.stop();
				// }
				if (mininetthread != null) {
					System.out.println("****** Stopping Mininet");
					mininetthread.stop();
				}
				button3.setEnabled(true);
			} catch (Exception e) {

			}
		}
	}

}
