package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.*;

public class PathFindingDijkstra implements PathFindingStrategy {

    ArrayList<Coordinate> finalPath;
    int counter = 0;



    public ArrayList<Coordinate> GetTotalPath(Coordinate startingPosition, HashMap<Coordinate, MapTile> wholeMap ){
        HashMap<Integer,Coordinate> keyLocations = new HashMap<>();
        finalPath = new ArrayList<>();
        Coordinate last_position = startingPosition;
        List<Node> pathToAppend = new ArrayList<>();
        List<Coordinate> coordToAppend;

        //Loops through whole map and populates keyLocations with the locations of the keys as coordinates
        for(Coordinate c : wholeMap.keySet()){
            if( wholeMap.get(c) instanceof LavaTrap){
                LavaTrap m = (LavaTrap) wholeMap.get(c);
                if(m.getKey() != 0){
                    keyLocations.put(m.getKey(), c);
                }
            }
            if(wholeMap.get(c).getType() == MapTile.Type.FINISH){
                keyLocations.put(0, c);
            }
        }

        for(int i = keyLocations.size()-1; i>=0; i--){
            Graph graphPath = initialiseDijkstras(last_position, wholeMap);
            coordToAppend = new ArrayList<>();
            for(Node n : graphPath.getNodes()){
                if(new Coordinate(n.getName()).equals(keyLocations.get(i))){
                    pathToAppend = n.getShortestPath();
                    //Convert to coordlist
                }
            }
            if(pathToAppend != null) {
            for(Node node : pathToAppend){
                coordToAppend.add(new Coordinate(node.getName()));
            }
            }
            finalPath.addAll(coordToAppend);
            last_position = keyLocations.get(i);
        }
        //append this to
        return finalPath;
    }


    public PathFindingDijkstra(){


    }



    private Graph initialiseDijkstras(Coordinate startingPosition,HashMap<Coordinate, MapTile> wholeMap) {
        Node startingNode = new Node("no");
        Graph graph = new Graph();
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
