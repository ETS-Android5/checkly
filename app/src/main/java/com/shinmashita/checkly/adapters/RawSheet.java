package com.shinmashita.checkly.adapters;

public class RawSheet {
    private String charAns;
    private String charKeys;
    private int imageSrc;

    public RawSheet(String charAns, String charKeys, int imageSrc) {
        this.charAns = charAns;
        this.charKeys = charKeys;
        this.imageSrc = imageSrc;
    }

    public String getCharAns() {
        return charAns;
    }

    public void setCharAns(String charAns) {
        this.charAns = charAns;
    }

    public String getCharKeys() {
        return charKeys;
    }

    public void setCharKeys(String charKeys) {
        this.charKeys = charKeys;
    }

    public int getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(int imageSrc) {
        this.imageSrc = imageSrc;
    }
}
