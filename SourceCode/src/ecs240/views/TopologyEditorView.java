package ecs240.views;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import org.eclipse.swt.dnd.*;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;

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
	private List dragList;
	private String itemSelected;

	private Section targetSection;
	private Composite targetArea;

	private DragSource source;
	private DropTarget target;
	private TextTransfer textTransfer;
	private FileTransfer fileTransfer;

	private Label anchorLabel;
	private Point mousePt;

	/**
	 * The constructor.
	 */
	public TopologyEditorView() {

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		display = parent.getDisplay();
		toolkit = new FormToolkit(display);

		itemSelected = new String();
		form = toolkit.createForm(parent);
		GridLayout formlayout = new GridLayout();
		formlayout.numColumns = 2;
		form.getBody().setLayout(formlayout);

		// create source area
		sourceSection = toolkit
				.createSection(form.getBody(), Section.TITLE_BAR);
		sourceSection.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				true));
		sourceSection.setText("Network Component");

		sourceArea = toolkit.createComposite(sourceSection, SWT.BORDER);
		sourceArea.setLayout(new GridLayout());

		dragList = new List(sourceArea, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
		dragList.setItems(new String[] { "Switch", "Router", "Host" });
		dragList.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
				1));
		sourceSection.setClient(sourceArea);

		// create target area
		targetSection = toolkit
				.createSection(form.getBody(), Section.TITLE_BAR);
		targetSection
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		targetSection.setText("Topology");

		targetArea = toolkit.createComposite(targetSection, SWT.BORDER);
		targetArea.setLayout(new FormLayout());
		// toolkit.createLabel(targetArea, "target");
		targetSection.setClient(targetArea);

		// add double click listener to dragList
		{
			dragList.addMouseListener(new MouseListener() {
				public void mouseDoubleClick(MouseEvent e) {
					itemSelected = dragList.getSelection()[0];
					
					Label label = new Label(targetArea, SWT.NONE);
					
					FormData data = new FormData();
					data.top = new FormAttachment(anchorLabel, 10,
							SWT.TOP);
					data.left = new FormAttachment(anchorLabel, 10,
							SWT.LEFT);
					label.setLayoutData(data);

					Image image = new Image(targetArea.getDisplay(),
							"/home/yixin/workspace/ECS240/icons/BMPIcon.jpg");

					label.setImage(image);
					targetArea.layout(true);
				}

				public void mouseDown(MouseEvent e) {

				}

				public void mouseUp(MouseEvent e) {

				}
			});
		}

		textTransfer = TextTransfer.getInstance();

		// define drag operation for source area
		source = new DragSource(dragList, DND.DROP_COPY);
		source.setTransfer(new Transfer[] { textTransfer });

		// define drop operation in target area
		target = new DropTarget(targetSection, DND.DROP_COPY | DND.DROP_DEFAULT);
		target.setTransfer(new Transfer[] { textTransfer });

		// define drag listener
		source.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				String selection = dragList.getSelection()[0];
				if (selection.length() == 0) {
					event.doit = false;
				}
			}

			public void dragSetData(DragSourceEvent event) {
				if (TextTransfer.getInstance().isSupportedType(event.dataType)) {
					String selection = dragList.getSelection()[0];
					if (selection.length() == 0) {
						event.doit = false;
					} else {
						event.data = selection;
					}

				}
			}

			public void dragFinished(DragSourceEvent event) {

			}
		});

		// define drop listener
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					// 给event.detail赋的值必须是event.operations中的一个,event.operations中
					// 的操作都是DragSource所支持的.
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
				for (int i = 0; i < event.dataTypes.length; i++) {
					if (textTransfer.isSupportedType(event.dataTypes[i])) {
						event.currentDataType = event.dataTypes[i];
						// 只允许COPY
						if (event.detail != DND.DROP_COPY) {
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
					event.detail = DND.DROP_COPY;
				} else {
					event.detail = DND.DROP_NONE;
				}
			}

			public void dragLeave(DropTargetEvent event) {

			}

			public void dropAccept(DropTargetEvent event) {

			}

			public void drop(DropTargetEvent event) {
				if (textTransfer.isSupportedType(event.currentDataType)) {
					if (((String) event.data).length() != 0) {
						Label label = new Label(targetArea, SWT.NONE);

						Point pt = display.map(null, targetArea, event.x,
								event.y);
						
						FormData data = new FormData();
						data.top = new FormAttachment(anchorLabel, pt.y,
								SWT.TOP);
						data.left = new FormAttachment(anchorLabel, pt.x,
								SWT.LEFT);
						label.setLayoutData(data);

						Image image = new Image(targetArea.getDisplay(),
								"/home/yixin/workspace/ECS240/icons/BMPIcon.jpg");

						label.setImage(image);
						targetArea.layout(true);
					}
				}
			}

		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {

	}
}