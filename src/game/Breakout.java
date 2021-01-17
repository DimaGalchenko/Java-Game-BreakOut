package game;

import acm.graphics.GLabel;
import acm.graphics.GObject;
import acm.graphics.GOval;
import acm.graphics.GRect;
import acm.util.RandomGenerator;
import com.shpp.cs.a.graphics.WindowProgram;

import java.awt.*;
import java.awt.event.MouseEvent;

public class Breakout extends WindowProgram {
    /** Width and height of application window in pixels */
    public static final int APPLICATION_WIDTH = 400;
    public static final int APPLICATION_HEIGHT = 600;

    /** Dimensions of game board (usually the same) */
    private static int WIDTH = APPLICATION_WIDTH;
    private static int HEIGHT = APPLICATION_HEIGHT;

    /** Dimensions of the paddle */
    private static final int PADDLE_WIDTH = 60;
    private static final int PADDLE_HEIGHT = 2;

    /** Offset of the paddle up from the bottom */
    private static final int PADDLE_Y_OFFSET = 30;

    /** Number of bricks per row */
    private static final int NBRICKS_PER_ROW = 10;

    /** Number of rows of bricks */
    private static final int NBRICK_ROWS = 10;

    /** Separation between bricks */
    private static final int BRICK_SEP = 4;

