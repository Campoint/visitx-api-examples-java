package net.campoint.visitx.api.examples.ressources;

import com.google.gson.annotations.SerializedName;

public class ProfileProperty {
    @SerializedName("LanguageCode")
    public String languageCode;

    @SerializedName("Key")
    public String key;

    @SerializedName("Value")
    public ProfilePropertyValue value;
}
