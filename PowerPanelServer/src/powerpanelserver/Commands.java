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

    public Commands(JFrame frame) {
        mainFrame = frame;
    }

    public enum Command {
        // enums will rule the world with a rusty fist

        HIBERNATE() {

            @Override
            public Map execute(String option) {
                try {
                    String command = System.getenv("WINDIR") + "\\system32\\rundll32.exe powrprof.dll,SetSuspendState Hibernate";
                    Process child = Runtime.getRuntime().exec(command);
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
                try {
                    String command = System.getenv("WINDIR") + "\\System32\\rundll32.exe user32.dll,LockWorkStation";
                    Process child = Runtime.getRuntime().exec(command);
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
                try {
                    String seconds = formatSeconds(Integer.parseInt(option));
                    System.out.print(option);
                    Process child = Runtime.getRuntime().exec("shutdown -s -t " + option);
                    response = "Shutdown in " + seconds;
                    status = "Shutdown in " + seconds;
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
                response = config.getPrivateKey();
                status = "Hello command executed";
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        RESTART() {

            @Override
            public Map execute(String option) {
                try {
                    Process child = Runtime.getRuntime().exec("shutdown -r");
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
                try {
                    Process child = Runtime.getRuntime().exec("shutdown -a");
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

        INFO() {

            @Override
            public Map execute(String option) {
                try {
                    Process child = Runtime.getRuntime().exec("shutdown -a");
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
