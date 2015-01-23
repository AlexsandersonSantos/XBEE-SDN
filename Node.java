package grafo;

import java.util.ArrayList;
import java.util.List;

public class Node implements Comparable<Node>{

	private int address16;
	private int address64;
	private Integer distance;
	private Node parent;
	private List<Edge> edges = new ArrayList<Edge>();
	
	public Node (int address16, int address64) {
		this.address16 = address16;
		this.address64 = address64;		
	}

	public int getAddress16() {
		return address16;
	}

	public void setAddress16(int address16) {
		this.address16 = address16;
	}

	public int getAddress64() {
		return address64;
	}

	public void setAddress64(int address64) {
		this.address64 = address64;
	}	
	
	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	public void addEdge (Node node, int weight) {
		edges.add(new Edge(node,weight));
	}
	
	public List<Edge> getNeighbors() {
		return edges;
	}

	@Override
	public int compareTo(Node node) {
		return distance.compareTo(node.getDistance());
	}
	
	
	
}
