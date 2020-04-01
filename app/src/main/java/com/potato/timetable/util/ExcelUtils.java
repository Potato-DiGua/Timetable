package com.potato.timetable.util;


import com.potato.timetable.bean.Course;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jxl.Cell;
import jxl.Range;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class ExcelUtils {

    /**
     * @param path     String
     * @param startRow int 课程表（不算表头）开始行数（从1开始）
     * @param startCol int 课程表（不算表头）开始列数（从1开始）
     * @return List<Course> 返回课程列表
     * <p>
     * 只读取6行7列
     */
    public static List<Course> handleExcel(String path, int startRow, int startCol) {
        InputStream inputStream = null;
        List<Course> courseList = new ArrayList<>();
        //Log.d("filePath",path);
        Workbook excel = null;
        try {
            File file = new File(path);

            if (file.exists()) {
                inputStream = new FileInputStream(file);
            } else {
                //Log.d("file","do not exist");
                return courseList;
            }
            excel = Workbook.getWorkbook(inputStream);
            Sheet rs = excel.getSheet(0);
            int rowCount = 6;
            int weight = 2;

            Range[] ranges = rs.getMergedCells();

//            for(int i=0;i<ranges.length;i++)
//            {
//                System.out.println(ranges[i].getBottomRight().getRow()-ranges[i].getTopLeft().getRow()+1);
//            }

            startCol -= 2;
            startRow -= 2;

            if(startCol+7>rs.getColumns()||startRow+rowCount>rs.getRows()){
                return courseList;
            }

            for (int i = 1; i <= 7; i++) {
                for (int j = 1; j <= rowCount; j++) {
                    Cell cell = rs.getCell(startCol + i, startRow + j);
                    String str = handleCell(cell.getContents());

                    int row_length = 1;
                    for (Range range:ranges) {
                        if (range.getTopLeft() == cell) {
                            row_length = range.getBottomRight().getRow() - cell.getRow() + 1;
                            break;
                        }
                    }

                    if (!str.isEmpty()) {

                        String[] strings = str.split("\n\n");
                        int length = strings.length;

                        for (String s:strings) {
                            Course course = getCourseFromString(s);
                            if (course == null)
                                continue;
                            course.setDayOfWeek(i);
                            course.setClassLength(weight * row_length);
                            course.setClassStart(j * 2 - 1);
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
                if (excel != null)
                    excel.close();
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static List<Course> handleExcel(String path) {
        return handleExcel(path, 2, 2);
    }

    /**
     * 从表格中的内容提取课程信息
     *
     * @param str String
     * @return Course
     */
    private static Course getCourseFromString(String str) {

        String[] contents = str.split("\n");
        if (contents.length < 4)
            return null;
        Course course = new Course();
        course.setName(contents[0]);
        course.setTeacher(contents[1]);

        course.setWeekOfTerm(getWeekOfTermFromString(contents[2]));

        course.setClassRoom(contents[3]);

        return course;

    }

    private static int getWeekOfTermFromString(String str) {
        //Log.d("excel",str);
        String[] s1=str.split("\\[");
        String[] s11=s1[0].split(",");

        int weekOfTerm=0;
        for(String s:s11)
        {
            if(s==null||s.isEmpty())
                continue;
            if(s.contains("-"))
            {
                int space=2;
                if(s1[1].equals("周]")){
                    space=1;
                }
                String[] s2=s.split("-");
                if(s2.length!=2)
                {
                    System.out.println("error");
                    return 0;
                }
                int p=Integer.parseInt(s2[0]);
                int q=Integer.parseInt(s2[1]);

                for(int n=p;n<=q;n+=space){
                    weekOfTerm+=1<<(Config.getMaxWeekNum()-n);
                }
            }else {
                weekOfTerm+=1<<(Config.getMaxWeekNum()-Integer.parseInt(s));
            }
        }
        return weekOfTerm;
    }

    /**
     * 去除字符串的首尾回车和空格
     *
     * @param str
     * @return
     */
    private static String handleCell(String str) {
        str = str.replaceAll("^\n|\n$", "");//去除首尾换行符
        str = str.trim();//去除首尾空格
        return str;
    }

}
