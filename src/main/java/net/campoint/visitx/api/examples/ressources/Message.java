package net.campoint.visitx.api.examples.ressources;

public class Message {
    public String key;
    public String type;
    public int version;
    public int seq;


    public MessageData data;
    public boolean deleted;

    public boolean isOnlineStateUpdateMessage() {
        return this.type.equals("vx.onlineState.videoChat");

    }


}
