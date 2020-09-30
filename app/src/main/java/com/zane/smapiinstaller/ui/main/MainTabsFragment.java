package com.zane.smapiinstaller.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayoutMediator;
import com.zane.smapiinstaller.MainActivity;
import com.zane.smapiinstaller.databinding.FragmentMainBinding;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

/**
 * @author Zane
 */
public class MainTabsFragment extends Fragment {

    private FragmentMainBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        MainTabPagerAdapter pagerAdapter = new MainTabPagerAdapter(this);
        binding.mainPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(binding.mainTabLayout, binding.mainPager,
                (tab, position) -> tab.setText(pagerAdapter.getTitle(position))
        ).attach();
        binding.mainPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= 3) {
                    MainActivity.instance.setFloatingBarVisibility(false);
                } else {
                    MainActivity.instance.setFloatingBarVisibility(true);
                }
            }
        });
    }
}
