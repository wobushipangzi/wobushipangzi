package com.liujie.result;

public class WXGlobalResult {

    private String fromUserName;

    private String sendTulMessage;

    private String getMessage;

    private String msgType;

    private String msgId;

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getSendTulMessage() {
        return sendTulMessage;
    }

    public void setSendTulMessage(String sendTulMessage) {
        this.sendTulMessage = sendTulMessage;
    }

    public String getGetMessage() {
        return getMessage;
    }

    public void setGetMessage(String getMessage) {
        this.getMessage = getMessage;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
}
