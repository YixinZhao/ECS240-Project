package ecs240;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ecs240.datas.Model;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ECS240"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	public static final String switchIcon = "icons/switch.jpg";
	public static final String serverIcon = "icons/server.jpg";
	public static final String clientIcon = "icons/client.jpg";

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
		ir.put(Model.SWITCH, Activator.getImageDescriptor(switchIcon));
		ir.put(Model.SERVER, Activator.getImageDescriptor(serverIcon));
		ir.put(Model.CLIENT, Activator.getImageDescriptor(clientIcon));
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
				if (name.equalsIgnoreCase(Model.SWITCH)) {
					desc = Activator
							.getImageDescriptor(switchIcon);
				} else if (name.equalsIgnoreCase(Model.SERVER)) {
					desc = Activator
							.getImageDescriptor(serverIcon);
				} else if (name.equalsIgnoreCase(Model.CLIENT)) {
					desc = Activator
							.getImageDescriptor(clientIcon);
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
}
