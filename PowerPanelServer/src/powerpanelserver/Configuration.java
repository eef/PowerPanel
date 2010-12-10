package powerpanelserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang.SystemUtils;

public class Configuration {

    //paths to settings and log files:
    private String appDataPath;
    private String powerPanelSettingsPath;
    private String configFileName;
    private String logFileName;

    private boolean firsttimerun;

    private String jsonString;
    private JSONObject jsonObject;

    public Configuration() {
        setSettingsPath();
    }

    private void setSettingsPath() {
        if (getOS().equals("windows")) {
            appDataPath = System.getenv("APPDATA");
            powerPanelSettingsPath = appDataPath + "\\wellbaked\\powerpanel\\";
        } else if (getOS().equals("linux")) {
            appDataPath = System.getenv("HOME");
            powerPanelSettingsPath = appDataPath + "/.wellbaked/powerpanel/";
        }
        configFileName = "config.cfg";
        logFileName = "powerpanel_log.xml";
    }

    public void loadConfig() {
        FileInputStream configFileInput = null;
        DataInputStream dis = null;
        try {
            FileInputStream fstream = new FileInputStream(powerPanelSettingsPath + configFileName);
            dis = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(dis));
            String strLine;
            jsonString = br.readLine();
            makeJSON(jsonString);
            dis.close();
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

    private void makeJSON(String jsonString) {
        try {
            jsonObject = (JSONObject) new JSONTokener(jsonString).nextValue();
        } catch (JSONException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean initaliseFiles(){
        boolean success = false;

        if (!checkForSettingsFolder()){
            firsttimerun = true;
            if (createSettingsFolder()) {
                success = true;
            }
        }
        if (!checkForLogFile()){
            if (createLogFile()) {
                success = true;
            }
        }

        if (!checkForConfigFile()){
            firsttimerun = true;
            if (createConfigFile()) {
                success = true;
            }
        }
        return success;
    }

    public boolean firstTimeRun(){
        return firsttimerun;
    }

    private boolean checkForSettingsFolder() {
        //returns true if the config folder exists false if not
        File file = new File(powerPanelSettingsPath);
        boolean exists = file.exists();
        if (exists) {
            return true;
        } else {
            return false;
        }
   }

    private boolean checkForLogFile() {
        //returns true if the config file exists false if not
        File file = new File(powerPanelSettingsPath + logFileName);
        boolean exists = file.exists();
        if (exists) {
            return true;
        } else {
            return false;
        }
    }

    private boolean checkForConfigFile() {
        File file = new File(powerPanelSettingsPath + configFileName);
        boolean exists = file.exists();
        if (exists) {
            return true;
        } else {
            return false;
        }
    }
    private boolean createSettingsFolder() {
        try {
            File file = new File(powerPanelSettingsPath);
            (file).mkdirs();
            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    private boolean createLogFile() {
        try {
            FileWriter fstream = new FileWriter(powerPanelSettingsPath + logFileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("");
            out.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    private boolean createConfigFile() {
        try {
            FileWriter fstream = new FileWriter(powerPanelSettingsPath + configFileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("{'privateKey':'" + generatePrivateKey() + "', 'macAddress':'" + macAddress() + "', 'hostName':'" + generateHostname() + "', 'osInfo':'" + generateOSInfo() + "', 'logging':'false'}");
            out.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
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
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, e);
        } catch (SocketException e) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, e);
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


    public boolean toggleLogging(boolean logging) {
        try {
            FileWriter fstream = new FileWriter(powerPanelSettingsPath + logFileName);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write("{'privateKey':'" + generatePrivateKey() + "', 'macAddress':'" + macAddress() + "', 'hostName':'" + generateHostname() + "', 'osInfo':'" + generateOSInfo() + "', 'logging':" + Boolean.toString(logging) + "}");
            out.close();
            return true;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }

    public String getPrivateKey() {
        try {
            System.out.print(jsonObject.getString("privateKey"));
            return jsonObject.getString("privateKey");
        } catch (JSONException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return "No Private Key";
        }
    }

    public String getMacAddress() {
        try {
            return jsonObject.getString("macAddress");
        } catch (JSONException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return "No Mac Address";
        }
    }

    public String getHostName() {
        try {
            return jsonObject.getString("hostName");
        } catch (JSONException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return "No Hostname";
        }
    }

    public String getSettingsPath() {
        return powerPanelSettingsPath;
    }

    public String getConfigFileName() {
        return configFileName;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public String getOsInfo() {
        try {
            return jsonObject.getString("osInfo");
        } catch (JSONException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return "No OS info";
        }
    }

    public boolean logging() {
        try {
            return jsonObject.getBoolean("logging");
        } catch (JSONException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public String getOS() {
        if (SystemUtils.IS_OS_WINDOWS) {
            return "windows";
        } else if (SystemUtils.IS_OS_MAC_OSX) {
            return "macosx";
        } else if (SystemUtils.IS_OS_LINUX) {
            return "linux";
        } else if (SystemUtils.IS_OS_OS2) {
            return "os2";
        } else if (SystemUtils.IS_OS_SOLARIS) {
            return "solaris";
        } else if (SystemUtils.IS_OS_SUN_OS) {
            return "sunos";
        } else if (SystemUtils.IS_OS_UNIX) {
            return "unix";
        } else if (SystemUtils.IS_OS_MAC) {
            return "mac";
        } else {
            return "unknown";
        }
    }
}
