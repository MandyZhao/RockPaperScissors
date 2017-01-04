package xiaolei_16117_cs532b_hw3;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;
//import static xiaolei_16117_cs532b_hw3.RockPaperScissorsConstant.TIMEOUT;

/*
 * Student Info: Name=Xiaolei Zhao, ID=16117 
 * Subject: CS532B_HW2_Fall_2016  
 * Author: mandy
 * Filename: RockPaperScissorsClient.java
 * Date and Time: Oct 31, 2016 3:38:40 PM
 * Project Name: Xiaolei_16117_CS532B_HW3
 */
/**
 *
 * @author mandy
 */
public class RockPaperScissorsClient extends JFrame
        implements Runnable, RockPaperScissorsConstant {

    // Create and initialize a title label
    private JLabel jlblTitle = new JLabel();

    // Create and initialize a status label
    private JLabel jlblStatus = new JLabel();
    private JLabel logInfo = new JLabel();
    // Input and output streams from/to server
    private DataInputStream fromServer;
    private DataOutputStream toServer;

    // Continue to play?
    private boolean continueToPlay = true;

    // Wait for the player to mark a cell
    private boolean waitingReady = true;
    private boolean waitingThrow = true;

    private int clientStatus = CLIENTSTARTED;
    // Host name or ip
    private String host = "localhost";

    private ImageIcon rock = new ImageIcon("rock.png");
    private ImageIcon paper = new ImageIcon("paper.png");
    private ImageIcon scissors = new ImageIcon("scissor.png");
    private ImageIcon empty = null;

    private int myThrow = ROCK;
    private int otherThrow = ROCK;
    private boolean quit = false;

    JLabel lbPlayer1 = new JLabel();
    JLabel lbPlayer2 = new JLabel();

    int player = -1;

    /**
     * Initialize UI
     */
    public RockPaperScissorsClient() {
        // Panel p to hold cells
        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        lbPlayer1.setHorizontalAlignment(JLabel.CENTER);
        lbPlayer1.setBorder(BorderFactory.createRaisedBevelBorder());

        lbPlayer2.setHorizontalAlignment(JLabel.CENTER);
        lbPlayer2.setBorder(BorderFactory.createRaisedBevelBorder());

        JPanel lbBtns = new JPanel();//default layout
        JButton Ready = new JButton("Ready");
        JButton Rock = new JButton("Rock");
        JButton Paper = new JButton("Paper");
        JButton Scissors = new JButton("Scissors");
        JButton Quit = new JButton("Quit");
        lbBtns.add(Ready);
        lbBtns.add(Rock);
        lbBtns.add(Paper);
        lbBtns.add(Scissors);
        lbBtns.add(Quit);

        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        p.add(lbPlayer1, c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        p.add(lbPlayer2, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weighty = 0;
        p.add(lbBtns, c);

        Ready.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                waitingReady = false;
            }
        });
        Rock.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (myThrow != TIMEOUT) {
                    myThrow = ROCK;
                    waitingThrow = false;
                }

            }
        });
        Paper.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (myThrow != TIMEOUT) {
                    myThrow = PAPER;
                    waitingThrow = false;
                }
            }
        });
        Scissors.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (myThrow != TIMEOUT) {
                    myThrow = SCISSORS;
                    waitingThrow = false;
                }
            }
        });
        Quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                quit = true;
                waitingReady = false;
                waitingThrow = false;
                jlblStatus.setText("You chose to quit");
                try {
                    toServer.writeInt(EXIT);
                } catch (IOException ex) {
                    Logger.getLogger(RockPaperScissorsClient.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        // Set properties for labels and borders for labels and panel
        p.setBorder(new LineBorder(Color.black, 1));
        jlblTitle.setHorizontalAlignment(JLabel.CENTER);
        jlblTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        jlblTitle.setBorder(new LineBorder(Color.black, 1));
        JPanel plog = new JPanel();
        plog.setLayout(new GridLayout(0, 1));

        plog.add(logInfo);
        jlblStatus.setBorder(new LineBorder(Color.black, 1));
        plog.add(jlblStatus);
        // Place the panel and the labels to the applet
        add(jlblTitle, BorderLayout.NORTH);
        add(p, BorderLayout.CENTER);
        add(plog, BorderLayout.SOUTH);
        setTitle("RockPaperScissorsClient");
        // Connect to the server
        connectToServer();

        // Display the frame
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void connectToServer() {
        try {
            // Create a socket to connect to the server
            Socket socket;
            socket = new Socket(host, 8000);

            // Create an input stream to receive data from the server
            fromServer = new DataInputStream(socket.getInputStream());

            // Create an output stream to send data to the server
            toServer = new DataOutputStream(socket.getOutputStream());
        } catch (Exception ex) {
            System.err.println(ex);
        }

        // Control the game on a separate thread
        Thread thread = new Thread(this);
        thread.start();
    }

    private void waitForReady() throws InterruptedException {
        while (waitingReady) {
            Thread.sleep(100);
        }

        waitingReady = true;
    }

    // Return that the Whether or not the player throws in time
    private void waitForThrow() throws InterruptedException {
        int counter = 0;
        while (waitingThrow) {
            Thread.sleep(100);
            counter++;
            if (counter % 10 == 0) {
                int i = counter / 10;
                jlblStatus.setText("There is " + (5 - i) + " seconds left");
            }
            if (counter > 50) {
                myThrow = TIMEOUT;
                jlblStatus.setText("Times out.");
                break;
            }
        }
        waitingThrow = true;

    }

    private void setImage(int player, int img) {
        JLabel lbtoChange = null;
        if (player == PLAYER1) {
            lbtoChange = lbPlayer1;
        } else {
            lbtoChange = lbPlayer2;
        }
        if (img == ROCK) {
            lbtoChange.setIcon(rock);
        } else if (img == PAPER) {
            lbtoChange.setIcon(paper);
        } else if (img == SCISSORS) {
            lbtoChange.setIcon(scissors);
        }
    }

    private boolean checkIfquit(int x) {
        if (x == EXIT) {
            if (player == PLAYER1) {
                jlblStatus.setText("Player 2 quit.");
            } else if (player == PLAYER2) {
                jlblStatus.setText("Player 1 quit.");
            }
            return true;
        }
        return false;
    }

    public void run() {
        try {
            // Get notification from the server
            player = fromServer.readInt();
            // Am I player 1 or 2?
            if (player == PLAYER1) {
                jlblTitle.setText("Player 1");
                jlblStatus.setText("Waiting for player 2 to join.");
                // The other player has joined

            } else if (player == PLAYER2) {
                jlblTitle.setText("Player 2");
                jlblStatus.setText("Waiting for player 1 ready.");
            }

            if (player == PLAYER1) {
                int msg = fromServer.readInt();
                jlblStatus.setText("Player 2 joined.");
            }
//            // Continue to play
            while (true) {
                int ifReady = fromServer.readInt();
                if (checkIfquit(ifReady)) {
                    break;
                }
                if (player == PLAYER1) {
                    jlblStatus.setText("You can ready.");

                } else if (player == PLAYER2) {
                    jlblStatus.setText("You can ready.");
                }

                waitForReady();
                toServer.writeInt(READY);
                int ifThrow = fromServer.readInt();
                if (checkIfquit(ifThrow)) {
                    break;
                }
                waitForThrow();
                setImage(player, myThrow);
                toServer.writeInt(myThrow);
                int result = fromServer.readInt();
                if (checkIfquit(result)) {
                    break;
                }
                if (player == PLAYER2) {
                    int img = fromServer.readInt();
                    if (checkIfquit(img)) {
                        break;
                    }
                    setImage(PLAYER1, img);
                } else if (player == PLAYER1) {
                    int img = fromServer.readInt();
                    if (checkIfquit(img)) {
                        break;
                    }
                    setImage(PLAYER2, img);
                }
                int player1_winTimes = fromServer.readInt();
                if (checkIfquit(player1_winTimes)) {
                    break;
                }
                int player2_winTimes = fromServer.readInt();
                if (checkIfquit(player2_winTimes)) {
                    break;
                }

                if (result == PLAYER1_WON) {
                    if (player == PLAYER1) {
                        logInfo.setText("You won!\n" + "You won " + player1_winTimes + " times,\n"
                                + "Player 2 won " + player2_winTimes + " times");

                    } else if (player == PLAYER2) {
                        logInfo.setText("Player 1 won.\n" + "You won " + player2_winTimes + " times,\n"
                                + "Player 1 won " + player1_winTimes + " times");
                    }
                } else if (result == PLAYER2_WON) {
                    if (player == PLAYER1) {
                        logInfo.setText("Player 2 won.\n" + "You won " + player1_winTimes + " times,\n"
                                + "Player 2 won " + player2_winTimes + " times");

                    } else if (player == PLAYER2) {
                        logInfo.setText("You won!\n" + "You won " + player2_winTimes + " times,\n"
                                + "Player 1 won " + player1_winTimes + " times");
                    }
                } else if (result == DRAW) {
                    if (player == PLAYER1) {
                        logInfo.setText("Draw.\n" + "You won " + player1_winTimes + " times,\n"
                                + "Player 2 won " + player2_winTimes + " times");

                    } else if (player == PLAYER2) {
                        logInfo.setText("Draw.\n" + "You won " + player2_winTimes + " times,\n"
                                + "Player 1 won " + player1_winTimes + " times");
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    /**
     * This main method enables the applet to run as an application
     */
    public static void main(String[] args) {
        // Create a frame
        RockPaperScissorsClient frame = new RockPaperScissorsClient();
    }

}
