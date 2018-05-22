package controller;

import java.util.*;

import tiles.*;
import tiles.MapTile.Type;
import utilities.Coordinate;
import utilities.PeekTuple;
import world.Car;
import world.World;
import world.WorldSpatial;

public class Dijkstras {
    public static List<Node> exploreDijkstras(HashMap<Coordinate, MapTile> wholeMap, Coordinate startingPosition){
        Node startingNode = new Node("no");
        Graph graph = new Graph();
        List<Node> returnPlaceholder = new ArrayList<>();

        //Build graph --------------------------------------------------------------
        for(Coordinate i: wholeMap.keySet()) {
            //Does not add node if it's of type WALL
            Node newNode = new Node(i.toString());
            graph.addNode(newNode);
        }

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
            //Looks for the adjacent nodes in graph
            for(Node cNode: graph.getNodes()) {
                for(String adjacentCoords : coords) {
                    if(cNode.getName().equals(adjacentCoords)) {
                        int danger = 1;
                        if(wholeMap.get(new Coordinate(cNode.getName())) instanceof LavaTrap) {
                            danger = 5;
                        }
                        if(wholeMap.get(new Coordinate(cNode.getName())).getType() == Type.WALL) {
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
