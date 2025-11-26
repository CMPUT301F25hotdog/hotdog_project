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
 * Fragment for displaying date filter radio buttons
 */
public class FilterDateFragment extends Fragment {

    private RadioGroup dateFilterRadioGroup;
    private RadioButton radioToday;
    private RadioButton radioTomorrow;
    private RadioButton radioWithin7Days;
    private RadioButton radioWithin14Days;
    private RadioButton radioThisMonth;
    private RadioButton radioAllDates;
    private DateFilter initialDateFilter = DateFilter.ALL_DATES;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_date, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        applyInitialSelection();
    }

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
     * Gets the currently selected date filter
     *
     * @return DateFilter enum value
     */
    public DateFilter getSelectedDateFilter() {
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

    public void setInitialSelection(DateFilter dateFilter) {
        this.initialDateFilter = dateFilter;
    }

    /**
     * Clears the date filter selection (sets to All Dates)
     */
    public void clearSelection() {
        if (radioAllDates == null) {
            return;
        }
        radioAllDates.setChecked(true);
    }
}