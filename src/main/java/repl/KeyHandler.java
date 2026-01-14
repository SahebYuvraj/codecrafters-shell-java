package repl;

import java.io.IOException;

public class KeyHandler {

    public KeyAction handle(int ch) throws IOException {

        if (ch == '\n' || ch == '\r') {
            return KeyAction.enter();
        }

        if (ch == '\t') {
            return KeyAction.tab();
        }

        if (ch == 127 || ch == 8) {
            return KeyAction.backspace();
        }

        if (ch == 27) { // ESC
            int ch2 = System.in.read();
            int ch3 = System.in.read();

            if (ch2 == 91) { // '['
                if (ch3 == 65) return KeyAction.historyUp();   // ↑
                if (ch3 == 66) return KeyAction.historyDown(); // ↓
            }
            return KeyAction.ignore();
        }

        if (ch >= 32) {
            return KeyAction.insert((char) ch);
        }

        return KeyAction.ignore();
    }
}
