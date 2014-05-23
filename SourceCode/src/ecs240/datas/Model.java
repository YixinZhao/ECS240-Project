package ecs240.datas;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

import ecs240.views.TopologyEditorView.ModelChangeEventListener;

public class Model {
	public static final int TYPE_INVALID = 0;
	public static final int TYPE_SWITCH = 1;
	public static final int TYPE_SERVER = 2;
	public static final int TYPE_CLIENT = 3;

	public static final String SWITCH = "switch";
	public static final String SERVER = "server";
	public static final String CLIENT = "client";

	private Hashtable<String, Node> nodes;// table of node id and node
											// instance mapping
	private ArrayList<Edge> edges; // list of edges

	private ArrayList<ModelChangeEventListener> listener;

	public Model() {
		nodes = new Hashtable<String, Node>();
		edges = new ArrayList<Edge>();
		listener = new ArrayList<ModelChangeEventListener>();
	}

	public void addModelListener(ModelChangeEventListener l) {
		listener.add(l);
	}

	public void removeModelListener(ModelChangeEventListener l) {
		listener.remove(l);
	}

	public void notifyModelListener() {
		for (Listener l : listener) {
			l.handleEvent(new Event());
		}
	}

	public void insertNode(String id, int type, int x, int y) {
		if (type != Model.TYPE_INVALID) {
			// check if node already exist
			if (isNodeExist(id)) {
				updateNode(id, type, x, y);
			} else {
				Node nd = new Node(type, id, x, y);
				nodes.put(id, nd);
			}
			notifyModelListener();
		}
	}

	public boolean isNodeExist(String id) {
		return nodes.containsKey(id);
	}

	public void updateNode(String id, int type, int x, int y) {
		System.out.println("updateNode:" + id + ";" + x + ";" + y);
		Node nd = nodes.get(id);
		nd.SetNodeCoordinates(x, y);
		for (Edge e : edges) {
			if (id.equals(e.getStartNodeID())) {
				e.setEdgeStartPoint(new Point(x, y));
			} else if (id.equals(e.getEndNodeID())) {
				e.setEdgeEndPoint(new Point(x, y));
			}
		}
	}

	public void insertEdge(Node startNode, Node endNode, Point startpt,
			Point endpt) {
		insertEdge(startNode.getNodeID(), endNode.getNodeID(), startpt, endpt);
	}
	
	public boolean insertEdge(String startNode, String endNode, Point startpt,
			Point endpt) {

		// check if edge already exist
		for (Edge e : edges) {
			if ((startNode.equals(e.getStartNodeID()) && endNode.equals(e
					.getEndNodeID()))
					|| (endNode.equals(e.getStartNodeID()) && startNode
							.equals(e.getEndNodeID()))) {
				return false;
			}
		}
		System.out.println("insertEdge: " + "start:" + startNode + ";"
				+ startpt + "end:" + endNode + ";" + endpt);
		Edge edge = new Edge(startNode, endNode, startpt, endpt);
		edges.add(edge);
		return true;// return true when new edge
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public Node getNodeByID(String id) {
		return nodes.get(id);
	}

}
