/*
 * Student Info: Name=Xiaolei Zhao, ID=16117 
 * Subject: CS532B_HW2_Fall_2016  * Author: mandy
 * Filename: RockPaperScissorsConstant.java
 * Date and Time: Oct 31, 2016 3:39:38 PM
 * Project Name: Xiaolei_16117_CS532B_HW3
 */
package xiaolei_16117_cs532b_hw3;

/**
 *
 * @author mandy
 */
public interface RockPaperScissorsConstant {
    public static int PLAYER1 = 1; // Indicate player 1
    public static int PLAYER2 = 2; // Indicate player 2
    public static int PLAYER2_JOINED = 5; // Indicate player 2 won
    public static int PLAYER1_WON = 1; // Indicate player 1 won
    public static int PLAYER2_WON = 2; // Indicate player 2 won
    public static int DRAW = 3; // Indicate a draw
    public static int CONTINUE = 4; // Indicate to continue
    
    public static final int EXIT = -1;
    public static final int CLIENTSTARTED = 0;
    public static final int READY = 1;
    public static final int THROW = 2;
    
    
    public static final int YOUCANREADY = 1;
    public static final int YOUCANTHROW = 2;
    
    
    public static final int ROCK = 1;
    public static final int PAPER = 2;
    public static final int SCISSORS = 3;
    public static final int TIMEOUT = -1000;
}
