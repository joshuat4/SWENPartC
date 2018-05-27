package mycontroller;

import tiles.MapTile;
import utilities.Coordinate;
import world.Car;
import world.WorldSpatial;

import java.util.*;

public class MyAIController extends CarController {

    private ExploreStrategy strat;
    private PathFindingStrategy strat2;
    // How many minimum units the wall is away from the player.
    private int wallSensitivity = 2;


    private boolean isFollowingWall = false; // This is initialized when the car sticks to a wall.
    private WorldSpatial.RelativeDirection lastTurnDirection = null; // Shows the last turn direction the car takes.
    private boolean isTurningLeft = false;
    private boolean isTurningRight = false;
    private WorldSpatial.Direction previousState = null; // Keeps track of the previous state
    private HashMap<Coordinate, MapTile> wholeMap;
    private ArrayList<Coordinate> finalPath ;
    private int trigger = 0;

    private boolean debug = false;
    int test = 0;
    private enum STATE{Forward, Turning, Braking};
    private STATE currState = STATE.Forward;

    private boolean isAccelerating = true;
    //
    // Car Speed to move at
    private final float CAR_SPEED = 3;
    // Offset used to differentiate between 0 and 360 degrees
    private int EAST_THRESHOLD = 3;

    private float carSpeed;
    private WorldSpatial.Direction carOrientation;
    private Coordinate currPos;
    private Coordinate targetPos;
    private int counter = 0;
    private int frameCounter = 0;
    private int turnIndex = 0;
    private ArrayList<Integer> turnList;
    private Coordinate nextTurn;
    private boolean timeToGo = false;
    private boolean justTurned = false;
    private int lastCase;
    private float timer = 0f;



    public MyAIController(Car car) {
        super(car);
        this.strat = new FollowWall(this);
        this.strat2 = new PathFindingDijkstra();

    }



    @Override
    public void update(float delta) {

    	try {
            if(strat.getWholeMap(delta) != null && trigger == 0 ){
                currState = STATE.Braking;
                applyBrake();
                wholeMap = strat.getWholeMap(delta);
                if(getSpeed()<=0.1f) {
                    finalPath = strat2.GetTotalPath(new Coordinate(getPosition()), wholeMap);
                    System.out.println(finalPath);
                    trigger = 1;

                }
            }
//            System.out.println(currState);
            if(finalPath.size() > 0) {
                // Gets what the car can see
                carSpeed = getSpeed();
                carOrientation = getOrientation();
                currPos = new Coordinate(getPosition());
                targetPos = finalPath.get(counter);
                turnList = getTurnList(finalPath);
//                System.out.println(turnList); //Prints in getTurnList


             decideState();

                switch(currState) {
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

                if(isAccelerating && getSpeed() < CAR_SPEED) {
                    System.out.println("ACCELERATE");
                    applyForwardAcceleration();
                }
//
                System.out.println("isAccelerating2: " + isAccelerating);
                System.out.println("currentAngle: " + getAngle());
                System.out.println("counter is: " + counter);
                System.out.println("framecounter is :" + frameCounter);
                System.out.println("currState is:" + currState);
                System.out.println("------------------------------------------------------------------[");
                System.out.println("carSpeed is: " + carSpeed);
                System.out.println("currPosition is: " + currPos.x + "," + currPos.y);
                System.out.println("targetPosition is: "+ targetPos.x + "," + targetPos.y);
                System.out.println("turnIndex: " + turnIndex + " turnList: " + turnList);
//        System.out.println("nextTurn is: "+ nextTurn.x + "," + nextTurn.y);
                System.out.println("timeToGo is: " + timeToGo);
                System.out.println("-----aaaa-----");

            }
        }

        catch(NullPointerException e) {
        }
    }






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
                    System.out.println("end of path");
                }
            }

//            System.out.println("distToTurn is: " + distToTurn);

