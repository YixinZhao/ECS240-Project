package ecs240.views;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.PlatformUI;

public class PolicyEditingDialog extends InputDialog {

	private String policyname;

	public PolicyEditingDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Policy Dialog", "Input the Controller Policy", null, null);
	}

	protected void buttonPressed(int buttonId) {
		super.buttonPressed(buttonId);
		System.out.println("getValue():" + this.getValue() + "policyname:"
				+ policyname);
		if (this.getValue() != null && this.getValue().length() != 0) {
			String dir = System.getenv("HOME") + "/pyretic/pyretic/modules";
			String file = dir + "/temp.py";
			BufferedWriter out;
			try {
				out = new BufferedWriter(new FileWriter(file));

				out.write("from pyretic.lib.corelib import *");
				out.newLine();
				out.write("from pyretic.lib.std import *");
				out.newLine();
				out.write("from pyretic.lib.query import *");
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("policy=" + this.getValue());
				out.newLine();
				out.newLine();
				out.newLine();
				out.write("def main():");
				out.newLine();
				out.write("\t return policy");
				out.newLine();
				out.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			policyname = "pyretic.modules.temp";
		}
	}

	public String getPolicy() {
		return policyname;
	}

	protected Control createDialogArea(Composite parent) {
		super.createDialogArea(parent);
		Button button = new Button(parent, SWT.BORDER | SWT.CENTER);
		button.setText("Choose from file");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {

				FileDialog dialog = new FileDialog(PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getShell(), SWT.OPEN);
				dialog.setFilterExtensions(new String[] { "*.py" });
				dialog.setFilterPath(System.getenv("HOME")
						+ "/pyretic/pyretic/modules");
				dialog.open();
				String str = dialog.getFileName();
				if (str.endsWith(".py")) {
					policyname = "pyretic.modules."
							+ str.substring(0, str.length() - 3);
					System.out.println(policyname);
				}

			}

			public void widgetDefaultSelected(SelectionEvent e) {
				this.widgetSelected(e);
			}

		});

		return null;
	}
}
