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
	
	public void setMsbELsb(int address16, int msb, int lsb){
		for (Node node : nodes) {
			if (node.getAddress16() == address16) {
				node.setMsb16(msb);
				node.setLsb16(lsb);
			}
		}		
	}
	
	public int getMSB(int address16){
		for (Node node : nodes) {
			if (node.getAddress16() == address16) {
				return node.getMsb16();
			}
		}
		return 0;
	}

	public int getLSB(int address16){
		for (Node node : nodes) {
			if (node.getAddress16() == address16) {
				return node.getLsb16();
			}
		}
		return 0;
	}
	
	public void addEdge(int address16, int n, int weight){
		for (int i=0;i<nodes.size()-1;i++) {
			Node node = nodes.get(i);
			if (node.getAddress16() == address16) {				
				for (Node no : nodes) {
					if (no.getAddress16() == n){
						node.addEdge(no, weight);						
					}
				}				
			}
		}		
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
	
	public int removeNode(int address16){
		for (int i=0;i<nodes.size()-1;i++) {
			Node node = nodes.get(i);
			if (node.getAddress16() == address16) {
				nodes.remove(i);
				return 1;
			}
		}    	
		return 0;
	}	
	
	
	public void addNode (Node node) {				
		nodes.add(node);		
	}	
	
	public List<Node> getNodeList () {				
		return nodes;		
	}
	
}
