package com.campus.lostfound.entity;

/**
 * 主要地点枚举
 * 提供中文描述与权重，用于权重匹配与展示
 */
public enum Location {

    LIBRARY("图书馆", 10),
    TEACHING_BUILDING_A("教学楼A", 8),
    TEACHING_BUILDING_B("教学楼B", 8),
    DORMITORY_NORTH("北区宿舍", 6),
    DORMITORY_SOUTH("南区宿舍", 6),
    CANTEEN_FIRST("第一食堂", 7),
    CANTEEN_SECOND("第二食堂", 7),
    SPORTS_FIELD("运动场", 6),
    GYM("体育馆", 5),
    LAB_BUILDING_A("实验楼A", 7),
    ADMIN_BUILDING("行政楼", 5),
    STUDENT_CENTER("学生活动中心", 5),
    CAMPUS_GATE("校门/门口", 4),
    EXPRESS_CABINET("快递柜", 5),
    OTHER("其他", 1);

    private final String description;
    private final int weight;

    Location(String description, int weight) {
        this.description = description;
        this.weight = weight;
    }

    public String getDescription() {
        return description;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return description;
    }
}

