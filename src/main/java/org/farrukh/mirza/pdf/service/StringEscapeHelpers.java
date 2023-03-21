package org.farrukh.mirza.pdf.service;
import org.apache.commons.lang3.StringEscapeUtils;

public class StringEscapeHelpers {
    public static String escapeHtml4WithoutSpecialCharacters(String input) {
        if (input == null) {
            return null;
        }

		StringBuilder output = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == 'ó' || c == 'Ó') {
                output.append(c);
            } else {
                output.append(StringEscapeUtils.escapeHtml4(Character.toString(c)));
            }
        }

		return output.toString();
    }
}
