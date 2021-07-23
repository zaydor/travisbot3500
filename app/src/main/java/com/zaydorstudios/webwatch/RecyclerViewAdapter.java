package com.zaydorstudios.webwatch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Stack;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    //vars
    private Stack<String> mURLs;
    private Stack<String> mIDs;
    private Context mContext;
    private boolean mIsDarkThemeOn;

    public RecyclerViewAdapter(Context context, Stack<String> URLs, Stack<String> IDs, boolean isDarkThemeOn) {
        mURLs = URLs;
        mIDs = IDs;
        mContext = context;
        mIsDarkThemeOn = isDarkThemeOn;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called.");

        holder.URLText.setText(mURLs.get(position));
        holder.IDText.setText(mIDs.get(position));

        if (mIsDarkThemeOn) {
            //holder.URLText.setTextColor(mContext.getResources().getColor(R.color.pastel_green, null));
            //holder.IDText.setTextColor(mContext.getResources().getColor(R.color.pastel_green, null));
            holder.SmallRectangle.setImageResource(R.drawable.ic_rectangle_updated_small_darkmode);
        } else {
            //holder.URLText.setTextColor(mContext.getResources().getColor(R.color.pastel_blue, null));
            //holder.IDText.setTextColor(mContext.getResources().getColor(R.color.pastel_blue, null));
            holder.SmallRectangle.setImageResource(R.drawable.ic_rectangle_updated_small);
        }
    }

    @Override
    public int getItemCount() {
        return mURLs.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView URLText;
        TextView IDText;
        ImageView SmallRectangle;

        public ViewHolder(View itemView) {
            super(itemView);
            URLText = itemView.findViewById(R.id.URLText_view);
            IDText = itemView.findViewById(R.id.IDText_view);
            SmallRectangle = itemView.findViewById(R.id.SmallRectangle);
        }
    }
}
