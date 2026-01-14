package repl;

public record KeyAction (Type type, char ch) {
    
    public enum Type {
        INSERT_CHAR,
        BACKSPACE,
        TAB,
        ENTER,
        HISTORY_UP,
        HISTORY_DOWN,
        IGNORE
    }

    public static KeyAction insert(char c) {
        return new KeyAction(Type.INSERT_CHAR, c);
    }

    public static KeyAction backspace() {
        return new KeyAction(Type.BACKSPACE, '\0');
    }

    public static KeyAction tab() {
        return new KeyAction(Type.TAB, '\0');
    }

    public static KeyAction enter() {
        return new KeyAction(Type.ENTER, '\0');
    }

    public static KeyAction historyUp() {
        return new KeyAction(Type.HISTORY_UP, '\0');
    }

    public static KeyAction historyDown() {
        return new KeyAction(Type.HISTORY_DOWN, '\0');
    }

    public static KeyAction ignore() {
        return new KeyAction(Type.IGNORE, '\0');
    }
}

