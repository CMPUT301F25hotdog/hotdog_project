package com.hotdog.elotto.ui.dashboard;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * ViewModel for the Dashboard screen.
 *
 * <p>This ViewModel manages the UI-related data for the DashboardFragment using
 * LiveData. It survives configuration changes and provides observable data to
 * the fragment.</p>
 *
 * <p>ViewModel layer component in MVVM architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author ELotto Team
 * @version 1.0
 * @since 2025-11-01
 */
public class DashboardViewModel extends ViewModel {

    /**
     * MutableLiveData holding the text to be displayed in the dashboard.
     */
    private final MutableLiveData<String> mText;

    /**
     * Constructs a new DashboardViewModel and initializes the text LiveData.
     *
     * <p>Sets the default text value to "This is dashboard fragment".</p>
     */
    public DashboardViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is dashboard fragment");
    }

    /**
     * Returns the LiveData containing the dashboard text.
     *
     * <p>Observers can subscribe to this LiveData to receive updates when
     * the text value changes.</p>
     *
     * @return LiveData containing the dashboard text string
     */
    public LiveData<String> getText() {
        return mText;
    }
}