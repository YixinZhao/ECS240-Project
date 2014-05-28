package ecs240;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ecs240.datas.Model;
import ecs240.datas.Utility;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ECS240"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	private static Model model = new Model();

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		ImageRegistry ir = plugin.getImageRegistry();
		ir.put(Utility.SWITCH, Activator.getImageDescriptor(Utility.switchIcon));
		ir.put(Utility.HOST, Activator.getImageDescriptor(Utility.hostIcon));
		ir.put(Utility.CONTROLLER,
				Activator.getImageDescriptor(Utility.controllerIcon));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static Image getImage(String name) {

		ImageRegistry ir = plugin.getImageRegistry();
		try {
			Image img = plugin.getImageRegistry().get(name);
			if (img == null) {
				ImageDescriptor desc;
				if (name.equalsIgnoreCase(Utility.SWITCH)) {
					desc = Activator.getImageDescriptor(Utility.switchIcon);
				} else if (name.equalsIgnoreCase(Utility.HOST)) {
					desc = Activator.getImageDescriptor(Utility.hostIcon);
				} else if (name.equalsIgnoreCase(Utility.HOST)) {
					desc = Activator.getImageDescriptor(Utility.controllerIcon);
				} else {
					return null;
				}
				ir.put(name, desc);
				img = ir.get(name);
			}
			return img;
		} catch (Exception e) {
		}
		return null;
	}

	public static Model getModel() {
		return model;
	}
}
