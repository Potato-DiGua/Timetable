package com.potato.timetable.util;


import com.potato.timetable.bean.Course;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jxl.Cell;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelUtils {

    public static List<Course> handleExcel(String path) {
        InputStream inputStream = null;
        List<Course> courseList = new ArrayList<>();
        //Log.d("filePath",path);
        Workbook excel=null;
        try {
            File file = new File(path);

            if (file.exists()) {
                inputStream = new FileInputStream(file);
            }else
            {
                //Log.d("file","do not exist");
                return courseList;
            }
            excel = Workbook.getWorkbook(inputStream);
            Sheet rs = excel.getSheet(0);
            int rowCount = rs.getRows() - 1;
            int weight;
            if(rowCount==6||rowCount==12)
            {
                weight=12 / rowCount;
            }
            else
                return null;

            Range[] ranges = rs.getMergedCells();

//            for(int i=0;i<ranges.length;i++)
//            {
//                System.out.println(ranges[i].getBottomRight().getRow()-ranges[i].getTopLeft().getRow()+1);
//            }


            for (int i = 1; i <=7; i++) {
                for (int j = 1; j <=rowCount; j++) {
                    Cell cell = rs.getCell(i, j);
                    String str = handleCell(cell.getContents());

                    int row_length = 1;
                    for (int n = 0; n < ranges.length; n++) {
                        if (ranges[n].getTopLeft() == cell) {
                            row_length = ranges[n].getBottomRight().getRow() - cell.getRow() + 1;
                            break;
                        }
                    }

                    if (!str.isEmpty()) {

                        String[] strings=str.split("\n\n");


                        int length=strings.length;

                        for(int index=0;index<length;index++)
                        {
                            Course course = getCourseFromString(strings[index]);
                            if(course==null)
                                continue;
                            course.setDayOfWeek(i);
                            course.setClassLength(weight * row_length);
                            course.setClassStart(j*2-1);
                            courseList.add(course);
                        }


                    }
                }

            }
            return courseList;
        } catch (BiffException | IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if(excel!=null)
                    excel.close();
                if(inputStream!=null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
    private static Course getCourseFromString(String str) {

        String[] contents = str.split("\n");
        if(contents.length<4)
            return null;
        Course course = new Course();
        course.setName(contents[0]);
        course.setTeacher(contents[1]);
        int week_start;
        int week_end;

        String[] aStr = contents[2].split("\\[");
        course.setWeekOfTerm(aStr[0]);

        //System.out.println(strs[1]);
        course.setWeekOptions(aStr[1].substring(0, aStr[1].length() - 1));

        course.setClassRoom(contents[3]);

        return course;

    }

    private static String handleCell(String str) {
        str = str.replaceAll("^\n|\n$", "");//去除首尾换行符
        str = str.trim();//去除首尾空格
        return str;
    }

/*
    public static void main(String[] args) {
        System.out.println("test");
        List<Course> list = new ExcelUtils().handleExcel("C:\\Users\\86543\\Desktop\\specialclass.xls");
        for (Course course:list) {

            System.out.println(course.getName() + "\n" + course.getWeekOptions() + course.getClassStart());
            System.out.println("------------------------");
        }

    }*/

}
