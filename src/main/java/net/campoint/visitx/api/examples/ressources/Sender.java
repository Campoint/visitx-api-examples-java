package net.campoint.visitx.api.examples.ressources;

import com.google.gson.annotations.SerializedName;

public class Sender {
    @SerializedName("UserID")
    public String UserId;

    @SuppressWarnings("SpellCheckingInspection")
    @SerializedName("Sendername")
    public String SenderName;
}
