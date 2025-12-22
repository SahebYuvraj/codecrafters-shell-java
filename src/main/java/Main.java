import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

    /*
    Using static: (learning)
    ✔ saves memory
    ✔ avoids creating objects unnecessarily
    ✔ makes constants accessible everywhere cleanly
    ✔ shows conceptually this value is global to the shell
    static means: this belongs to the CLASS, not to an OBJECT one for all
    */

    /*
    Can switch to printf 
    1) printf is formatting-aware - This scales cleanly.
    2) printf is platform-independent for newlines
    3) Performance (small advantage)
     */

    /* 
    mistakes I made - scanner is being created in the loop - should be outside (leaks)
    Priority 1 — Separate parsing & execution completely
    Priority 2 - Clean naming conventions
     */

    /*
    NAMING CONVENTIONS USED
    classes - PascalCase
    methods - camelCase
    constants - UPPER_SNAKE_CASE
    variables - camelCase
    Boolean variables - isSomething, hasSomething (verb like)
     */

    /*
    can make an inner class and then have attributes to mark file location and command.
     */

public class Main {

    static class ParsedCommand {
        String[] args;
        boolean redirectStdout;
        String redirectFile;
    }

    private static final String PROMPT = "$ ";
    private static final String EXIT_COMMAND = "exit";
    private static final String ECHO_COMMAND = "echo";
    private static final String TYPE_COMMAND = "type";
    private static final String PWD_COMMAND = "pwd";
    private static final String CD_COMMAND = "cd";

    private static final List<String> SHELL_BUILTINS =
            List.of(EXIT_COMMAND, ECHO_COMMAND, TYPE_COMMAND, PWD_COMMAND, CD_COMMAND);

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print(PROMPT);

            String input = scanner.nextLine().trim();
            if (input.isEmpty()) continue;

            ParsedCommand parsed = parseCommand(input);
            String[] commandParts = parsed.args;
            String command = commandParts[0];

            PrintStream out = System.out;
            if (parsed.redirectStdout) {
                out = new PrintStream(new FileOutputStream(parsed.redirectFile));
            }

            switch (command) {
                case EXIT_COMMAND:
                    exitCommand(commandParts);
                    break;
                case ECHO_COMMAND:
                    echoCommand(commandParts, out);
                    break;
                case TYPE_COMMAND:
                    typeCommand(commandParts, out);
                    break;
                case PWD_COMMAND:
                    pwdCommand(out);
                    break;
                case CD_COMMAND:
                    cdCommand(commandParts);
                    break;
                default:
                    externalCommand(commandParts, out);
                    break;
            }

            if (out != System.out) {
                out.close();
            }
        }
    }

    private static void exitCommand(String[] args) {
        if (args.length > 1) {
            System.out.println("exit: too many arguments");
            return;
        }
        System.exit(0);
    }

    private static void echoCommand(String[] args, PrintStream out) {
        for (int i = 1; i < args.length; i++) {
            out.print(args[i]);
            if (i < args.length - 1) out.print(" ");
        }
        out.println();
    }

    private static void typeCommand(String[] args, PrintStream out) {
        if (args.length != 2) {
            System.out.println("type: invalid number of arguments");
            return;
        }

        String target = args[1];

        if (SHELL_BUILTINS.contains(target)) {
            out.println(target + " is a shell builtin");
            return;
        }

        String[] paths = System.getenv("PATH").split(File.pathSeparator);
        for (String path : paths) {
            File f = new File(path, target);
            if (f.exists() && f.canExecute()) {
                out.println(target + " is " + f.getAbsolutePath());
                return;
            }
        }

        System.out.println(target + ": not found");
    }

    private static void pwdCommand(PrintStream out) {
        out.println(System.getProperty("user.dir"));
    }

    private static void cdCommand(String[] args) {
        if (args.length != 2) {
            System.out.println("cd: invalid number of arguments");
            return;
        }

        String path = args[1];
        if (path.startsWith("~")) {
            path = System.getenv("HOME") + path.substring(1);
        }

        File target = new File(path);
        if (!target.isAbsolute()) {
            target = new File(System.getProperty("user.dir"), path);
        }

        try {
            File canonical = target.getCanonicalFile();
            if (!canonical.exists() || !canonical.isDirectory()) {
                System.out.println("cd: no such file or directory: " + args[1]);
                return;
            }
            System.setProperty("user.dir", canonical.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("cd: error changing directory");
        }
    }

    private static void externalCommand(String[] args, PrintStream out) {
        try {
            Process process = Runtime.getRuntime().exec(args);
            process.getInputStream().transferTo(out);
            process.getErrorStream().transferTo(System.err);
            process.waitFor();
        } catch (Exception e) {
            System.out.println(args[0] + ": command not found");
        }
    }

    private static ParsedCommand parseCommand(String input) {

        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        boolean insideSingleQuote = false;
        boolean insideDoubleQuote = false;

        boolean redirectStdout = false;
        String redirectFile = null;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' && !insideDoubleQuote) {
                insideSingleQuote = !insideSingleQuote;
                continue;
            }

            if (c == '"' && !insideSingleQuote) {
                insideDoubleQuote = !insideDoubleQuote;
                continue;
            }

            if (!insideSingleQuote && !insideDoubleQuote &&
                    (c == '>' || (c == '1' && i + 1 < input.length() && input.charAt(i + 1) == '>'))) {

                redirectStdout = true;
                if (c == '1') i++;

                if (current.length() > 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                }

                i++;
                while (i < input.length() && input.charAt(i) == ' ') i++;

                StringBuilder file = new StringBuilder();
                while (i < input.length() && input.charAt(i) != ' ') {
                    file.append(input.charAt(i));
                    i++;
                }

                redirectFile = file.toString();
                break;
            }

            if (c == ' ' && !insideSingleQuote && !insideDoubleQuote) {
                if (current.length() > 0) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }

        if (current.length() > 0) {
            parts.add(current.toString());
        }

        ParsedCommand pc = new ParsedCommand();
        pc.args = parts.toArray(new String[0]);
        pc.redirectStdout = redirectStdout;
        pc.redirectFile = redirectFile;
        return pc;
    }
}

