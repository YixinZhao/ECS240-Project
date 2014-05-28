package ecs240.datas;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Model {

	private Hashtable<String, Node> nodes;// table of node id and node
											// instance mapping
	private ArrayList<Edge> edges; // list of edges

	private int switchCount;
	private int clientCount;
	private int serverCount;

	private ArrayList<Listener> listener;

	public Model() {
		nodes = new Hashtable<String, Node>();
		edges = new ArrayList<Edge>();
		listener = new ArrayList<Listener>();
		switchCount = 0;
		clientCount = 0;
		serverCount = 0;
	}

	public void addModelListener(Listener l) {
		listener.add(l);
	}

	public void removeModelListener(Listener l) {
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
		case Utility.TYPE_CONTROLLER: {
			text = str + clientCount;
			clientCount++;
			break;
		}
		case Utility.TYPE_HOST: {
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

	public boolean dumpToFile() {
		//TODO: use relative path
		String fileName = "/home/yixin/info.txt";
		BufferedWriter out;
		System.out.println("dumpToFile:");
		try {
			out = new BufferedWriter(new FileWriter(fileName));

			for (Iterator<String> it = nodes.keySet().iterator(); it.hasNext();) {
				String ndID = it.next();
				out.write("node:" + ndID);
				out.newLine();
			}
			for (Edge e : edges) {
				out.write("link:" + e.getStartNodeID() + ";" + e.getEndNodeID());
				out.newLine();
			}
			out.close();
			return true;
		} catch (IOException e1) {
			e1.printStackTrace();
			return false;
		}

	}

	public class ModelChangeEvent extends Event {
		public int eventType = Utility.EVENT_INVALID;
	}
}
