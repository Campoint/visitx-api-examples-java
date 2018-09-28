package net.campoint.visitx.api.examples.ressources;

import com.google.gson.annotations.SerializedName;

public class ProfilePropertyValue {

    @SerializedName("__type")
    public String propertyType;

    @SerializedName("LanguageCode")
    public String languageCode;

    @SerializedName("Value")
    public String value;

    @SerializedName("TranslationKey")
    public String translationKey;

    public boolean isMultiValue() {
        return this.propertyType.startsWith("MultiValueProfileProperty");
    }
}
