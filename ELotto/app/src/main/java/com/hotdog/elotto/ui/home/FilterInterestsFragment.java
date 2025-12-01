package com.hotdog.elotto.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hotdog.elotto.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Fragment responsible for displaying and managing tag-based event interest filtering.
 *
 * <p>This component dynamically creates a list of checkboxes based on a predefined set of
 * interest categories. Users may select multiple interests, which can later be applied as
 * filters to refine event recommendations.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Dynamic creation of checkboxes for each available tag</li>
 *     <li>Support for pre-selecting previously chosen tags</li>
 *     <li>Utility methods for retrieving or clearing selected tags</li>
 * </ul>
 *
 * <p>This fragment serves as a reusable input component for filtering UI services.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently.</p>
 *
 * @version 1.0
 * @since 2025-11-01
 */
public class FilterInterestsFragment extends Fragment {

    /**
     * List of predefined interest tags available for selection.
     */
    private static final List<String> AVAILABLE_TAGS = Arrays.asList(
            "Music",
            "Sports",
            "Technology",
            "Art",
            "Food",
            "Business",
            "Health & Wellness",
            "Networking"
    );

    /**
     * Container layout that will host dynamically generated checkboxes.
     */
    private LinearLayout interestsContainer;

    /**
     * Mapping of tag names to their corresponding checkbox UI elements.
     */
    private Map<String, CheckBox> tagCheckboxes;

    /**
     * Set of tags that should be pre-selected when the fragment is displayed.
     */
    private Set<String> initialSelectedTags = new HashSet<>();

    /**
     * Inflates the layout that will host the checkbox UI for tag selection.
     *
     * @param inflater LayoutInflater used to inflate the fragment's layout
     * @param container parent view into which this fragment's UI will be inserted
     * @param savedInstanceState previously saved instance state, if available
     * @return the root layout view for this fragment
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_interests, container, false);
    }

    /**
     * Called after the layout is created.
     *
     * <p>Initializes the container layout and populates it with dynamically created
     * checkboxes representing each available tag.</p>
     *
     * @param view root view of the inflated fragment layout
     * @param savedInstanceState previously saved instance state, if available
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        interestsContainer = view.findViewById(R.id.interestsContainer);
        tagCheckboxes = new HashMap<>();

        createTagCheckboxes();
    }

    /**
     * Dynamically generates checkbox UI elements for each tag in
     * {@link #AVAILABLE_TAGS} and adds them to the fragment layout.
     *
     * <p>Previously selected tags (from {@link #initialSelectedTags}) are checked by default.</p>
     */
    private void createTagCheckboxes() {
        for (String tag : AVAILABLE_TAGS) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(tag);
            checkBox.setTextSize(16);
            checkBox.setPadding(0, 12, 0, 12);

            if (initialSelectedTags.contains(tag)) {
                checkBox.setChecked(true);
            }
            tagCheckboxes.put(tag, checkBox);
            interestsContainer.addView(checkBox);
        }
    }

    /**
     * Retrieves the set of currently selected tags.
     *
     * @return a {@link Set} containing the names of tags whose checkboxes are checked;
     *         if the UI is not yet initialized, the initial selection is returned instead
     */
    public Set<String> getSelectedTags() {
        Set<String> selectedTags = new HashSet<>();

        if (tagCheckboxes == null || tagCheckboxes.isEmpty()){
            return initialSelectedTags;
        }
        for (Map.Entry<String, CheckBox> entry : tagCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedTags.add(entry.getKey());
            }
        }

        return selectedTags;
    }

    /**
     * Sets the initial tags to be checked when the fragment is first displayed.
     *
     * @param selectedTags the set of tags to pre-select
     */
    public void setInitialSelection(Set<String> selectedTags) {
        this.initialSelectedTags = new HashSet<>(selectedTags);
    }

    /**
     * Clears all selected tags by unchecking every checkbox in the layout.
     * If checkboxes have not been created yet, the call completes safely.
     */
    public void clearSelection() {
        if (tagCheckboxes == null) {
            return;
        }
        for (CheckBox checkBox : tagCheckboxes.values()) {
            checkBox.setChecked(false);
        }
    }
}
