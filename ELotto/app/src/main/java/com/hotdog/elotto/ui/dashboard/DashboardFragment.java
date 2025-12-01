package com.hotdog.elotto.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.hotdog.elotto.databinding.FragmentDashboardBinding;

/**
 * Fragment for displaying the dashboard screen.
 *
 * <p>This fragment uses View Binding and ViewModel architecture to display
 * dashboard content. It observes data from DashboardViewModel and updates
 * the UI accordingly.</p>
 *
 * <p>View layer component in MVVM architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author ELotto Team
 * @version 1.0
 * @since 2025-11-01
 */
public class DashboardFragment extends Fragment {

    /**
     * View binding instance for accessing layout views safely.
     */
    private FragmentDashboardBinding binding;

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * <p>Initializes the ViewModel, inflates the layout using View Binding, and
     * sets up observers for LiveData from the ViewModel. The text displayed in
     * the dashboard TextView is automatically updated when the ViewModel's text
     * LiveData changes.</p>
     *
     * @param inflater the LayoutInflater object that can be used to inflate views
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState the previously saved state of the fragment
     * @return the root View of the fragment's layout
     */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textDashboard;
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    /**
     * Called when the view previously created by onCreateView has been detached from the fragment.
     *
     * <p>Cleans up the binding reference to prevent memory leaks.</p>
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}