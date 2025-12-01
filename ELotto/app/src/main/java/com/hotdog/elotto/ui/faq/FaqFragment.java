package com.hotdog.elotto.ui.faq;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.hotdog.elotto.R;

/**
 * Fragment for displaying Frequently Asked Questions.
 *
 * <p>This fragment provides users with information about the lottery system through
 * an expandable/collapsible FAQ card interface. Users can tap the card to toggle
 * between a brief prompt and detailed explanation.</p>
 *
 * <p>Features include:</p>
 * <ul>
 *     <li>Expandable/collapsible FAQ card with animated arrow indicator</li>
 *     <li>Brief prompt text when collapsed: "Tap to learn about the selection criteria"</li>
 *     <li>Detailed explanation when expanded about lottery selection process</li>
 *     <li>Smooth 200ms rotation animation for the dropdown arrow</li>
 *     <li>Back button navigation to return to previous screen</li>
 * </ul>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author
 * @version 1.0
 * @since 2025-11-01
 */
public class FaqFragment extends Fragment {

    /**
     * Button for navigating back to the previous screen.
     */
    private ImageButton backButtonFaq;

    /**
     * Container view for the FAQ card that users can tap to expand/collapse.
     */
    private View faqCard;

    /**
     * Arrow image that rotates to indicate expanded/collapsed state.
     */
    private ImageView dropArrow;

    /**
     * TextView displaying the FAQ question.
     */
    private TextView faqQuestion;

    /**
     * TextView displaying the FAQ answer text that changes based on expanded state.
     */
    private TextView faqSubtext;

    /**
     * Flag tracking whether the FAQ card is currently expanded.
     */
    private boolean isExpanded = false;

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     *
     * <p>Inflates the FAQ layout, binds all UI elements, sets up the back button
     * to pop the navigation back stack, initializes the FAQ card in collapsed state
     * with default text, and sets up the click listener to toggle expansion.</p>
     *
     * @param inflater the LayoutInflater object that can be used to inflate views
     * @param container the parent view that the fragment's UI should be attached to
     * @param savedInstanceState the previously saved state of the fragment
     * @return the root View of the fragment's layout
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_faq, container, false);

        // Bind UI
        backButtonFaq = v.findViewById(R.id.backButtonFaq);
        faqCard = v.findViewById(R.id.faqCard);
        dropArrow = v.findViewById(R.id.dropArrow);
        faqQuestion = v.findViewById(R.id.faqQuestion);
        faqSubtext = v.findViewById(R.id.faqSubtext);

        backButtonFaq.setOnClickListener(x ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        faqSubtext.setText("Tap to learn about the selection criteria");
        dropArrow.setRotation(0);

        // Toggle expand/collapse
        faqCard.setOnClickListener(v1 -> toggleFaq());

        return v;
    }

    /**
     * Toggles the expansion state of the FAQ card.
     *
     * <p>When expanding (isExpanded = false):</p>
     * <ul>
     *     <li>Updates text to detailed explanation about lottery selection process</li>
     *     <li>Rotates arrow 180 degrees over 200ms to point upward</li>
     * </ul>
     *
     * <p>When collapsing (isExpanded = true):</p>
     * <ul>
     *     <li>Updates text to brief prompt: "Tap to learn about the selection criteria"</li>
     *     <li>Rotates arrow back to 0 degrees over 200ms to point downward</li>
     * </ul>
     *
     * <p>Flips the isExpanded flag after updating the UI.</p>
     */
    private void toggleFaq() {
        if (!isExpanded) {
            faqSubtext.setText("The lottery system randomly selects entrants based on available spots. Re-draws may happen if winners don't confirm, ensuring fairness for all entrants.");
            dropArrow.animate().rotation(180).setDuration(200).start();
        } else {
            faqSubtext.setText("Tap to learn about the selection criteria");
            dropArrow.animate().rotation(0).setDuration(200).start();
        }
        isExpanded = !isExpanded;
    }
}