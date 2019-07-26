package com.shinmashita.checkly.adapters;

public class Sheet {
    private String sheetName;
    private int score;
    private int items;
    private int imageResource;

    public Sheet(String sheetName, int score, int items, int imageResource) {
        this.sheetName = sheetName;
        this.score = score;
        this.items = items;
        this.imageResource = imageResource;
    }

    public String getSheetName() {
        return sheetName;
    }

    public int getScore() {
        return score;
    }

    public int getItems() {
        return items;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setItems(int items) {
        this.items = items;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }
}
