package grafo;

import java.util.ArrayList;
import java.util.List;

public class Topology {	
	
	private List<Node> nodes;
	
	public Topology() {
		nodes = new ArrayList<Node>();
	}	
	
	public Node getNodeByAddress16 (int address16) {    	
    	
		for (Node node : nodes) {
			if (node.getAddress16() == address16) {
				return node;
			}
		}    	
		return null;
    }	
	
	public int getIndexByAddress16 (int address16) {    	
    	
		for (int i=0;i<nodes.size()-1;i++) {
			Node node = nodes.get(i);
			if (node.getAddress16() == address16) {
				return i;
			}
		}    	
		return -1;
    }
	
	public void addNode (Node node) {				
		nodes.add(node);		
	}	
	
	public List<Node> getNodeList () {				
		return nodes;		
	}
	
}
