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
 * Dialog fragment for filtering events by interests and date ranges.
 *
 * <p>This component presents a tabbed interface that allows users to select
 * filtering criteria for events. It uses a {@link ViewPager2} paired with a
 * {@link TabLayout} to host two child fragments:</p>
 *
 * <ul>
 *     <li>{@link FilterInterestsFragment} – for selecting interest tags</li>
 *     <li>{@link FilterDateFragment} – for selecting a date range filter</li>
 * </ul>
 *
 * <p>Once the user applies the filters, the selected interest tags and date
 * filter are returned to the caller through the
 * {@link OnFilterAppliedListener} callback interface.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently.</p>
 *
 * @version 1.0
 * @since 2025-11-01
 */
public class FilterDialogFragment extends DialogFragment {

    /**
     * TabLayout used to display and switch between "Interests" and "Date" tabs.
     */
    private TabLayout filterTabLayout;

    /**
     * ViewPager2 used to host and swipe between the filter fragments.
     */
    private ViewPager2 filterViewPager;

    /**
     * Button that applies the current filter selections and notifies the listener.
     */
    private Button applyFiltersButton;

    /**
     * Button that clears all filter selections within the dialog.
     */
    private Button clearFiltersButton;

    /**
     * Adapter responsible for providing {@link FilterInterestsFragment} and
     * {@link FilterDateFragment} pages to the {@link ViewPager2}.
     */
    private FilterPagerAdapter pagerAdapter;

    /**
     * Fragment for selecting interest-based tags.
     */
    private FilterInterestsFragment interestsFragment;

    /**
     * Fragment for selecting date-based filters.
     */
    private FilterDateFragment dateFragment;

    /**
     * Listener used to deliver the selected filter criteria back to the caller.
     */
    private OnFilterAppliedListener listener;

    /**
     * Set of tags that should be initially selected when the dialog is shown.
     */
    private Set<String> initialSelectedTags = new HashSet<>();

    /**
     * Initial date filter to be pre-selected when the dialog is shown.
     */
    private DateFilter initialDateFilter = DateFilter.ALL_DATES;

    /**
     * Callback interface for receiving applied filter values from this dialog.
     */
    public interface OnFilterAppliedListener {
        /**
         * Called when the user taps the apply button and confirms their filter choices.
         *
         * @param selectedTags the set of selected interest tags
         * @param dateFilter   the chosen {@link DateFilter} for date-based filtering
         */
        void onFilterApplied(Set<String> selectedTags, DateFilter dateFilter);
    }

    /**
     * Creates a new instance of {@link FilterDialogFragment}.
     *
     * @return a new {@link FilterDialogFragment} instance
     */
    public static FilterDialogFragment newInstance() {
        return new FilterDialogFragment();
    }

    /**
     * Registers a listener to receive callbacks when filters are applied.
     *
     * @param listener the {@link OnFilterAppliedListener} to be notified
     */
    public void setOnFilterAppliedListener(OnFilterAppliedListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the fragment is created.
     *
     * <p>Sets the dialog style and theme used when rendering this filter dialog.</p>
     *
     * @param savedInstanceState previously saved instance state, if available
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_ELotto);
    }

    /**
     * Inflates the layout for this dialog fragment.
     *
     * @param inflater  LayoutInflater used to inflate views in this fragment
     * @param container parent view that this fragment's UI should be attached to
     * @param savedInstanceState previously saved instance state, if available
     * @return the root view of the dialog layout
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_filter_events, container, false);
    }

    /**
     * Called after the view hierarchy has been created.
     *
     * <p>Initializes UI components, sets up the {@link ViewPager2} with its
     * corresponding fragments, and attaches click listeners to control buttons.</p>
     *
     * @param view               the root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved instance state, if available
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        setupViewPager();
        setupListeners();
    }

    /**
     * Binds the UI elements from the inflated layout to their corresponding fields.
     *
     * @param view the root view containing the dialog's UI components
     */
    private void initializeViews(View view) {
        filterTabLayout = view.findViewById(R.id.filterTabLayout);
        filterViewPager = view.findViewById(R.id.filterViewPager);
        applyFiltersButton = view.findViewById(R.id.applyFiltersButton);
        clearFiltersButton = view.findViewById(R.id.clearFiltersButton);
    }

    /**
     * Configures the {@link ViewPager2} and {@link TabLayout} to display
     * the interests and date filter fragments as separate tabs.
     *
     * <p>Applies any initial filter selections to the child fragments before
     * attaching them to the pager.</p>
     */
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

    /**
     * Sets up click listeners for the apply and clear filter buttons.
     */
    private void setupListeners() {
        applyFiltersButton.setOnClickListener(v -> applyFilters());
        clearFiltersButton.setOnClickListener(v -> clearFilters());
    }

    /**
     * Collects the selected tags and date filter from the child fragments,
     * notifies the registered listener, and dismisses the dialog.
     */
    private void applyFilters() {
        Set<String> selectedTags = interestsFragment.getSelectedTags();
        DateFilter dateFilter = dateFragment.getSelectedDateFilter();

        if (listener != null) {
            listener.onFilterApplied(selectedTags, dateFilter);
        }

        dismiss();
    }

    /**
     * Clears all filter selections within the interests and date fragments.
     */
    private void clearFilters() {
        interestsFragment.clearSelection();
        dateFragment.clearSelection();
    }

    /**
     * Sets the currently active filters so they can be reflected when the dialog is displayed.
     *
     * @param selectedTags the set of tags that should be pre-selected
     * @param dateFilter   the date filter that should be pre-selected
     */
    public void setCurrentFilters(Set<String> selectedTags, DateFilter dateFilter) {
        this.initialSelectedTags = new HashSet<>(selectedTags);
        this.initialDateFilter = dateFilter;
    }

    /**
     * Adjusts the dialog window size when the fragment becomes visible.
     *
     * <p>This implementation sets the width of the dialog window to match
     * the parent and the height to wrap its content.</p>
     */
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
