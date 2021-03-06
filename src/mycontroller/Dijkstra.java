package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.HashMap;

public class Dijkstra {
	
    public Dijkstra(){

    }

    //initialize a dijkstra's graph given a map and a source node.
    public static Graph initialiseDijkstras(Coordinate startingPosition, HashMap<Coordinate, MapTile> wholeMap) {
        Node startingNode = new Node("no");
        Graph graph = new Graph();
        
        //go through each individual node..
        for(Coordinate i: wholeMap.keySet()) {
            //Does not add node if it's of type WALL, add all others.
            Node newNode = new Node(i.toString());
            graph.addNode(newNode);
        }
        
        //go through each node in the graph and create edges to other nodes based on
        //the map given to the method. road to road edges have weights of 1 while lava
        //tiles have a weight of 5. edges to Wall have values of 100 000.
        for(Node currNode : graph.getNodes()) {
            String nodeName = currNode.getName(); //e.g. 1,2
            String[] splitCoordinate = nodeName.split(",");
            int x = Integer.parseInt(splitCoordinate[0]);
            int y = Integer.parseInt(splitCoordinate[1]);
            ArrayList<String> coords = new ArrayList<>();
            String right = String.format("%d,%d", x+1, y);
            String left = String.format("%d,%d", x-1, y);
            String up = String.format("%d,%d", x, y+1);
            String down = String.format("%d,%d", x, y-1);
            coords.add(right);
            coords.add(left);
            coords.add(up);
            coords.add(down);
            //Looks for the adjacent nodes in graph..
            for(Node cNode: graph.getNodes()) {
                for(String adjacentCoords : coords) {
                	//set edge weights from node to node.
                    if(cNode.getName().equals(adjacentCoords)) {
                        int danger = 1;
                        if(wholeMap.get(new Coordinate(cNode.getName())) instanceof LavaTrap) {
                            danger = 5;
                        }
                        if(wholeMap.get(new Coordinate(cNode.getName())).getType() == MapTile.Type.WALL) {
                            danger = 100000;
                        }
                        currNode.addDestination(cNode, danger);
                    }
                }
                //More efficient to do startingNode retrieval here
                if(cNode.getName().equals(startingPosition.toString())) {
                    startingNode = cNode;
                }
            }
        }
        return Graph.calculateShortestPathFromSource(graph, startingNode);
    }
}
