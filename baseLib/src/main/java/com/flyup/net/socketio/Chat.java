package com.flyup.net.socketio;

public class Chat {

    private String from;
    private String icon;
    private String nickname;
    private String toUser;
    private String type;
    private String targetId;
    private String msg;
    private String regTime;
    //消息创建时间
    private long cTime;
    //发送者性别
    private int sex;
    private String msgId;
    //picUrl
    private String pic;
    //ext
    private String ext;

    public Chat() {
    }

    public Chat(String from,String toUser,String type,String targetId,String msg,String regTime,int sex) {
        super();
        this.from = from;
        this.toUser = toUser;
        this.type = type;
        this.targetId = targetId;
        this.msg = msg;
        this.regTime = regTime;
        this.sex = sex;
    }

    public String getToUser() {
        return toUser;
    }

    public void setToUser(String toUser) {
        this.toUser = toUser;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getRegTime() {
        return regTime;
    }

    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public long getcTime() {
        return cTime;
    }

    public void setcTime(long cTime) {
        this.cTime = cTime;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
