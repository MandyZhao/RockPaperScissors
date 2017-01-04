/*
 * Student Info: Name=Xiaolei Zhao, ID=16117 
 * Subject: CS532B_HW2_Fall_2016  * Author: mandy
 * Filename: RockPaperScissorsServer.java
 * Date and Time: Oct 31, 2016 3:39:47 PM
 * Project Name: Xiaolei_16117_CS532B_HW3
 */
package xiaolei_16117_cs532b_hw3;

import java.awt.BorderLayout;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author mandy
 */
public class RockPaperScissorsServer extends JFrame
        implements RockPaperScissorsConstant {

    DefaultListModel<Integer> sessions = new DefaultListModel<>();

    public static void main(String[] args) {
        RockPaperScissorsServer frame = new RockPaperScissorsServer();
    }

    public RockPaperScissorsServer() {
        JTextArea jtaLog = new JTextArea();

        // Create a scroll pane to hold text area
        JScrollPane scrollPane = new JScrollPane(jtaLog);
        // Add the scroll pane to the frame
        add(scrollPane, BorderLayout.CENTER);
        JPanel pList = new JPanel(new BorderLayout());
//        paneRight.setPreferredSize(new Dimension(80, 0));// Fixed width

        JList<Integer> listBox = new JList<>(sessions);
        pList.add(listBox, BorderLayout.CENTER);
        JLabel lblText = new JLabel("Session List");
        pList.add(lblText, BorderLayout.NORTH);
        add(pList, BorderLayout.WEST);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 300);
        setTitle("RockPaperScissorsServer");
        setVisible(true);

        try {
            // Create a server socket
            ServerSocket serverSocket = new ServerSocket(8000);
            jtaLog.append(new Date()
                    + ": Server started at socket 8000\n");

            // Number a session
            int sessionNo = 1;

            // Ready to create a session for every two players
            while (true) {
                jtaLog.append(new Date()
                        + ": Wait for players to join session " + sessionNo + '\n');

                // Connect to player 1
                Socket player1 = serverSocket.accept();

                jtaLog.append(new Date() + ": Player 1 joined session "
                        + sessionNo + '\n');
                jtaLog.append("Player 1's IP address"
                        + player1.getInetAddress().getHostAddress() + '\n');

                // Notify that the player is Player 1
                new DataOutputStream(
                        player1.getOutputStream()).writeInt(PLAYER1);

                // Connect to player 2
                Socket player2 = serverSocket.accept();

                jtaLog.append(new Date()
                        + ": Player 2 joined session " + sessionNo + '\n');
                jtaLog.append("Player 2's IP address"
                        + player2.getInetAddress().getHostAddress() + '\n');
                sessions.addElement(sessionNo);
                // Notify that the player is Player 2
                new DataOutputStream(
                        player2.getOutputStream()).writeInt(PLAYER2);

                // Display this session and increment session number
                jtaLog.append(new Date() + ": Start a thread for session "
                        + sessionNo++ + '\n');

                // Create a new thread for this session of two players
                HandleASession task = new HandleASession(player1, player2);

                // Start the new thread
                new Thread(task).start();
            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public synchronized void RemoveMe(int sessionId) {
        sessions.removeElement(sessionId);
    }
}

class HandleASession implements Runnable, RockPaperScissorsConstant {

    private Socket player1;
    private Socket player2;

    // Create and initialize cells
    //private char[][] cell = new char[3][3];
    private DataInputStream fromPlayer1;
    private DataOutputStream toPlayer1;
    private DataInputStream fromPlayer2;
    private DataOutputStream toPlayer2;

    // Continue to play
    private boolean continueToPlay = true;

    /**
     * Construct a thread
     */
    public HandleASession(Socket player1, Socket player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    private boolean checkIfquit(int player, int x) {
        DataOutputStream otherPlayer = (player == PLAYER1) ? toPlayer2 : toPlayer1;
        if (x == EXIT) {
            try {
                otherPlayer.writeInt(EXIT);
            } catch (IOException ex) {
                Logger.getLogger(HandleASession.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        }
        return false;
    }

    /**
     * Implement the run() method for the thread
     */
    public void run() {
        try {
            // Create data input and output streams
            fromPlayer1 = new DataInputStream(
                    player1.getInputStream());
            toPlayer1 = new DataOutputStream(
                    player1.getOutputStream());
            fromPlayer2 = new DataInputStream(
                    player2.getInputStream());
            toPlayer2 = new DataOutputStream(
                    player2.getOutputStream());

            // Write anything to notify player 1 to start
            // This is just to let player 1 know to ready
            toPlayer1.writeInt(PLAYER2_JOINED);
            int msgPlay1 = EXIT;
            int msgPlay2 = EXIT;

            int player1WonTimes = 0;
            int player2WonTimes = 0;
            // Continuously serve the players and determine and report
            // the game status to the players
            while (true) {

                toPlayer1.writeInt(YOUCANREADY);
                toPlayer2.writeInt(YOUCANREADY);
                msgPlay1 = fromPlayer1.readInt();
                if (checkIfquit(PLAYER1, msgPlay1)) {
                    break;
                }
                msgPlay2 = fromPlayer2.readInt();
                if (checkIfquit(PLAYER2, msgPlay2)) {
                    break;
                }

                toPlayer1.writeInt(YOUCANTHROW);
                toPlayer2.writeInt(YOUCANTHROW);
                msgPlay1 = fromPlayer1.readInt();
                if (checkIfquit(PLAYER1, msgPlay1)) {
                    break;
                }
                msgPlay2 = fromPlayer2.readInt();
                if (checkIfquit(PLAYER2, msgPlay2)) {
                    break;
                }

                int winner = Result(msgPlay1, msgPlay2);
                if (winner == PLAYER1) {
                    player1WonTimes++;
                } else if (winner == PLAYER2) {
                    player2WonTimes++;
                }

                toPlayer1.writeInt(winner);
                toPlayer2.writeInt(winner);

                toPlayer1.writeInt(msgPlay2);
                toPlayer2.writeInt(msgPlay1);

                toPlayer1.writeInt(player1WonTimes);
                toPlayer1.writeInt(player2WonTimes);
                toPlayer2.writeInt(player1WonTimes);
                toPlayer2.writeInt(player2WonTimes);

            }
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    private int Result(int player1, int player2) {
        if (player1 == player2) {
            return DRAW;
        }

        if (player1 == TIMEOUT) {
            return PLAYER2_WON;
        } else if (player2 == TIMEOUT) {
            return PLAYER1_WON;
        }

        if (player1 - 1 == player2 || player1 + 2 == player2) {
            return PLAYER1_WON;
        } else {
            return PLAYER2_WON;
        }
    }

}
