package se.ltu.navigator.search;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.function.Consumer;

import se.ltu.navigator.R;

public class SearchViewHolder extends RecyclerView.ViewHolder {

    private final TextView text;
    private final Consumer<String> searcher;

    public SearchViewHolder(@NonNull View itemView, Consumer<String> searcher) {
        super(itemView);

        this.text = itemView.findViewById(R.id.result);
        this.searcher = searcher;
    }

    public void populate(String result) {
        text.setText(result);
        itemView.setOnClickListener(view -> {
            searcher.accept(result);
        });
    }
}
