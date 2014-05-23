package ecs240.datas;

import org.eclipse.swt.graphics.Point;

public class Edge {

	private String startNodeID;
	private String endNodeID;
	private Point startPt;// coordinates corresponding to targetArea
	private Point endPt;// coordinates corresponding to targetArea

	public Edge(String startID, String endID, Point startpt, Point endpt) {
		startNodeID = startID;
		endNodeID = endID;
		startPt = startpt;
		endPt = endpt;
	}

	public void setConnection(String startID, String endID, Point startpt,
			Point endpt) {
		startNodeID = startID;
		endNodeID = endID;
		startPt = startpt;
		endPt = endpt;
	}

	public void setEdgeLocation(Point start, Point end) {
		startPt = start;
		endPt = end;
	}

	public void setEdgeStartPoint(Point start) {
		startPt = start;
	}

	public void setEdgeEndPoint(Point end) {
		endPt = end;
	}

	public String getStartNodeID() {
		return startNodeID;
	}

	public Point getStartPoint() {
		return startPt;
	}

	public String getEndNodeID() {
		return endNodeID;
	}

	public Point getEndPoint() {
		return endPt;
	}

}
