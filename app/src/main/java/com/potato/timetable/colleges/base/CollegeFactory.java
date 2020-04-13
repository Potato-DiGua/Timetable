package com.potato.timetable.colleges.base;

import com.potato.timetable.colleges.CSUCollege;
import com.potato.timetable.colleges.ShmtuCollege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollegeFactory {
    private static List<String> collegeNameList = new ArrayList<>();
    static {
        collegeNameList.add(CSUCollege.NAME);
        collegeNameList.add(ShmtuCollege.NAME);
        sortCollegeNameList();//升序排序
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
        else if (collegeName.equals(CSUCollege.NAME)) {
            return new CSUCollege();
        } else if (collegeName.equals(ShmtuCollege.NAME)) {
            return new ShmtuCollege();
        }
        else {
            return null;
        }
    }
}
