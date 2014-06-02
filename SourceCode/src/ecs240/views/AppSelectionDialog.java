package ecs240.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class AppSelectionDialog extends MessageDialog {
	String selection;
	Combo combo;

	public AppSelectionDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				"Application Selection", null, "Hello World", SWT.NONE,
				new String[] { "Cancel", "Done" }, 0);

	}

	public String getApp() {
		return selection;
	}

	protected Control createCustomArea(Composite parent) {
		combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
		combo.setBounds(50, 50, 150, 65);
		String items[] = { "pingall", "iperf" };
		combo.setItems(items);
		return null;
	}

	protected void buttonPressed(int buttonId) {
		if (buttonId == 1) {
			selection = combo.getText();
			System.out.println(selection);
		}
		setReturnCode(buttonId);
		close();
	}
}
