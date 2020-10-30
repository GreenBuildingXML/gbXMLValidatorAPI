package com.bimport.asharea.common.auzreFile;

public class LinkBuilder {
    public static String buildTestgbXMLLink(String file) {
        String url = "https://ashraedev01.file.core.windows.net/gbxml-test-file/cases/";
        url += file;
        url += "?sv=2019-12-12&ss=bfqt&srt=sco&sp=rwdlacupx&se=2022-01-04T14:49:59Z&st=2020-10-02T05:49:59Z&spr=https&sig=IH52C5thjGkKceqf0dPGxclZ302gpSqiUOqbeJQVxHw%3D";
        url += "&random=" + System.nanoTime();
        return url;
    }

    public static String buildLv1TestgbXMLLink(String file) {
        String url = "https://ashraedev01.file.core.windows.net/gbxml-test-file/lv1/";
        url += file;
        url += "?sv=2019-12-12&ss=bfqt&srt=sco&sp=rwdlacupx&se=2022-01-04T14:49:59Z&st=2020-10-02T05:49:59Z&spr=https&sig=IH52C5thjGkKceqf0dPGxclZ302gpSqiUOqbeJQVxHw%3D";
        url += "&random=" + System.nanoTime();
        return url;
    }
}
