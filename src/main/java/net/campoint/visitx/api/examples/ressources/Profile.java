package net.campoint.visitx.api.examples.ressources;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Profile {
    @SerializedName("ProfileProperties")
    public ArrayList<ProfileProperty> profileProperties;
}
