package com.zane.smapiinstaller.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zane.smapiinstaller.MainActivity
import com.zane.smapiinstaller.databinding.FragmentMainBinding

/**
 * @author Zane
 */
class MainTabsFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val pagerAdapter = MainTabPagerAdapter(this)
        binding.mainPager.adapter = pagerAdapter
        TabLayoutMediator(
            binding.mainTabLayout, binding.mainPager
        ) { tab, position ->
            tab.text = pagerAdapter.getTitle(position)
        }.attach()
        binding.mainPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                MainActivity.instance!!.setFloatingBarVisibility(position < 3)
            }
        })
    }
}