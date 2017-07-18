package com.example.sample.googlemapproject.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Model POJO class for crime
 */

public class Crime implements Parcelable {

    @SerializedName("beat")
    @Expose
    private String beat;
    @SerializedName("block")
    @Expose
    private String block;
    @SerializedName("rdNo")
    @Expose
    private String rdNo;
    @SerializedName("communityArea")
    @Expose
    private String communityArea;
    @SerializedName("dateOccurred")
    @Expose
    private String dateOccurred;
    @SerializedName("iucrDescription")
    @Expose
    private String iucrDescription;
    @SerializedName("cpdDistrict")
    @Expose
    private String cpdDistrict;
    @SerializedName("iucr")
    @Expose
    private String iucr;
    @SerializedName("lastUpdated")
    @Expose
    private String lastUpdated;
    @SerializedName("locationDesc")
    @Expose
    private String locationDesc;
    @SerializedName("primary")
    @Expose
    private String primary;
    @SerializedName("ward")
    @Expose
    private String ward;
    @SerializedName("xCoordinate")
    @Expose
    private Integer xCoordinate;
    @SerializedName("yCoordinate")
    @Expose
    private Integer yCoordinate;

    private String colorCode;

    private Crime(final Parcel in) {
        beat = in.readString();
        block = in.readString();
        rdNo = in.readString();
        communityArea = in.readString();
        dateOccurred = in.readString();
        iucrDescription = in.readString();
        cpdDistrict = in.readString();
        iucr = in.readString();
        lastUpdated = in.readString();
        locationDesc = in.readString();
        primary = in.readString();
        ward = in.readString();
        colorCode = in.readString();
    }

    public static final Creator<Crime> CREATOR = new Creator<Crime>() {
        @Override
        public Crime createFromParcel(final Parcel in) {
            return new Crime(in);
        }

        @Override
        public Crime[] newArray(final int size) {
            return new Crime[size];
        }
    };

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(final String code) {
        colorCode = code;
    }

    public String getBeat() {
        return beat;
    }

    public void setBeat(final String beat) {
        this.beat = beat;
    }

    public String getBlock() {
        return block;
    }

    public void setBlock(final String block) {
        this.block = block;
    }

    public String getRdNo() {
        return rdNo;
    }

    public void setRdNo(final String rdNo) {
        this.rdNo = rdNo;
    }

    public String getCommunityArea() {
        return communityArea;
    }

    public void setCommunityArea(final String communityArea) {
        this.communityArea = communityArea;
    }

    public String getDateOccurred() {
        return dateOccurred;
    }

    public void setDateOccurred(final String dateOccurred) {
        this.dateOccurred = dateOccurred;
    }

    public String getIucrDescription() {
        return iucrDescription;
    }

    public void setIucrDescription(final String iucrDescription) {
        this.iucrDescription = iucrDescription;
    }

    public String getCpdDistrict() {
        return cpdDistrict;
    }

    public void setCpdDistrict(final String cpdDistrict) {
        this.cpdDistrict = cpdDistrict;
    }

    public String getIucr() {
        return iucr;
    }

    public void setIucr(final String iucr) {
        this.iucr = iucr;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(final String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLocationDesc() {
        return locationDesc;
    }

    public void setLocationDesc(final String locationDesc) {
        this.locationDesc = locationDesc;
    }

    public String getPrimary() {
        return primary;
    }

    public void setPrimary(final String primary) {
        this.primary = primary;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(final String ward) {
        this.ward = ward;
    }

    public Integer getXCoordinate() {
        return xCoordinate;
    }

    public void setXCoordinate(final Integer xCoordinate) {
        this.xCoordinate = xCoordinate;
    }

    public Integer getYCoordinate() {
        return yCoordinate;
    }

    public void setYCoordinate(final Integer yCoordinate) {
        this.yCoordinate = yCoordinate;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     * @see #CONTENTS_FILE_DESCRIPTOR
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     * May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(beat);
        dest.writeString(block);
        dest.writeString(rdNo);
        dest.writeString(communityArea);
        dest.writeString(dateOccurred);
        dest.writeString(iucrDescription);
        dest.writeString(cpdDistrict);
        dest.writeString(iucr);
        dest.writeString(lastUpdated);
        dest.writeString(locationDesc);
        dest.writeString(primary);
        dest.writeString(ward);
        dest.writeString(colorCode);
    }
}
