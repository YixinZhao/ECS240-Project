package ecs240.datas;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import ecs240.Activator;

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

	public void deleteNode(String id) {
		System.out.println("delete:" + id);
		if (isNodeExist(id)) {
			Iterator<Edge> it = edges.iterator();
			Edge e;
			while (it.hasNext()) {
				e = (Edge) it.next();
				if (id.equals(e.getStartNodeID())
						|| id.equals(e.getEndNodeID())) {
					it.remove();
				}
			}
			nodes.remove(id);
			ModelChangeEvent event = new ModelChangeEvent();
			event.eventType = Utility.EVENT_DEL_NODE;
			notifyModelListener(event);
		}
	}

	public boolean isNodeExist(String id) {
		return nodes.containsKey(id);
	}

	public void updateNode(String id, int x, int y) {
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

	public Hashtable<String, Node> getNodes() {
		return nodes;
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
		String loc = Activator.getDefault().getBundle().getLocation();
		String dir = loc.substring(loc.lastIndexOf(':') + 1)
				+ "src/ecs240/views";
		String fileName = dir + "/info.txt";
		BufferedWriter out;
		System.out.println("dumpToFile:");
		try {
			out = new BufferedWriter(new FileWriter(fileName));

			for (Iterator<String> it = nodes.keySet().iterator(); it.hasNext();) {
				String ndID = it.next();
				out.write("node:" + ndID + ";"
						+ nodes.get(ndID).getNodeCoordinates().x + ","
						+ nodes.get(ndID).getNodeCoordinates().y);
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

	public void loadFromFile(String file) {
		BufferedReader in;
		System.out.println("******loadFromFile:" + file);
		nodes.clear();
		edges.clear();
		try {
			in = new BufferedReader(new FileReader(file));
			String line = in.readLine();
			while (line != null) {
				if (line.startsWith("node:")) {
					String ndID = line.substring(line.indexOf(':') + 1,
							line.indexOf(';'));
					int x = Integer.parseInt(line.substring(
							line.indexOf(';') + 1, line.indexOf(',')));
					int y = Integer
							.parseInt(line.substring(line.indexOf(',') + 1));
					nodes.put(ndID, new Node(ndID, x, y));
				} else if (line.startsWith("link:")) {
					String start = line.substring(line.indexOf(':') + 1,
							line.indexOf(';'));
					String end = line.substring(line.indexOf(';') + 1);
					Point startpt = nodes.get(start).getNodeCoordinates();
					Point endpt = nodes.get(end).getNodeCoordinates();
					Edge e = new Edge(start, end, startpt, endpt);
					edges.add(e);
				}
				line = in.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ModelChangeEvent event = new ModelChangeEvent();
		event.eventType = Utility.EVENT_RELOAD;
		notifyModelListener(event);
	}

	public class ModelChangeEvent extends Event {
		public int eventType = Utility.EVENT_INVALID;
	}
}
