package com.potato.timetable.bean;


import androidx.annotation.NonNull;

public class Course implements Cloneable{
    private String name;//课程名
    private String teacher;//教授名字
    private int classLength =0;//课程时长
    private int classStart =-1;//课程开始节数
    private String weekOptions ="周";//单周，双周，周
    private String classRoom;//上课地点
    private String weekOfTerm;//开始上课的周
    private int dayOfWeek =0;//在周几上课 值1-7
    public int getClassStart() {
        return classStart;
    }

    public void setClassStart(int classStart) {
        this.classStart = classStart;
    }

    public String getWeekOfTerm() {
        return weekOfTerm;
    }

    public void setWeekOfTerm(String weekOfTerm) {
        this.weekOfTerm = weekOfTerm;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getClassRoom() {
        return classRoom;
    }

    public void setClassRoom(String classRoom) {
        this.classRoom = classRoom;
    }

    public int getClassLength() {
        return classLength;
    }

    public void setClassLength(int classLength) {
        if(classLength <=0)
            classLength =1;
        else if(classLength >12)
            classLength =12;
        this.classLength = classLength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }

    public String getWeekOptions() {
        return weekOptions;
    }

    public void setWeekOptions(String weekOptions) {
        this.weekOptions = weekOptions;
    }
    @NonNull
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
