package se.ltu.navigator.navinfo;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Observable;
import java.util.Observer;

import se.ltu.navigator.R;

public class NavInfoViewHolder extends RecyclerView.ViewHolder implements Observer {
    private final TextView content;
    private final TextView title;
    private NavInfo lastNavInfo;

    public NavInfoViewHolder(View itemView) {
        super(itemView);

        content = itemView.findViewById(R.id.content);
        title = itemView.findViewById(R.id.title);
    }

    public void populate(NavInfo navInfo) {
        if (lastNavInfo != null) lastNavInfo.unregisterListener(this);
        lastNavInfo = navInfo;
        lastNavInfo.registerListener(this);

        content.setText(navInfo.getData());
        title.setText(navInfo.getTitle());
    }

    @Override
    public void update(Observable o, Object arg) {
        content.setText((String) arg);
    }
}
