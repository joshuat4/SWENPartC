package mycontroller;

import tiles.LavaTrap;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.ArrayList;
import java.util.*;

public class PathFindingDijkstra extends Dijkstra implements PathFindingStrategy {

    ArrayList<Coordinate> finalPath;

    public PathFindingDijkstra(){


    }


    public ArrayList<Coordinate> GetTotalPath(Coordinate startingPosition, HashMap<Coordinate, MapTile> wholeMap) {
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

}
