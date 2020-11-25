package com.potato.timetable.colleges.base

import com.potato.timetable.colleges.CSUCollege
import junit.framework.TestCase

class CollegeFactoryTest : TestCase() {

    fun testGetCollegeNameList() {
        println(CollegeFactory.collegeNameList)
    }

    fun testCreateCollege() {
        assertNull(CollegeFactory.createCollege(null))
        assertNull(CollegeFactory.createCollege(""))
        assertTrue(CollegeFactory.createCollege(CSUCollege.NAME) is CSUCollege)
    }
}