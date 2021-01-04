package com.bimport.ashrae.common;

import com.bimport.ashrae.common.hash.HashMethod;
import com.bimport.ashrae.common.hash.Hasher;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.util.HtmlUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?)://)"
                    + "(([\\w\\-]+\\.)*?([\\w\\-.~]+/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static String createHyperLinks(String text, JsonObject links) {
        if (isNullOrEmpty(text)) {
            return text;
        }

        int start = 0;
        int end = text.length();

        StringBuilder sb = new StringBuilder();

        Matcher matcher = urlPattern.matcher(text);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            String url = text.substring(matchStart, matchEnd);

            if (matchStart > start) {
                sb.append(text, start, matchStart);
            }

            String urlHash = Hasher.hash(url, HashMethod.MD5);
            links.addProperty(urlHash, url);

            sb.append("<span class=\"snapshot-").append(urlHash).append("\"></span>");
            start = matchEnd;
        }

        if (start < end) {
            sb.append(text, start, end);
        }

        return sb.toString();
    }

    public static String convertLineBreakToHTML(String text) {
        if (text==null || text.length()==0) {
            return text;
        }
        return text.replaceAll("\n", "<br/>");
    }

    public static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException ignore) {
        }

        return false;
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String convertToCamel(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll("([A-Z])", "_$1").toLowerCase();
    }

    public static String checkNullAndEmpty(String str, String defVal) {
        if (isNullOrEmpty(str)) {
            return defVal;
        }
        return str;
    }

    public static String base64Encode(String str) {
        return Base64.encodeBase64String(str.getBytes(StandardCharsets.UTF_8));
    }

    public static String base64Decode(String base64) {
        return new String(Base64.decodeBase64(base64), StandardCharsets.UTF_8);
    }


    public static String capitalFirstLetter(String str) {
        if (str == null || str.length() <= 0) {
            return null;
        }

        str = str.toLowerCase();
        char first = str.charAt(0);
        return Character.toUpperCase(first) + str.substring(1);
    }

    private static String convert(String value, String fromEncoding, String toEncoding) {
        try {
            return new String(value.getBytes(fromEncoding), toEncoding);
        } catch (UnsupportedEncodingException ignored) {
            return null;
        }
    }

    public static String charset(String value) {
        String probe = StandardCharsets.UTF_8.name();
        Charset[] charSets = {
                StandardCharsets.UTF_8,
                StandardCharsets.ISO_8859_1,
                StandardCharsets.US_ASCII,
                StandardCharsets.UTF_16,
                StandardCharsets.UTF_16BE,
                StandardCharsets.UTF_16LE};
        for (Charset charset : charSets) {
            String converted = convert(value, charset.name(), probe);
            if (converted != null && converted.equals(convert(converted, probe, charset.name()))) {
                return charset.name();
            }
        }
        return "Unknown Charset";
    }

    public static String tinyURLGenKey(String input) {
        String timeStamp = Long.toHexString(System.nanoTime());
        String hash = Hasher.hash(input, HashMethod.MD5);
        return hash.substring(0, 2) + timeStamp.substring(4) + hash.substring(3, 5);
    }

    public static String escapeHTML(String text) {
        if (isNullOrEmpty(text)) {
            return text;
        }
        return HtmlUtils.htmlEscape(text, StandardCharsets.UTF_8.name());
    }

    public static String escapeForCSV(int num) {
        return String.valueOf(num);
    }

    public static String escapeForCSV(double value) {
        return NumUtil.formatNumber2(value);
    }

    public static String escapeForCSV(String value, boolean escape) {
        value = StringUtil.checkNullAndEmpty(value, "");
        if (escape) {
            value = StringEscapeUtils.escapeCsv(value);
        }
        return value;
    }
}
