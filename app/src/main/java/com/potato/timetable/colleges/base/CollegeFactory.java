package com.potato.timetable.colleges.base;

import com.potato.timetable.colleges.CSUCollegeV2;
import com.potato.timetable.colleges.ShmtuCollege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollegeFactory {
    private static List<String> collegeNameList = new ArrayList<>();
    static {
        collegeNameList.add("中南大学");
        collegeNameList.add("上海海事大学");
        sortCollegeNameList();
    }

    public static List<String> getCollegeNameList() {
        return collegeNameList;
    }
    private static void sortCollegeNameList(){
        Collections.sort(collegeNameList);
    }

    public static College createCollege(String collegeName) {
        if (collegeName == null) {
            return null;
        }
        else if (collegeName.equals("中南大学")) {
            return new CSUCollegeV2();
        } else if (collegeName.equals("上海海事大学")) {
            return new ShmtuCollege();
        }
        else {
            return null;
        }
    }
}
