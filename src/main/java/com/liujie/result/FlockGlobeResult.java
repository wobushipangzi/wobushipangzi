package com.liujie.result;

import java.util.List;

public class FlockGlobeResult {

    private String flockName; //群名称

    private String nickName;  //发来消息的群成员名称

    private String displayName;  //发来消息的群成员备注名

    private List userNameList;  //群成员集合

    public String getFlockName() {
        return flockName;
    }

    public void setFlockName(String flockName) {
        this.flockName = flockName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List getUserNameList() {
        return userNameList;
    }

    public void setUserNameList(List nickNameList) {
        this.userNameList = nickNameList;
    }
}
