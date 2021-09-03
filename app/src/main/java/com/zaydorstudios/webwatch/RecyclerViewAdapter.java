package com.zaydorstudios.webwatch;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Stack;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    //vars
    private Stack<String> mURLs;
    private Stack<String> mIDs;
    private Context mContext;
    private boolean mIsDarkThemeOn;
    private boolean[] mDidSiteChange;

    public RecyclerViewAdapter(Context context, Stack<String> URLs, Stack<String> IDs, boolean[] didSiteChange, boolean isDarkThemeOn) {
        mURLs = URLs;
        mIDs = IDs;
        mContext = context;
        mIsDarkThemeOn = isDarkThemeOn;
        mDidSiteChange = didSiteChange;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");
        System.out.println("onBindViewHolder");
        holder.URLText.setText(mURLs.get(position));
        holder.IDText.setText(mIDs.get(position));

        if (mDidSiteChange[position]) {
            holder.didChangeMark.setImageResource(R.drawable.ic_baseline_check_24);
        } else {
            holder.didChangeMark.setImageResource(R.drawable.ic_baseline_cross_24);
        }

        if (mIsDarkThemeOn) {
            //holder.URLText.setTextColor(mContext.getResources().getColor(R.color.pastel_green, null));
            //holder.IDText.setTextColor(mContext.getResources().getColor(R.color.pastel_green, null));
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.rect_night_background, null));
        } else {
            //holder.URLText.setTextColor(mContext.getResources().getColor(R.color.pastel_blue, null));
            //holder.IDText.setTextColor(mContext.getResources().getColor(R.color.pastel_blue, null));
            holder.cardView.setCardBackgroundColor(mContext.getResources().getColor(R.color.colorTransparent, null));
        }

    }

    @Override
    public int getItemCount() {
        return mURLs.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView URLText;
        TextView IDText;
        CardView cardView;
        ImageView didChangeMark;

        public ViewHolder(View itemView) {
            super(itemView);
            URLText = itemView.findViewById(R.id.URLText_view);
            IDText = itemView.findViewById(R.id.IDText_view);
            cardView = itemView.findViewById(R.id.ItemCardView);
            didChangeMark = itemView.findViewById(R.id.didChangeMark);
            itemView.setOnClickListener(v -> {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse((String) URLText.getText()));
                mContext.startActivity(browserIntent);
            });
        }
    }
}
