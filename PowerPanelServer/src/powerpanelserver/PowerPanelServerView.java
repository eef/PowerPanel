package powerpanelserver;

import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.net.SocketException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingWorker;

public final class PowerPanelServerView extends FrameView {

    Configuration config = new Configuration();
    SwingWorker worker;
    DatagramSocket serverSocket;
    JFrame mainFrame;
    DatagramPacket sendPacket;
    byte[] receiveData = new byte[1024];
    byte[] sendData = new byte[1024];
    InetAddress IPAddress;
    int port;
    String capitalizedSentence;
    String sentence;
    Commands commander;
    ActionListener startListener = new ActionListener() {

        public void actionPerformed(ActionEvent event) {
            jButton1.setVisible(false);
            jButton2.setVisible(true);
            statusMessageLabel.setText("Server: Online");
            worker = new SwingWorker() {

                public Object doInBackground() {
                    try {
                        serverSocket = new DatagramSocket(2501);
                        System.out.println("STARTED");
                        while (true) {
                            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                            serverSocket.receive(receivePacket);
                            sentence = new String(receivePacket.getData());
                            System.out.println("RECEIVED: " + sentence);
                            IPAddress = receivePacket.getAddress();
                            port = receivePacket.getPort();
                            EventQueue.invokeLater(new runCommand(sentence));
                            statusAnimationLabel.setIcon(busyIcons[0]);
                        }
                    } catch (SocketException ex) {
                        statusMessageLabel.setText("Server: Offline");
                        return "";
                    } catch (IOException io) {
                        statusMessageLabel.setText("Server: Offline");
                        return "Interrupted";
                    }
                }

                @Override
                public void done() {
                    worker.cancel(true);
                    serverSocket.close();
                    statusMessageLabel.setText("Server: Offline");
                }
            };

            worker.execute();
        }
    };
    ActionListener interruptListener = new ActionListener() {

        public void actionPerformed(ActionEvent event) {
            jButton2.setVisible(false);
            worker.cancel(true);
            jButton1.setVisible(true);
        }
    };

    public PowerPanelServerView(SingleFrameApplication app) {
        super(app);

        initComponents();
        mainFrame = PowerPanelServerApp.getApplication().getMainFrame();
        ResourceMap resourceMap = getResourceMap();
        Image icon = Toolkit.getDefaultToolkit().getImage("icon_small.png");
        mainFrame.setIconImage(icon);
        commander = new Commands(mainFrame);
        if (!config.checkForConfigFile()) {
            showInstructions();
        }

        System.out.print(config.getOS());

        jButton2.addActionListener(interruptListener);

        jButton1.addActionListener(startListener);

        jButton2.setVisible(false);

        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            mainFrame = PowerPanelServerApp.getApplication().getMainFrame();
            aboutBox = new PowerPanelServerAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PowerPanelServerApp.getApplication().show(aboutBox);
    }

    @Action
    public void showInstructions() {
        if (instructionsBox == null) {
            mainFrame = PowerPanelServerApp.getApplication().getMainFrame();
            instructionsBox = new Instructions(mainFrame, true);
            instructionsBox.setLocationRelativeTo(mainFrame);
        }
        PowerPanelServerApp.getApplication().show(instructionsBox);
    }

    private final class setStatus implements Runnable {

        private final String status;

        public setStatus(String status) {
            this.status = status;
        }

        public void run() {
            try {
                statusMessageLabel.setText("Use this shit");
            } catch (Exception e) {
                Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, e);
            }


        }
    }

    private final class runCommand implements Runnable {

        private final String command;

        public runCommand(String command) {
            this.command = command;
        }

        public void run() {
            try {
                Map<String, String> ret = commander.runCommand(this.command);
                capitalizedSentence = sentence.toUpperCase();
                sendData = ret.get("response").getBytes();
                sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                serverSocket.send(sendPacket);
                statusMessageLabel.setText("Server: Online - " + ret.get("status"));
                statusAnimationLabel.setIcon(idleIcon);
            } catch (Exception e) {
                Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, e);
            }


        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setMaximumSize(new java.awt.Dimension(200, 200));
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setPreferredSize(new java.awt.Dimension(174, 25));

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(powerpanelserver.PowerPanelServerApp.class).getContext().getResourceMap(PowerPanelServerView.class);
        jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
        jButton1.setToolTipText(resourceMap.getString("jButton1.toolTipText")); // NOI18N
        jButton1.setName("jButton1"); // NOI18N

        jButton2.setText(resourceMap.getString("jButton2.text")); // NOI18N
        jButton2.setToolTipText(resourceMap.getString("jButton2.toolTipText")); // NOI18N
        jButton2.setName("jButton2"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addContainerGap(322, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addGap(230, 230, 230))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(powerpanelserver.PowerPanelServerApp.class).getContext().getActionMap(PowerPanelServerView.class, this);
        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setText(resourceMap.getString("aboutMenuItem.text")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        jMenuItem1.setAction(actionMap.get("showInstructions")); // NOI18N
        jMenuItem1.setText(resourceMap.getString("jMenuItem1.text")); // NOI18N
        jMenuItem1.setToolTipText(resourceMap.getString("jMenuItem1.toolTipText")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        helpMenu.add(jMenuItem1);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 280, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private JDialog instructionsBox;
}
