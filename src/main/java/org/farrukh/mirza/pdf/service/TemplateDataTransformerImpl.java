/*
 * {{{ header & license
 * Copyright (c) 2016 Farrukh Mirza
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */

/**
 * @author Farrukh Mirza
 * @date 8 Jul 2016
 * Repeat view implementation on 27/09/2018
 * Dublin, Ireland
 */
package org.farrukh.mirza.pdf.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.farrukh.mirza.pdf.spi.CustomHtmlTagsEnum;
import org.farrukh.mirza.pdf.spi.TemplateDataTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.jayway.jsonpath.JsonPath;

@Service
public class TemplateDataTransformerImpl extends BaseImpl implements TemplateDataTransformer {
    /*
     * https://github.com/json-path/JsonPath
     */
    private final Logger logger = LoggerFactory.getLogger(TemplateDataTransformerImpl.class);

//	@Deprecated
//	private ObjectMapper mapper = new ObjectMapper();

    @Override
    public String getFormedHTML(String htmlBody, String css) {
        htmlBody = correctHtml(htmlBody);
        htmlBody = getFormedHTMLWithCSS(htmlBody, css);
        return htmlBody;
    }

    @Override
    public boolean isJsonArray(String json) {
        if (StringUtils.isNoneBlank(json) && StringUtils.isNoneBlank(json.trim()) && json.trim().startsWith("[") && json.trim().endsWith("]")) {
            return true;
        }
        return false;
    }

