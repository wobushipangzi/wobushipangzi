package com.liujie.Utils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class TransCoding {

    /**
     * 将ISO-8859-1的字符编码转为"UTF-8"
     * @param oldStr
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String transcoding(String oldStr) {
        if(Charset.forName("ISO-8859-1").newEncoder().canEncode(oldStr)){
            try {
                String newStr = new String(oldStr.getBytes("iso-8859-1"), "UTF-8");
                return newStr;
            } catch (UnsupportedEncodingException e) {
                System.out.println("字符转码异常");
            }
        }
        return oldStr;
    }
}
