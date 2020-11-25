package com.potato.timetable.colleges.base

import android.text.TextUtils
import com.potato.timetable.colleges.CSUCollege
import com.potato.timetable.colleges.ShmtuCollege
import java.util.*
import kotlin.collections.HashMap

object CollegeFactory {
    private val collegeMap = HashMap<String, Class<out College>>()

    init {
        collegeMap[CSUCollege.NAME] = CSUCollege::class.java
        collegeMap[ShmtuCollege.NAME] = ShmtuCollege::class.java
    }

    @JvmStatic
    val collegeNameList: List<String> = ArrayList(collegeMap.keys)
    init {
        collegeNameList.sorted()
    }

    /**
     * 获取
     */
    @JvmStatic
    fun createCollege(collegeName: String?): College? {
        if(TextUtils.isEmpty(collegeName)){
            return null;
        }
        return collegeMap[collegeName]?.newInstance();
    }


}