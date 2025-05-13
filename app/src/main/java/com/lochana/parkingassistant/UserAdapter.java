package com.lochana.parkingassistant;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.firebase.firestore.auth.User;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView, pointsTextView,rankTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nicknameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
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

        String currentUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        // Check if this user is the current user
        if (user.getEmail() != null && user.getEmail().equals(currentUserEmail)) {
            holder.nicknameTextView.setText("You");
        } else {
            holder.nicknameTextView.setText(user.getNickname());
        }

        //holder.nicknameTextView.setText(user.getNickname());
        holder.pointsTextView.setText("Points : " + String.valueOf(user.getPoints()));
        holder.rankTextView.setText(String.valueOf(user.getRank()));
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

