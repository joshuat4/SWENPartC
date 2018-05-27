package mycontroller;
import tiles.MapTile;
import utilities.Coordinate;

import java.util.*;

public interface PathFindingStrategy {

    //All path finding strategies require a map of the entire relevant area to initialise (The result of an
    //exploration strategy)

    public ArrayList<Coordinate> GetTotalPath(Coordinate startingPosition, HashMap<Coordinate, MapTile> wholeMap);
}
