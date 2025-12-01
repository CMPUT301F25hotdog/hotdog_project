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
 * Adapter for displaying user profiles in the Admin's profile management interface.
 *
 * <p>This adapter binds User data to view holders and provides functionality for
 * viewing user details, deleting profiles, and revoking organizer privileges. User
 * types are color-coded for easy visual identification (orange for organizers,
 * green for entrants).</p>
 *
 * <p>View layer component in MVC architecture pattern.</p>
 *
 * <p><b>Outstanding Issues:</b> None currently</p>
 *
 * @author Tatsat
 * @version 1.0
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    /**
     * The list of users to display in the RecyclerView.
     */
    private final List<User> users;

    /**
     * Listener for handling profile actions.
     */
    private final OnProfileActionListener listener;

    /**
     * Callback interface for handling profile actions.
     *
     * <p>Implementing classes should handle the business logic for viewing user
     * details, deleting profiles, and revoking organizer privileges.</p>
     */
    public interface OnProfileActionListener {
        /**
         * Called when the administrator clicks to view detailed user information.
         *
         * @param user the user whose details should be displayed
         */
        void onViewDetails(User user);

        /**
         * Called when the administrator clicks to delete a user profile.
         *
         * @param user the user whose profile should be deleted
         */
        void onDeleteProfile(User user);

        /**
         * Called when the administrator clicks to revoke organizer privileges from a user.
         *
         * @param user the user whose organizer privileges should be revoked
         */
        void onRevokeOrganizer(User user);
    }

    /**
     * Constructs a new AdminProfileAdapter with the specified users and action listener.
     *
     * @param users the list of users to display
     * @param listener the callback listener for handling profile actions
     */
    public AdminProfileAdapter(List<User> users, OnProfileActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    /**
     * Creates a new ViewHolder by inflating the admin profile item layout.
     *
     * @param parent the ViewGroup into which the new View will be added
     * @param viewType the view type of the new View
     * @return a new ProfileViewHolder that holds the inflated View
     */
    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    /**
     * Binds user profile data to the ViewHolder at the specified position.
     *
     * @param holder the ViewHolder to bind data to
     * @param position the position of the item in the adapter's data set
     */
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user, listener);
    }

    /**
     * Returns the total number of users in the adapter.
     *
     * @return the size of the users list
     */
    @Override
    public int getItemCount() {
        return users.size();
    }

    /**
     * ViewHolder class for individual user profile items in the admin profile list.
     *
     * <p>Displays user information including name, email, and user type with color
     * coding. Shows the revoke organizer button only for users with organizer privileges.</p>
     */
    static class ProfileViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserType;
        private final ImageView ivViewDetails;
        private final ImageView ivRevokeOrganizer;
        private final ImageView ivDelete;

        /**
         * Constructs a new ProfileViewHolder and initializes all view references.
         *
         * @param itemView the root view of the profile item layout
         */
        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserType = itemView.findViewById(R.id.tv_user_type);
            ivViewDetails = itemView.findViewById(R.id.iv_view_details);
            ivRevokeOrganizer = itemView.findViewById(R.id.iv_revoke_organizer);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }

        /**
         * Binds user profile data to the view components and sets up click listeners.
         *
         * <p>This method displays the user's name, email, and type with appropriate
         * color coding:</p>
         * <ul>
         *     <li>Organizers: Orange text with visible revoke button</li>
         *     <li>Entrants: Green text with hidden revoke button</li>
         *     <li>Unknown type: Gray "User" text with hidden revoke button</li>
         * </ul>
         *
         * <p>Sets click listeners for view details, delete profile, and revoke organizer
         * actions. The revoke organizer listener is only attached for organizer users.</p>
         *
         * @param user the user object containing data to display
         * @param listener the listener to handle profile action callbacks
         */
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