    /** Width of a brick */
    private static final int BRICK_WIDTH =
            (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

    /** Height of a brick */
    private static final int BRICK_HEIGHT = 8;

    /** Radius of the ball in pixels */
    private static final int BALL_RADIUS = 7;

    /** Offset of the top brick row from the top */
    private static final int BRICK_Y_OFFSET = 70;

    /** Number of turns */
    private static int nTurns = 3;

    /**Variables responsible for movement in x and y*/
    private double vx, vy;

    /**Score of the broken bricks*/
    private int score = 0;

    /** Variables responsible for rectangle, looking like rocket*/
    private final GRect rocket = createRocket();

    /** Variables responsible for oval, looking like ball*/
    private final GOval ball = createBall();

    /** Matrix of the rectangle that are bricks*/
    private final GRect[][] matrixOfBricks = new GRect[NBRICKS_PER_ROW][NBRICK_ROWS];

    /** Label that demonstrate score*/
    private final GLabel scoreOfBricks = new GLabel("Score: " + score);

    /** Label that demonstrate number of score*/
    private final GLabel numberOfTurns = new GLabel("Turns: " + nTurns);

    /** Label that demonstrate label GameOver*/
    private final GLabel label = new GLabel("GameOver!");

    /** Variables that save status of game*/
    private boolean isGameStopped = true;


    public void run() {
        //set size of screen
        WIDTH = getWidth();
        HEIGHT = getHeight();
        setSize(WIDTH,HEIGHT);
        //this is cycle which consists three games
        while(nTurns!=0){
            initGameObjects();
            addMouseListeners();
            this.waitForClick();
            gameLogic();
            if (isGameStopped) break;
            clearAllField();
        }
    }

    /**
     * Method that set random color of rocket
     */
    public void setRandomColorForRocket(){
        rocket.setColor(RandomGenerator.getInstance().nextColor());
    }

    /**
     * Method which rendering all objects on the game field
     */
    public void initGameObjects(){
        createMatrixOfBrick();
        add(rocket,WIDTH/2.0-PADDLE_WIDTH/2.0,HEIGHT-PADDLE_Y_OFFSET);
        add(ball,rocket.getX()+PADDLE_WIDTH/2.0-BALL_RADIUS,HEIGHT-PADDLE_Y_OFFSET-BALL_RADIUS*2);
        add(scoreOfBricks,10,10);
        add(numberOfTurns,20+scoreOfBricks.getWidth(),10);
    }

    /**
     * Method which sets values dx and dy
     * For dx method sets random value from 1.0 to 3.0
     * For dy method sets value 3.0
     */
    public void initDxDy(){
        RandomGenerator rgen = RandomGenerator.getInstance();
        vx = rgen.nextDouble(1.0, 3.0);
        if (rgen.nextBoolean(0.5))
            vx = -vx;
        vy=3.0;
    }

    /**
     * Method show result in upper left corner
     * @param score - number of broken bricks
     * @param nTurns - number of turns
     */
    public void showScore(int score,int nTurns){
        scoreOfBricks.setLabel("Score: " + score);
        numberOfTurns.setLabel("Turns: " + nTurns);
    }

    /**
     * Method which clear all objects on the field
     */
    public void clearAllField(){
        remove(rocket);
        remove(ball);
        for(int i =0 ; i<NBRICKS_PER_ROW; i++){
            for(int j = 0;j<NBRICK_ROWS;j++){
                remove(matrixOfBricks[i][j]);
            }
        }
        remove(numberOfTurns);
        remove(scoreOfBricks);
        remove(label);
    }

    /**
     * Main method which respond for the game logic
     */
    public void gameLogic(){
        initDxDy();
        isGameStopped = false;
        while(!isGameStopped){
            GObject collider = getCollidingObject();
        if(ballCollapseWithLeftAndRightWall()){
            vx*=(-1);
        }
        if(ballCollapseWithTopWall()){
            vy*=(-1);
        }
        if(collider == rocket){
            vy*=(-1);
            collidingWithRocket(collider);
            setRandomColorForRocket();
        }else if(collider !=null &&
                collider !=label &&
                collider !=numberOfTurns &&
                collider !=scoreOfBricks){
            vy*=(-1);
            remove(collider);
            score++;
        }

        if(score == (NBRICK_ROWS*NBRICKS_PER_ROW)){
            gameWin();
            isGameStopped = true;
            break;
        }
        if(ballCollapseWithBottomWall()){
            gameOver();
            break;
        }
        ball.move(vx,vy);
        showScore(score,nTurns);
        pause(10);
        }

    }

    /**
     * Show that positive result if game end with win
     */
    private void gameWin() {
        clearAllField();
        label.setLabel("You Win!!!");
        label.setFont(new Font("Arial",Font.BOLD,20));
        label.setColor(Color.orange);
        add(label,WIDTH/2.0-label.getWidth()/2,HEIGHT/2.0);
        pause(2000);
    }

    /**
     * Method that create matrix of GRect objects with pause in 3 milliseconds
     */
    public void createMatrixOfBrick(){
        for(int i =0 ; i<NBRICKS_PER_ROW; i++){
            for(int j = 0;j<NBRICK_ROWS;j++){
                matrixOfBricks[i][j] = createBrick(getColor(j));
                add(matrixOfBricks[i][j],
                        i*BRICK_WIDTH+i*BRICK_SEP,BRICK_Y_OFFSET+BRICK_HEIGHT*j+BRICK_SEP*j);
                pause(3);
            }
        }
    }


    /**
     * Method that return color which depending on brick's index in matrix
     * @param i - index of brick in matrix
     * @return return color depending on index
     */
    private Color getColor(int i) {
        if(i%10<2){
            return Color.RED;
        }else if(i%10<=3){
            return Color.ORANGE;
        }else if(i%10<=5){
            return Color.YELLOW;
        }else if(i%10<=7){
            return Color.GREEN;
        }else {
            return Color.CYAN;
        }

    }

    /**
     * Method which create brick base on object GRect
     * @param color - color of GRect
     * @return GRect that are brick
     */
    public GRect createBrick(Color color){
        GRect brick = new GRect(BRICK_WIDTH,BRICK_HEIGHT);
        brick.setFilled(true);
        brick.setColor(color);
        return brick;
    }

    /**
     * Method that return if ball collapse with wall
     * @return true if ball collapse with left or right wall
     */
    private boolean ballCollapseWithLeftAndRightWall() {
        return ball.getX()+BALL_RADIUS*2>WIDTH || ball.getX()<0;
    }

    /**
     * Method that return if ball collapse with wall
     * @return true if ball collapse with top wall
     */
    private boolean ballCollapseWithTopWall() {
        return  ball.getY()<0;
    }

    /**
     * Method that return if ball collapse with wall
     * @return true if ball collapse with bottom wall
     */
    private boolean ballCollapseWithBottomWall(){
        return ball.getY()+BALL_RADIUS*2>HEIGHT;
    }

    /**
     * Method that return if ball collapse with object and return this object
     * @return the object that the ball hit
     */
    private GObject getCollidingObject(){
        GObject[] collidingObjects = new GObject[4];
        collidingObjects[0] = getCollidingObjectWithBottomLeft();
        collidingObjects[1] = getCollidingObjectWithBottomRight();
        collidingObjects[2] = getCollidingObjectWithTopLeft();
        collidingObjects[3] = getCollidingObjectWithTopRight();
        for(GObject object: collidingObjects){
            if(object!=null) return object;
        }
        return null;
    }

    private GObject getCollidingObjectWithTopLeft(){
        if(getElementAt(ball.getX(),ball.getY())!=null){
            return getElementAt(ball.getX(),ball.getY());
        }
        return null;
    }

    private GObject getCollidingObjectWithTopRight(){
        if(getElementAt(ball.getX()+BALL_RADIUS*2,ball.getY())!=null){
            return getElementAt(ball.getX()+BALL_RADIUS*2,ball.getY());
        }
        return null;
    }

    private GObject getCollidingObjectWithBottomRight(){
        if(getElementAt(ball.getX()+BALL_RADIUS*2,ball.getY()+BALL_RADIUS*2)!=null){
            return getElementAt(ball.getX()+BALL_RADIUS*2,ball.getY()+BALL_RADIUS*2);
        }
        return null;
    }

    private GObject getCollidingObjectWithBottomLeft(){
        if(getElementAt(ball.getX(),ball.getY()+BALL_RADIUS*2)!=null){
            return getElementAt(ball.getX(),ball.getY()+BALL_RADIUS*2);
        }
        return null;
    }

    /**
     * Method which beats of ball in that direction, depending on which part of the rocket the ball hit
     * @param collider - collider object, in this situation it's rocket
     */
    private void collidingWithRocket(Object collider){
        if(getElementAt(ball.getX(),ball.getY()+BALL_RADIUS)!=null){
            vy*=-1;
            vx*=-1;
        }
        if(getElementAt(ball.getX()+BALL_RADIUS*2,ball.getY()+BALL_RADIUS)!=null){
            vy*=-1;
            vx*=-1;
        }
        if(ball.getX()<((GRect)collider).getX()+((GRect)collider).getWidth()/2 &&
                ball.getX()>((GRect)collider).getX()){
            if(vx>0){
                vx*=(-1);
            }else{
                vx*=1;
            }
        }else if(ball.getX()>((GRect)collider).getX()+((GRect)collider).getWidth()/2 &&
                ball.getX()<((GRect)collider).getX()+((GRect)collider).getWidth()){
            if(vx<0){
                vx*=(-1);
            }else{
                vx*=1;
            }
        }
    }

    /**
     * Method that crate rocket
     * @return GRect looking like rocket
     */
    public GRect createRocket(){
        GRect rocket = new GRect(PADDLE_WIDTH,PADDLE_HEIGHT);
        rocket.setFilled(true);
        rocket.setColor(Color.BLACK);
        //add(rocket,WIDTH/2-PADDLE_WIDTH/2,HEIGHT-PADDLE_Y_OFFSET);
        return rocket;
    }

    /**
     * Method that crate ball
     * @return GOval looking like ball
     */
    public GOval createBall(){
        GOval ball = new GOval(BALL_RADIUS*2,BALL_RADIUS*2);
        ball.setFilled(true);
        ball.setColor(Color.GREEN);
        return ball;
    }

    /**
     * Mouse that listening event with mouse
     * @param mouseEvent event with mouse
     */
    @Override
    public void mouseMoved(MouseEvent mouseEvent) {

            if (ball.getY() == HEIGHT - PADDLE_Y_OFFSET - BALL_RADIUS * 2) {
                if (mouseEvent.getX() > PADDLE_WIDTH / 2 && mouseEvent.getX() < WIDTH - PADDLE_WIDTH / 2)
                    if(isGameStopped){
                        rocket.setLocation(mouseEvent.getX() - PADDLE_WIDTH / 2.0 - 15,
                            HEIGHT - PADDLE_Y_OFFSET);
                        ball.setLocation(rocket.getX() + rocket.getWidth() / 2 - BALL_RADIUS ,
                        HEIGHT - PADDLE_Y_OFFSET - BALL_RADIUS * 2);}
                pause(10);
            } else {
                if (mouseEvent.getX() > PADDLE_WIDTH / 2 && mouseEvent.getX() < WIDTH - PADDLE_WIDTH / 2)
                    rocket.setLocation(mouseEvent.getX() - PADDLE_WIDTH / 2.0, HEIGHT - PADDLE_Y_OFFSET);
                pause(10);
            }


    }

    /**
     * Method show label "Game over" ,sets value of score zero and decrement nTurn and make pause
     *
     */
    public void gameOver(){
        label.setFont(new Font("Arial",Font.BOLD,20));
        label.setColor(Color.orange);
        add(label,WIDTH/2.0-label.getWidth()/2,HEIGHT/2.0);
        nTurns--;
        score = 0;
        showScore(0,nTurns);
        pause(2000);
    }

    private class Main {
    }
}
