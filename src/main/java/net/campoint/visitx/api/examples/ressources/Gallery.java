package net.campoint.visitx.api.examples.ressources;

import com.google.gson.annotations.SerializedName;

public class Gallery {

    @SerializedName("Price")
    public Float price;

    @SerializedName("UserId")
    public String userId;

    @SerializedName("UmaId")
    public String umaId;

    public boolean hasValidGalleryPrice(){
        return this.price != null && this.price > 0;
    }
}