    @Override
    public String transformHTMLTemplate(String htmlTemplate, String jsonString) {
        try {
            String htmlWithoutRepeatInput = htmlTemplate;
            String htmlWithoutRepeatOutput = htmlTemplate;
            // Replace all Repeat tags with repeated html
            // While loop will stop when all repeat tags are replaced and
            while (!(htmlWithoutRepeatOutput = transformRepeatTagInTemplate(htmlWithoutRepeatInput, "$.", jsonString)).equalsIgnoreCase(htmlWithoutRepeatInput)) {
                htmlWithoutRepeatInput = htmlWithoutRepeatOutput;
            }

            htmlTemplate = htmlWithoutRepeatOutput;

            List<String> keys = getUniqueKeysFromTemplate(htmlTemplate);
            Map<String, String> keyVals = getValuesFromJson(keys, jsonString);

            return transformTemplate(htmlTemplate, keyVals);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        return htmlTemplate;
    }

    @Override
    public List<String> transformHTMLTemplates(String htmlTemplate, String jsonData) {
        try {
            List<String> html = new ArrayList<>();

            int arrayLength = Integer.parseInt(JsonPath.read(jsonData, "$.length()").toString());
            for (int i = 0; i < arrayLength; i++) {
                String htmlTemplatePerObject = htmlTemplate;
                String htmlWithoutRepeatInput = htmlTemplatePerObject;
                String htmlWithoutRepeatOutput = htmlTemplatePerObject;
                // Replace all Repeat tags with repeated html
                // While loop will stop when all repeat tags are replaced and
                while (!(htmlWithoutRepeatOutput = transformRepeatTagInTemplate(htmlWithoutRepeatInput, "$.[" + i + "].", jsonData)).equalsIgnoreCase(htmlWithoutRepeatInput)) {
                    htmlWithoutRepeatInput = htmlWithoutRepeatOutput;
                }

                htmlTemplatePerObject = htmlWithoutRepeatOutput;

                //This can't be outside loop because repeat depends on the Json Object
                //Thus keys can be separate for each json object in the main json array.
                List<String> keys = getUniqueKeysFromTemplate(htmlTemplatePerObject);
                Map<String, String> keyVals = getValuesFromJson(keys, "$.[" + i + "].", jsonData);
                String template = htmlTemplatePerObject;
                template = transformTemplate(template, keyVals);

                html.add(template);
            }

            return html;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }

    private String transformTemplate(String template, Map<String, String> keyVals) {
        for (Entry<String, String> e : keyVals.entrySet()) {
            if (StringUtils.isBlank(e.getValue()) || "null".equalsIgnoreCase(e.getValue())) {
                e.setValue("");
            }

            //Find [ and ] characters in the keys and replace them with \[ and \] in the keys
            String key = e.getKey();
            key = key.contains("[") ? key.replaceAll("\\[", "\\\\[") : key;
            key = key.contains("]") ? key.replaceAll("\\]", "\\\\]") : key;

            //Then use the formed key to replace values in the text
            template = template.replaceAll("\\{" + key + "\\}", e.getValue());
        }

        return template;
    }

    private String transformRepeatTagInTemplate(String template, String jsonSelector, String json) {
        if (CustomHtmlTagsEnum.REPEAT.isPresentAndValid(template)) {
            final String innerHtml = CustomHtmlTagsEnum.REPEAT.getInnerHtml(template);

            // Get all unique keys from the innerHTML of the <repeat>...</repeat> tag
            // The resulting keys in the list would be like below as an example:
            // 1. parent.child[*].name.first
            // 2. parent.child[*].name.last
            // 3. parent.child[*].dob
            // 4. parent.child[*].address
            List<String> keys = getUniqueKeysFromTemplate(innerHtml);

            // Now get the array part(s) separated and unique, e.g., the list from above
            // example will result in a hash set of a single element, i.e.,
            // --> parent.child
            HashSet<String> keyArrayElement = new HashSet<>();
            for (String k : keys) {
                keyArrayElement.add(k.substring(0, k.indexOf("[")));
            }

            // Next find among all arrays referenced under the repeat tag, which one has the
            // most elements.
            // Assumption: If multiple arrays are referenced, then all are assumed to be of
            // same size within the <repeat> tag
            // Assumption: Only a single level of <repeat> is considered. Nested <repeat>
            // tags are not allowed
            int maxArraySize = 0;
            for (String k : keyArrayElement) {
                int arraySize = Integer.parseInt(JsonPath.read(json, jsonSelector + k + ".length()").toString());
                maxArraySize = Math.max(arraySize, maxArraySize);
            }

            // Now repeat the inner HTML with indexed keys instead of the template key
            // parent.child[*].name.first will be replaced by parent.child[0].name.first,
            // then parent.child[1].name.first, ...
            StringBuilder indexedInnerHTML = new StringBuilder();
            for (int i = 0; i < maxArraySize; i++) {
                String indexedInnerHTMLRow = innerHtml;

                for (String k : keyArrayElement) {
                    indexedInnerHTMLRow = indexedInnerHTMLRow.replaceAll(k + "\\[" + JSON_OBJECT_ARRAY_REPEAT_TAG_WILDCARD + "\\]", k + "\\[" + i + "\\]");

                }

                indexedInnerHTML.append(indexedInnerHTMLRow);
            }

            // Now replace the original template text containing REPEAT tag with the actual
            // repetition of indexed HTML
            // Original: <repeat>{parent.child[*].name.first}
            // {parent.child[*].name.last}</repeat>
            // Result: {parent.child[0].name.first}
            // {parent.child[0].name.last}{parent.child[1].name.first}
            // {parent.child[1].name.last}
            // template = template.replaceFirst(CustomHtmlTagsEnum.REPEAT.getTagWithInnerHtmlSubstring(template),
            // indexedInnerHTML);
            template = CustomHtmlTagsEnum.REPEAT.replaceTagWithInnerHtmlByReplacement(template, indexedInnerHTML.toString());
        }
        return template;
    }

    private Map<String, String> getValuesFromJson(List<String> keys, String json) {
        return getValuesFromJson(keys, "$.", json);
    }

    private Map<String, String> getValuesFromJson(List<String> keys, String jsonSelector, String json) {
        Map<String, String> map = new HashMap<>();

        for (String k : keys) {
            String val = "";
            try {
                val = val + JsonPath.read(json, jsonSelector + k);
            } catch (Throwable t) {
                // val = "N/A";
                logger.error("Exception while reading key value for {}: {}", k, t.getMessage());
            }

            String escaped = StringEscapeHelpers.escapeHtml4WithoutSpecialCharacters(val);

            map.put(k, escaped);
        }

        return map;
    }

    private List<String> getUniqueKeysFromTemplate(String template) {
        List<String> keys = new ArrayList<>();
        Pattern p = Pattern.compile("\\{.*?\\}");
        Matcher m = p.matcher(template);
        while (m.find()) {
            String k = m.group().subSequence(1, m.group().length() - 1).toString();
            if (!keys.contains(k)) keys.add(k);
        }

        return keys;
    }

    @Deprecated
    private String getHtmlFromTemplateAndData(String htmlTemplate, Map<String, Object> data) {

        for (Entry<String, Object> e : data.entrySet()) {
            htmlTemplate = htmlTemplate.replaceAll("\\{" + e.getKey() + "\\}", (String) e.getValue());
        }


        return htmlTemplate;
    }

    @Deprecated
    private String transformTemplate(String template, String json) {
        List<String> keys = getUniqueKeysFromTemplate(template);
        Map<String, String> keyVals = getValuesFromJson(keys, json);

        return transformTemplate(template, keyVals);
    }
}
