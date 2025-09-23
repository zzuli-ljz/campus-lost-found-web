package com.campus.lostfound.entity;

/**
 * 详细地点枚举类
 * 定义每个主要地点下的具体位置及其权重值
 */
public enum DetailedLocation {
    
    // 教学楼A详细位置
    TEACHING_BUILDING_A_FLOOR_1("教学楼A-1楼", 2),
    TEACHING_BUILDING_A_FLOOR_2("教学楼A-2楼", 2),
    TEACHING_BUILDING_A_FLOOR_3("教学楼A-3楼", 2),
    TEACHING_BUILDING_A_FLOOR_4("教学楼A-4楼", 2),
    TEACHING_BUILDING_A_FLOOR_5("教学楼A-5楼", 2),
    TEACHING_BUILDING_A_CLASSROOM_101("教学楼A-101教室", 3),
    TEACHING_BUILDING_A_CLASSROOM_102("教学楼A-102教室", 3),
    TEACHING_BUILDING_A_CLASSROOM_201("教学楼A-201教室", 3),
    TEACHING_BUILDING_A_CLASSROOM_202("教学楼A-202教室", 3),
    TEACHING_BUILDING_A_CORRIDOR("教学楼A-走廊", 1),
    TEACHING_BUILDING_A_STAIRS("教学楼A-楼梯", 1),
    TEACHING_BUILDING_A_TOILET("教学楼A-卫生间", 1),
    
    // 教学楼B详细位置
    TEACHING_BUILDING_B_FLOOR_1("教学楼B-1楼", 2),
    TEACHING_BUILDING_B_FLOOR_2("教学楼B-2楼", 2),
    TEACHING_BUILDING_B_FLOOR_3("教学楼B-3楼", 2),
    TEACHING_BUILDING_B_FLOOR_4("教学楼B-4楼", 2),
    TEACHING_BUILDING_B_CLASSROOM_101("教学楼B-101教室", 3),
    TEACHING_BUILDING_B_CLASSROOM_102("教学楼B-102教室", 3),
    TEACHING_BUILDING_B_CLASSROOM_201("教学楼B-201教室", 3),
    TEACHING_BUILDING_B_CLASSROOM_202("教学楼B-202教室", 3),
    TEACHING_BUILDING_B_CORRIDOR("教学楼B-走廊", 1),
    TEACHING_BUILDING_B_STAIRS("教学楼B-楼梯", 1),
    TEACHING_BUILDING_B_TOILET("教学楼B-卫生间", 1),
    
    // 图书馆详细位置
    LIBRARY_MAIN_FLOOR_1("主图书馆-1楼", 3),
    LIBRARY_MAIN_FLOOR_2("主图书馆-2楼", 3),
    LIBRARY_MAIN_FLOOR_3("主图书馆-3楼", 3),
    LIBRARY_MAIN_FLOOR_4("主图书馆-4楼", 3),
    LIBRARY_MAIN_READING_ROOM("主图书馆-阅览室", 4),
    LIBRARY_MAIN_STACK_ROOM("主图书馆-书库", 4),
    LIBRARY_MAIN_ENTRANCE("主图书馆-入口", 2),
    LIBRARY_MAIN_CORRIDOR("主图书馆-走廊", 1),
    LIBRARY_MAIN_TOILET("主图书馆-卫生间", 1),
    
    // 宿舍详细位置
    DORMITORY_NORTH_BUILDING_1("北区宿舍-1号楼", 2),
    DORMITORY_NORTH_BUILDING_2("北区宿舍-2号楼", 2),
    DORMITORY_NORTH_BUILDING_3("北区宿舍-3号楼", 2),
    DORMITORY_NORTH_FLOOR_1("北区宿舍-1楼", 1),
    DORMITORY_NORTH_FLOOR_2("北区宿舍-2楼", 1),
    DORMITORY_NORTH_FLOOR_3("北区宿舍-3楼", 1),
    DORMITORY_NORTH_FLOOR_4("北区宿舍-4楼", 1),
    DORMITORY_NORTH_FLOOR_5("北区宿舍-5楼", 1),
    DORMITORY_NORTH_FLOOR_6("北区宿舍-6楼", 1),
    DORMITORY_NORTH_CORRIDOR("北区宿舍-走廊", 1),
    DORMITORY_NORTH_TOILET("北区宿舍-卫生间", 1),
    
    DORMITORY_SOUTH_BUILDING_1("南区宿舍-1号楼", 2),
    DORMITORY_SOUTH_BUILDING_2("南区宿舍-2号楼", 2),
    DORMITORY_SOUTH_BUILDING_3("南区宿舍-3号楼", 2),
    DORMITORY_SOUTH_FLOOR_1("南区宿舍-1楼", 1),
    DORMITORY_SOUTH_FLOOR_2("南区宿舍-2楼", 1),
    DORMITORY_SOUTH_FLOOR_3("南区宿舍-3楼", 1),
    DORMITORY_SOUTH_FLOOR_4("南区宿舍-4楼", 1),
    DORMITORY_SOUTH_FLOOR_5("南区宿舍-5楼", 1),
    DORMITORY_SOUTH_FLOOR_6("南区宿舍-6楼", 1),
    DORMITORY_SOUTH_CORRIDOR("南区宿舍-走廊", 1),
    DORMITORY_SOUTH_TOILET("南区宿舍-卫生间", 1),
    
    // 食堂详细位置
    CANTEEN_FIRST_FLOOR_1("第一食堂-1楼", 2),
    CANTEEN_FIRST_FLOOR_2("第一食堂-2楼", 2),
    CANTEEN_FIRST_WINDOW_1("第一食堂-1号窗口", 3),
    CANTEEN_FIRST_WINDOW_2("第一食堂-2号窗口", 3),
    CANTEEN_FIRST_WINDOW_3("第一食堂-3号窗口", 3),
    CANTEEN_FIRST_DINING_AREA("第一食堂-用餐区", 2),
    CANTEEN_FIRST_ENTRANCE("第一食堂-入口", 1),
    
