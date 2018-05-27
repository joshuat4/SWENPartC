package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

import java.util.*;

public class MyAIController extends CarController {
    private boolean debug = false;

    private ExploreStrategy exploreStrategy;
    private PathFindingStrategy pathFindingStrategy;

    private HashMap<Coordinate, MapTile> wholeMap;
    private ArrayList<Coordinate> finalPath;

    private enum STATE{Forward, Turning, Braking};
    private STATE currState = STATE.Forward;

    private boolean isAccelerating = true;
    private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.


    // Car Speed to move at
    private final float CAR_SPEED = 3;

    private float carSpeed;
    private WorldSpatial.Direction carOrientation;
    private Coordinate currPos;
    private Coordinate targetPos;

    private ArrayList<Integer> turnList;
    private Coordinate nextTurn;
    private int turnIndex = 0;

    private boolean timeToGo = false;
    private int lastCase;

    private int trigger = 0;
    private int counter = 0;
    private int frameCounter = 0;


    public MyAIController(Car car) {
        super(car);
        this.exploreStrategy = new FollowWall(this); //Assign a explore strategy here.
        this.pathFindingStrategy = new PathFindingDijkstra(); //Assign a path finding strategy here.

    }



    @Override
    public void update(float delta) {

    	try {
            if(exploreStrategy.getWholeMap(delta) != null && trigger == 0 ){ // Once we have seen enough of the map,
                                                                            // Brake and switch to pathFindingStrategy
                                                                            // to obtain a path to follow.
                currState = STATE.Braking;
                applyBrake();
                wholeMap = exploreStrategy.getWholeMap(delta);
                if(getSpeed()<=0.1f) {
                    finalPath = pathFindingStrategy.GetTotalPath(new Coordinate(getPosition()), wholeMap);
                    System.out.println(finalPath);
                    trigger = 1;

                }
            }

            if(finalPath.size() > 0) { // Only performs the following code once we have a final path to follow.
                carSpeed = getSpeed();
                carOrientation = getOrientation();
                currPos = new Coordinate(getPosition());
                targetPos = finalPath.get(counter);
                turnList = getTurnList(finalPath);

             decideState(); // Decide what state we should be in.

                switch(currState) { // Performs an action based on current state.
                    case Forward:
                        forwardState();
                        timeToGo = false;
                        break;
                    case Turning:
                        isAccelerating = false;
                        turningState(delta);
                        break;
                    case Braking:
                        isAccelerating = false;
                        brakingState();
                        applyBrake();
                        break;

                }

                if(isAccelerating && getSpeed() < CAR_SPEED) { // Only accelerate if we are below the speed limit.
                    applyForwardAcceleration();
                }
            }
        }

        catch(NullPointerException e) {
        }
    }





    // Logic to help decide what state the car should be in.
    private void decideState() {

        if (turnIndex < turnList.size() ) {
            nextTurn =finalPath.get(turnList.get(turnIndex));
        }
            if(currPos.equals(nextTurn)) {
                //currState = STATE.Turning;
            }
            if(currPos.equals(targetPos)) {
                counter += 1;
                try {
                    finalPath.get(counter+1);
                }
                catch(Exception e) {
//                    System.out.println("end of path");
                }
            }

            if((brakeLogic(nextTurn))){
                currState = STATE.Braking;
            }

//            System.out.println(getSpeed());
            if(getSpeed() <= 0.1f && getOrientation() != getDirection(currPos, targetPos)) {
                    currState = STATE.Turning;
                }
            if (timeToGo) {
                currState = STATE.Forward;
                //if we have moved past the turning block
                if(counter-1 >= turnList.get(turnIndex)) {
                    turnIndex += 1 ;
                }
            }


    }


    /*Helper function to abstract brake logic.
    Should help us stop in the exact tile we want to stop in. */
    private boolean brakeLogic(Coordinate nextTurn){
        debugPrint("getX");
        float distToPointX = (Math.abs(getX() - nextTurn.x));
        float distToPointY = (Math.abs(getY() - nextTurn.y));
        float distToPoint;

        debugPrint("dist to PointX: "+ distToPointX);
        debugPrint("dist to PointY: "+ distToPointY);
        debugPrint("speed: " + getSpeed());

        if(getOrientation().equals(WorldSpatial.Direction.EAST) ||
                getOrientation().equals(WorldSpatial.Direction.WEST)){
            distToPoint = distToPointX;
        } else {
            distToPoint = distToPointY;
        }

        double res = (Math.pow(getSpeed(), 2.0))/4f; //Some stopping distance formula from physics.
                                                    // Dunno Acceleration rate so 4f is an approximation.
        debugPrint("distToPoint: "+ distToPoint);
        debugPrint("res: "+ res);

        if(distToPoint < 0.20f){
            return true;
        }

        if(distToPoint <=  res ){
            return true;
        } else {
            return false;
        }

    }


    //Pseudo States because we weren't allowed to modify Car
    private void forwardState() {
        applySafeForwardAcceleration();
    }

    private void turningState(float delta) {
        frameCounter += 1;
        faceTarget(currPos, targetPos, delta);
    }

    private void brakingState() {
        applyBrake();
    }


