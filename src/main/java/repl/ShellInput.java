package repl;

import java.io.IOException;

import history.History;

public class ShellInput {

    private static final String PROMPT = "$ ";

    private final History history;
    private final StringBuilder buffer = new StringBuilder();
    private final KeyHandler keyHandler = new KeyHandler();
    private final TabCompletion tabCompletion = new TabCompletion();

    public ShellInput(History history) {
        this.history = history;
    }

    public String readLine() throws IOException {
        buffer.setLength(0);
        printPrompt();

        while (true) {
            int ch = System.in.read();
            KeyAction action = keyHandler.handle(ch);

            switch (action.type()) {

                case INSERT_CHAR -> {
                    buffer.append(action.ch());
                    System.out.print(action.ch());
                    System.out.flush();
                    tabCompletion.resetTab();
                }

                case BACKSPACE -> {
                    if (buffer.length() > 0) {
                        buffer.setLength(buffer.length() - 1);
                        System.out.print("\b \b");
                        System.out.flush();
                        tabCompletion.resetTab();
                    }
                }

                case TAB -> {
                    boolean changed = tabCompletion.completeLine(buffer);
                    redrawLine();
                    if (!changed) beep();
                }

                case HISTORY_UP -> {
                    String prev = history.getPrevious();
                    if (prev != null) {
                        buffer.setLength(0);
                        buffer.append(prev);
                        redrawLine();
                        tabCompletion.resetTab();
                    } else beep();
                }

                case HISTORY_DOWN -> {
                    String next = history.getNext();
                    if (next != null) {
                        buffer.setLength(0);
                        buffer.append(next);
                        redrawLine();
                        tabCompletion.resetTab();
                    } else beep();
                }

                case ENTER -> {
                    System.out.println();
                    tabCompletion.resetTab();
                    return buffer.toString().trim();
                }

                case IGNORE -> beep();
            }
        }
    }

    private void printPrompt() {
        System.out.print(PROMPT);
        System.out.flush();
    }

    private void redrawLine() {
        System.out.print("\r\033[2K");
        printPrompt();
        System.out.print(buffer);
        System.out.flush();
    }

    private void beep() {
        System.out.print("\007");
        System.out.flush();
    }
}
