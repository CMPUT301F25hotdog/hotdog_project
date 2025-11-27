package com.hotdog.elotto.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hotdog.elotto.R;
import com.hotdog.elotto.helpers.UserType;
import com.hotdog.elotto.model.User;

import java.util.List;

/**
 * Adapter for displaying user profiles in admin panel
 */
public class AdminProfileAdapter extends RecyclerView.Adapter<AdminProfileAdapter.ProfileViewHolder> {

    private Context context;
    private List<User> users;
    private OnProfileActionListener listener;

    public interface OnProfileActionListener {
        void onViewProfile(User user);
        void onFlagProfile(User user);
        void onDeleteProfile(User user);
    }

    public AdminProfileAdapter(Context context, List<User> users, OnProfileActionListener listener) {
        this.context = context;
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_profile, parent, false);
        return new ProfileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User user = users.get(position);
        holder.bind(user);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class ProfileViewHolder extends RecyclerView.ViewHolder {
        TextView userType, userName, userEmail;
        ImageView btnView, btnFlag, btnDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);

            // Use correct IDs matching item_admin_profile.xml
            userType = itemView.findViewById(R.id.user_type);
            userName = itemView.findViewById(R.id.user_name);
            userEmail = itemView.findViewById(R.id.user_email);
            btnView = itemView.findViewById(R.id.btn_view_profile);
            btnFlag = itemView.findViewById(R.id.btn_flag_profile);
            btnDelete = itemView.findViewById(R.id.btn_delete_profile);
        }

        public void bind(User user) {
            userName.setText(user.getName() != null ? user.getName() : "Unknown");
            userEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");

            UserType type = user.getType();
            if (type != null) {
                userType.setText(type.toString());

                if (type == UserType.Organizer) {
                    userType.setBackgroundResource(R.drawable.status_badge_background);
                } else if (type == UserType.Entrant) {
                    userType.setBackgroundResource(R.drawable.button_pending_background);
                } else if (type == UserType.Admin) {
                    userType.setBackgroundResource(R.drawable.button_primary_background);
                }
            } else {
                userType.setText("Unknown");
            }

            btnView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewProfile(user);
                }
            });

            btnFlag.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFlagProfile(user);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteProfile(user);
                }
            });
        }
    }
}