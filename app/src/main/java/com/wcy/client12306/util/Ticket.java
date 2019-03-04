package com.wcy.client12306.util;

public class Ticket {
    String buyCode; //0
    String trainId; //3
    String startStation; //4 始发站 6过站(上车站)
    String arrivalStation; //5 终点站 7过站(下车站)

    String startTime; //8
    String arrivalTime; //9

    String throughTime; //10

    String specialSeat; //32
    String levelOneSeat; //31
    String levelTwoSeat; //30
    String seniorSoft; //21
    String levelOneSoft; //23
    String bulletSoft; //33
    String hardsLeeper; //28
    String softSeat; //?
    String hardSeat; //29
    String noSeat; //26
    String other; //?

    public String getBuyCode() {
        return buyCode;
    }

    public void setBuyCode(String buyCode) {
        this.buyCode = buyCode;
    }

    public String getTrainId() {
        return trainId;
    }

    public void setTrainId(String trainId) {
        this.trainId = trainId;
    }

    public String getStartStation() {
        return startStation;
    }

    public void setStartStation(String startStation) {
        this.startStation = startStation;
    }

    public String getArrivalStation() {
        return arrivalStation;
    }

    public void setArrivalStation(String arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getThroughTime() {
        return throughTime;
    }

    public void setThroughTime(String throughTime) {
        this.throughTime = throughTime;
    }

    public String getSpecialSeat() {
        return specialSeat;
    }

    public void setSpecialSeat(String specialSeat) {
        this.specialSeat = specialSeat;
    }

    public String getLevelOneSeat() {
        return levelOneSeat;
    }

    public void setLevelOneSeat(String levelOneSeat) {
        this.levelOneSeat = levelOneSeat;
    }

    public String getLevelTwoSeat() {
        return levelTwoSeat;
    }

    public void setLevelTwoSeat(String levelTwoSeat) {
        this.levelTwoSeat = levelTwoSeat;
    }

    public String getSeniorSoft() {
        return seniorSoft;
    }

    public void setSeniorSoft(String seniorSoft) {
        this.seniorSoft = seniorSoft;
    }

    public String getLevelOneSoft() {
        return levelOneSoft;
    }

    public void setLevelOneSoft(String levelOneSoft) {
        this.levelOneSoft = levelOneSoft;
    }

    public String getBulletSoft() {
        return bulletSoft;
    }

    public void setBulletSoft(String bulletSoft) {
        this.bulletSoft = bulletSoft;
    }

    public String getHardsLeeper() {
        return hardsLeeper;
    }

    public void setHardsLeeper(String hardsLeeper) {
        this.hardsLeeper = hardsLeeper;
    }

    public String getSoftSeat() {
        return softSeat;
    }

    public void setSoftSeat(String softSeat) {
        this.softSeat = softSeat;
    }

    public String getHardSeat() {
        return hardSeat;
    }

    public void setHardSeat(String hardSeat) {
        this.hardSeat = hardSeat;
    }

    public String getNoSeat() {
        return noSeat;
    }

    public void setNoSeat(String noSeat) {
        this.noSeat = noSeat;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }
}
