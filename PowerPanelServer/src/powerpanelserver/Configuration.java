package powerpanelserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ListResourceBundle;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Configuration extends ListResourceBundle {

    private String appDataPath = System.getenv("APPDATA");
    private String powerPanelSettingsPath = appDataPath + "\\wellbaked\\powerpanel\\";
    private String jsonString;

    public Object[][] getContents() {
        return contents;
    }
    static final Object[][] contents = {
        {"macAddress", macAddress()},
        {"hostName", generateHostname()}
    };

    public void loadConfig() {
        FileInputStream configFileInput = null;
        DataInputStream dis = null;
        try {
            FileInputStream fstream = new FileInputStream(powerPanelSettingsPath + "config.cfg");
            dis = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String strLine;
            jsonString = br.readLine();
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                dis.close();
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public boolean checkForConfigFile() {
        File file = new File(powerPanelSettingsPath);
        boolean exists = file.exists();
        if (!exists) {
            boolean success = (file).mkdirs();
            System.out.println("the file or directory you are searching does not exist: " + exists);
            if (success) {
                System.out.println("Directory: " + powerPanelSettingsPath + " created");
                if (createConfigFile(powerPanelSettingsPath)) {
                    return false;
                }
            }
        } else {
            System.out.println("the file or directory exists: " + exists);
            return true;
        }
        return true;
    }

    private static String macAddress() {
        String macAdd = "";
        try {
            InetAddress address = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(address);
            if (ni != null) {
                byte[] mac = ni.getHardwareAddress();
                if (mac != null) {
                    for (int i = 0; i < mac.length; i++) {
                        macAdd = macAdd + String.format("%02X%s", mac[i], (i < mac.length - 1) ? ":" : "");
                    }
                } else {
                    System.out.println("Address doesn't exist or is not accessible.");
                }
            } else {
                System.out.println("Network Interface for the specified address is not found.");
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return macAdd;
    }

    private static String generatePrivateKey() {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        return uuid;
    }

    private static String generateHostname() {
        String computerName = "";
        try {
            computerName = InetAddress.getLocalHost().getHostName();
            return computerName;
        } catch (UnknownHostException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
        return computerName;
    }

    private String generateOSInfo() {
        String nameOS = "os.name";
        String versionOS = "os.version";
        return System.getProperty(nameOS) + " " + System.getProperty(versionOS);
    }

    private boolean createConfigFile(String path) {
        try {
            FileWriter fstream = new FileWriter(path + "config.cfg");
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("{'privateKey':'" + generatePrivateKey() + "', 'macAddress':'" + macAddress() + "', 'hostName':'" + generateHostname() + "', 'osInfo':'" + generateOSInfo() + "'}");
            out.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}
