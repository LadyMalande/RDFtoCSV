package com.miklosova.rdftocsvw.output_processor;

import com.opencsv.CSVWriter;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.Writer;

public class CustomCSVWriter extends CSVWriter {
    public CustomCSVWriter(Writer writer) {
        super(writer);
    }

    public CustomCSVWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd) {
        super(writer, separator, quotechar, escapechar, lineEnd);
    }

    @Override
    protected void writeNext(String[] nextLine, boolean applyQuotesToAll, Appendable appendable) throws IOException {
        if (nextLine == null) {
            return;
        }

        for (int i = 0; i < nextLine.length; i++) {

            if (i != 0) {
                appendable.append(separator);
            }

            String nextElement = nextLine[i];

            if (StringUtils.isEmpty(nextElement)) {
                continue;
            }

            Boolean stringContainsSpecialCharacters = stringContainsSpecialCharacters(nextElement);

            appendQuoteCharacterIfNeeded(applyQuotesToAll, appendable, stringContainsSpecialCharacters);

            if (stringContainsSpecialCharacters) {
                processLine(nextElement, appendable);
            } else {
                appendable.append(nextElement);
            }

            appendQuoteCharacterIfNeeded(applyQuotesToAll, appendable, stringContainsSpecialCharacters);
        }

        appendable.append(lineEnd);
        writer.write(appendable.toString());
    }

    private void appendQuoteCharacterIfNeeded(boolean applyQuotesToAll, Appendable appendable, Boolean stringContainsSpecialCharacters) throws IOException {
        if ((applyQuotesToAll || stringContainsSpecialCharacters) && quotechar != NO_QUOTE_CHARACTER) {
            appendable.append(quotechar);
        }
    }
}
