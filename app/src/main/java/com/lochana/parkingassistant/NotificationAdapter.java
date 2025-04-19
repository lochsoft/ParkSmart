package com.lochana.parkingassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<NotificationEntity> notifications;

    public NotificationAdapter(List<NotificationEntity> notifications) {
        this.notifications = notifications;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titleText);
            message = itemView.findViewById(R.id.messageText);
        }
    }

    @Override
    public NotificationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        NotificationEntity notification = notifications.get(position);
        holder.title.setText(notification.title);
        holder.message.setText(notification.message);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }
}

