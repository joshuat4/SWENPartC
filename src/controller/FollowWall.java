package controller;

import tiles.MapTile;
import tiles.MapTile.Type;
import utilities.Coordinate;
import world.Car;
import world.World;
import world.WorldSpatial;
import tiles.LavaTrap;
import tiles.TrapTile;

import java.util.*;

public class FollowWall implements  ExploreStrategy{
    // How many minimum units the wall is away from the player.
    private int wallSensitivity = 2;
    private CarController controller;

    private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
    private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
    private boolean isTurningLeft = false;
    private boolean isTurningRight = false;
    private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
    private HashMap<Coordinate, MapTile> wholeMap;
    private LinkedHashSet<Integer> keyTest = new LinkedHashSet<Integer>(); 
    private ArrayList<Integer> keySeen = new ArrayList<Integer>();

    // Car Speed to move at
    private final float CAR_SPEED = 3;

    // Offset used to differentiate between 0 and 360 degrees
    private int EAST_THRESHOLD = 3;


    public FollowWall(CarController controller){
        this.controller = controller;
        wholeMap = controller.getMap();
    }

    public HashMap<Coordinate, MapTile> getWholeMap(CarController controller) {
        return null;
    }
    


    public HashMap<Coordinate, MapTile> getWholeMap(float delta) {

        // Gets what the car can see
        HashMap<Coordinate, MapTile> currentView = controller.getView();
        
        
        
        if(keySeen.size()+1 == controller.getKey()) {
        	System.out.println("SWITCH TO PATHFINDING");
        	return wholeMap;
        }
        
        checkStateChange();
       
        for(Coordinate i : currentView.keySet()) {
            //Building up wholeMap with data from currentView
            if(currentView.get(i).getType() == Type.TRAP || currentView.get(i).getType() == Type.FINISH) {
                wholeMap.put(i, currentView.get(i));
                if(currentView.get(i).getType() == Type.TRAP && currentView.get(i) instanceof LavaTrap) {
                	MapTile m = currentView.get(i);
                	LavaTrap potentialKey = (LavaTrap) m;
                	if(potentialKey.getKey() != 0 && keyTest.add(potentialKey.getKey())) {
                		keySeen.add(potentialKey.getKey());
                	}
                }  	
            }
        }

        // If you are not following a wall initially, find a wall to stick to!
        if(!isFollowingWall){
            if(controller.getSpeed() < CAR_SPEED){
                controller.applyForwardAcceleration();
            }
            // Turn towards the north
            if(!controller.getOrientation().equals(WorldSpatial.Direction.NORTH)){
                lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
                applyLeftTurn(controller.getOrientation(),delta);
            }
            if(checkNorth(currentView)){
                // Turn right until we go back to east!
                if(!controller.getOrientation().equals(WorldSpatial.Direction.EAST)){
                    lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
                    applyRightTurn(controller.getOrientation(),delta);
                }
                else{
                    isFollowingWall = true;
                }
            }
        }
        // Once the car is already stuck to a wall, apply the following logic
        else{

            // Readjust the car if it is misaligned.
            readjust(lastTurnDirection,delta);

            if(isTurningRight){
                applyRightTurn(controller.getOrientation(),delta);
            }
            else if(isTurningLeft){
                // Apply the left turn if you are not currently near a wall.
                if(!checkFollowingWall(controller.getOrientation(),currentView)){
                    applyLeftTurn(controller.getOrientation(),delta);
                }
                else{
                    isTurningLeft = false;
                }
            }
            // Try to determine whether or not the car is next to a wall.
            else if(checkFollowingWall(controller.getOrientation(),currentView)){
                // Maintain some velocity
                if(controller.getSpeed() < CAR_SPEED){
                    controller.applyForwardAcceleration();
                }
                // If there is wall ahead, turn right!
                if(checkWallAhead(controller.getOrientation(),currentView)){
                    lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
                    isTurningRight = true;

                }

            }
            // This indicates that I can do a left turn if I am not turning right
            else{
                lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
                isTurningLeft = true;
            }
        }
        return null;
        

    }

