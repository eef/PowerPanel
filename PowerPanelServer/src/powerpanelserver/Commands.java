package powerpanelserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Commands extends Utils {

    private static String status;
    private static String response;
    private static Map<String, String> ret = new HashMap<String, String>();
    private static JFrame mainFrame;
    private static JDialog instructionsBox;
    private static int confirm;
    private static Configuration config = new Configuration();
    private static String pairAnswer;
    private static String command;
    private static Process child;
    private static String seconds;

    public Commands(JFrame frame) {
        mainFrame = frame;
        config.loadConfig();
    }

    public enum Command {
        // enums will rule the world with a rusty fist

        HIBERNATE() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    if (config.getOS().equals("windows")) {
                        command = System.getenv("WINDIR") + "\\system32\\rundll32.exe powrprof.dll,SetSuspendState Hibernate";
                    } else if (config.getOS().equals("linux")) {
                        command = "dbus-send --print-reply --system --dest=org.freedesktop.Hal /org/freedesktop/Hal/devices/computer org.freedesktop.Hal.Device.SystemPowerManagement.Hibernate";
                    }
                    child = Runtime.getRuntime().exec(command);
                    response = "Hiberating";
                    status = "Hibernating";
                } catch (IOException ex) {
                    response = "Hibernate failed";
                    status = "Hibernate failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        LOCK() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    command = System.getenv("WINDIR") + "\\System32\\rundll32.exe user32.dll,LockWorkStation";
                    child = Runtime.getRuntime().exec(command);
                    response = "Computer locked";
                    status = "Computer Locked";
                } catch (IOException ex) {
                    response = "Lock failed";
                    status = "Lock failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        SHUTDOWN() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    seconds = formatSeconds(Integer.parseInt(option));
                    response = "Shutdown in " + seconds;
                    status = "Shutdown in " + seconds;
                } catch (Exception ex) {
                    response = "Shutdown command failed";
                    status = "Shutdown command failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("shutdown", option);
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        ACTUAL_SHUTDOWN() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    if (config.getOS().equals("windows")) {
                        command = "shutdown -s";
                    } else if (config.getOS().equals("linux")) {
                        command = "dbus-send --print-reply --system --dest=org.freedesktop.Hal /org/freedesktop/Hal/devices/computer org.freedesktop.Hal.Device.SystemPowerManagement.Shutdown";
                    }
                    child = Runtime.getRuntime().exec(command);
                    response = "Shutting down";
                    status = "Shutdown down";
                } catch (IOException ex) {
                    response = "Shutdown command failed";
                    status = "Shutdown command failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        HELLO() {

            @Override
            public Map execute(String option) {
                ret.clear();
                response = config.getPrivateKey();
                status = "Hello command executed";
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        REBOOT() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    if (config.getOS().equals("windows")) {
                        command = "shutdown -r";
                    } else if (config.getOS().equals("linux")) {
                        command = "dbus-send --print-reply --system --dest=org.freedesktop.Hal /org/freedesktop/Hal/devices/computer org.freedesktop.Hal.Device.SystemPowerManagement.Reboot";
                    }
                    child = Runtime.getRuntime().exec(command);
                    response = "Restarting";
                    status = "Restarting";
                } catch (IOException ex) {
                    response = "Restart failed";
                    status = "Restart failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        CANCEL() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    response = "Shutdown canceled";
                    status = "Shutdown canceled";
                } catch (Exception ex) {
                    response = "Shutdown cancel failed";
                    status = "Shutdown cancel failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("cancelShutdown", "yes");
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        INFO() {

            @Override
            public Map execute(String option) {
                ret.clear();
                try {
                    child = Runtime.getRuntime().exec("shutdown -a");
                    response = "Shutdown canceled";
                    status = "Shutdown canceled";
                } catch (IOException ex) {
                    response = "Shutdown cancel failed";
                    status = "Shutdown cancel failed";
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        PAIR() {

            @Override
            public Map execute(String option) {
                ret.clear();
                confirm = JOptionPane.showConfirmDialog(null, "Would you like to pair?");
                if (confirm == 0) {
                    status = "Pair successful";
                    pairAnswer = "yes";
                } else {
                    status = "Did not pair";
                    pairAnswer = "no";
                }
                response = "{'pairaccepted':'" + pairAnswer + "', 'pkey':'" + config.getPrivateKey() + "','mac':'" + config.getMacAddress() + "','name':'" + config.getHostName() + "'}";
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        };

        public abstract Map execute(String option);
    }

    public Map runCommand(String command) {
        String[] options = command.split(":");
        return Command.valueOf(options[0].trim().toUpperCase()).execute(options[1].trim());
    }
}
