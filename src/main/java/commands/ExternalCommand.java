package commands;

import java.io.File;
import java.io.PrintStream;

public class ExternalCommand {

    public static void run(String[] commandParts, PrintStream out, PrintStream err){
        String executable = commandParts[0];

        File commandFile = findExecutableFile(executable);
        if(commandFile == null){
            err.println(executable + ": command not found");
            return;
        } 
        else {
            try {
                Process process = Runtime.getRuntime().exec(commandParts);
                process.getInputStream().transferTo(out);
                process.getErrorStream().transferTo(err);
                process.waitFor();
                return;
 
            } 
            catch (Exception e) {
                err.println(e.getMessage());
                return;
            }
        }
    }

    public static File findExecutableFile(String command){
        String pathEnv = System.getenv("PATH");
        if(pathEnv == null) return null;
        
        String[] paths = pathEnv.split(System.getProperty("path.separator"));

        for (String path : paths) {
            File dir = new File(path);
            File commandFile = new File(dir, command);
            if(commandFile.exists() && commandFile.canExecute()){
                return commandFile;
            }
        }
        return null;
    }
    
}
