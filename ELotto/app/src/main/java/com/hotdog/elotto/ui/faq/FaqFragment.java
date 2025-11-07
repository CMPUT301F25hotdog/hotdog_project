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

public class FaqFragment extends Fragment {

    private ImageButton backButtonFaq;
    private View faqCard;
    private ImageView dropArrow;
    private TextView faqQuestion, faqSubtext;
    private boolean isExpanded = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_faq, container, false);

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

        faqCard.setOnClickListener(v1 -> toggleFaq());

        return v;
    }

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
