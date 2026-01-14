import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import commands.Builtins;
import commands.ExternalCommand;
import history.History;
import parse.ParsedCommand;
import parse.Parser;
import pipes.PipelineRunner;
import repl.ShellInput;

public class Main {

    private static final Parser PARSER = new Parser();
    private static final History HISTORY = new History();

    private static final PipelineRunner.BullitinRunner BUILTIN_RUNNER =
        new PipelineRunner.BullitinRunner() {
            @Override
            public void run(ParsedCommand pc, PrintStream out, PrintStream err) throws IOException {
                try { Builtins.run(pc, out, err, HISTORY); }
                catch (Exception ex) { throw new IOException(ex); }
            }

            @Override
            public boolean isShellBuiltin(String commandName) {
                return Builtins.isBuiltin(commandName);
            }
        };
    
    public static void main(String[] args) throws Exception {

        TerminalModeController.setRawMode();
        Runtime.getRuntime().addShutdownHook(new Thread(TerminalModeController::restoreTerminal));

        String histFile = System.getenv("HISTFILE");
        if (histFile != null) HISTORY.loadIfExists(histFile);

        ShellInput shellInput = new ShellInput(HISTORY);

        while(true){
            String line = shellInput.readLine();
            if (line == null) break; 
            if (line.isBlank()) continue;

            HISTORY.addEntry(line);
            try { runOneCommandLine(line); }
            catch (Exception e) { System.err.println("Error: " + e.getMessage());}

        }
    }

    private static void runOneCommandLine(String input) throws Exception {

        ParsedCommand parsed = PARSER.parseCommand(input);

        if (parsed.args.length == 0) return; 
        String[] commandParts = parsed.args;

        PrintStream out = parsed.redirectStdout ? new PrintStream(new FileOutputStream(parsed.redirectFile, parsed.appendStdout)) : System.out;
        PrintStream err = parsed.redirectStderr ? new PrintStream(new FileOutputStream(parsed.stderrFile, parsed.appendStderr)) : System.err;

        try{
            if (input.contains("|")) {
                PipelineRunner.run(input, out, err, BUILTIN_RUNNER, PARSER);
                return;
            }
            execute(parsed,out,err);
        } catch (Exception e){
            err.println("Error executing pipeline: " + e.getMessage());
        } finally {
            out.flush();
            err.flush();
            if (out != System.out) {out.close();} 
            if (err != System.err) {err.close();}
        }
    }

    private static void execute(ParsedCommand parsed, PrintStream out, PrintStream err) throws Exception {
        String[] commandParts = parsed.args;
        String command = commandParts[0];
        if (Builtins.isBuiltin(command)) {
            Builtins.run(parsed, out, err, HISTORY);
        } else {
            ExternalCommand.run(commandParts, out, err);
        }
    }
}
