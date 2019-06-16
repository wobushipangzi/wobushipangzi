package com.liujie.Utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;


public class HttpClientUtils {

    private static final CloseableHttpClient httpClient = createHttpClient();

    /**
     * post方式只提交URL
     *
     * @param url
     * @return
     */
    public static String httpPostRequest(String url) {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }

    /**
     * post方式发送json数据 "application/json"
     *
     * @param url
     * @param json
     * @return
     */
    public static String httpPostJsonRestRequest(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(json, "UTF-8");
        entity.setContentType("application/json");
        httpPost.setEntity(entity);
        return getResult(httpPost);
    }

    /**
     * post方式发送json数据 "application/x-www-form-urlencoded"
     *
     * @param url
     * @param json
     * @return
     */
    public static String httpPostRestRequest(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(json, "UTF-8");
        entity.setContentType("application/x-www-form-urlencoded");
        httpPost.setEntity(entity);
        return getResult(httpPost);
    }

    /**
     * post方式发送request请求
     *
     * @param url
     * @param params
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String httpPostRequest(String url, Map<String, String> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
        return getResult(httpPost);
    }

    public static String httpPostReq(String url, Map<String, Object> params) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(url);
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (String key : params.keySet()) {
            Object value = params.get(key);
            if (value instanceof Collection || value instanceof Map) {
                pairs.add(new BasicNameValuePair(key, JSONObject.toJSONString(value)));
            } else {
                pairs.add(new BasicNameValuePair(key, String.valueOf(value)));
            }
        }
        httpPost.setEntity(new UrlEncodedFormEntity(pairs, "UTF-8"));
        return getResult(httpPost);
    }

    /**
     * get方式提交
     *
     * @param url
     * @return
     */
    public static String httpGetRequest(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return getResult(httpGet);
    }



    /**
     * get方式发送请求
     *
     * @param url
     * @param params
     * @return
     * @throws URISyntaxException
     */
    public static String httpGetRequest(String url, Map<String, String> params) {
        URIBuilder ub = new URIBuilder();
        ub.setPath(url);

        ArrayList<NameValuePair> pairs = covertParams2NVPS(params);
        ub.setParameters(pairs);

        HttpGet httpGet = null;
        try {
            httpGet = new HttpGet(ub.build());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return getResult(httpGet);
    }


    private static String getResult(HttpRequestBase request) {
        CloseableHttpClient httpClient = getHttpClient();
        try {
            CloseableHttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private static CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    private static ArrayList<NameValuePair> covertParams2NVPS(Map<String, String> params) {
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        for (Map.Entry<String, String> param : params.entrySet()) {
            pairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
        }
        return pairs;
    }

    /**
     * 通过连接池获取HttpClient
     *
     * @return
     */
    private static CloseableHttpClient createHttpClient() {
        RequestConfig config = RequestConfig.custom()
                .setSocketTimeout(30000)
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(30000)
                .build();
        return HttpClients.custom().setDefaultRequestConfig(config)
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(20)
                .build();
    }
}