    /**
     * Readjust the car to the orientation we are in.
     * @param lastTurnDirection
     * @param delta
     */
    private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
        if(lastTurnDirection != null){
            if(!isTurningRight && lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
                adjustRight(controller.getOrientation(),delta);
            }
            else if(!isTurningLeft && lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
                adjustLeft(controller.getOrientation(),delta);
            }
        }

    }

    /**
     * Try to orient myself to a degree that I was supposed to be at if I am
     * misaligned.
     */
    private void adjustLeft(WorldSpatial.Direction orientation, float delta) {

        switch(orientation){
            case EAST:
                if(controller.getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
                    controller.turnRight(delta);
                }
                break;
            case NORTH:
                if(controller.getAngle() > WorldSpatial.NORTH_DEGREE){
                    controller.turnRight(delta);
                }
                break;
            case SOUTH:
                if(controller.getAngle() > WorldSpatial.SOUTH_DEGREE){
                    controller.turnRight(delta);
                }
                break;
            case WEST:
                if(controller.getAngle() > WorldSpatial.WEST_DEGREE){
                    controller.turnRight(delta);
                }
                break;

            default:
                break;
        }

    }

    private void adjustRight(WorldSpatial.Direction orientation, float delta) {
        switch(orientation){
            case EAST:
                if(controller.getAngle() > WorldSpatial.SOUTH_DEGREE && controller.getAngle() < WorldSpatial.EAST_DEGREE_MAX){
                    controller.turnLeft(delta);
                }
                break;
            case NORTH:
                if(controller.getAngle() < WorldSpatial.NORTH_DEGREE){
                    controller.turnLeft(delta);
                }
                break;
            case SOUTH:
                if(controller.getAngle() < WorldSpatial.SOUTH_DEGREE){
                    controller.turnLeft(delta);
                }
                break;
            case WEST:
                if(controller.getAngle() < WorldSpatial.WEST_DEGREE){
                    controller.turnLeft(delta);
                }
                break;

            default:
                break;
        }

    }

    /**
     * Checks whether the car's state has changed or not, stops turning if it
     *  already has.
     */
    private void checkStateChange() {
        if(previousState == null){
            previousState = controller.getOrientation();
        }
        else{
            if(previousState != controller.getOrientation()){
                if(isTurningLeft){
                    isTurningLeft = false;
                }
                if(isTurningRight){
                    isTurningRight = false;
                }
                previousState = controller.getOrientation();
            }
        }
    }

    /**
     * Turn the car counter clock wise (think of a compass going counter clock-wise)
     */
    private void applyLeftTurn(WorldSpatial.Direction orientation, float delta) {
        switch(orientation){
            case EAST:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.NORTH)){
                    controller.turnLeft(delta);
                }
                break;
            case NORTH:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.WEST)){
                    controller.turnLeft(delta);
                }
                break;
            case SOUTH:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.EAST)){
                    controller.turnLeft(delta);
                }
                break;
            case WEST:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.SOUTH)){
                    controller.turnLeft(delta);
                }
                break;
            default:
                break;

        }

    }

    /**
     * Turn the car clock wise (think of a compass going clock-wise)
     */
    private void applyRightTurn(WorldSpatial.Direction orientation, float delta) {
        switch(orientation){
            case EAST:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.SOUTH)){
                    controller.turnRight(delta);
                }
                break;
            case NORTH:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.EAST)){
                    controller.turnRight(delta);
                }
                break;
            case SOUTH:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.WEST)){
                    controller.turnRight(delta);
                }
                break;
            case WEST:
                if(!controller.getOrientation().equals(WorldSpatial.Direction.NORTH)){
                    controller.turnRight(delta);
                }
                break;
            default:
                break;

        }

    }

    /**
     * Check if you have a wall in front of you!
     * @param orientation the orientation we are in based on WorldSpatial
     * @param currentView what the car can currently see
     * @return
     */
    private boolean checkWallAhead(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView){
        switch(orientation){
            case EAST:
                return checkEast(currentView);
            case NORTH:
                return checkNorth(currentView);
            case SOUTH:
                return checkSouth(currentView);
            case WEST:
                return checkWest(currentView);
            default:
                return false;

        }
    }

    /**
     * Check if the wall is on your left hand side given your orientation
     * @param orientation
     * @param currentView
     * @return
     */
    private boolean checkFollowingWall(WorldSpatial.Direction orientation, HashMap<Coordinate, MapTile> currentView) {

        switch(orientation){
            case EAST:
                return checkNorth(currentView);
            case NORTH:
                return checkWest(currentView);
            case SOUTH:
                return checkEast(currentView);
            case WEST:
                return checkSouth(currentView);
            default:
                return false;
        }

    }


    /**
     * Method below just iterates through the list and check in the correct coordinates.
     * i.e. Given your current position is 10,10
     * checkEast will check up to wallSensitivity amount of tiles to the right.
     * checkWest will check up to wallSensitivity amount of tiles to the left.
     * checkNorth will check up to wallSensitivity amount of tiles to the top.
     * checkSouth will check up to wallSensitivity amount of tiles below.
     */
    public boolean checkEast(HashMap<Coordinate, MapTile> currentView){
        // Check tiles to my right
        Coordinate currentPosition = new Coordinate(controller.getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x+i, currentPosition.y));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public boolean checkWest(HashMap<Coordinate,MapTile> currentView){
        // Check tiles to my left
        Coordinate currentPosition = new Coordinate(controller.getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x-i, currentPosition.y));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public boolean checkNorth(HashMap<Coordinate,MapTile> currentView){
        // Check tiles to towards the top
        Coordinate currentPosition = new Coordinate(controller.getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y+i));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

    public boolean checkSouth(HashMap<Coordinate,MapTile> currentView){
        // Check tiles towards the bottom
        Coordinate currentPosition = new Coordinate(controller.getPosition());
        for(int i = 0; i <= wallSensitivity; i++){
            MapTile tile = currentView.get(new Coordinate(currentPosition.x, currentPosition.y-i));
            if(tile.isType(MapTile.Type.WALL)){
                return true;
            }
        }
        return false;
    }

}
