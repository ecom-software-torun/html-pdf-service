package org.farrukh.mirza.pdf.rest;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.farrukh.mirza.pdf.rest.dto.PdfRequest;
import org.farrukh.mirza.pdf.spi.TemplateDataTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("service/template/html")
public class HtmlTemplateResource {
    private static final Logger logger = LoggerFactory.getLogger(HtmlTemplateResource.class);

    protected final TemplateDataTransformer templateDataTransformer;

    public HtmlTemplateResource(
            @Qualifier("templateDataTransformerImpl") TemplateDataTransformer templateDataTransformer) {
        this.templateDataTransformer = templateDataTransformer;
    }

    private List<String> convertFromTemplateToHtml(String html, String css, String json) {
        try {

            html = StringEscapeUtils.unescapeHtml4(html);
            css = StringUtils.isBlank(css) ? "" : StringEscapeUtils.unescapeHtml4(css);
            json = StringUtils.isBlank(json) ? "" : StringEscapeUtils.unescapeHtml4(json);
            logger.debug("HTML: " + html);
            logger.debug("CSS: " + css);
            logger.debug("JSON: " + json);

            List<String> htmls = new ArrayList<>();
            List<String> formedHtmls = new ArrayList<>();

            if (StringUtils.isNotBlank(json)) {
                if (templateDataTransformer.isJsonArray(json)) {
                    htmls = templateDataTransformer.transformHTMLTemplates(html, json);
                } else {
                    htmls.add(templateDataTransformer.transformHTMLTemplate(html, json));
                }
            }

            // getFormedHTML() performs null check on css by default, so no
            // need to do it here.
            for(String h: htmls){
                formedHtmls.add(templateDataTransformer.getFormedHTML(h, css));
            }

            return formedHtmls;
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }

        return null;
    }

    /**
     * [NOT WORKING] To be rewritten for Spring Boot 3
     *
     * @param html
     * @param req
     * @return
     */
    @RequestMapping(value = "params", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public List<String> convertHtmlToPdf(@RequestParam("html") String html, HttpServletRequest req) {
        logger.debug("This will convert html template and css request params to html and return as List of String.");
        try {
            return convertFromTemplateToHtml(html, req.getParameter("css"), req.getParameter("json"));
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }

        return null;
    }

    /**
     * [POSSIBLY NOT WORKING] To be rewritten for Spring Boot 3
     *
     * @param reqBody
     * @return
     */
    @RequestMapping(value = "body", method = RequestMethod.POST)
    @ResponseBody
    public List<String> convertHtmlToPdf(@RequestBody PdfRequest reqBody) {
        logger.debug("This will convert html template and css request params to html and return as List of String.");
        try {
            return convertFromTemplateToHtml(reqBody.getHtml(), reqBody.getCss(), reqBody.getJson());
        } catch (Throwable t) {
            logger.error(t.getMessage(), t);
        }

        return null;
    }

}
