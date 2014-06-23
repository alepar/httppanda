package ru.alepar.httppanda.upload.netty;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Range {

    private static Pattern BYTE_PATTERN = Pattern.compile("bytes=(\\d+)-(\\d*)");

    public final Long start;
    public final Long end;

    public Range(String rangeHeader) {
        final Matcher matcher = BYTE_PATTERN.matcher(rangeHeader);

        if (!matcher.matches()) {
            throw new RuntimeException("invalid range format: " + rangeHeader);
        }

        start = Long.valueOf(matcher.group(1));
        final String endString = matcher.group(2);
        if (endString != null && !endString.isEmpty()) {
            end = Long.valueOf(endString);
        } else {
            end = null;
        }
    }
}
