package com.potato.timetable.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType


abstract class ViewBindingFragment<T : ViewBinding> : BaseFragment() {
    private var _binding: T? = null
    protected val binding
        get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        val entityClass = (this.javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<*>
        val inflate = entityClass.getDeclaredMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
        val obj = inflate.invoke(null, layoutInflater, container, false)
        if (obj == null || obj !is ViewBinding) {
            throw RuntimeException("视图绑定错误,无法得到视图绑定类")
        }
        _binding = obj as T


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}