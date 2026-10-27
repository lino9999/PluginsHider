package io.github.lino9999.pluginshider.acl;

import java.util.Locale;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class CommandRule {
    private final String original;
    private final boolean negated;
    private final boolean wildcard;
    private final boolean isRegex;
    private final Pattern pattern;
    private final String cleanCommand;
    private final String[] parts;

    public CommandRule(String raw) {
        this.original = raw.trim();
        String current = this.original;
        if (current.startsWith("!")) {
            this.negated = true;
            current = current.substring(1).trim();
        } else {
            this.negated = false;
        }

        if (current.startsWith("/")) {
            current = current.substring(1).trim();
        }

        if (current.startsWith("regex:")) {
            this.isRegex = true;
            this.wildcard = false;
            this.cleanCommand = current.substring(6).trim();
            Pattern compiled;
            try {
                compiled = Pattern.compile(this.cleanCommand, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                compiled = null;
            }
            this.pattern = compiled;
            this.parts = new String[0];
        } else if (current.equals("*")) {
            this.isRegex = false;
            this.wildcard = true;
            this.pattern = null;
            this.cleanCommand = "*";
            this.parts = new String[] { "*" };
        } else {
            this.isRegex = false;
            this.wildcard = false;
            this.pattern = null;
            this.cleanCommand = current.toLowerCase(Locale.ROOT);
            this.parts = this.cleanCommand.split("\\s+");
        }
    }

    public boolean matches(String commandLine) {
        if (commandLine == null) {
            return false;
        }
        String clean = commandLine.trim();
        if (clean.startsWith("/")) {
            clean = clean.substring(1).trim();
        }

        if (wildcard) {
            return true;
        }

        if (isRegex) {
            return pattern != null && pattern.matcher(clean).find();
        }

        String[] testParts = clean.toLowerCase(Locale.ROOT).split("\\s+");
        if (parts.length == 0 || testParts.length == 0) {
            return false;
        }

        if (testParts.length < parts.length) {
            return false;
        }

        for (int i = 0; i < parts.length; i++) {
            if (!parts[i].equals("*") && !parts[i].equals(testParts[i])) {
                return false;
            }
        }
        return true;
    }

    public boolean matchesRoot(String rootCommand) {
        if (rootCommand == null) {
            return false;
        }
        String clean = rootCommand.trim();
        if (clean.startsWith("/")) {
            clean = clean.substring(1).trim();
        }
        int space = clean.indexOf(' ');
        if (space != -1) {
            clean = clean.substring(0, space);
        }
        clean = clean.toLowerCase(Locale.ROOT);

        if (wildcard) {
            return true;
        }
        if (parts.length > 0) {
            if (negated) {
                return parts.length == 1 && parts[0].equalsIgnoreCase(clean);
            }
            return parts[0].equalsIgnoreCase(clean);
        }
        if (isRegex && pattern != null) {
            return pattern.matcher(clean).find();
        }
        return false;
    }

    public boolean isNegated() {
        return negated;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public String getCleanCommand() {
        return cleanCommand;
    }

    public String[] getParts() {
        return parts;
    }

    public String getOriginal() {
        return original;
    }
}
