package com.lochana.parkingassistant;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.google.firebase.firestore.auth.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    private List<User> userList;
    FirebaseFirestore db;
    ImageView avatarImageView;

    public UserAdapter(List<User> userList) {
        this.userList = userList;
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView nicknameTextView, pointsTextView,rankTextView;
        ImageView trophyImage,avatarImageView;
        public UserViewHolder(View itemView) {
            super(itemView);
            nicknameTextView = itemView.findViewById(R.id.nicknameTextView);
            pointsTextView = itemView.findViewById(R.id.pointsTextView);
            rankTextView = itemView.findViewById(R.id.rankTextView);
            trophyImage = itemView.findViewById(R.id.trophyImage);
            avatarImageView = itemView.findViewById(R.id.avatarImg);
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
            holder.nicknameTextView.setText(R.string.you);
        } else {
            holder.nicknameTextView.setText(user.getNickname());
        }

        //holder.nicknameTextView.setText(user.getNickname());
        String pointsTxt = "Points : " + String.valueOf(user.getPoints());
        holder.pointsTextView.setText(pointsTxt);
        String rankTxt = "Rank : " + String.valueOf(user.getRank());
        holder.rankTextView.setText(rankTxt);

        // show a trophy image for the rank 1 user
        if (user.getRank() == 1) {
            holder.trophyImage.setVisibility(View.VISIBLE);
        } else if (user.getRank() == 2) {
            holder.trophyImage.setImageResource(R.drawable.silver_trophy2);
            holder.trophyImage.setVisibility(View.VISIBLE);
        }
        else if (user.getRank() == 3) {
            holder.trophyImage.setImageResource(R.drawable.bronze_trophy);
            holder.trophyImage.setVisibility(View.VISIBLE);
        }

        /// set user avatr in the leaderboard
        if (user.getAvatar() != null) {
            holder.avatarImageView.setImageResource(user.getAvatar().intValue());
        } else {
           // holder.avatarImageView.setImageResource(R.drawable.default_avatar);  // fallback avatar
            Log.d("Contributions", "Avatar not found");
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }
}

