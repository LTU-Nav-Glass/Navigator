package se.ltu.navigator.navinfo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import se.ltu.navigator.R;

public class NavInfoAdapter extends RecyclerView.Adapter<NavInfoViewHolder> {

    @NonNull
    @Override
    public NavInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Creating view from XML
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.nav_info_view, parent, false);
        return new NavInfoViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NavInfoViewHolder holder, int position) {
        holder.populate(NavInfo.values()[position]);
    }

    @Override
    public int getItemCount() {
        return NavInfo.values().length;
    }
}
