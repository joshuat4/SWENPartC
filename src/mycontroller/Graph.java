package mycontroller;
import java.util.*;

//graph is mostly used for Dijkstra's. But can be used for other algorithms that
//utilize graph in the future.
public class Graph {
    private Set<Node> nodes = new HashSet<>();
    
    
    //add nodes to the graph.
    public void addNode(Node nodeA) {
        nodes.add(nodeA);
    }
    
    //this method gets shortest path to every node in the graph from a specific source.
    public static Graph calculateShortestPathFromSource(Graph graph, Node source) {
        source.setDanger(0);
        Set<Node> settledNodes = new HashSet<>();  //visted
        Set<Node> unsettledNodes = new HashSet<>(); //unvisited
     
        unsettledNodes.add(source);
     
        //while there are nodes that are still unexplored...
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
    
    
    //get the node with the lowestDistance from our currentNode. D
    private static Node getLowestDistanceNode(Set < Node > unsettledNodes) {
        Node lowestDistanceNode = null;
        int lowestDistance = Integer.MAX_VALUE;
        
        for (Node node: unsettledNodes) {
        	//getDanger() returns node to node edge weight.
            int nodeDistance = node.getDanger();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }

    
    //calculates the minimum distance between source to a node.
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
