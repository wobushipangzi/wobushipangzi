package com.liujie.Utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.ResourceBundle;

public class JedisUtils {

    private JedisUtils() {}
    private static JedisPool jedisPool;
    private static int maxtotal;
    private static int maxwaitmillis;
    private static String host;
    private static int port;
    private static String password;
    private static int timeOut;

    /*读取jedis.properties配置文件*/
    static{
        ResourceBundle rb = ResourceBundle.getBundle("jedis");
        maxtotal = Integer.parseInt(rb.getString("maxtotal"));
        maxwaitmillis = Integer.parseInt(rb.getString("maxwaitmillis"));
        host = rb.getString("host");
        port = Integer.parseInt(rb.getString("port"));
        password = rb.getString("password");
        timeOut = Integer.parseInt(rb.getString("timeOut"));
    }

    /*创建连接池*/
    static{
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(maxtotal);
        jedisPoolConfig.setMaxWaitMillis(maxwaitmillis);
        jedisPool = new JedisPool(jedisPoolConfig,host,port,timeOut,password);
    }

    /*获取jedis*/
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }

    /*关闭Jedis*/
    public static void close(Jedis jedis){
        if(jedis!=null){
            jedis.close();
        }
    }
}


