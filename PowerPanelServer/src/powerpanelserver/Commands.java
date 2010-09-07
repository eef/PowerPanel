package powerpanelserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Commands {

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
                response = "Hibernate command received";
                status = "Hibernate command executed";
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        SHUTDOWN() {

            @Override
            public Map execute(String option) {
                try {
                    Process child = Runtime.getRuntime().exec("" + option);

                } catch (IOException ex) {
                    Logger.getLogger(PowerPanelServerView.class.getName()).log(Level.SEVERE, null, ex);
                }
                response = "Shutdown command received";
                status = "Shutdown command executed";
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        HELLO() {

            @Override
            public Map execute(String option) {
                response = "Hello command received";
                status = "Hello command executed";
                ret.put("status", status);
                ret.put("response", response);
                return ret;
            }
        },
        RESTART() {

            @Override
            public Map execute(String option) {
                response = "Restart command received";
                status = "Restart command executed";
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
        return Command.valueOf(options[0].trim().toUpperCase()).execute(options[0].trim());
    }
}
