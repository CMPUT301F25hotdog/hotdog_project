package com.hotdog.elotto.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Manages the two filter tabs (Interests and Date)
 */
public class FilterPagerAdapter extends FragmentStateAdapter {

    private final FilterInterestsFragment interestsFragment;
    private final FilterDateFragment dateFragment;

    public FilterPagerAdapter(@NonNull Fragment fragment,
                              FilterInterestsFragment interestsFragment,
                              FilterDateFragment dateFragment) {
        super(fragment);
        this.interestsFragment = interestsFragment;
        this.dateFragment = dateFragment;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return interestsFragment;
        } else {
            return dateFragment;
        }
    }

    // viewPager needs to know many tabs there are
    @Override
    public int getItemCount() {
        return 2;
    }
}