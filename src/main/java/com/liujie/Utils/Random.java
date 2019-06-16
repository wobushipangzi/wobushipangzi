package com.liujie.Utils;

public class Random {

    /**
     * 获取任意位数随机数
     */
    public static StringBuffer getRandomNum(int digit) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < digit; i++) {
            int v = (int) (Math.random()*10);
            stringBuffer = stringBuffer.append(v);
        }
        return stringBuffer;
    }
}
