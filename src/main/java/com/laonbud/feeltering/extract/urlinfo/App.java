package com.laonbud.feeltering.extract.urlinfo;

import java.io.IOException;

/**
 * Hello world!
 *	main  test
 */

public class App 
{
    public static void main( String[] args ) throws IOException
    {
        // test
        String targetURL = "http://www.sfu.ca/sasdoc/sashtml/lrcon/zenid-63.htm";
        
        // url 정보 추출
        ExtractUrlInfo test = new ExtractUrlInfo();
        UrlInfo testObj = new UrlInfo();
        testObj = test.ExtractInfo(targetURL);
        testObj.showObjt();
    }
}
