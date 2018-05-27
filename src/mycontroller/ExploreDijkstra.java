package mycontroller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;

public class ExploreDijkstra extends Dijkstra{
    public static List<Node> exploreDijkstras(HashMap<Coordinate, MapTile> wholeMap, Coordinate startingPosition){
        Node startingNode = new Node("no");
        Graph graph = new Graph();
        List<Node> returnPlaceholder = new ArrayList<>();

        //Build graph --------------------------------------------------------------
        graph = initialiseDijkstras(startingPosition, wholeMap);
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
