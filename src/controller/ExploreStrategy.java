package controller;

import tiles.MapTile;
import utilities.Coordinate;
import java.util.*;

public interface ExploreStrategy {

    public HashMap<Coordinate, MapTile> getWholeMap(CarController controller);

    public void update(float delta);

}
