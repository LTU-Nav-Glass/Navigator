package se.ltu.navigator.search;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import se.ltu.navigator.R;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    private final TextView text;

    public SearchViewHolder(@NonNull View itemView) {
        super(itemView);

        text = itemView.findViewById(R.id.result);
    }

    public void populate(String result) {
        text.setText(result);
    }
}
