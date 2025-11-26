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
 * Fragment for displaying interest/tag filter checkboxes
 */
public class FilterInterestsFragment extends Fragment {

    // These are the predefined tags that match what organizers can select when creating an event
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

    private LinearLayout interestsContainer;
    private Map<String, CheckBox> tagCheckboxes;
    private Set<String> initialSelectedTags = new HashSet<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filter_interests, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        interestsContainer = view.findViewById(R.id.interestsContainer);
        tagCheckboxes = new HashMap<>();

        createTagCheckboxes();
    }

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
     * Gets the set of currently selected tags.
     *
     * @return Set of selected tag names
     */
    public Set<String> getSelectedTags() {
        Set<String> selectedTags = new HashSet<>();

        for (Map.Entry<String, CheckBox> entry : tagCheckboxes.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedTags.add(entry.getKey());
            }
        }

        return selectedTags;
    }

    public void setInitialSelection(Set<String> selectedTags) {
        this.initialSelectedTags = new HashSet<>(selectedTags);
    }

    /**
     * Clears all checkbox selections.
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