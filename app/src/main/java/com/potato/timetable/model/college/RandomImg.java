package com.potato.timetable.model.college;

public class RandomImg {
    private String base64;
    private int randomCodeLength;

    public RandomImg() {
    }

    public RandomImg(String base64, int randomCodeLength) {
        this.base64 = base64;
        this.randomCodeLength = randomCodeLength;
    }

    public String getBase64() {
        return base64;
    }

    public void setBase64(String base64) {
        this.base64 = base64;
    }

    public int getRandomCodeLength() {
        return randomCodeLength;
    }

    public RandomImg setRandomCodeLength(int randomCodeLength) {
        this.randomCodeLength = randomCodeLength;
        return this;
    }
}
