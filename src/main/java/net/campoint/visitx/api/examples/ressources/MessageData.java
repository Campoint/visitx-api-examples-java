package net.campoint.visitx.api.examples.ressources;

public class MessageData {
    public User user;

    public Boolean voyeur;
    public Boolean multi;
    public Boolean single;
    public Boolean messenger;

    public boolean isAvailableForChat() {
        return
                this.voyeur || this.multi || this.single || this.messenger;
    }
}
