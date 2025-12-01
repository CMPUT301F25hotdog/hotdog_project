package com.hotdog.elotto.ui.home;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * Adapter responsible for supplying the appropriate filter fragment
 * to the {@link androidx.viewpager2.widget.ViewPager2} when navigating
 * between filter tabs.
 *
 * <p>This adapter manages two filter pages:</p>
 * <ul>
 *     <li>{@link FilterInterestsFragment} – allows selecting interest tags</li>
 *     <li>{@link FilterDateFragment} – allows selecting a date range</li>
 * </ul>
 *
 * <p>Both fragment instances are supplied via the constructor and retained
 * internally, ensuring that user selections persist while switching
 * between tabs.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently.</p>
 *
 * @version 1.0
 * @since 2025-11-01
 */
public class FilterPagerAdapter extends FragmentStateAdapter {

    /**
     * Fragment responsible for selecting event interest tags.
     */
    private final FilterInterestsFragment interestsFragment;

    /**
     * Fragment responsible for selecting date range filters.
     */
    private final FilterDateFragment dateFragment;

    /**
     * Creates a new pager adapter that manages the filter fragments.
     *
     * @param fragment the host fragment for the ViewPager2
     * @param interestsFragment the fragment used for tag-based filtering
     * @param dateFragment the fragment used for date-based filtering
     */
    public FilterPagerAdapter(@NonNull Fragment fragment,
                              FilterInterestsFragment interestsFragment,
                              FilterDateFragment dateFragment) {
        super(fragment);
        this.interestsFragment = interestsFragment;
        this.dateFragment = dateFragment;
    }

    /**
     * Returns the fragment associated with the specified position.
     *
     * @param position index of the selected tab (0 for interests, 1 for date)
     * @return the corresponding filter fragment
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            return interestsFragment;
        } else {
            return dateFragment;
        }
    }

    /**
     * Returns the total number of filter pages used by the ViewPager.
     *
     * @return the number of available filter fragments (always 2)
     */
    @Override
    public int getItemCount() {
        return 2;
    }
}
