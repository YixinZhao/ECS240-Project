package ecs240.datas;

import java.util.ArrayList;
import java.util.Hashtable;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import ecs240.views.TopologyEditorView.ModelChangeEventListener;

public class Model {

	private Hashtable<String, Node> nodes;// table of node id and node
											// instance mapping
	private ArrayList<Edge> edges; // list of edges

	private int switchCount;
	private int clientCount;
	private int serverCount;

	private ArrayList<ModelChangeEventListener> listener;

	public Model() {
		nodes = new Hashtable<String, Node>();
		edges = new ArrayList<Edge>();
		listener = new ArrayList<ModelChangeEventListener>();
		switchCount = 0;
		clientCount = 0;
		serverCount = 0;
	}

	public void addModelListener(ModelChangeEventListener l) {
		listener.add(l);
	}

	public void removeModelListener(ModelChangeEventListener l) {
		listener.remove(l);
	}

	public void notifyModelListener(ModelChangeEvent e) {
		for (Listener l : listener) {
			l.handleEvent(e);
		}
	}

	public void insertNode(String string, int x, int y) {
		ModelChangeEvent e = new ModelChangeEvent();

		if (isNodeExist(string)) {// check if node already exist
			updateNode(string, x, y);
			e.eventType = Utility.EVENT_UPDATE_NODE;
			e.data = getNodeByID(string);

		} else {// new node
			String id = generateNodeID(string);
			Node nd = new Node(id, x, y);
			System.out.println("newNode:" + id + ";" + x + ";" + y);
			nodes.put(id, nd);
			e.eventType = Utility.EVENT_NEW_NODE;
			e.data = nd;
		}

		notifyModelListener(e);
	}

	public boolean isNodeExist(String id) {
		return nodes.containsKey(id);
	}

	public void updateNode(String id, int x, int y) {
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

		ModelChangeEvent e = new ModelChangeEvent();
		e.eventType = Utility.EVENT_NEW_EDGE;
		e.data = edge;
		notifyModelListener(e);
		return true;// return true when new edge
	}

	public ArrayList<Edge> getEdges() {
		return edges;
	}

	public Node getNodeByID(String id) {
		return nodes.get(id);
	}

	public String generateNodeID(String str) {
		String text = str;
		switch (Utility.getTypeFromID(str)) {
		case Utility.TYPE_CLIENT: {
			text = str + clientCount;
			clientCount++;
			break;
		}
		case Utility.TYPE_SERVER: {
			text = str + serverCount;
			serverCount++;
			break;
		}
		case Utility.TYPE_SWITCH: {
			text = str + switchCount;
			switchCount++;
			break;
		}
		}
		return text;
	}

	public class ModelChangeEvent extends Event {
		public int eventType = Utility.EVENT_INVALID;
	}
}
