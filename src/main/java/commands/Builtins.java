package commands;

import java.io.File;
import java.io.PrintStream;
import java.util.Set;

import history.History;
import parse.ParsedCommand;

public class Builtins {

    public static final String EXIT = "exit";
    public static final String ECHO = "echo";
    public static final String TYPE = "type";
    public static final String PWD  = "pwd";
    public static final String CD   = "cd";
    public static final String HISTORY = "history";

    private static final Set<String> BUILTINS = Set.of(EXIT, ECHO, TYPE, PWD, CD, HISTORY);

    public static boolean isBuiltin(String commandName) {
        return BUILTINS.contains(commandName);
    }

    public static void run(ParsedCommand cmd, PrintStream out, PrintStream err, History history) throws Exception {
        String[] args = cmd.args;
        String name = args[0];

        switch (name) {
            case EXIT:   exit(args, err, history); break;
            case ECHO:   echo(args, out); break;
            case TYPE:   type(args, out, err); break;
            case PWD:    pwd(out); break;
            case CD:     cd(args, err); break;
            case HISTORY: historyCmd(args, out, history); break;
            default:
                err.println(name + ": command not found");
        }
    }

    public static void exit(String[] commandParts, PrintStream err, History history){ 
        if (commandParts.length > 1) {
            err.println("exit: too many arguments");
            return;
        }
        String histFile = System.getenv("HISTFILE");
        if (histFile != null) {
            // appendHistoryToFile(histFile);
            history.appendToFile(histFile);
        }
        System.exit(0);
        
    }

    private static void echo(String[] commandParts, PrintStream out){
        StringBuilder message = new StringBuilder();
        for (int i = 1; i < commandParts.length; i++){
            message.append(commandParts[i]);
            if (i < commandParts.length - 1){
                message.append(" ");
            }
        }
        out.println(message.toString());
    }

    private static void type(String[] commandParts, PrintStream out, PrintStream err){
        if (commandParts.length != 2) {
            err.println("type: invalid number of arguments");
            return;
        }
        
        String secondaryCommand = commandParts[1];
        if(BUILTINS.contains(secondaryCommand)){
            out.println(secondaryCommand + " is a shell builtin");
        } 
        else {
            String pathEnv = System.getenv("PATH");
            String[] paths = pathEnv.split(System.getProperty("path.separator"));
            for (String path : paths) {
                File dir = new File(path);
                File externalcommandFile = new File(dir, secondaryCommand);
                if(externalcommandFile.exists() && externalcommandFile.canExecute()){
                    out.println(secondaryCommand + " is " + externalcommandFile.getAbsolutePath());
                    return;
                }
            }
            err.println(secondaryCommand + ": not found");
            
        }
    }

    private static void pwd(PrintStream out){
        String currentDir = System.getProperty("user.dir");
        out.println(currentDir);
    }

    private static void cd(String[] commandParts, PrintStream err){
        File target;
        if(commandParts.length != 2){
            err.println("cd: invalid number of arguments");
            return;
        }

        String path = commandParts[1];

        if(path.startsWith("~") || path.startsWith("~/")){
            String home = System.getenv("HOME");
            path = home + path.substring(1);
        }

        if (new File(path).isAbsolute()) { target = new File(path); }

        else {
            String currentDir = System.getProperty("user.dir");
            target = new File(currentDir, path);
        }

        try{
            File CanonicalFile = target.getCanonicalFile();
            if(!CanonicalFile.exists() || !CanonicalFile.isDirectory()){
                err.println("cd: no such file or directory: " + path);
                return;
            
            }
            System.setProperty("user.dir", CanonicalFile.getAbsolutePath());
        } 
        catch (Exception e){
            // System.out.println("cd: error changing directory: " + e.getMessage()); 
            err.println("cd: error changing directory: " + e.getMessage());
            return;
        }
    }

    private static void historyCmd(String[] args, PrintStream out, History history) {
        if (args.length == 1) { history.printAll(out); return; }
        if (args.length == 2) { history.printLastN(out, Integer.parseInt(args[1])); return; }

        if (args.length == 3 && args[1].equals("-r")) { history.readFromFile(args[2]); return; }
        if (args.length == 3 && args[1].equals("-w")) { history.writeToFile(args[2]); return; }
        if (args.length == 3 && args[1].equals("-a")) { history.appendToFile(args[2]); return; }
    }
    
}
