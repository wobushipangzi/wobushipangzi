

import com.liujie.Utils.TimeUtils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Random {

    public static int i = 0;

    /**
     * 测试超时问题
     * @param args
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        //设置将要在几点钟唤醒
        final Integer rouseTime = 92700; //每两位数分别对应24小时制的时分秒
        final Integer hour = 9;  //设置时
        final Integer minute = 6;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    Long sleepSeconds = null; //睡眠时间

                    //获取当前的系统时间
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
                    Integer nowTime = Integer.valueOf(dateFormat.format(new Date()));
                    if (nowTime < rouseTime) {
                        //证明是在当天
                        sleepSeconds = TimeUtils.getsomeSeconds(0, hour, minute);
                        System.out.println(sleepSeconds);
                    } else {
                        //证明是在设定之间之后 需要隔天
                        sleepSeconds = TimeUtils.getsomeSeconds(1, hour, minute);

                        System.out.println(sleepSeconds);
                    }

                }
            }
        }).start();
    }


    /**
     * 十六进制转换字符串
     * @param hex String 十六进制
     * @return String 转换后的字符串
     */
    public static String hex2bin(String hex) {
        String digital = "0123456789ABCDEF";
        char[] hex2char = hex.toCharArray();
        byte[] bytes = new byte[hex.length() / 2];
        int temp;
        for (int i = 0; i < bytes.length; i++) {
            temp = digital.indexOf(hex2char[2 * i]) * 16;
            temp += digital.indexOf(hex2char[2 * i + 1]);
            bytes[i] = (byte) (temp & 0xff);
        }
        return new String(bytes);
    }

    /**
     * 字符串转java字节码
     * @param b
     * @return
     */
    public static byte[] hex2byte(byte[] b) {
        if ((b.length % 2) != 0) {
            throw new IllegalArgumentException("长度不是偶数");
        }
        byte[] b2 = new byte[b.length / 2];
        for (int n = 0; n < b.length; n += 2) {
            String item = new String(b, n, 2);
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个进制字节

            b2[n / 2] = (byte) Integer.parseInt(item, 16);
        }
        b = null;
        return b2;
    }


    /**
     * 把字节写进文件
     * @throws IOException
     */
    public static void writeBytesToFile2(byte[] bytes) throws IOException{

        ByteBuffer bb = ByteBuffer.wrap(bytes);

        FileChannel fc = new FileOutputStream("D:\\test\\ceshi2.mp3").getChannel();
        fc.write(bb);
        fc.close();
    }

    /**
     * 十六进制转为二进制
     * @param hexStr
     * @return
     */
    public static byte[] parseHexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }


    /**
     * 16进制转二进制
     * @param num
     */
    public static void toHex(int num){

        //& 两两为1即为1
        //>>>无符号右移
        /**
         * eg.60
         *       0000-0000 0000-0000 0000-0000 0011-1100   60的二进制表示
         * &     0000-0000 0000-0000 0000-0000 0000-1111   15的二进制表示
         * &后的值   0000-0000 0000-0000 0000-0000 0000-1100          值为12即16进制的C
         */
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 8; i++) {
            int temp = num & 15;
            if(temp>9){
                sb.append((char)(temp-10+'A'));//强转成16进制

            }else{
                sb.append(temp);
            }
            num = num >>>4;
        }
        System.out.println(sb.reverse());//0000003C

    }



    //十六进制转二进制
    public static byte[] hexStringToByte(String hex) {
        int len = (hex.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }
        return result;
    }

    private static byte toByte(char c) {
        byte b = (byte) "0123456789abcdef".indexOf(c);
        return b;
    }
}
