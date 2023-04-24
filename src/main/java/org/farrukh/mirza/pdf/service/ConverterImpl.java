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
 * 24/06/2016
 * Dublin, Ireland
 */
package org.farrukh.mirza.pdf.service;

import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.farrukh.mirza.pdf.spi.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.List;


@Service
public class ConverterImpl extends BaseImpl implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(
            ConverterImpl.class
    );

    @Override
    public void convertHtmlToPdf(String html, OutputStream out) {
        convertHtmlToPdf(html, null, out);
    }

    @Override
    public void convertHtmlToPdf(String html, String css, OutputStream out) {
        try {
            html = correctHtml(html);
            html = getFormedHTMLWithCSS(html, css);

            //This ITextRenderer is from the Flying Saucer library under LGPL license.
            //Should not be confused with the actual iText library.
            ITextRenderer r = new ITextRenderer();

            addFontDirectory(r.getFontResolver(), "/usr/share/fonts/TTF/");
            r.setDocumentFromString(html);
            r.layout();

            r.createPDF(out, false);
            r.getWriter().getInfo().put(PdfName.PRODUCER, new PdfString("-"));
            r.getWriter().getInfo().put(PdfName.CREATOR, new PdfString("-"));
            r.finishPDF();
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void convertHtmlToPdf(List<String> htmls, OutputStream out) {
        convertHtmlToPdf(htmls, null, out);
    }

    @Override
    public void convertHtmlToPdf(
            List<String> htmls,
            String css,
            OutputStream out
    ) {
        try {
            PDFMergerUtility merge = new PDFMergerUtility();

            for (String html : htmls) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();

                // convertHtmlToPdf() performs null check on css by default, so
                // no need to do it here.
                convertHtmlToPdf(html, css, bos);

                ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                merge.addSource(bis);
            }

            merge.setDestinationStream(out);
            merge.mergeDocuments(null);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }

    private void addFontDirectory(ITextFontResolver r, String dir)
            throws DocumentException, IOException {
        File f = new File(dir);
        if (f.isDirectory()) {
            File[] files = f.listFiles(
                    new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            String lower = name.toLowerCase();
                            return lower.endsWith(".ttf");
                        }
                    }
            );
            for (int i = 0; i < files.length; i++) {
                r.addFont(files[i].getAbsolutePath(), BaseFont.IDENTITY_H, true);
            }
        }
    }
}
