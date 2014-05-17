package ecs240.views;

import org.eclipse.swt.dnd.*;

import org.eclipse.ui.*;
import org.eclipse.ui.part.*;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

import org.eclipse.jface.viewers.*;
import org.eclipse.jface.action.*;
import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class DragTargetView extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "ecs240.views.DragTargetView";

	private FormToolkit toolkit;
	private ScrolledForm form;
	private DropTarget target;
	private TextTransfer textTransfer;
	private GridLayout layout;

	private Table table;

	/*
	 * The content provider class is responsible for providing objects to the
	 * view. It can wrap existing objects in adapters or simply return objects
	 * as-is. These objects may be sensitive to the current input of the view,
	 * or ignore it and always show the same content (like Task List, for
	 * example).
	 */

	/**
	 * The constructor.
	 */
	public DragTargetView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		toolkit = new FormToolkit(parent.getDisplay());
		form = toolkit.createScrolledForm(parent);
		form.setText("Form Title");
		form.getBody().setLayout(new GridLayout());

		table = new Table(parent, SWT.BORDER);
		target = new DropTarget(table, DND.DROP_COPY | DND.DROP_DEFAULT);
		// target = new DropTarget(form.getBody(), DND.DROP_COPY
		// | DND.DROP_DEFAULT);
		textTransfer = TextTransfer.getInstance();
		target.setTransfer(new Transfer[] { textTransfer });
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
					TableItem item = new TableItem(table, SWT.NONE);
					item.setText((String) event.data);
					// Label label = toolkit.createLabel(form.getBody(),
					// (String) event.data);
					// form.redraw();
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