//            if(timeToGo && justTurned){
//                System.out.println("Clown Fiesta");
//               currState = STATE.Forward;
//               justTurned = false;
//            }

            if((brakeLogic(nextTurn))){
                currState = STATE.Braking;
            }

            System.out.println(getSpeed());
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






    private boolean brakeLogic(Coordinate nextTurn){
        debugPrint("getX");
        //System.out.println(getX());
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

        //Should Add some logic for negative direction.


        double res = (Math.pow(getSpeed(), 2.0))/4f; //Some stopping distance formula. dunno Acceleration rate tho.
        debugPrint("distToPoint: "+ distToPoint);
        debugPrint("res: "+ res);

        if(distToPoint < 0.20f){
            //  System.out.println("HHH");
            return true;
        }

        //  System.out.println("TRUTH :" + (distToPoint <= res) );

        if(distToPoint <=  res ){
            return true;
        } else {
            // System.out.println(" OK K");
            return false;
        }

    }






    //
    private void forwardState() {

        applySafeForwardAcceleration();
    }


    private void turningState(float delta) {
        frameCounter += 1;
        faceTarget(currPos, targetPos, delta);
    }

    private void brakingState() {
        System.out.println("BRAAAAAKE");
        applyBrake();
    }

    private void faceTarget(Coordinate currPos, Coordinate targetPos, float delta) {
        WorldSpatial.Direction targetDir = getDirection(currPos, targetPos);
//    	System.out.println(currPos.x + "," + currPos.y);
//    	System.out.println(targetPos.x + "," + targetPos.y);
//    	System.out.println("-----------------------");




        // System.out.println("targetDir degree is: " + directionToDegree(targetDir));
        if(Float.compare(getAngle(), directionToDegree(targetDir)) != 0) {
            WorldSpatial.RelativeDirection leftRight = leftOrRight(targetDir);
            if(leftRight == null) {
//                System.out.println("test2");
//                justTurned = true;
//                timeToGo = true;
//                frameCounter = 0;
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





    private void rotateAntiClockwise( float delta) {
        turnLeft(delta);
        if(frameCounter ==4) {
            applySafeForwardAcceleration();
            //  System.out.println(frameCounter);
            frameCounter = 0;
        }
    }

    private void rotateClockwise( float delta) {
        turnRight(delta);
        if(frameCounter ==4) {
            System.out.println("OOOOOOER");
            applySafeForwardAcceleration();
            //System.out.println(frameCounter);
            frameCounter = 0;
        }
    }




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
        isAccelerating = true;
        //	applyForwardAcceleration();

    }



//    /**
//     * Readjust the car to the orientation we are in.
//     * @param lastTurnDirection
//     * @param delta
//     */
//    private void readjust(WorldSpatial.RelativeDirection lastTurnDirection, float delta) {
//        if(lastTurnDirection != null){
//            if(lastTurnDirection.equals(WorldSpatial.RelativeDirection.RIGHT)){
//                adjustRight(getOrientation(),delta);
//            }
//            else if(lastTurnDirection.equals(WorldSpatial.RelativeDirection.LEFT)){
//                adjustLeft(getOrientation(),delta);
//            }
//        }
//
//    }

//    /**
//     * Try to orient myself to a degree that I was supposed to be at if I am
//     * misaligned.
//     */
//    private void adjustLeft(WorldSpatial.Direction orientation, float delta) {
//
//        switch(orientation){
//            case EAST:
//                if(getAngle() > WorldSpatial.EAST_DEGREE_MIN+EAST_THRESHOLD){
//                    turnRight(delta);
//                }
//                break;
//            case NORTH:
//                if(getAngle() > WorldSpatial.NORTH_DEGREE){
//                    turnRight(delta);
//                }
//                break;
//            case SOUTH:
//                if(getAngle() > WorldSpatial.SOUTH_DEGREE){
//                    turnRight(delta);
//                }
//                break;
//            case WEST:
//                if(getAngle() > WorldSpatial.WEST_DEGREE){
//                    turnRight(delta);
//                }
//                break;
//
//            default:
//                break;
//        }
//
//    }
//
//    private void adjustRight(WorldSpatial.Direction orientation, float delta) {
//        switch(orientation){
//            case EAST:
//                if(getAngle() > WorldSpatial.SOUTH_DEGREE && getAngle() < WorldSpatial.EAST_DEGREE_MAX){
//                    turnLeft(delta);
//                }
//                break;
//            case NORTH:
//                if(getAngle() < WorldSpatial.NORTH_DEGREE){
//                    turnLeft(delta);
//                }
//                break;
//            case SOUTH:
//                if(getAngle() < WorldSpatial.SOUTH_DEGREE){
//                    turnLeft(delta);
//                }
//                break;
//            case WEST:
//                if(getAngle() < WorldSpatial.WEST_DEGREE){
//                    turnLeft(delta);
//                }
//                break;
//
//            default:
//                break;
//        }
//
//    }
//
////
//
//    private WorldSpatial.Direction oppositeOfOrientation(WorldSpatial.Direction dir){
//        switch(dir){
//            case EAST:
//                return WorldSpatial.Direction.WEST;
//            case WEST:
//                return WorldSpatial.Direction.EAST;
//            case NORTH:
//                return WorldSpatial.Direction.SOUTH;
//            case SOUTH:
//                return WorldSpatial.Direction.NORTH;
//        }
//        return null;
//    }


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
                System.out.println("THIS IS BAD LINE 883");
//                return WorldSpatial.RelativeDirection.LEFT;
        }

        //  System.out.println("THIS IS BAD LINE 887");
        return null;

    }

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



