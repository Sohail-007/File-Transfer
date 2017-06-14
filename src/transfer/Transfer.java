/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transfer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 *
 * @author Sohail
 */
public class Transfer extends JApplet {

    private static final int JFXPANEL_WIDTH_INT = 640;
    private static final int JFXPANEL_HEIGHT_INT = 480;
    private static JPanel fxContainer;
    public final static int SOCKET_PORT = 9500;
    public static Label msg;
    private static int clnt = 0;
    private boolean ISserver = false;
    private final static int SIZE = 65535;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
                } catch (Exception e) {
                }

                JFrame frame = new JFrame("Transfer Files");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                JApplet applet = new Transfer();
                applet.init();
                frame.setContentPane(applet.getContentPane());

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                applet.start();
            }
        });
    }

    class ClientThread extends Thread implements Runnable {

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        String[] FILE_TO_SEND = null;
        String[] FILE_NAME = null;
        int cnt;
        double totalSize;
        Socket sock;

        public ClientThread() {
            super();
        }

        public ClientThread(Socket sck, String[] a, String[] b, double t) {
            sock = sck;
            cnt = 0;
            totalSize = t;
            FILE_TO_SEND = new String[a.length];
            FILE_NAME = new String[b.length];
            for (String file : a) {
                FILE_TO_SEND[cnt++] = file;
                System.out.println(file);
            }
            cnt = 0;
            for (String file : b) {
                System.out.println(file);
                FILE_NAME[cnt++] = file;
            }
        }

        @Override
        public void run() {
            try {
                JProgressBar dpg = new JProgressBar(0, 100);
                dpg.setVisible(false);
                dpg.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, 35));
                dpg.setStringPainted(true);
                //JOptionPane.showMessageDialog(fxContainer, "Client connected");
                System.out.println("Accepted connection : " + sock);
                os = sock.getOutputStream();
                PrintStream out = new PrintStream(os);
                System.out.println(totalSize);
                out.print(totalSize + "\n");
                for (int i = 0; i < cnt; i++) {
                    File myFile = new File(FILE_TO_SEND[i]);
                    byte[] mybytearray = new byte[SIZE];
                    fis = new FileInputStream(myFile);
                    bis = new BufferedInputStream(fis);
                    out.print(FILE_NAME[i] + "\n");
                    os.flush();
                    double prog = (double) myFile.length();
                    out.print(Long.toString(myFile.length()) + "\n");
                    os.flush();
                    System.out.println("Sending " + FILE_TO_SEND[i] + "(" + myFile.length() + " bytes)");
                    int bytesRead;
                    double completed = 0;
                    while ((bytesRead = bis.read(mybytearray)) > 0) {
                        os.write(mybytearray, 0, bytesRead);
                        completed += bytesRead;
                        final double com = completed;
                        if ((int) ((completed / prog) * 100) > dpg.getValue()) {
                            SwingUtilities.invokeLater(() -> {
                                if (!dpg.isVisible()) {
                                    dpg.setVisible(true);
                                }
                                int temp = (int) ((com / prog) * 100);
                                dpg.setValue(temp);
                                if (temp == 100) {
                                    dpg.setVisible(false);
                                }
                                fxContainer.revalidate();
                                fxContainer.repaint();
                            });
                        }
                        System.out.println(completed);
                    }
                    os.flush();
                    System.out.println("Done.");
                    //while(servsock.isBound());
                }
                JOptionPane.showMessageDialog(fxContainer, "File successfully sent");
                try {

                    if (bis != null) {
                        bis.close();
                    }
                    if (os != null) {
                        os.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
                }

            } catch (IOException ex) {
                Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void init() {
        fxContainer = new JPanel(new GridLayout(3, 1));
        fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        fxContainer.setBackground(Color.LIGHT_GRAY);
        add(fxContainer, BorderLayout.CENTER);
        try {
            createScene();
        } catch (UnknownHostException ex) {
            Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void createScene() throws UnknownHostException {
        JButton btn = new JButton();
        InetAddress IP = InetAddress.getLocalHost();
        JPanel panel2 = new JPanel();
        panel2.setBackground(Color.lightGray);
        panel2.setVisible(true);
        JButton btn2 = new JButton();
        btn2.setText("RECIEVE");
        JButton dis = new JButton("STOP SERVER");
        dis.setVisible(ISserver);
        btn.setText("SEND");
        btn.addActionListener((java.awt.event.ActionEvent e) -> {
            @SuppressWarnings("null")
            Thread t = new Thread(() -> {
                btn.setEnabled(false);
                btn2.setEnabled(false);
                int cnt = 0;
                double totalSize = 0;
                String[] FILE_TO_SEND = null;
                String[] FILE_NAME = null;
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(fxContainer);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFile = fileChooser.getSelectedFiles();
                    FILE_TO_SEND = new String[selectedFile.length];
                    FILE_NAME = new String[selectedFile.length];
                    for (File file : selectedFile) {
                        FILE_TO_SEND[cnt] = file.getAbsolutePath();
                        FILE_NAME[cnt++] = file.getName();
                        totalSize += (double) file.length();
                    }
                }
                ServerSocket servsock = null;
                if (FILE_NAME != null) {
                    try {
                        servsock = new ServerSocket(SOCKET_PORT);
                        Thread x = new Thread(() -> {
                            JOptionPane.showMessageDialog(fxContainer, "Server started on " + IP.getHostAddress());
                        });
                        x.start();
                        ISserver = true;
                        SwingUtilities.invokeLater(() -> {
                            btn.setEnabled(!ISserver);
                            dis.setVisible(ISserver);
                            btn2.setEnabled(!ISserver);
                            fxContainer.revalidate();
                            fxContainer.repaint();
                        });
                        System.out.println("Waiting...");
                        System.out.println("IP of my system is := " + IP.getHostAddress());
                        final ServerSocket sersoc = servsock;

                        dis.addActionListener((java.awt.event.ActionEvent e1) -> {

                            if (sersoc != null) {
                                ISserver = false;

                                SwingUtilities.invokeLater(() -> {
                                    btn2.setEnabled(!ISserver);
                                    btn.setEnabled(!ISserver);
                                    dis.setVisible(ISserver);
                                    fxContainer.revalidate();
                                    fxContainer.repaint();
                                });

                                try {
                                    sersoc.close();
                                } catch (IOException ex) {
                                    Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        });

                        while (ISserver) {
                            try {
                                Socket sock = servsock.accept();
                                ClientThread client = new ClientThread(sock, FILE_TO_SEND, FILE_NAME, totalSize);
                                client.start();
                            } catch (IOException ex) {
                                Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }

                    } catch (IOException ex) {
                        Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);

                    }
                } else {
                    btn.setEnabled(true);
                    btn2.setEnabled(true);
                }
            });
            t.start();
        });

        btn2.addActionListener((java.awt.event.ActionEvent e) -> {
            Thread t = new Thread(() -> {

                panel2.setLayout(new BorderLayout());
                JProgressBar dpg = new JProgressBar(0, 100);
                dpg.setVisible(false);
                dpg.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, 35));
                dpg.setStringPainted(true);
                panel2.add(dpg, BorderLayout.SOUTH);
                String SERVER = null;
                UIManager.put("OptionPane.okButtonText", "Connect");
                SERVER = JOptionPane.showInputDialog(fxContainer, "Enter ip address:");
                UIManager.put("OptionPane.okButtonText", "OK");
                if (SERVER != null) {
                    System.out.println(System.getProperty("user.home") + "/Downloads");
                    System.out.println("Server found on " + SERVER);
                    int bytesRead = 0;
                    FileOutputStream fos = null;
                    BufferedOutputStream bos = null;
                    Socket sock = null;
                    try {
                        sock = new Socket(SERVER, SOCKET_PORT);
                        InputStream is = sock.getInputStream();
                        System.out.println("Connecting...");
                        BufferedReader input = new BufferedReader(new InputStreamReader(is));
                        double totalSize = Double.parseDouble(input.readLine());
                        double totalComp = 0;
                        double prev = 0;
                        while (totalSize > totalComp) {
                            String FILE_NAME = input.readLine();
                            System.out.println("Recieving " + FILE_NAME);
                            double prog;
                            prog = (double) Long.parseLong(input.readLine());
                            String FILE_TO_RECEIVED = System.getProperty("user.home") + "/Downloads/";
                            FILE_TO_RECEIVED += FILE_NAME;
                            // receive file
                            byte[] mybytearray = new byte[SIZE];
                            fos = new FileOutputStream(FILE_TO_RECEIVED);
                            bos = new BufferedOutputStream(fos);
                            double completed = 0;
                            while (completed < prog) {
                                if ((completed + SIZE) <= prog) {
                                    bytesRead = is.read(mybytearray, 0, SIZE);
                                } else {
                                    bytesRead = is.read(mybytearray, 0, (int) (prog - completed));
                                }
                                bos.write(mybytearray, 0, bytesRead);
                                completed += bytesRead;
                                totalComp += bytesRead;
                                final double com = totalComp;
                                if ((int) ((totalComp / totalSize) * 100) > dpg.getValue()) {

                                    SwingUtilities.invokeLater(() -> {
                                        if (!dpg.isVisible()) {
                                            dpg.setVisible(true);
                                        }
                                        int temp = (int) ((com / totalSize) * 100);
                                        dpg.setValue(temp);
                                        if (temp == 100) {
                                            dpg.setVisible(false);
                                        }
                                        fxContainer.revalidate();
                                        fxContainer.repaint();
                                    });

                                }
                                //System.out.println(dpg.getValue());
                            }
                            System.out.println(totalSize + " : " + totalComp + " : " + completed);
                            //totalComp+=completed;

                            bos.flush();
                            System.out.println("File " + FILE_TO_RECEIVED
                                    + " downloaded (" + completed + " bytes read)");
                            FILE_TO_RECEIVED = System.getProperty("user.home") + "/Downloads/";
                            JOptionPane.showMessageDialog(fxContainer, "Successfully downloaded files at location " + FILE_TO_RECEIVED);
                        }
                        SwingUtilities.invokeLater(() -> {
                            dpg.setVisible(false);
                            fxContainer.repaint();
                        });
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(fxContainer, "SERVER ERROR:Could not establish Connection", "Connection Failed", JOptionPane.ERROR_MESSAGE, null);
                        Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        try {
                            if (fos != null) {
                                fos.close();
                            }
                            if (bos != null) {
                                bos.close();
                            }
                            if (sock != null) {
                                sock.close();
                            }
                        } catch (IOException ex) {
                            Logger.getLogger(Transfer.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            });
            t.start();
        });
        JLabel lbl = new JLabel("File Transfer", SwingConstants.CENTER);

        lbl.setFont(
                new Font("Monotype Corsiva", Font.ITALIC, 50));
        fxContainer.add(lbl);
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        panel.setBackground(Color.LIGHT_GRAY);
        Dimension x = new Dimension();

        x.setSize(
                200, 50);
        btn.setPreferredSize(x);

        btn.setBackground(
                new Color(0, 255, 128));
        btn.setFont(
                new Font("Arial", Font.PLAIN, 25));
        btn2.setPreferredSize(x);

        btn2.setBackground(
                new Color(0, 128, 255));
        btn2.setFont(
                new Font("Arial", Font.PLAIN, 25));

        dis.setBackground(
                new Color(255, 64, 64));
        dis.setFont(
                new Font("Arial", Font.PLAIN, 25));

        c.gridx = 0;
        c.gridy = 0;

        panel.add(btn, c);
        c.gridy = 0;
        c.gridx = 1;
        c.insets = new Insets(0, 50, 0, 0);

        panel.add(btn2, c);
        c.gridy = 1;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 2;
        c.insets = new Insets(25, 0, 0, 0);
        panel.add(dis, c);

        c.gridheight = 1;
        fxContainer.add(panel, c);
        fxContainer.add(panel2, BorderLayout.SOUTH);
    }
}
