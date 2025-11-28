package com.hotdog.elotto.ui.notifications;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.hotdog.elotto.R;
import com.bumptech.glide.Glide;

import java.util.zip.Inflater;

public class InboxNotificationContentFragment extends Fragment {
    private String title=null;
    private String body=null;
    private String iconImageURL=null;

    public InboxNotificationContentFragment(String title, String body, String iconImageURL) {
        this.title=title;
        this.body=body;
        this.iconImageURL=iconImageURL;


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        return inflater.inflate(R.layout.fragment_inbox_notification_content, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView notifTitle = view.findViewById(R.id.inboxNotificationTitle);
        notifTitle.setText(this.title);
        TextView notifBody = view.findViewById(R.id.inboxNotificationBody);
        notifBody.setText(this.body);
        ImageView notifIcon = view.findViewById(R.id.notificationIconImage);
        Glide.with(this).load(this.iconImageURL).placeholder(R.drawable.baseline_image_24).into(notifIcon);
    }
}
