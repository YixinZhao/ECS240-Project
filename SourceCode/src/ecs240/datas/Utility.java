package ecs240.datas;

public final class Utility {// general purpose usage

	public static final int TYPE_INVALID = 0;
	public static final int TYPE_SWITCH = 1;
	public static final int TYPE_HOST = 2;
	public static final int TYPE_CONTROLLER = 3;

	public static final String SWITCH = "switch";
	public static final String HOST = "host";
	public static final String CONTROLLER = "controller";

	public static final String switchIcon = "icons/switch.jpg";
	public static final String hostIcon = "icons/host.jpg";
	public static final String controllerIcon = "icons/controller.jpg";

	public static final int EVENT_INVALID = 0;
	public static final int EVENT_NEW_NODE = 1;
	public static final int EVENT_UPDATE_NODE = 2;
	public static final int EVENT_NEW_EDGE = 3;

	public static int getTypeFromID(String text) {
		int type = TYPE_INVALID;
		if (text.startsWith(SWITCH)) {
			type = TYPE_SWITCH;
		} else if (text.startsWith(HOST)) {
			type = TYPE_HOST;
		} else if (text.startsWith(CONTROLLER)) {
			type = TYPE_CONTROLLER;
		}
		return type;
	}
}
