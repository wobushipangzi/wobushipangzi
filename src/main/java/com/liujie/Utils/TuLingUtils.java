package com.liujie.Utils;

import com.alibaba.fastjson.JSONObject;
import com.liujie.result.TuLingGlobalResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TuLingUtils {

    /**
     * 给图灵机器人发送消息
     * @param i
     * @param apiList
     * @param sendTulMessage
     * @return
     * @throws IOException
     */
    public static TuLingGlobalResult sendMessageToTul(int i, List<String> apiList, String sendTulMessage) throws IOException {
        /**
         * 给图灵机器人发送消息v2.0
         */
        TuLingGlobalResult tuLingGlobalResult = new TuLingGlobalResult();

        //图灵机器人URL
        String sendTulUrl = "http://openapi.tuling123.com/openapi/api/v2";

        //获取接收到的消息  转为UTF-8编码
        Map<String, Object> tulMap = new HashMap<>();
        Map<String, Object> perceptionMap = new HashMap<>();
        Map<String, Object> userInfoMap = new HashMap<>();
        Map<String, Object> inputTextMap = new HashMap<>();

        inputTextMap.put("text", sendTulMessage);
        perceptionMap.put("inputText", inputTextMap);

        userInfoMap.put("apiKey", apiList.get(i));
        userInfoMap.put("userId", "1234567");

        tulMap.put("reqType", 0);
        tulMap.put("perception", perceptionMap);
        tulMap.put("userInfo", userInfoMap);

        String tulJson = JSONObject.toJSONString(tulMap);

        //发送消息给图灵机器人
        String tulMessage = HttpClientUtils.httpPostJsonRestRequest(sendTulUrl, tulJson);

        //解析图灵机器人返回的消息
        Map getTulMap = JSONObject.parseObject(tulMessage, Map.class);
        String code = ((Map) getTulMap.get("intent")).get("code").toString();
        List resultList = (List) getTulMap.get("results");

        String tulreplyMessage = null;
        for (Object o : resultList) {
            Map resultMap = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class);
            if ("text".equals(resultMap.get("resultType"))) {
                Map valuesMap = (Map) resultMap.get("values");
                //获取到图灵机器人返回的消息
                tulreplyMessage = valuesMap.get("text").toString();
                while (tulreplyMessage.startsWith("　")) {//这里判断是不是全角空格
                    tulreplyMessage = tulreplyMessage.substring(1, tulreplyMessage.length()).trim();
                }
                while (tulreplyMessage.endsWith("　")) {
                    tulreplyMessage = tulreplyMessage.substring(0, tulreplyMessage.length() - 1).trim();
                }
                //对图灵机器人返回消息解码
                tulreplyMessage = TransCoding.transcoding(tulreplyMessage);
            }
        }
        tuLingGlobalResult.setCode(code);
        tuLingGlobalResult.setTulreplyMessage(tulreplyMessage);
        return tuLingGlobalResult;
    }

        /**
         * 给图灵机器人发送消息V1.0
         */

        /*//获取接收到的消息  转为UTF-8编码
        String sendTulMessage = new String(content.getBytes("iso8859-1"), "UTF-8");
        System.out.println("接收到的微信消息为：==>" + sendTulMessage);
        while (sendTulMessage.startsWith("　")) {//这里判断是不是全角空格
            sendTulMessage = sendTulMessage.substring(1, sendTulMessage.length()).trim();
        }
        while (sendTulMessage.endsWith("　")) {
            sendTulMessage = sendTulMessage.substring(0, sendTulMessage.length() - 1).trim();
        }
        String sendTulUrl = "http://www.tuling123.com/openapi/api?key=" + apiList.get(i) + "&info=" + sendTulMessage;

        return HttpClientUtils.httpGetRequest(sendTulUrl);*/

}
