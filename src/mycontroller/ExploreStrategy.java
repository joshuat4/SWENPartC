package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import java.util.*;

public interface ExploreStrategy {

    public HashMap<Coordinate, MapTile> getWholeMap(float delta);


}
