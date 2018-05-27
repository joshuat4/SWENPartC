package mycontroller;
import java.util.*;

public class Node {
    private String name;
    private List<Node> shortestPath = new LinkedList<>();
    private Integer danger = Integer.MAX_VALUE;
    private Map<Node, Integer> adjacentNodes = new HashMap<>();
    
    
    public Node(String name) {
        this.setName(name);  
    }
     
    
    
    //adds adjacent nodes to the node.
    public void addDestination(Node destination, int danger) {
        adjacentNodes.put(destination, danger);
    }
 
    // getters and setters
    public int getDanger() {
    	return this.danger;
    }
    
    public void setDanger(int value) {
    	this.danger = value;
    }
    
    public List<Node> getShortestPath(){
    	return this.shortestPath;
    }
    public void setShortestPath(List<Node> x) {
    	this.shortestPath = x;
    }
    public Map<Node,Integer> getAdjacentNodes(){
    	return adjacentNodes;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
