package com.zane.smapiinstaller.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.zane.smapiinstaller.R
import com.zane.smapiinstaller.ui.about.AboutFragment
import com.zane.smapiinstaller.ui.config.ConfigFragment
import com.zane.smapiinstaller.ui.download.DownloadableContentFragment
import com.zane.smapiinstaller.ui.help.HelpFragment
import com.zane.smapiinstaller.ui.install.InstallFragment

internal class MainTabPagerAdapter(private val fragment: Fragment) : FragmentStateAdapter(
    fragment
) {
    override fun createFragment(position: Int): Fragment {
        val args = Bundle()
        val fragment: Fragment
        return when (position) {
            1 -> {
                fragment = ConfigFragment()
                fragment.setArguments(args)
                fragment
            }

            2 -> {
                fragment = DownloadableContentFragment()
                fragment.setArguments(args)
                fragment
            }

            3 -> {
                fragment = HelpFragment()
                fragment.setArguments(args)
                fragment
            }

            4 -> {
                fragment = AboutFragment()
                fragment.setArguments(args)
                fragment
            }

            0 -> {
                fragment = InstallFragment()
                fragment.setArguments(args)
                fragment
            }

            else -> {
                fragment = InstallFragment()
                fragment.setArguments(args)
                fragment
            }
        }
    }

    fun getTitle(position: Int): String {
        return when (position) {
            1 -> fragment.getString(R.string.menu_config)
            2 -> fragment.getString(R.string.menu_download)
            3 -> fragment.getString(R.string.menu_help)
            4 -> fragment.getString(R.string.menu_about)
            0 -> fragment.getString(R.string.menu_install)
            else -> fragment.getString(R.string.menu_install)
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}