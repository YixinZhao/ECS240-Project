package ecs240.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

public class AttributeEditingDialog extends MessageDialog {


	public AttributeEditingDialog(String title) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				title, null, "Hello World", SWT.NONE, new String[] { "Cancel",
						"Done" }, 0);

	}

	protected Control createCustomArea(Composite parent) {
		return null;
	}

}
