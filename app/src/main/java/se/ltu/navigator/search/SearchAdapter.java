package se.ltu.navigator.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.function.Consumer;

import se.ltu.navigator.R;
import se.ltu.navigator.navinfo.NavInfo;

public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {

    private String[] results;
    private final Consumer<String> searcher;

    public SearchAdapter(Consumer<String> searcher) {
        this.searcher = searcher;
        this.results = new String[0];
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creating view from XML
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_view, parent, false);
        return new SearchViewHolder(itemView, searcher);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        holder.populate(results[position]);
    }

    @Override
    public int getItemCount() {
        return results.length;
    }

    public void setResults(List<String> results) {
        this.results = results.toArray(results.toArray(new String[0]));
        notifyDataSetChanged();
    }
}
