package ecs240.datas;

import org.eclipse.swt.graphics.Point;

public class Node {

	private String nodeID;
	private int x; // x coordinates corresponding to targetArea;
	private int y; // y coordinates corresponding to targetArea;

	Node() {
	}

	Node(String id, int x, int y) {
		nodeID = id;
		this.x = x;
		this.y = y;
	}

	public void setNodeID(String id) {
		nodeID = id;
	}

	public void SetNodeCoordinates(int x, int y) {
		this.x = x;
		this.y = y;
	}

	public void SetNodeCoordinates(Point pt) {
		this.x = pt.x;
		this.y = pt.y;
	}

	public String getNodeID() {
		return nodeID;
	}

	public int getNodeType() {
		return Utility.getTypeFromID(nodeID);
	}

	public Point getNodeCoordinates() {
		return (new Point(this.x, this.y));
	}
}
