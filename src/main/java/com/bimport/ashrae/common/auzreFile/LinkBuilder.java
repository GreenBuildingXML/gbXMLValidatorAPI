package com.bimport.ashrae.common.auzreFile;

public class LinkBuilder {
    public static String buildTestgbXMLLink(String file) {
        String url = "https://schemaserverstorage.file.core.windows.net/gbxml-test-file/cases/";
        url += file;
        url += "?sv=2019-12-12&ss=bfqt&srt=sco&sp=rwdlacupx&se=2030-01-12T00:05:25Z&st=2021-01-11T16:05:25Z&spr=https&sig=Dxl2qXpPcoQ85EHMK7onFVR%2FBH31mdzy83tXy4Vzfq4%3D";
        url += "&random=" + System.nanoTime();
        return url;
    }

    public static String buildLv1TestgbXMLLink(String file) {
        String url = "https://schemaserverstorage.file.core.windows.net/gbxml-test-file/lv1/";
        url += file;
        url += "?sv=2019-12-12&ss=bfqt&srt=sco&sp=rwdlacupx&se=2030-01-12T00:05:25Z&st=2021-01-11T16:05:25Z&spr=https&sig=Dxl2qXpPcoQ85EHMK7onFVR%2FBH31mdzy83tXy4Vzfq4%3D";
        url += "&random=" + System.nanoTime();
        return url;
    }
}
