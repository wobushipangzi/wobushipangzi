package com.liujie.robot;


import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.liujie.Utils.*;
import com.liujie.Utils.Random;
import com.liujie.result.FlockGlobeResult;
import com.liujie.result.TuLingGlobalResult;
import com.liujie.result.WXGlobalResult;
import redis.clients.jedis.Jedis;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;


public class Robot {

    public static boolean sumSwitch = false;  //自动回复开关

    public static boolean replySwitch = false;  //各回复功能开关

    public static boolean customSwitch = false;  //自定义回复开关

    public static boolean customSmallSwitch = false;  //自定义回复内层开关

    public static boolean repeaterSwitch = false;  //复读机开关

    public static boolean reCallSwitch = false;  //消息防撤回开关

    public static int i = 0;   //切换apiKey

    public static void main(String[] args) {

        final List<String> apiList = new ArrayList<>();
        apiList.add("35c35802633a4092923d4a302473267d");
        apiList.add("7098fdc7da1d45f38a22d87a9e23f364");
        apiList.add("a678c77e6fb248d6beede8987bb616d7");
        apiList.add("9e79506f0f9f4a0582f1525cc858bc61");
        apiList.add("0a8cb64588b5456a99fd3dcc51f3dc1b");
        apiList.add("23c3488aac50441bb6de68d99c711280");
        apiList.add("4a0488cdce684468b95591a641f0971d");
        apiList.add("7c8cdb56b0dc4450a8deef30a496bd4c");
        apiList.add("37caebb606414754ac902bb4f32aeff9");
        apiList.add("60a7aca5461b40aa9ff138e436ad412f");
        apiList.add("fa3f411eaf1c41fe98f34b231c38365d");
        apiList.add("a99a4b7a2cc740da81f67eb25c5337ef");
        apiList.add("9b9662b726f3474f9574cd90fd78f4d8");
        apiList.add("453b2da4ec4f4bec947fda36f6e1eedf");
        apiList.add("8edce3ce905a4c1dbb965e6b35c3834d");

        //生成登录微信的二维码
        String uuid = null;
        try {
            uuid = WeiXinUtils.getQRCode();
        } catch (IOException e) {
            System.out.println("获取微信二维码异常");
        }

        //轮询登录
        String loginSuccessUrl = null;
        try {
            loginSuccessUrl = WeiXinUtils.pollLogin(uuid);
        } catch (IOException e) {
            System.out.println("轮询登录异常");
        }

        //获取用户重要参数
        String userParameters = null;
        try {
            userParameters = HttpClientUtils.httpGetRequest(loginSuccessUrl);
            //System.out.println("获取用户重要参数：==>" + userParameters);
        } catch (IOException e) {
            //System.out.println("获取用户重要参数异常");
        }

        String[] skeys = userParameters.split("skey");
        final String skey = skeys[1].substring(1, skeys[1].length() - 2);
        String[] wxsids = userParameters.split("wxsid");
        final String wxsid = wxsids[1].substring(1, wxsids[1].length() - 2);
        String[] wxuins = userParameters.split("wxuin");
        final String wxuin = wxuins[1].substring(1, wxuins[1].length() - 2);
        String[] pass_tickets = userParameters.split("pass_ticket");
        String pass_ticket = pass_tickets[1].substring(1, pass_tickets[1].length() - 2);


        /**
         * 初始化微信
         */
        String initializeUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxinit?pass_ticket=" + pass_ticket;

        StringBuffer append = Random.getRandomNum(15);
        String deviceID = "e" + append;
        Map<String, String> map = new HashMap<>();
        map.put("Uin", wxuin);
        map.put("Sid", wxsid);
        map.put("Skey", skey);
        map.put("DeviceID", deviceID);
        String toJSONString = JSONObject.toJSONString(map);

        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("BaseRequest", toJSONString);
        String json = JSONObject.toJSONString(jsonMap);

        //发送初始化微信信息
        String initializeStr = HttpClientUtils.httpPostJsonRestRequest(initializeUrl, json);
        Map initializeMap = JSONObject.parseObject(initializeStr, Map.class);
        Map syncKeyMap = (Map) initializeMap.get("SyncKey");
        if(syncKeyMap.size() < 2){
            initializeUrl = "https://wx2.qq.com/cgi-bin/mmwebwx-bin/webwxinit?r=-" + Random.getRandomNum(9);
            //发送初始化微信信息
            initializeStr = HttpClientUtils.httpPostJsonRestRequest(initializeUrl, json);

            initializeMap = JSONObject.parseObject(initializeStr, Map.class);
            syncKeyMap = (Map) initializeMap.get("SyncKey");
        }

        /**
         * 获取syncKey
         */
        String syncKey = WeiXinUtils.getSyncKey(syncKeyMap);

        //获取测试微信号的名字（随机码）
        Map userMap = JSONObject.parseObject(JSONObject.toJSONString(initializeMap.get("User")), Map.class);
        final String userName = userMap.get("UserName").toString();

        /**
         * 获取所有微信联系人
         */
        //拼装获取所有好友列表的URL
        String getFriendsUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxgetcontact?pass_ticket=" + pass_ticket + "&skey=" + skey;
        String getFriends = HttpClientUtils.httpPostJsonRestRequest(getFriendsUrl, json);
        //解析微信好友
        Map friendsMap = JSONObject.parseObject(getFriends, Map.class);
        List memberList = (List) friendsMap.get("MemberList");

        final HashMap<String, String> remarkRandomUserNameMap = new HashMap<>();
        HashMap<String, String> nickRandomUserNameMap = new HashMap<>();

        //所有好友的标识都存入这个集合
        final Set<String> fromUserNameSet = new HashSet<>();

        for (Object o : memberList) {
            Map memberMap = JSONObject.parseObject(o.toString(), Map.class);
            String randomUserName = memberMap.get("UserName").toString();
            String subRandomUserName = randomUserName.substring(0,2);

            if(!("@@".equals(subRandomUserName))){
                fromUserNameSet.add(randomUserName);
            }

            //获取微信备注名称
            String remarkName = memberMap.get("RemarkName").toString();
            //转换字符编码
            remarkName = TransCoding.transcoding(remarkName);
            if (StringUtils.isNotEmpty(remarkName) && StringUtils.isNotEmpty(randomUserName)) {
                remarkRandomUserNameMap.put(remarkName, randomUserName);
            }

            //获取微信昵称
            String nickName = memberMap.get("NickName").toString();
            //转换字符编码
            nickName = TransCoding.transcoding(nickName);
            if (StringUtils.isNotEmpty(nickName) && StringUtils.isNotEmpty(randomUserName)) {
                nickRandomUserNameMap.put(nickName, randomUserName);
            }
        }

        String sendMessageToSelf = "您好！欢迎使用我不是胖子版微信助手!\n" +
                "\n回复 1 开启自动回复" +
                "\n回复 2 关闭自动回复" +
                "\n回复 3 开启消息防撤回" +
                "\n回复 4 关闭消息防撤回" +
                "\n回复 天气 查询气象信息";

        WeiXinUtils.sendWXMessage(skey,wxsid,wxuin,userName,userName,sendMessageToSelf);

        /**
         * 定时唤醒功能
         */
        //设置要唤醒人的备注名字
        final String remarkUserName = "晖宝宝";
        //设置将要在几点钟唤醒
        final Integer rouseTime = 82500; //每两位数分别对应24小时制的时分秒
        final Integer hour = 8;  //设置时
        final Integer minute = 25;
        new Thread(new Runnable() {
            @Override
            public void run() {
            while (true){

                Long sleepSeconds = null; //睡眠时间

                //获取当前的系统时间
                SimpleDateFormat dateFormat = new SimpleDateFormat("HHmmss");
                Integer nowTime = Integer.valueOf(dateFormat.format(new Date()));
                if(nowTime < rouseTime){
                    //证明是在当天
                    sleepSeconds = TimeUtils.getsomeSeconds(0, hour, minute);
                }else if(nowTime > rouseTime){   //防止一条消息重复发送多次，不设置等于的情况
                    //证明是在设定之间之后 需要隔天
                    sleepSeconds = TimeUtils.getsomeSeconds(1, hour, minute);
                }
                //睡眠对应时间 在设定时间唤醒
                try {
                    Thread.sleep(sleepSeconds * 1000);
                } catch (InterruptedException e) {
                    System.out.println("唤醒功能睡眠异常");
                }

                //通过备注名称拿到发送消息需要的名称
                String randomUserName = remarkRandomUserNameMap.get(remarkUserName);
                //设置发送内容
                String sendWXMessageStr = "小仙女赶紧起床了 快看看今天天气怎么样 ";
                String sendTulMessage = "今天浮山天气怎么样";
                TuLingGlobalResult tuLingGlobalResult = null;
                try {
                    tuLingGlobalResult = TuLingUtils.sendMessageToTul(i, apiList, sendTulMessage);
                } catch (IOException e) {
                    System.out.println("唤醒功能发送图灵机器人消息异常");
                }
                String tulreplyMessage = tuLingGlobalResult.getTulreplyMessage();
                sendWXMessageStr = sendWXMessageStr + tulreplyMessage;
                WeiXinUtils.sendWXMessage(skey,wxsid,wxuin,userName,randomUserName,sendWXMessageStr);

                //把唤醒人信息存入redis
                Jedis jedis = JedisUtils.getJedis();
                //组装key
                String rouseKey = "huanxing" + randomUserName;
                jedis.setex(rouseKey, 12 * 60 * 60,String.valueOf(new Date().getTime()));
                jedis.close();

                try {
                    Thread.sleep(1000*2);
                } catch (InterruptedException e) {
                    System.out.println("唤醒线程睡眠异常");
                }
            }
            }
        }).start();


        /**
         * 起一个线程监测发送的消息过去了多长时间
         */
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    //自动回复开启的情况下
                    if(sumSwitch){
                        /**
                         * 如果对方三分钟没有说话  自动回复
                         */
                        //获取redis存放的时间
                        Jedis jedis = JedisUtils.getJedis();
                        //获取key
                        for (String fromUserNameKey : fromUserNameSet) {
                            //自定义回复关闭 并且复读机功能关闭 才实现此功能
                            if(!customSwitch && !repeaterSwitch){
                                if(StringUtils.isNotEmpty(jedis.get(fromUserNameKey)) && !(userName.equals(fromUserNameKey))){
                                    //获取当前时间戳
                                    long currentTime = new Date().getTime();
                                    //获取redis存放的时间戳
                                    long timeValue = Long.valueOf(jedis.get(fromUserNameKey));
                                    //如果会话时间超过三分钟  自动回复
                                    if((currentTime - timeValue) > (1000*60*3-1050) && (currentTime - timeValue) < (1000*60*3+1050)){
                                        //发送自动回复消息 并且结束循环
                                        String tulreplyMessage = "咦？你都三分钟没说话了，再不说话我走了哦[奸笑]";
                                        if(!(userName.equals(fromUserNameKey))){
                                            WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, fromUserNameKey, tulreplyMessage);
                                        }
                                    }
                                }
                            }

                            /**
                             * 以下功能为多次唤醒功能 三分钟一次
                             */

                            //获取待唤醒人的时间戳
                            String rouseKey = "huanxing" + remarkRandomUserNameMap.get(remarkUserName);
                            if(StringUtils.isNotEmpty(jedis.get(rouseKey))){
                                //如果时间过了三分钟
                                long rouseTime = Long.valueOf(jedis.get(rouseKey));
                                long currentTime = new Date().getTime();
                                if((currentTime - rouseTime) > (1000*60*3-1050) && (currentTime - rouseTime) < (1000*60*3+1050)){
                                    //再次发送唤醒消息
                                    String tulreplyMessage = "过去三分钟了，赶紧起床哇！！！";
                                    WeiXinUtils.sendWXMessage(skey,wxsid,wxuin,userName,remarkRandomUserNameMap.get(remarkUserName),tulreplyMessage);
                                }
                            }
                        }
                        jedis.close();
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        System.out.println("问候功能线程睡眠异常");
                    }
                }
            }
        }).start();


        //设置不对谁自动回复
        List<String> noReply = new ArrayList<>();
        //noReply.add("段朋");
        //noReply.add("晖宝宝");
        noReply.add("国美~辰辉");
        noReply.add("国美~韩达维");
        //noReply.add("邓博阳");


        /**
         * 核心部分
         * 1检查新消息
         * 2获取新消息
         * 3发送消息
         */
        TuLingGlobalResult tuLingGlobalResultWeater = null;   //查询天气时统一返回对象
        globalCycle:
        while (true) {

            //检查新消息
            String retcodeStr = null;
            try {
                retcodeStr = WeiXinUtils.checkNewMessage(skey, wxsid, wxuin, syncKey);
            } catch (IOException e) {
                System.out.println("监测新消息异常");
            }

            if ("0".equals(retcodeStr)) {

                /**
                 * 获取最新消息
                 */
                WXGlobalResult wxGlobalResult = WeiXinUtils.getNewMessage(skey, wxsid, wxuin, syncKeyMap, nickRandomUserNameMap, remarkRandomUserNameMap, userName);


                /**
                 * 监测群消息是否有小程序
                 */
                /*if(wxGlobalResult != null && StringUtils.isNotEmpty(wxGlobalResult.getFromUserName())){
                    String subFromUserName = wxGlobalResult.getFromUserName().substring(0, 2);
                    if ("@@".equals(subFromUserName) && StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage()) && !(userName.equals(wxGlobalResult.getFromUserName())) && ("49".equals(wxGlobalResult.getMsgType()) && ("33".equals(wxGlobalResult.getAppMsgType())))) {
                        if(StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage())){

                            String[] splitMessage = wxGlobalResult.getSendTulMessage().split(":<br/>");
                            //获取发送消息的人
                            String fromUserName = splitMessage[0];
                            //获取群成员信息
                            FlockGlobeResult flockMessage = WeiXinUtils.getFlockMessage(skey, wxsid, wxuin, pass_ticket, deviceID, wxGlobalResult, splitMessage);
                            //获取redis中存储的违规次数
                            Jedis jedis1 = JedisUtils.getJedis();
                            String countStr = jedis1.get(fromUserName);
                            if(StringUtils.isEmpty(countStr)){
                                countStr = "1";
                            }
                            Integer count = Integer.valueOf(countStr);
                            jedis1.close();
                            if(userName.equals(flockMessage.getUserNameList().get(0)) && count < 3){
                                String nickName = flockMessage.getNickName();
                                //发送提示信息
                                //WeiXinUtils.sendWXMessage(skey,wxsid,wxuin,userName,wxGlobalResult.getFromUserName(),"@"+nickName+"发小程序" + count + "次，超过三次将移出群聊");
                                System.out.println("@"+nickName+"发小程序" + count + "次，超过三次将移出群聊");
                                //把用户信息和次数存入redis
                                Jedis jedis = JedisUtils.getJedis();
                                jedis.set(fromUserName,String.valueOf(count + 1));
                                jedis.close();
                            }
                            if(userName.equals(flockMessage.getUserNameList().get(0)) && count > 3){
                                String nickName = flockMessage.getNickName();
                                //发送提示信息
                                //WeiXinUtils.sendWXMessage(skey,wxsid,wxuin,userName,wxGlobalResult.getFromUserName(),"@"+nickName+"发小程序" + count + "次，现将你移出群聊");
                                System.out.println("@"+nickName+"发小程序" + count + "次，现将你移出群聊");
                                //清空redis
                                Jedis jedis = JedisUtils.getJedis();
                                jedis.del(fromUserName);
                                jedis.close();

                                // 执行移出群聊操作
                                String kickPersonUrl = "https://wx.qq.com/cgi-bin/mmwebwx-bin/webwxupdatechatroom?fun=delmember&pass_ticket=" + pass_ticket;
                                HashMap<String, Object> delPerSonMap = new HashMap<>();
                                HashMap<String, String> baseRequestMap = new HashMap<>();

                                deviceID = "e" + Random.getRandomNum(15);
                                baseRequestMap.put("DeviceID",deviceID);
                                baseRequestMap.put("Sid",wxsid);
                                baseRequestMap.put("Skey",skey);
                                baseRequestMap.put("Uin",wxuin);

                                delPerSonMap.put("BaseRequest", baseRequestMap);
                                delPerSonMap.put("ChatRoomName", wxGlobalResult.getFromUserName());
                                delPerSonMap.put("DelMemberList", fromUserName);

                                String delPerSonJson = JSONObject.toJSONString(delPerSonMap);
                                String delResponse = HttpClientUtils.httpPostJsonRestRequest(kickPersonUrl, delPerSonJson);
                                System.out.println("踢人：==>" + delResponse);
                                //解析
                            }
                        }
                    }
                }*/

                /**
                 * 自动回复开关
                 */
                autoResponse(skey, wxsid, wxuin, userName, wxGlobalResult);

                /**
                 * 天气查询功能
                 */
                if(userName.equals(wxGlobalResult.getFromUserName()) && wxGlobalResult.getSendTulMessage() != null
                        && ((wxGlobalResult.getSendTulMessage().contains("天气") || wxGlobalResult.getSendTulMessage().contains("温度") || wxGlobalResult.getSendTulMessage().contains("气温")) ||
                        ((tuLingGlobalResultWeater != null) && tuLingGlobalResultWeater.getTulreplyMessage().contains("请问你想查询哪个城市")))){
                    try {
                        tuLingGlobalResultWeater = TuLingUtils.sendMessageToTul(i, apiList, wxGlobalResult.getSendTulMessage());
                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, tuLingGlobalResultWeater.getTulreplyMessage());
                    } catch (IOException e) {
                        System.out.println("查询天气发送图灵机器人异常");
                    }
                }


                /**
                 * 开关消息防撤回功能
                 */
                if(userName.equals(wxGlobalResult.getFromUserName()) && "4".equals(wxGlobalResult.getSendTulMessage())){
                    if(reCallSwitch == false){
                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "当前已经是关闭防撤回状态！");
                    }else {
                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "防撤回功能已关闭！");
                        reCallSwitch = false;
                    }
                }else if(userName.equals(wxGlobalResult.getFromUserName()) && "3".equals(wxGlobalResult.getSendTulMessage())){
                    if(reCallSwitch == true){
                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "当前已经是开启防撤回状态！");
                    }else {
                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "防撤回功能已开启！");
                        reCallSwitch = true;
                    }
                }

                /**
                 * 消息防撤回
                 */
                WeiXinUtils.stopReCall(skey, wxsid, wxuin, userName, remarkRandomUserNameMap, nickRandomUserNameMap, wxGlobalResult, reCallSwitch, pass_ticket);


                if (StringUtils.isNotEmpty(wxGlobalResult.getFromUserName())) {
                    String subFromUserName = wxGlobalResult.getFromUserName().substring(0, 2);

                    if ("@@".equals(subFromUserName) && StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage()) && !(userName.equals(wxGlobalResult.getFromUserName())) && ("1".equals(wxGlobalResult.getMsgType()) || "47".equals(wxGlobalResult.getMsgType()))) {

                        /**
                         * 获取群消息
                         */
                        //接收到的消息为
                        String sendTulMessage = wxGlobalResult.getSendTulMessage();
                        if(StringUtils.isNotEmpty(sendTulMessage)){
                            String[] splitMessage = sendTulMessage.split(":<br/>");
                            //获取群成员信息
                            FlockGlobeResult flockMessage = WeiXinUtils.getFlockMessage(skey, wxsid, wxuin, pass_ticket, deviceID, wxGlobalResult, splitMessage);
                            String flockName = flockMessage.getFlockName();
                            String nickName = flockMessage.getNickName();
                            String displayName = flockMessage.getDisplayName();
                            if(splitMessage.length > 1){
                                System.out.println("[" + flockName + "] ["+ (StringUtils.isNotEmpty(displayName)? displayName : nickName) + "] 发来微信消息：==> " + splitMessage[1]);
                            }
                        }
                    }
                }


                /**
                 * 发送消息
                 */
                TuLing:
                while (sumSwitch) {

                    if (StringUtils.isNotEmpty(wxGlobalResult.getFromUserName())) {
                        String subFromUserName = wxGlobalResult.getFromUserName().substring(0, 2);

                        if (!("@@".equals(subFromUserName)) && StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage()) && !(userName.equals(wxGlobalResult.getFromUserName())) && ("1".equals(wxGlobalResult.getMsgType()) || "47".equals(wxGlobalResult.getMsgType()))) {

                            //设置不对谁自动回复
                            for (String noReplyUserName : noReply) {
                                if (wxGlobalResult.getFromUserName().equals(remarkRandomUserNameMap.get(noReplyUserName)) || wxGlobalResult.getFromUserName().equals(nickRandomUserNameMap.get(noReplyUserName))) {
                                    break TuLing;
                                }
                            }

                            /**
                             * 结束唤醒功能
                             */
                            //根据获取到的fromUserName 得到备注名称
                            String fromUserName = wxGlobalResult.getFromUserName();
                            if(StringUtils.isNotEmpty(remarkUserName)){
                                if(fromUserName.equals(remarkRandomUserNameMap.get(remarkUserName))){
                                    //删掉redis中的数据
                                    Jedis jedis = JedisUtils.getJedis();
                                    //组装要删除的key
                                    String rouseKey = "huanxing" + remarkRandomUserNameMap.get(remarkUserName);
                                    jedis.del(rouseKey);
                                    jedis.close();
                                }
                            }

                            /**
                             * 配合完成三分钟问候功能
                             */
                            //把获取到消息的时间存入redis
                            Jedis jedis = JedisUtils.getJedis();
                            //设置key
                            String getMessageKey = wxGlobalResult.getFromUserName();
                            //把获取到消息的时间戳存入redis  设置过期时间5分钟
                            String getMessageTime = String.valueOf(new Date().getTime());
                            jedis.setex(getMessageKey,300, getMessageTime);
                            JedisUtils.close(jedis);

                            //准备发送的消息
                            String tulreplyMessage = null;
                            //如果所有的apikey都用完了 返回自定义消息 并且结束本次循环
                            if (i >= apiList.size()) {
                                tulreplyMessage = "人家还小嘛，听不懂你在说什么呢！";
                                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tulreplyMessage);
                                break TuLing;
                            }

                            if (wxGlobalResult.getSendTulMessage().contains("你是谁") || wxGlobalResult.getSendTulMessage().contains("你叫什么")) {
                                tulreplyMessage = "我是程序员胖胖哦！(可爱的女朋友给起的名字[害羞])";
                                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tulreplyMessage);
                                break TuLing;
                            }

                            //避免无用消息请求，在前面拦截，返回自定义消息 并且结束本次循环
                            if (wxGlobalResult.getSendTulMessage().length() >= 149) {
                                tulreplyMessage = "你一口气说的也太长了吧，我都看不过来了[抠鼻]";
                                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tulreplyMessage);
                                break TuLing;
                            }

                            /**
                             * 如果是自定义回复  回复用户的自定义信息即可
                             */
                            if(customSwitch){
                                //从redis把存入的自定义消息拿出来
                                String customMessageKey = "customMessage";
                                Jedis jedis1 = JedisUtils.getJedis();
                                String customMessage = jedis1.get(customMessageKey);
                                jedis1.close();
                                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), customMessage);
                                break TuLing;
                            }

                            /**
                             * 复读机回复
                             */
                            if(repeaterSwitch){
                                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), wxGlobalResult.getSendTulMessage());
                                break TuLing;
                            }

                            /**
                             * 把接收到微信消息发送给图灵机器人
                             */
                            TuLingGlobalResult tuLingGlobalResult = null;
                            try {
                                tuLingGlobalResult = TuLingUtils.sendMessageToTul(i, apiList, wxGlobalResult.getSendTulMessage());
                            } catch (IOException e) {
                                System.out.println("发送消息给图灵机器人异常");
                            }

                            //如果返回码为成功一类  100开头的都为成功
                            if (tuLingGlobalResult.getCode().contains("100")) {

                                //如果图灵机器人返回的消息带有 图灵机器人关键字  返回自定义消息
                                if (tuLingGlobalResult.getTulreplyMessage().contains("图灵机器人")) {
                                    tulreplyMessage = "我是程序员胖胖哦！(可爱的女朋友给起的名字[害羞])";
                                    WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tulreplyMessage);
                                } else {
                                    /**
                                     * 直接将图灵机器人返回的消息发送给微信
                                     */
                                    WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tuLingGlobalResult.getTulreplyMessage());
                                }
                                //终止下一次循环
                                break TuLing;
                            } else {
                                //如果图灵返回的消息为请求次数超限制! 证明次数已用完  换一个apikey 再次调用 但是不发消息
                                if ("请求次数超限制!".equals(tuLingGlobalResult.getTulreplyMessage())) {
                                    i ++;
                                    System.out.println("正在使用第" + (i + 1) + "个apiKey【" + apiList.get(i) + "】");
                                    if (i >= apiList.size()) {
                                        tulreplyMessage = "[坏笑][坏笑][坏笑]";
                                        WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tulreplyMessage);
                                        break TuLing;
                                    }
                                    //如果调用错误了 给微信发送自定义消息 并且终止循环
                                } else {
                                    tulreplyMessage = "我正在努力成长中哦！";
                                    WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, wxGlobalResult.getFromUserName(), tulreplyMessage);
                                    break TuLing;
                                }
                            }
                        }else {
                            break TuLing;
                        }
                    }else {
                        break TuLing;
                    }
                }
                if (StringUtils.isNotEmpty(wxGlobalResult.getGetMessage())){
                    Map getKeyMessageMap = JSONObject.parseObject(wxGlobalResult.getGetMessage(), Map.class);

                    //重新从接收到的消息里面获取消息检查的syncKey
                    syncKey = WeiXinUtils.getSyncKeyTwo(getKeyMessageMap);

                    //重新从接收到的消息里面获取接收最新消息需要的syncKeyMap
                    syncKeyMap = JSONObject.parseObject(JSONObject.toJSONString(getKeyMessageMap.get("SyncCheckKey")), Map.class);
                }
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




    /**
     * 自动回复开关控制
     * @param skey
     * @param wxsid
     * @param wxuin
     * @param userName
     * @param wxGlobalResult
     */
    private static void autoResponse(String skey, String wxsid, String wxuin, String userName, WXGlobalResult wxGlobalResult) {

        if(customSmallSwitch){
            if(userName.equals(wxGlobalResult.getFromUserName()) && StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage())){
                //拿到用户设置的自定义消息
                String customMessage = wxGlobalResult.getSendTulMessage();  //用户自定义回复消息
                //将自定义的消息存入redis
                Jedis jedis = JedisUtils.getJedis();
                String customMessageKey = "customMessage";
                jedis.set(customMessageKey,customMessage);
                jedis.close();
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "自定义回复已开启！");
                customSwitch = true;
                sumSwitch = true;
                replySwitch = false;
                customSmallSwitch = false;
            }
        }

        if(replySwitch && StringUtils.isNotEmpty(wxGlobalResult.getSendTulMessage())){
            if(userName.equals(wxGlobalResult.getFromUserName()) && "A".equals(wxGlobalResult.getSendTulMessage().toUpperCase())){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "智能回复已开启！");
                sumSwitch = true;
                replySwitch = false;
            }
            if(userName.equals(wxGlobalResult.getFromUserName()) && "B".equals(wxGlobalResult.getSendTulMessage().toUpperCase())){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "请输入回复消息内容:");
                replySwitch = false;
                customSmallSwitch = true;
            }
            if(userName.equals(wxGlobalResult.getFromUserName()) && "C".equals(wxGlobalResult.getSendTulMessage().toUpperCase())){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "复读机回复已开启！");
                sumSwitch = true;
                repeaterSwitch = true;
                replySwitch = false;
            }
        }

        if(userName.equals(wxGlobalResult.getFromUserName()) && "2".equals(wxGlobalResult.getSendTulMessage())){
            if(!sumSwitch){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "当前已经是关闭自动回复状态！");
            }else {
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "自动回复已关闭！");
                sumSwitch = false;
                customSwitch = false;
                repeaterSwitch = false;
                //删掉redis中存储自定义回复信息
                Jedis jedis = JedisUtils.getJedis();
                String customMessageKey = "customMessage";
                jedis.del(customMessageKey);
                jedis.close();
            }
        }else if(userName.equals(wxGlobalResult.getFromUserName()) && "1".equals(wxGlobalResult.getSendTulMessage())){
            if(sumSwitch && !customSwitch && !repeaterSwitch){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "当前是智能回复,如您想切换回复类型，请先输入2关闭自动回复！");
            }else if(sumSwitch && customSwitch && !repeaterSwitch){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "当前是自定义回复,如您想切换回复类型，请先输入2关闭自动回复！");
            }else if(sumSwitch && !customSwitch && repeaterSwitch){
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "当前是复读机回复,如您想切换回复类型，请先输入2关闭自动回复！");
            }else {
                WeiXinUtils.sendWXMessage(skey, wxsid, wxuin, userName, userName, "请选择自动回复类型\n输入 A 智能回复\n输入 B 自定义回复\n输入 C 复读机回复");
                replySwitch = true;
            }
        }
    }
}