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

    	
    //this method gets the total SHORTEST path from start to finish of the map. It will give
    //the path that includes all the keys in the neccesary order to finish the maze.Our implementation
    //gives the correct path for the car to traverse, but we could not get the car to actually 
    //follow the path properly without clipping a wall or turn properly. The initial skeleton
    //code given to us was difficult to understand and use in the context 2D traversal.
    //However, the path is correct and if provided with easier car movement methods, would have
    //worked as it should.
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
            //puts the finish tile as a "key" so the AI will go there at the end.
            if(wholeMap.get(c).getType() == MapTile.Type.FINISH){
                keyLocations.put(0, c);
            }
        }
        
        //get a path from source to any of the keys(or finish tile).
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
          //append the path to a TotalPath object that will be returned later.
            finalPath.addAll(coordToAppend);
            //change source position to where the last key was( the one we just collected).
            last_position = keyLocations.get(i);
        }
        return finalPath;
    }

}
