package org.farrukh.mirza.pdf.test.converter;

import org.farrukh.mirza.pdf.service.StringEscapeHelpers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


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
        assertNull(StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(null));
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_emptyInput() {
        String input = "";
        String expectedOutput = "";
        String actualOutput = StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input);
        assertEquals(expectedOutput, actualOutput);
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_allSpecialCharacters() {
        // Test input with all special characters
        String input = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ";
        String expectedOutput = "ÀÁÂÃÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõöøùúûüýþÿ";
        assertEquals(expectedOutput, StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input));
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_someSpecialCharacters() {
        // Test input with some special characters
        String input = "This is à test with & and \" characters.";
        String expectedOutput = "This is à test with &amp; and &quot; characters.";
        assertEquals(expectedOutput, StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input));
    }

    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_tagsAndSpecialCharacters() {
        // Test input with HTML tags and special characters
        String input = "<h1>This is à test with & and \" characters.</h1>";
        String expectedOutput = "&lt;h1&gt;This is à test with &amp; and &quot; characters.&lt;/h1&gt;";
        assertEquals(expectedOutput, StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(input));
    }

    // this is not the best way, but updating junit to v5 is not possible at the moment
    // in junit v5 it is possible to define test params with attributes
    @Test
    public void testEscapeHtml4WithoutSpecialCharacters_foreignNames() {
        HashMap<String, String> testdata = new HashMap<String, String>() {
            {
                put("Andrés Sánchez", "Andrés Sánchez");
                put("Zoë Johnson", "Zoë Johnson");
                put("Pénélope Martin", "Pénélope Martin");
                put("Søren Jensen", "Søren Jensen");
                put("Ðavid García", "Ðavid García");
                put("Hélène Dupont", "Hélène Dupont");
                put("Jörg Müller", "Jörg Müller");
                put("María del Carmen Pérez", "María del Carmen Pérez");
                put("Kristján Jónsson", "Kristján Jónsson");
                put("Åsa Lindström", "Åsa Lindström");
            }
        };

        for (Map.Entry<String, String> pair : testdata.entrySet()) {
            assertEquals(pair.getValue(), StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(pair.getKey()));
        }
    }
}
