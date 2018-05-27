package mycontroller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;



//NOT ACTUALLY USED IN OUR IMPLEMENTATION.


//this class was initially made so Dijkstra is called by passing currentView as it's input.
//This way, the car theoretically should pick the closest node with unexplored edges
//and go there. Once there, it will call dijkstra's again until the whole map is explored.
//We managed to get the logic and path for the car to work, however when we tried getting
//the car to actually follow said path, it failed to do so due to the complex nature of
//the car movement code. The car would often clip walls, or fail turns which resulted in 
//its eventual death. Integrating this with the complex car movement code give nto us
//proved to be too difficult and hence was abandoned. However, we have included our attempt
// for DCD correctness.

public class ExploreDijkstra extends Dijkstra{
    public static List<Node> exploreDijkstras(HashMap<Coordinate, MapTile> wholeMap, Coordinate startingPosition){
        Node startingNode = new Node("no");
        List<Node> returnPlaceholder = new ArrayList<>();

        //Build graph --------------------------------------------------------------
        Graph graph = initialiseDijkstras(startingPosition, wholeMap);
        //--------------------------------------------------------------
        Graph smollGraph = Graph.calculateShortestPathFromSource(graph,startingNode);


        ArrayList<Node> exposedNodeList = new ArrayList<>();
        for(Node node : graph.getNodes()) {
            boolean noWall = true;
            if(node.getAdjacentNodes().size() < 4 && wholeMap.get( new Coordinate(node.getName())).getType() != Type.WALL) {
                List<Node> currShort = node.getShortestPath();
                for(Node currNode : currShort) {
                    if(getCoordType(wholeMap, currNode.getName()) == Type.WALL) {
                        noWall = false;
                    }
                }

                if(noWall) {
                    exposedNodeList.add(node);
                }
            }
        }
        Collections.sort(exposedNodeList, new Comparator<Node>() {
            public int compare(Node obj1, Node obj2) {
                return obj1.getAdjacentNodes().size() - obj2.getAdjacentNodes().size();
            }
        });
        Node nextDest = exposedNodeList.get(0);
        for(Node n : smollGraph.getNodes()) {
            if(n.getName().equals(nextDest.getName())) {
                System.out.println(n.getName());
                return n.getShortestPath();
            }
        }
        return returnPlaceholder;
    }


    public static MapTile.Type getCoordType(HashMap<Coordinate, MapTile> wholeMap, String coord){
        return wholeMap.get(new Coordinate(coord)).getType();
    }
}
