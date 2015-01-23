package grafo;

import grafo.Node;

public class Edge {
	
	private Node node; 
	private int weight;
	
	public Edge (Node node, int weight) {		
		this.setNode(node);
		this.weight = weight;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}	
	
}
