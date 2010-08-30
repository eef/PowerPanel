/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package powerpanelserver;

/**
 *
 * @author arthur
 */
public class Commands {

    public enum Command {
        HIBERNATE, SHUTDOWN, HELLO, RESTART, PAIR;
    }

    public String runCommand(String command) {
        String[] options = command.split(":");
        
        switch (Command.valueOf(options[0].trim().toUpperCase())) {
            case HELLO:
                System.out.print("Hello command received.");
                return "Hello command executed";
            default:
                return "No command executed";
        }
    }
}
