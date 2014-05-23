package ecs240.datas;

public final class Utility {

	public static final int TYPE_INVALID = 0;
	public static final int TYPE_SWITCH = 1;
	public static final int TYPE_SERVER = 2;
	public static final int TYPE_CLIENT = 3;

	public static final String SWITCH = "switch";
	public static final String SERVER = "server";
	public static final String CLIENT = "client";

	public static final String switchIcon = "icons/switch.jpg";
	public static final String serverIcon = "icons/server.jpg";
	public static final String clientIcon = "icons/client.jpg";

	public static final int EVENT_INVALID = 0;
	public static final int EVENT_NEW_NODE = 1;
	public static final int EVENT_UPDATE_NODE = 2;
	public static final int EVENT_NEW_EDGE = 3;

	public static int getTypeFromID(String text) {
		int type = TYPE_INVALID;
		if (text.startsWith(SWITCH)) {
			type = TYPE_SWITCH;
		} else if (text.startsWith(SERVER)) {
			type = TYPE_SERVER;
		} else if (text.startsWith(CLIENT)) {
			type = TYPE_CLIENT;
		}
		return type;
	}
}
