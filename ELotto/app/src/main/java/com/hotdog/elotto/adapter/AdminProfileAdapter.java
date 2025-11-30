package com.hotdog.elotto.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.User;

import java.util.List;

/**
 * Adapter for displaying user profiles in admin browse screen.
 *
 * @author Admin Module
 * @version 1.0
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    private final List<User> users;
    private final OnProfileActionListener listener;

    public interface OnProfileActionListener {
        void onViewDetails(User user);
        void onDeleteProfile(User user);
        void onRevokeOrganizer(User user);
    }

    public AdminProfileAdapter(List<User> users, OnProfileActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserType;
        private final ImageView ivViewDetails;
        private final ImageView ivRevokeOrganizer;
        private final ImageView ivDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserType = itemView.findViewById(R.id.tv_user_type);
            ivViewDetails = itemView.findViewById(R.id.iv_view_details);
            ivRevokeOrganizer = itemView.findViewById(R.id.iv_revoke_organizer);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        public void bind(User user, OnProfileActionListener listener) {
            // Set name
            String name = user.getName() != null ? user.getName() : "[No Name]";
            tvUserName.setText(name);

            // Set email
            String email = user.getEmail() != null ? user.getEmail() : "[No Email]";
            tvUserEmail.setText(email);

            // Set user type with color coding
            if (user.getType() != null) {
                tvUserType.setText(user.getType().toString());

                // Color code based on user type
                if (user.getType() == UserType.Organizer) {
                    // Orange for organizers
                    tvUserType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.waitlist_orange));
                    ivRevokeOrganizer.setVisibility(View.VISIBLE);
                } else {
                    // Green for entrants
                    tvUserType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.success_green));
                    ivRevokeOrganizer.setVisibility(View.GONE);
                }
            } else {
                // Default to "User" with gray color
                tvUserType.setText("User");
                tvUserType.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.form_icon));
                ivRevokeOrganizer.setVisibility(View.GONE);
            }

            // Set click listeners
            ivViewDetails.setOnClickListener(v -> listener.onViewDetails(user));
            ivDelete.setOnClickListener(v -> listener.onDeleteProfile(user));

            if (user.getType() == UserType.Organizer) {
                ivRevokeOrganizer.setOnClickListener(v -> listener.onRevokeOrganizer(user));
            }
        }
    }
}