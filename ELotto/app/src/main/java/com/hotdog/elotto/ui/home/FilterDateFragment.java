package com.hotdog.elotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.hotdog.elotto.R;

/**
 * Fragment responsible for displaying and managing date-based event filtering options.
 *
 * <p>This UI component allows users to choose from predefined date filters using
 * a group of radio buttons. The selected filter can later be retrieved and used
 * by the hosting activity or fragment to refine event results based on the
 * user's time preference.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Six date range filter options (Today, Tomorrow, Within 7 Days, Within 14 Days, This Month, All Dates)</li>
 *     <li>Default preset selection via {@link #setInitialSelection(DateFilter)}</li>
 *     <li>Utility method to reset selection to "All Dates"</li>
 * </ul>
 *
 * <p>This fragment is used as a reusable filtering component in the home screen.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently.</p>
 *
 * @version 1.0
 * @since 2025-11-01
 */
public class FilterDateFragment extends Fragment {

    /**
     * RadioGroup containing all available date filter options.
     */
    private RadioGroup dateFilterRadioGroup;

    /**
     * RadioButton option for selecting events happening today.
     */
    private RadioButton radioToday;

    /**
     * RadioButton option for selecting events happening tomorrow.
     */
    private RadioButton radioTomorrow;

    /**
     * RadioButton option for selecting events happening within the next 7 days.
     */
    private RadioButton radioWithin7Days;

    /**
     * RadioButton option for selecting events happening within the next 14 days.
     */
    private RadioButton radioWithin14Days;

    /**
     * RadioButton option for selecting events occurring in the current calendar month.
     */
    private RadioButton radioThisMonth;

    /**
     * RadioButton option for selecting events regardless of date.
     */
    private RadioButton radioAllDates;

    /**
     * Initial date filter state applied when the fragment is displayed.
     */
    private DateFilter initialDateFilter = DateFilter.ALL_DATES;


    /**
     * Inflates the fragment layout containing the date filter radio group.
     *
     * @param inflater LayoutInflater used to inflate views in this fragment
     * @param container Parent view into which this fragment's UI will be placed
     * @param savedInstanceState Previously saved instance state, if available
     * @return the root view associated with this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_date, container, false);
    }

    /**
     * Initializes UI components once the layout is created and applies the initial filter selection.
     *
     * @param view the root view returned by {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     * @param savedInstanceState previously saved instance state, if available
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        applyInitialSelection();
    }

    /**
     * Sets the appropriate radio button based on the {@link #initialDateFilter} value.
     * Executed only during fragment initialization.
     */
    private void applyInitialSelection() {
        switch (initialDateFilter) {
            case TODAY:
                radioToday.setChecked(true);
                break;
            case TOMORROW:
                radioTomorrow.setChecked(true);
                break;
            case WITHIN_7_DAYS:
                radioWithin7Days.setChecked(true);
                break;
            case WITHIN_14_DAYS:
                radioWithin14Days.setChecked(true);
                break;
            case THIS_MONTH:
                radioThisMonth.setChecked(true);
                break;
            case ALL_DATES:
            default:
                radioAllDates.setChecked(true);
                break;
        }
    }

    /**
     * Binds all UI radio button elements to their corresponding layout components.
     *
     * @param view the root fragment view containing the radio button elements
     */
    private void initializeViews(View view) {
        dateFilterRadioGroup = view.findViewById(R.id.dateFilterRadioGroup);
        radioToday = view.findViewById(R.id.radioToday);
        radioTomorrow = view.findViewById(R.id.radioTomorrow);
        radioWithin7Days = view.findViewById(R.id.radioWithin7Days);
        radioWithin14Days = view.findViewById(R.id.radioWithin14Days);
        radioThisMonth = view.findViewById(R.id.radioThisMonth);
        radioAllDates = view.findViewById(R.id.radioAllDates);
    }

    /**
     * Retrieves the currently selected date filter from the radio group.
     *
     * @return the {@link DateFilter} representing the selected time range. If the view
     *         has not been initialized, the {@link #initialDateFilter} value is returned instead.
     */
    public DateFilter getSelectedDateFilter() {
        if (dateFilterRadioGroup == null){
            return initialDateFilter;
        }
        int selectedId = dateFilterRadioGroup.getCheckedRadioButtonId();

        if (selectedId == R.id.radioToday) {
            return DateFilter.TODAY;
        } else if (selectedId == R.id.radioTomorrow) {
            return DateFilter.TOMORROW;
        } else if (selectedId == R.id.radioWithin7Days) {
            return DateFilter.WITHIN_7_DAYS;
        } else if (selectedId == R.id.radioWithin14Days) {
            return DateFilter.WITHIN_14_DAYS;
        } else if (selectedId == R.id.radioThisMonth) {
            return DateFilter.THIS_MONTH;
        } else {
            return DateFilter.ALL_DATES;
        }
    }

    /**
     * Sets the initial preselected radio button before the fragment UI is displayed.
     * This method should be called before view creation to ensure correct initialization.
     *
     * @param dateFilter the {@link DateFilter} to apply as the initial selection
     */
    public void setInitialSelection(DateFilter dateFilter) {
        this.initialDateFilter = dateFilter;
    }

    /**
     * Resets the filter selection to "All Dates".
     * If the views have not been initialized yet, the call is ignored safely.
     */
    public void clearSelection() {
        if (radioAllDates == null) {
            return;
        }
        radioAllDates.setChecked(true);
    }
}
