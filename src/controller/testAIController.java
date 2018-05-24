package controller;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

import java.util.HashMap;

public class testAIController extends CarController {

    private ExploreStrategy strat;
    // How many minimum units the wall is away from the player.
    private int wallSensitivity = 2;


    private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
    private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
    private boolean isTurningLeft = false;
    private boolean isTurningRight = false;
    private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
    private HashMap<Coordinate, MapTile> wholeMap;


    // Car Speed to move at
    private final float CAR_SPEED = 3;

    // Offset used to differentiate between 0 and 360 degrees
    private int EAST_THRESHOLD = 3;

    public testAIController(Car car) {
        super(car);
        this.strat = new FollowWall(this);
    }



    @Override
    public void update(float delta) {
       strat.update(delta);
    }


}
