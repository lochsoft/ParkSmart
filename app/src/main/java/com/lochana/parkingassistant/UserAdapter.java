package com.lochana.parkingassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.firebase.firestore.auth.User;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView, pointsTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nicknameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
        }
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        holder.nicknameTextView.setText(user.getNickname());
        holder.pointsTextView.setText(String.valueOf(user.getPoints()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

