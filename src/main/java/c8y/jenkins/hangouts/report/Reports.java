package c8y.jenkins.hangouts.report;

public class Reports {

    public static String truncate(String longString, int lines) {
        int index = 0;
        int endIndex = 0;

        while (index < lines) {
            int nextIndex = longString.indexOf('\n', endIndex);
            if (nextIndex < 0) {
                break;
            }
            endIndex = nextIndex + 1;
            index++;
        }

        if (index == lines) {
            return longString.substring(0, endIndex);
        } else {
            return longString;
        }
    }

    public static void dropCharacters(StringBuilder buffer, char toRemove) {
        int i = 0;
        for (; i < buffer.length(); ) {
            if (buffer.charAt(i) == toRemove) {
                buffer.deleteCharAt(i);
            } else {
                ++i;
            }
        }
    }

}
