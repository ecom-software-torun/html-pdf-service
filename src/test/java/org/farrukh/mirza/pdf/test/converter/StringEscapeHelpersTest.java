package org.farrukh.mirza.pdf.test.converter;

import org.farrukh.mirza.pdf.service.StringEscapeHelpers;
import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class StringEscapeHelpersTest {
    @Test
    public void testEscapeHtml4WithoutSpecialCharacters() {
        String input = "<script>alert('XSS');</script>";
        String expectedOutput = "&lt;script&gt;alert('XSS');&lt;/script&gt;";
        String actualOutput = StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_withPolishCharacters() {
        String input = "Każdy z nas lubi karmić żółwie w ogrodzie.";
        String expectedOutput = "Każdy z nas lubi karmić żółwie w ogrodzie.";
        String actualOutput = StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_nullInput() {
        String input = null;
        String expectedOutput = null;
        String actualOutput = StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_emptyInput() {
        String input = "";
        String expectedOutput = "";
        String actualOutput = StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input);
        assertEquals(expectedOutput, actualOutput);
    }
}
