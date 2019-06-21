package com.liujie.Utils;

import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.liujie.result.FlockGlobeResult;
import com.liujie.result.WXGlobalResult;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class WeiXinUtils {

    /**
     * 生成登录微信的二维码
     * @return
     * @throws IOException
     */
    public static String getQRCode() throws IOException {
        /**
         * 获取生成二维码所需的uuid
         */
        //组装登录的请求参数
        String loginUrl = "https://login.weixin.qq.com/jslogin?appid=wx782c26e4c19acffb&fun=new&lang=zh_CN&_=";

        //发送登录请求
        String s = HttpClientUtils.httpGetRequest(loginUrl);
        String uuid = s.substring(50, 62);

        /**
         * 生成登录二维码
         */
        //组装生成二维码的URL
        String QRCodeUrl = "https://login.wx.qq.com/qrcode/" + uuid + "?t=webwx";
        System.out.println("请手动访问如下网址：==>" + QRCodeUrl);
        return uuid;
    }


    /**
     * 轮询登录
     */
    public static String pollLogin(String uuid) throws IOException {
        //组装轮询手机端是否已经扫描二维码并确认在Web端登录的URL
        String pollUrl = "https://login.wx.qq.com/cgi-bin/mmwebwx-bin/login?tip=1&uuid="+uuid;
        String loginSuccessUrl = null;
        boolean flag = true;
        while (flag){
            String returnCode = HttpClientUtils.httpGetRequest(pollUrl);
            if(returnCode.length() > 30){
                loginSuccessUrl = returnCode.substring(38, returnCode.length() - 2) + "&fun=new";
                System.out.println("返回码为：==>" + returnCode);
                flag = false;
            }
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return loginSuccessUrl;
    }


    /**
     * 获取syncKey
     * @param syncKeyMap
     * @return
     */
    public static String getSyncKey(Map<String, Object> syncKeyMap) {
        List list = (List) syncKeyMap.get("List");
        StringBuffer syncAppend = new StringBuffer();
        for (Object o : list) {
            Map mapSync = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class);
            syncAppend.append(mapSync.get("Key") + "_" + mapSync.get("Val") + "%7C");
        }
        return syncAppend.substring(0,syncAppend.length()-3);
    }


    /**
     * 检查是否有新消息
     * @param skey
     * @param wxsid
     * @param wxuin
     * @param syncKey
     * @return
     * @throws IOException
     */
    public static String checkNewMessage( String skey, String wxsid, String wxuin, String syncKey) throws IOException {
        /**
         * 消息检查，是否有新消息进入
         */
        String deviceID = "e" + Random.getRandomNum(15);
        String syncServiceUrl ="https://webpush.wx.qq.com/cgi-bin/mmwebwx-bin/synccheck?r="+new Date().getTime()+"&skey="+skey+"&sid="+wxsid+"&uin="+wxuin+"&deviceid="+deviceID+"&synckey="+syncKey+"&_="+new Date().getTime();
        //监听是否接收到新消息
        String newMessage = HttpClientUtils.httpGetRequest(syncServiceUrl);
        if(StringUtils.isNotEmpty(newMessage)){
            String retcodeStr = JSONObject.parseObject(newMessage.split("=")[1], Map.class).get("retcode").toString();
            if(!("0".equals(retcodeStr))){
                System.out.println("监听异常消息：==>" + newMessage);
            }
            return retcodeStr;
        }
        return "";
    }


    /**
     * 获取最新消息
     * @param skey
     * @param wxsid
     * @param wxuin
     * @param syncKeyMap
     * @param nickRandomUserNameMap
     * @param remarkRandomUserNameMap
     * @param userName
     * @return
     */
    public static WXGlobalResult getNewMessage(String skey, String wxsid, String wxuin, Map<String, Object> syncKeyMap, Map<String,String> nickRandomUserNameMap, Map<String,String> remarkRandomUserNameMap, String userName) {

        WXGlobalResult wxGlobalResult = new WXGlobalResult();

        //获取最新消息的url
        String getMessageUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsync?sid="+wxsid+"&skey="+skey;

        //接收消息的请求参数
        HashMap<String, Object> getMessageMap = new HashMap<>();
        HashMap<String, Object> baseRequestMap = new HashMap<>();
        String deviceID = "e" + Random.getRandomNum(15);

        baseRequestMap.put("DeviceID",deviceID);
        baseRequestMap.put("Sid",wxsid);
        baseRequestMap.put("Skey",skey);
        baseRequestMap.put("Uin",wxuin);

        getMessageMap.put("SyncKey",syncKeyMap);
        getMessageMap.put("rr","-"+new Date().getTime()/1000);
        getMessageMap.put("BaseRequest",baseRequestMap);
        String getMessageJson = JSONObject.toJSONString(getMessageMap);

        String getMessage = HttpClientUtils.httpPostJsonRestRequest(getMessageUrl, getMessageJson);

        //System.out.println("获取到的信息为：==> " + getMessage);

        //发送消息人
        String fromUserName = null;
        //消息内容 iso-8859-1  需要转码
        String content = null;
        //消息类型
        String msgType = null;
        //消息编号
        String msgId = null;
        //小程序编号
        String appMsgType = null;
        String sendTulMessage = null;
        if(StringUtils.isNotEmpty(getMessage) && JSONObject.parseObject(getMessage, Map.class) != null && StringUtils.isNotEmpty(JSONObject.parseObject(getMessage, Map.class).get("AddMsgList").toString())){
            //解析新消息
            List msgList = JSONObject.parseObject(JSONObject.toJSONString(JSONObject.parseObject(getMessage, Map.class).get("AddMsgList")),List.class);

            WXMessage:
            for (Object o : msgList) {
                content = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class).get("Content").toString();
                fromUserName = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class).get("FromUserName").toString();

                //如果获取到了消息，结束循环
                if (StringUtils.isNotEmpty(content) && StringUtils.isNotEmpty(fromUserName)) {
                    msgType = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class).get("MsgType").toString();
                    msgId = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class).get("MsgId").toString();
                    appMsgType = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class).get("AppMsgType").toString();
                    break WXMessage;
                }
            }

            //反转Map集合  通过value拿到key
            String nickName = null;
            for (String nickRandomUserNameKey : nickRandomUserNameMap.keySet()) {
                if(StringUtils.isNotEmpty(fromUserName) && fromUserName.equals(nickRandomUserNameMap.get(nickRandomUserNameKey))){
                    nickName = nickRandomUserNameKey;
                    if(StringUtils.isBlank(nickName)){
                        nickName = "";
                    }
                }
            }

            String remarkName = null;
            for (String remarkRandomUserNameMapKey : remarkRandomUserNameMap.keySet()) {
                if(StringUtils.isNotEmpty(fromUserName) && fromUserName.equals(remarkRandomUserNameMap.get(remarkRandomUserNameMapKey))){
                    remarkName = remarkRandomUserNameMapKey;
                    if(StringUtils.isBlank(remarkName)){
                        remarkName = "";
                    }
                }
            }

            if (StringUtils.isNotEmpty(content)) {
                //如果微信返回字符编码为ISO-8859-1 需要转换为UTF-8
                sendTulMessage = TransCoding.transcoding(content);
                if(userName.equals(fromUserName)){
                    System.out.println("我回复微信消息：==>" + sendTulMessage);
                }else if(!("@@".equals(fromUserName.substring(0,2)))){
                    System.out.println("[" + (StringUtils.isNotEmpty(remarkName)? remarkName : nickName) + "] 发来微信消息：==>" + sendTulMessage);
                }
            }
        }


        wxGlobalResult.setFromUserName(fromUserName);
        wxGlobalResult.setSendTulMessage(sendTulMessage);
        wxGlobalResult.setGetMessage(getMessage);
        wxGlobalResult.setMsgType(msgType);
        wxGlobalResult.setMsgId(msgId);
        wxGlobalResult.setAppMsgType(appMsgType);
        return wxGlobalResult;
    }


    /**
     * 发送消息给微信
     * @param skey
     * @param wxsid
     * @param wxuin
     * @param userName
     * @param fromUserName
     * @param tulreplyMessage
     */
    public static void sendWXMessage(String skey, String wxsid, String wxuin, String userName, Object fromUserName, String tulreplyMessage) {

        System.out.println("我回复微信消息：==>" + tulreplyMessage);

        //组装发送微信消息URL
        String sendMessageUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxsendmsg";
        String  deviceID = "e" + Random.getRandomNum(15);
        Map<String, Object> sendMap = new HashMap<>();
        Map<String, Object> dskuMap = new HashMap<>();
        Map<String, Object> ccloftyMap = new HashMap<>();

        dskuMap.put("DeviceID",deviceID);
        dskuMap.put("Sid",wxsid);
        dskuMap.put("Skey",skey);
        dskuMap.put("Uin",wxuin);

        String ClientMsgId = ""+new Date().getTime()+Random.getRandomNum(4);
        ccloftyMap.put("ClientMsgId",ClientMsgId);
        ccloftyMap.put("Content",tulreplyMessage);
        ccloftyMap.put("FromUserName",userName);
        ccloftyMap.put("LocalID",ClientMsgId);
        ccloftyMap.put("ToUserName",fromUserName);
        ccloftyMap.put("Type",1);

        sendMap.put("BaseRequest",dskuMap);
        sendMap.put("Msg",ccloftyMap);
        sendMap.put("Scene",0);

        String sendJson = JSONObject.toJSONString(sendMap);
        //调用发送微信消息接口  post
        HttpClientUtils.httpPostJsonRestRequest(sendMessageUrl, sendJson);
    }

    /**
     * 重新从接收到的消息里面获取消息检查的syncKey
     * @param getKeyMessageMap
     * @return
     */
    public static String getSyncKeyTwo(Map<String, Object> getKeyMessageMap) {
        Map getSyncKeyMap = JSONObject.parseObject(getKeyMessageMap.get("SyncKey").toString(), Map.class);
        List syncKeyList = (List) getSyncKeyMap.get("List");
        StringBuffer syncKeyAppend = new StringBuffer();
        for (Object o : syncKeyList) {
            Map mapSync = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class);
            syncKeyAppend.append(mapSync.get("Key") + "_" + mapSync.get("Val") + "%7C");
        }
        String syncKey = syncKeyAppend.substring(0,syncKeyAppend.length()-3);
        return syncKey;
    }


    /**
     * 防撤回功能
     * @param skey
     * @param wxsid
     * @param wxuin
     * @param userName
     * @param remarkRandomUserNameMap
     * @param nickRandomUserNameMap
     * @param wxGlobalResult
     * @param reCallSwitch
     * @param pass_ticket
     */
    public static void stopReCall(String skey, String wxsid, String wxuin, String userName, HashMap<String, String> remarkRandomUserNameMap, HashMap<String, String> nickRandomUserNameMap, WXGlobalResult wxGlobalResult, boolean reCallSwitch, String pass_ticket) {

        //将所有拿到的消息，携带msgId 全部以fromUserName:msgId为key存入redis 过期时间2分15秒
        if (StringUtils.isNotEmpty(wxGlobalResult.getFromUserName())) {
            String subFromUserName = wxGlobalResult.getFromUserName().substring(0, 2);
            if (StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage()) && !(userName.equals(wxGlobalResult.getFromUserName())) && ("1".equals(wxGlobalResult.getMsgType()) || "47".equals(wxGlobalResult.getMsgType()))) {
                Jedis jedis = JedisUtils.getJedis();
                String key = wxGlobalResult.getFromUserName() + ":" + wxGlobalResult.getMsgId();
                String value = wxGlobalResult.getSendTulMessage();
                jedis.setex(key,135, value);
                jedis.close();
            }
            reCall:
            while (reCallSwitch){
                //如果发来消息msgId为10002 代表对方撤回了消息，截获撤回消息的msgId 拿到对应的消息
                if(StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage()) && !(userName.equals(wxGlobalResult.getFromUserName())) && "10002".equals(wxGlobalResult.getMsgType())){
                    //截取消息中的msgId
                    String subMsgId = wxGlobalResult.getSendTulMessage().split("&lt;msgid&gt;") [1];
                    String oldMsgId = subMsgId.split("&lt;/msgid&gt;")[0];
                    String key = wxGlobalResult.getFromUserName() + ":" + oldMsgId;

                    if(!("@@".equals(subFromUserName))){
                        //反转Map集合  通过value拿到key
                        String nickName = null;
                        for (String nickRandomUserNameKey : nickRandomUserNameMap.keySet()) {
                            if(StringUtils.isNotEmpty(wxGlobalResult.getFromUserName()) && wxGlobalResult.getFromUserName().equals(nickRandomUserNameMap.get(nickRandomUserNameKey))){
                                nickName = nickRandomUserNameKey;
                            }
                        }

                        String remarkName = null;
                        for (String remarkRandomUserNameMapKey : remarkRandomUserNameMap.keySet()) {
                            if(StringUtils.isNotEmpty(wxGlobalResult.getFromUserName()) && wxGlobalResult.getFromUserName().equals(remarkRandomUserNameMap.get(remarkRandomUserNameMapKey))){
                                remarkName = remarkRandomUserNameMapKey;
                                if(StringUtils.isEmpty(remarkName)){
                                    remarkName = "";
                                }
                            }
                        }
                        Jedis jedis = JedisUtils.getJedis();
                        if(StringUtils.isEmpty(jedis.get(key))){
                            break reCall;
                        }
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                        String reCallMessage = dateFormat.format(new Date()) + " [" + (StringUtils.isNotEmpty(remarkName)? remarkName : nickName) + "] 撤回消息 ==> " + jedis.get(key);
                        jedis.close();
                        //发送给文件传输助手
                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, "filehelper", reCallMessage);
                    }

                    if("@@".equals(subFromUserName)){
                        Jedis jedis = JedisUtils.getJedis();
                        if(StringUtils.isEmpty(jedis.get(key))){
                            break reCall;
                        }
                        String sendTulMessage = jedis.get(key);
                        jedis.close();

                        String[] splitMessage = sendTulMessage.split(":<br/>");
                        String deviceID = "e" + Random.getRandomNum(15);
                        /**
                         * 获取群成员信息
                         */
                        FlockGlobeResult flockMessage = getFlockMessage(skey, wxsid, wxuin, pass_ticket, deviceID, wxGlobalResult, splitMessage);

                        String flockName = flockMessage.getFlockName();
                        String nickName = flockMessage.getNickName();
                        String displayName = flockMessage.getDisplayName();

                        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
                        if(splitMessage.length > 1){
                            String reCallMessage = dateFormat.format(new Date()) + " [" + flockName + "] [" + (StringUtils.isNotEmpty(displayName)? displayName : nickName) + "]撤回消息 ==> " + splitMessage[1];
                            WeiXinUtils.sendWXMessage(skey,wxsid,wxuin,userName,"filehelper", reCallMessage);
                        }
                    }
                }
                break reCall;
            }
        }
    }



    /**
     * 获取群成员信息
     * @param skey
     * @param wxsid
     * @param wxuin
     * @param pass_ticket
     * @param deviceID
     * @param wxGlobalResult
     */
    public static FlockGlobeResult getFlockMessage(String skey, String wxsid, String wxuin, String pass_ticket, String deviceID, WXGlobalResult wxGlobalResult, String[] splitMessage) {

        FlockGlobeResult flockGlobeResult = new FlockGlobeResult();

        Map<Object, Object> flockMap = new HashMap<>();
        Map<Object, Object> baseRequestMap = new HashMap<>();
        baseRequestMap.put("Uin", wxuin);
        baseRequestMap.put("Sid", wxsid);
        baseRequestMap.put("Skey", skey);
        baseRequestMap.put("DeviceID", deviceID);
        HashMap<String, String> listMap = new HashMap<>();
        ArrayList<Map<String, String>> list = new ArrayList<>();
        listMap.put("UserName", wxGlobalResult.getFromUserName());
        listMap.put("EncryChatRoomId", "");
        list.add(listMap);
        flockMap.put("BaseRequest", baseRequestMap);
        flockMap.put("Count", 1);
        flockMap.put("List", list);
        String flockJson = JSONObject.toJSONString(flockMap);
        String flockUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxbatchgetcontact?pass_ticket=" + pass_ticket + "&type=ex";
        String flockStr = HttpClientUtils.httpPostJsonRestRequest(flockUrl, flockJson);
        //System.out.println("获取到的群消息为：==>" + flockStr);
        //解析群信息
        Map flockMessageMap = JSONObject.parseObject(flockStr, Map.class);
        List contactList = (List) flockMessageMap.get("ContactList");
        String nickName = null;
        String displayName = null;
        List<String> userNameList = new ArrayList<>();
        for (Object o : contactList) {
            Map contactMap = JSONObject.parseObject(JSONObject.toJSONString(o), Map.class);
            String flockName = contactMap.get("NickName").toString();
            flockName = TransCoding.transcoding(flockName);

            List memberList1 = (List) contactMap.get("MemberList");
            for (Object o1 : memberList1) {
                Map memberMap = JSONObject.parseObject(JSONObject.toJSONString(o1), Map.class);
                if(splitMessage [0].equals(memberMap.get("UserName"))){
                    nickName = memberMap.get("NickName").toString();
                    nickName = TransCoding.transcoding(nickName);
                    displayName = memberMap.get("DisplayName").toString();
                    displayName = TransCoding.transcoding(displayName);
                }
                userNameList.add(TransCoding.transcoding(memberMap.get("UserName").toString()));
            }
            flockGlobeResult.setFlockName(flockName);
            flockGlobeResult.setNickName(nickName);
            flockGlobeResult.setUserNameList(userNameList);
            flockGlobeResult.setDisplayName(displayName);
        }
        return flockGlobeResult;
    }
}
