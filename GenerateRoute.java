package routing;

import grafo.Edge;
import grafo.Node;
import grafo.Topology;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GenerateRoute {	
    
	List<Node> nodes;
    Set<Node> visitedNodes;
	
    public GenerateRoute() {
    	nodes = new ArrayList<Node>();
        visitedNodes = new HashSet<Node>();
	}
    
	public Node getLowerCostPath(List<Node> nodes) {
		
		Collections.sort(nodes);
		
		Node node;
		
		for(int i=0;i<nodes.size();i++) {
			node = nodes.get(i);
			if (!visitedNodes.contains((node))) {
				return node;
			}
		}
		
		return null;
	
	}
	
    // Algoritmo de Dijkstra
    public List<Integer> requestShortestPath(Topology topo, int sAdd16,
                    int dAdd16) {

    	Node node;
    	Node actualNode = null;
    	List<Integer> shortestPath = new ArrayList<Integer>();
    	
    	//System.out.println(nodes.size());
    	
		//Configuração das distâncias
		for (int i=0; i<topo.getNodeList().size(); i++) {
			node =  topo.getNodeList().get(i);		
			if (node.getAddress16() == sAdd16) {				
				node.setDistance(0);
				node.setParent(node);
				actualNode = node;
			} else {
				node.setDistance(9999);
			}			
			nodes.add(node);
		}
    	
		//System.out.println("Teste 1");
		
		if (!actualNode.equals(null)) {
			while (actualNode.getAddress16() != dAdd16) {
				//System.out.println("----> "+actualNode.getAddress16());
				for (Edge edge : actualNode.getNeighbors()) {				
					if (!visitedNodes.contains((edge.getNode()))) {
						//System.out.println(edge.getNode().getAddress16());
						if (edge.getNode().getDistance()>(actualNode.getDistance()-edge.getWeight())) { //Modificação + por -
							edge.getNode().setDistance(actualNode.getDistance()-edge.getWeight());		////Modificação + por -	
							edge.getNode().setParent(actualNode);
						}						
						
					}
				}				
				visitedNodes.add(actualNode);
				actualNode = getLowerCostPath(nodes);				
			}
			
			do {
				shortestPath.add(actualNode.getParent().getAddress16());
				actualNode = actualNode.getParent();
			} while (actualNode.getParent().getAddress16() != sAdd16);			
		} else {
			System.out.println("Source address node not found.");
		}		
		    	
    	return shortestPath;
            
    }

}