    //Helper function to make the car face a target coordinate.
    private void faceTarget(Coordinate currPos, Coordinate targetPos, float delta) {
        WorldSpatial.Direction targetDir = getDirection(currPos, targetPos);
        if(Float.compare(getAngle(), directionToDegree(targetDir)) != 0) {
            WorldSpatial.RelativeDirection leftRight = leftOrRight(targetDir);
            if(leftRight == null) {
                //Not on the left or right.
                //Probably behind us.
            }
            else {
                switch(leftRight) {
                    case LEFT:
                        lastTurnDirection = WorldSpatial.RelativeDirection.LEFT;
                        rotateAntiClockwise(delta);
                        break;

                    case RIGHT:
                        lastTurnDirection = WorldSpatial.RelativeDirection.RIGHT;
                        rotateClockwise(delta);
                        break;

                    default:
                        // timeToGo = true;
                        frameCounter = 0;
                }
            }

        }
        else {
            if(timeToGo == false){
                timeToGo = true;

            }
            frameCounter = 0;
        }
    }




    // Helper function to facilitate Anti-Clockwise, "on-the-spot" rotation
    private void rotateAntiClockwise( float delta) {
        turnLeft(delta);
        if(frameCounter ==4) {
            applySafeForwardAcceleration();
            frameCounter = 0;
        }
    }

    // Helper function to facilitate Clockwise, "on-the-spot" rotation
    private void rotateClockwise( float delta) {
        turnRight(delta);
        if(frameCounter ==4) {
            applySafeForwardAcceleration();
            frameCounter = 0;
        }
    }



    // Returns a list containing the index of turns from a path.
    private ArrayList<Integer> getTurnList(ArrayList<Coordinate> wholeList){
        ArrayList<WorldSpatial.Direction> directionList = new ArrayList<>();
        ArrayList<Integer> result = new ArrayList<>();
        int turns = 1;
        for(int x = 0; x<wholeList.size()-1; x++) {
            directionList.add(getDirection(wholeList.get(x),wholeList.get(x+1)));
        }
        for(int j =0; j<directionList.size()-1;j++) {
            if(!directionList.get(j).equals(directionList.get(j+1))){
                result.add(turns);
            }
            turns += 1;
        }

        result.add(wholeList.size()-1);
        return result;
    }

    private void applySafeForwardAcceleration() {
//        if(getSpeed() < CAR_SPEED) {
            isAccelerating = true;
//        }
    }

    // Helper function to convert a Direction to a number in degrees.
    private float directionToDegree(WorldSpatial.Direction dir){
        switch(dir){
            case WEST:
                return WorldSpatial.WEST_DEGREE;
            case EAST:
                return WorldSpatial.EAST_DEGREE_MIN;
            case NORTH:
                return WorldSpatial.NORTH_DEGREE;
            case SOUTH:
                return WorldSpatial.SOUTH_DEGREE;
        }
        return 0;
    }

    // Helper function to determine whether a target is on the left or the right of the car.
    private WorldSpatial.RelativeDirection leftOrRight(WorldSpatial.Direction targetDir){
        int caseNum = -1;
        if(Float.compare(getAngle(), 0f) == 0 || Float.compare(getAngle(), 360f) == 0) {
            lastCase = 0;
            caseNum = 0;
        }
        else if(Float.compare(getAngle(), 90f) == 0) {
            lastCase = 1;
            caseNum = 1;
        }
        else if(Float.compare(getAngle(), 180f) == 0) {
            lastCase = 2;
            caseNum = 2;
        }
        else if(Float.compare(getAngle(), 270f) == 0) {
            lastCase = 3;
            caseNum = 3;
        }else{
            caseNum = lastCase;
        }


        switch (caseNum){
            case 0:
                switch (targetDir){
                    case NORTH:
                        return WorldSpatial.RelativeDirection.LEFT;
                    case SOUTH:
                        return WorldSpatial.RelativeDirection.RIGHT;
                    case WEST:
                        return WorldSpatial.RelativeDirection.LEFT;
                }
                break;


            case 3:
                switch(targetDir){
                    case WEST:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case EAST:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case NORTH:
                        return WorldSpatial.RelativeDirection.LEFT;

                }
                break;

            case 2:
                switch (targetDir){
                    case NORTH:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case SOUTH:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case EAST:
                        return WorldSpatial.RelativeDirection.LEFT;

                }
                break;

            case 1:
                switch (targetDir){
                    case EAST:
                        return WorldSpatial.RelativeDirection.RIGHT;

                    case WEST:
                        return WorldSpatial.RelativeDirection.LEFT;

                    case SOUTH:
                        return WorldSpatial.RelativeDirection.LEFT;
                }
                break;
            default:
        }
        return null;

    }


    // A Helper function that returns the direction of a destination relative to the source.
    private WorldSpatial.Direction getDirection(Coordinate source, Coordinate destination){
        if(destination.x > source.x) {
            return WorldSpatial.Direction.EAST;
        } else if(destination.x < source.x) {
            return WorldSpatial.Direction.WEST;
        } else if(destination.y > source.y) {
            return WorldSpatial.Direction.NORTH;
        } else if (destination.y < source.y){
            return WorldSpatial.Direction.SOUTH;
        } else {
            return null;
        }
    }


    // Debug Helpers
    private void debugPrint(String message){
        if(debug){
            System.out.println("DEBUGGER: "+ message);
        }
    }


    public void debugPrint(List<Node> path){
        if(debug){
            System.out.print("Debugger: [");
            for(Node i: path){
                System.out.print("("+i.getName()+")");
            }
            System.out.println("]");
        }

    }



}



