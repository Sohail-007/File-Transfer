/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transfer2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Label;
import java.awt.event.ActionListener;
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
public class Transfer2 extends JApplet {

    private static final int JFXPANEL_WIDTH_INT = 640;
    private static final int JFXPANEL_HEIGHT_INT = 480;
    private static JPanel fxContainer;
    public final static int SOCKET_PORT = 9500;
    public static Label msg;

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

                JApplet applet = new Transfer2();
                applet.init();
                frame.setContentPane(applet.getContentPane());

                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                applet.start();
            }
        });
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
            Logger.getLogger(Transfer2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @throws UnknownHostExcept2ion
     */
    public void createScene() throws UnknownHostException {
        JButton btn = new JButton();
        InetAddress IP = InetAddress.getLocalHost();
        JPanel panel2 = new JPanel(new BorderLayout());
        panel2.setBackground(Color.lightGray);
        panel2.setVisible(true);
        JProgressBar dpg = new JProgressBar(0, 100);
        dpg.setVisible(false);
        dpg.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, 35));
        dpg.setStringPainted(true);
        panel2.add(dpg, BorderLayout.SOUTH);
        btn.setText("SEND");
        btn.addActionListener(new ActionListener() {

            @Override
            @SuppressWarnings("empty-statement")
            public void actionPerformed(java.awt.event.ActionEvent e) {

                Thread t = new Thread(() -> {
                    String FILE_TO_SEND = null;
                    String FILE_NAME = null;
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                    int result = fileChooser.showOpenDialog(fxContainer);
                    if (result == JFileChooser.APPROVE_OPTION) {
                        File selectedFile = fileChooser.getSelectedFile();
                        System.out.println("Selected file: " + selectedFile.getAbsolutePath() + " and name is " + selectedFile.getName());
                        FILE_TO_SEND = selectedFile.getAbsolutePath();
                        FILE_NAME = selectedFile.getName() + "\n";
                    }
                    FileInputStream fis = null;
                    BufferedInputStream bis = null;
                    OutputStream os = null;
                    ServerSocket servsock = null;
                    Socket sock = null;
                    try {
                        JOptionPane.showMessageDialog(fxContainer, "Server started on " + IP.getHostAddress());
                        servsock = new ServerSocket(SOCKET_PORT);
                        System.out.println("Waiting...");
                        System.out.println("IP of my system is := " + IP.getHostAddress());

                        try {
                            sock = servsock.accept();
                            JOptionPane.showMessageDialog(fxContainer, "Client connected");
                            System.out.println("Accepted connection : " + sock);
                            File myFile = new File(FILE_TO_SEND);
                            byte[] mybytearray = new byte[65535];
                            fis = new FileInputStream(myFile);
                            bis = new BufferedInputStream(fis);
                            os = sock.getOutputStream();
                            PrintStream out = new PrintStream(os);
                            out.print(FILE_NAME);
                            os.flush();
                            System.out.println(mybytearray.length);
                            double prog = (double) myFile.length();
                            out.print(Long.toString(myFile.length()) + "\n");
                            os.flush();
                            System.out.println("Sending " + FILE_TO_SEND + "(" + myFile.length() + " bytes)");
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
                            JOptionPane.showMessageDialog(fxContainer, "File successfully sent");
                        } finally {
                            try {
                                if (bis != null) {
                                    bis.close();
                                }
                                if (os != null) {
                                    os.close();
                                }
                                if (sock != null) {
                                    sock.close();
                                }
                            } catch (IOException ex) {
                                Logger.getLogger(Transfer2.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(Transfer2.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        if (servsock != null) {
                            try {
                                servsock.close();
                            } catch (IOException ex) {
                                Logger.getLogger(Transfer2.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                });
                t.start();
            }
        });
        JButton btn2 = new JButton();
        btn2.setText("RECIEVE");

        btn2.addActionListener(new ActionListener() {

            @Override
            @SuppressWarnings("empty-statement")
            public void actionPerformed(java.awt.event.ActionEvent e) {

                Thread t = new Thread(() -> {
                    String SERVER = null;
                    UIManager.put("OptionPane.okButtonText", "Connect");
                    SERVER = JOptionPane.showInputDialog(fxContainer, "Enter ip address:");
                    UIManager.put("OptionPane.okButtonText", "OK");
                    String FILE_TO_RECEIVED = System.getProperty("user.home") + "/Downloads/";
                    System.out.println(System.getProperty("user.home") + "/Downloads");
                    System.out.println("Server found on " + SERVER);
                    int bytesRead = 1;
                    FileOutputStream fos = null;
                    BufferedOutputStream bos = null;

                    Socket sock = null;
                    try {
                        sock = new Socket(SERVER, SOCKET_PORT);
                        System.out.println("Connecting...");
                        BufferedReader input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        String FILE_NAME = input.readLine();
                        final double prog;
                        prog = (double) Long.parseLong(input.readLine());
                        FILE_TO_RECEIVED += FILE_NAME;
                        // receive file
                        InputStream is = sock.getInputStream();
                        byte[] mybytearray = new byte[65535];
                        fos = new FileOutputStream(FILE_TO_RECEIVED);
                        bos = new BufferedOutputStream(fos);
                        System.out.println("success");
                        double completed = 0;
                        System.out.println("success1");
                        while ((bytesRead = is.read(mybytearray)) > 0) {
                            bos.write(mybytearray, 0, bytesRead);
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
                        }
                        SwingUtilities.invokeLater(() -> {
                            dpg.setVisible(false);
                            fxContainer.repaint();
                        });
                        bos.flush();
                        System.out.println("File " + FILE_TO_RECEIVED
                                + " downloaded (" + completed + " bytes read)");
                        JOptionPane.showMessageDialog(fxContainer, FILE_NAME + " successfully downloaded at location " + FILE_TO_RECEIVED);
                    } catch (IOException ex) {
                        Logger.getLogger(Transfer2.class.getName()).log(Level.SEVERE, null, ex);
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
                            Logger.getLogger(Transfer2.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                });
                t.start();
            }
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
        c.gridx = 0;
        c.gridy = 0;

        panel.add(btn, c);
        c.gridy = 1;
        c.insets = new Insets(30, 0, 0, 0);

        panel.add(btn2, c);
        c.gridheight = 2;
        fxContainer.add(panel, c);
        fxContainer.add(panel2, BorderLayout.SOUTH);
    }
}
