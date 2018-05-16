package controller;
import java.util.*;

public class Graph {
	 
    private Set<Node> nodes = new HashSet<>();
     
    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }
    
    public static Graph calculateShortestPathFromSource(Graph graph, Node source) {
        source.setDanger(0);
     
        Set<Node> settledNodes = new HashSet<>();
        Set<Node> unsettledNodes = new HashSet<>();
     
        unsettledNodes.add(source);
     
        while (unsettledNodes.size() != 0) {
            Node currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (Map.Entry < Node, Integer> adjacencyPair: currentNode.getAdjacentNodes().entrySet()) {
                Node adjacentNode = adjacencyPair.getKey();
                Integer edgeWeight = adjacencyPair.getValue();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return graph;
    }
    
    
    
    private static Node getLowestDistanceNode(Set < Node > unsettledNodes) {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        for (Node node: unsettledNodes) {
            int nodeDistance = node.getDanger();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    private static void calculateMinimumDistance(Node evaluationNode,
    		  Integer edgeWeigh, Node sourceNode) {
    		    Integer sourceDanger = sourceNode.getDanger();
    		    if (sourceDanger + edgeWeigh < evaluationNode.getDanger()) {
    		        evaluationNode.setDanger(sourceDanger + edgeWeigh);
    		        LinkedList<Node> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
    		        shortestPath.add(sourceNode);
    		        evaluationNode.setShortestPath(shortestPath);
    		    }
    		}
 
    // getters and setters 
    public Set<Node> getNodes(){
    	return this.nodes; 
    }
}
