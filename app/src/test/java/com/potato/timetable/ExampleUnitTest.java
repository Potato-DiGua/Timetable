package com.potato.timetable;

import com.potato.timetable.util.ExcelUtils;
import com.potato.timetable.util.Utils;

import org.junit.Test;

import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void test(){
        for(String s:"1-8[周]".split("\\["))
        {
            System.out.println(s);
        }
        ExcelUtils.getWeekOfTermFromString("1-8[周]");
    }

    /**
     * 自动化测试
     * Utils.getStringFromWeekOfTerm
     */
    @Test
    public void autoTestgetStringFromWeekOfTerm() {
        int max=0x1ffffff+1;
        int maxWeekNum=25;
        Random r=new Random();
        for(int i=0;i<100;i++)
        {
            int random=r.nextInt(max);
            String result=Utils.getStringFromWeekOfTerm(random);
            System.out.println(result);
            System.out.println(Integer.toBinaryString(random));

            String[] strings=result.split(" ");
            String[] s2=strings[0].split(",");

            int a=0;
            for(String str:s2)
            {
                if(str==null||str.isEmpty())
                    continue;
                if(str.contains("-"))
                {
                    int space=2;
                    if(strings[1].equals("[周]")){
                        space=1;
                    }
                    String[] strs=str.split("-");
                    if(strs.length!=2)
                    {
                        System.out.println("error");
                        return;
                    }
                    int p=Integer.valueOf(strs[0]);
                    int q=Integer.valueOf(strs[1]);

                    for(int n=p;n<=q;n+=space){
                        a+=1<<(maxWeekNum-n);
                    }
                }else {
                    a+=1<<(maxWeekNum-Integer.valueOf(str));
                }
            }
            assertEquals(random,a);
        }
        //System.out.println(Utils.getStringFromWeekOfTerm(Integer.valueOf("01111,01010,10101,01010,11111".replace(",",""),2)));
    }
}