    CANTEEN_SECOND_FLOOR_1("第二食堂-1楼", 2),
    CANTEEN_SECOND_FLOOR_2("第二食堂-2楼", 2),
    CANTEEN_SECOND_WINDOW_1("第二食堂-1号窗口", 3),
    CANTEEN_SECOND_WINDOW_2("第二食堂-2号窗口", 3),
    CANTEEN_SECOND_WINDOW_3("第二食堂-3号窗口", 3),
    CANTEEN_SECOND_DINING_AREA("第二食堂-用餐区", 2),
    CANTEEN_SECOND_ENTRANCE("第二食堂-入口", 1),
    
    // 运动场详细位置
    SPORTS_FIELD_TRACK("运动场-跑道", 2),
    SPORTS_FIELD_FOOTBALL("运动场-足球场", 2),
    SPORTS_FIELD_BASKETBALL("运动场-篮球场", 2),
    SPORTS_FIELD_VOLLEYBALL("运动场-排球场", 2),
    SPORTS_FIELD_TENNIS("运动场-网球场", 2),
    SPORTS_FIELD_STAND("运动场-看台", 1),
    SPORTS_FIELD_ENTRANCE("运动场-入口", 1),
    
    // 体育馆详细位置
    GYM_FLOOR_1("体育馆-1楼", 2),
    GYM_FLOOR_2("体育馆-2楼", 2),
    GYM_BASKETBALL_COURT("体育馆-篮球场", 3),
    GYM_VOLLEYBALL_COURT("体育馆-排球场", 3),
    GYM_BADMINTON_COURT("体育馆-羽毛球场", 3),
    GYM_EQUIPMENT_ROOM("体育馆-器材室", 2),
    GYM_ENTRANCE("体育馆-入口", 1),
    
    // 实验楼详细位置
    LAB_BUILDING_A_FLOOR_1("实验楼A-1楼", 3),
    LAB_BUILDING_A_FLOOR_2("实验楼A-2楼", 3),
    LAB_BUILDING_A_FLOOR_3("实验楼A-3楼", 3),
    LAB_BUILDING_A_LAB_101("实验楼A-101实验室", 4),
    LAB_BUILDING_A_LAB_102("实验楼A-102实验室", 4),
    LAB_BUILDING_A_LAB_201("实验楼A-201实验室", 4),
    LAB_BUILDING_A_LAB_202("实验楼A-202实验室", 4),
    LAB_BUILDING_A_CORRIDOR("实验楼A-走廊", 1),
    LAB_BUILDING_A_STAIRS("实验楼A-楼梯", 1),
    LAB_BUILDING_A_TOILET("实验楼A-卫生间", 1),
    
    // 行政楼详细位置
    ADMIN_BUILDING_FLOOR_1("行政楼-1楼", 2),
    ADMIN_BUILDING_FLOOR_2("行政楼-2楼", 2),
    ADMIN_BUILDING_FLOOR_3("行政楼-3楼", 2),
    ADMIN_BUILDING_OFFICE_101("行政楼-101办公室", 3),
    ADMIN_BUILDING_OFFICE_102("行政楼-102办公室", 3),
    ADMIN_BUILDING_OFFICE_201("行政楼-201办公室", 3),
    ADMIN_BUILDING_OFFICE_202("行政楼-202办公室", 3),
    ADMIN_BUILDING_CORRIDOR("行政楼-走廊", 1),
    ADMIN_BUILDING_STAIRS("行政楼-楼梯", 1),
    ADMIN_BUILDING_TOILET("行政楼-卫生间", 1),
    
    // 学生活动中心详细位置
    STUDENT_CENTER_FLOOR_1("学生活动中心-1楼", 2),
    STUDENT_CENTER_FLOOR_2("学生活动中心-2楼", 2),
    STUDENT_CENTER_MEETING_ROOM("学生活动中心-会议室", 3),
    STUDENT_CENTER_ACTIVITY_ROOM("学生活动中心-活动室", 3),
    STUDENT_CENTER_OFFICE("学生活动中心-办公室", 2),
    STUDENT_CENTER_CORRIDOR("学生活动中心-走廊", 1),
    STUDENT_CENTER_STAIRS("学生活动中心-楼梯", 1),
    STUDENT_CENTER_TOILET("学生活动中心-卫生间", 1),
    
    // 其他详细位置
    GATE_MAIN_ENTRANCE("正门-入口", 2),
    GATE_MAIN_EXIT("正门-出口", 2),
    GATE_SIDE_ENTRANCE("侧门-入口", 2),
    GATE_SIDE_EXIT("侧门-出口", 2),
    PARKING_LOT_AREA_A("停车场-A区", 2),
    PARKING_LOT_AREA_B("停车场-B区", 2),
    PARKING_LOT_AREA_C("停车场-C区", 2),
    GARDEN_PATH("花园-小径", 1),
    GARDEN_BENCH("花园-长椅", 1),
    GARDEN_LAWN("花园-草坪", 1),
    
    // 快递相关位置
    EXPRESS_CABINET_NEARBY("快递柜附近", 2),
    EXPRESS_CABINET_AREA_A("快递柜-A区", 2),
    EXPRESS_CABINET_AREA_B("快递柜-B区", 2),
    EXPRESS_CABINET_AREA_C("快递柜-C区", 2),
    
    OTHER_UNKNOWN("其他-未知", 1);
    
    private final String description;
    private final int weight;
    
    DetailedLocation(String description, int weight) {
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