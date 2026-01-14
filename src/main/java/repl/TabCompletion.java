package repl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabCompletion {

    private boolean tabPending = false;
    private String tabPrefix = null;
    private List<String> tabMatches = null;

    public boolean completeLine(StringBuilder buffer) {
        String prefix = buffer.toString();

        if (prefix.indexOf(' ') != -1) {
            resetTab();
            return false;
        }

        if ("echo".startsWith(prefix) && !prefix.equals("echo")) {
            buffer.setLength(0);
            buffer.append("echo ");
            resetTab();
            return true;
        }

        if ("exit".startsWith(prefix) && !prefix.equals("exit")) {
            buffer.setLength(0);
            buffer.append("exit ");
            resetTab();
            return true;
        }

        List<String> matches = findExecutableMatches(prefix);
        Collections.sort(matches);

        if (matches.isEmpty()) {
            resetTab();
            return false;
        }

        if (matches.size() == 1) {
            buffer.setLength(0);
            buffer.append(matches.get(0)).append(" ");
            resetTab();
            return true;
        }

        String lcp = longestCommonPrefix(matches);
        if (lcp.length() > prefix.length()) {
            buffer.setLength(0);
            buffer.append(lcp);
            resetTab();
            return true;
        }

        boolean samePrefixAsLastTime = tabPending && prefix.equals(tabPrefix);
        if (!samePrefixAsLastTime) {
            tabPending = true;
            tabPrefix = prefix;
            tabMatches = matches;
            return false;
        }


        printMatches(tabMatches);
        resetTab();
        return true;
    }

    private List<String> findExecutableMatches(String prefix) {
        List<String> matches = new ArrayList<>();
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null) return matches;

        String[] paths = pathEnv.split(System.getProperty("path.separator"));

        for (String dirPath : paths) {
            File dir = new File(dirPath);
            File[] files = dir.listFiles();
            if (files == null) continue;

            for (File f : files) {
                if (f.isFile()
                        && f.canExecute()
                        && f.getName().startsWith(prefix)) {
                    matches.add(f.getName());
                }
            }
        }
        return matches;
    }

    private String longestCommonPrefix(List<String> strings) {
        String prefix = strings.get(0);
        for (int i = 1; i < strings.size(); i++) {
            String s = strings.get(i);
            int j = 0;
            while (j < prefix.length()
                    && j < s.length()
                    && prefix.charAt(j) == s.charAt(j)) {
                j++;
            }
            prefix = prefix.substring(0, j);
            if (prefix.isEmpty()) break;
        }
        return prefix;
    }

    private void printMatches(List<String> matches) {
        System.out.println();
        for (int i = 0; i < matches.size(); i++) {
            if (i > 0) System.out.print("  ");
            System.out.print(matches.get(i));
        }
        System.out.println();
    }

    public void resetTab() {
        tabPending = false;
        tabPrefix = null;
        tabMatches = null;
    } 
    
}
