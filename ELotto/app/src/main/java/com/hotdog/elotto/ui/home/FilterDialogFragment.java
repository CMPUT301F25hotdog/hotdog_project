package com.hotdog.elotto.ui.home;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.hotdog.elotto.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Dialog fragment for filtering events by tags and date ranges.
 * Utilizes the ViewPager2 which will hold the two fragments and allow swiping:
 * https://www.geeksforgeeks.org/android/viewpager2-in-android-with-example/

 */
public class FilterDialogFragment extends DialogFragment {

    private TabLayout filterTabLayout;
    private ViewPager2 filterViewPager;
    private Button applyFiltersButton;
    private Button clearFiltersButton;
    private FilterPagerAdapter pagerAdapter;
    private FilterInterestsFragment interestsFragment;
    private FilterDateFragment dateFragment;
    private OnFilterAppliedListener listener;
    private Set<String> initialSelectedTags = new HashSet<>();
    private DateFilter initialDateFilter = DateFilter.ALL_DATES;

    public interface OnFilterAppliedListener {
        void onFilterApplied(Set<String> selectedTags, DateFilter dateFilter);
    }

    public static FilterDialogFragment newInstance() {
        return new FilterDialogFragment();
    }

    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_ELotto);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_filter_events, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupViewPager();
        setupListeners();
    }

    private void initializeViews(View view) {
        filterTabLayout = view.findViewById(R.id.filterTabLayout);
        filterViewPager = view.findViewById(R.id.filterViewPager);
        applyFiltersButton = view.findViewById(R.id.applyFiltersButton);
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton);
    }

    private void setupViewPager() {
        interestsFragment = new FilterInterestsFragment();
        dateFragment = new FilterDateFragment();
        interestsFragment.setInitialSelection(initialSelectedTags);
        dateFragment.setInitialSelection(initialDateFilter);
        pagerAdapter = new FilterPagerAdapter(this, interestsFragment, dateFragment);
        filterViewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(filterTabLayout, filterViewPager,
                (tab, position) -> {
                    if (position == 0) {
                        tab.setText("Interests");
                    } else {
                        tab.setText("Date");
                    }
                }).attach();
    }

    private void setupListeners() {
        applyFiltersButton.setOnClickListener(v -> applyFilters());
        clearFiltersButton.setOnClickListener(v -> clearFilters());
    }

    private void applyFilters() {
        Set<String> selectedTags = interestsFragment.getSelectedTags();
        DateFilter dateFilter = dateFragment.getSelectedDateFilter();

        if (listener != null) {
            listener.onFilterApplied(selectedTags, dateFilter);
        }

        dismiss();
    }

    private void clearFilters() {
        interestsFragment.clearSelection();
        dateFragment.clearSelection();
    }

    public void setCurrentFilters(Set<String> selectedTags, DateFilter dateFilter) {
        this.initialSelectedTags = new HashSet<>(selectedTags);
        this.initialDateFilter = dateFilter;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}