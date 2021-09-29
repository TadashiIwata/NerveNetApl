package jp.co.nassua.nervenet.groupchatmain;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import jp.co.nassua.nervenet.voicemessage.R;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.GroupViewHolder> {

    private ArrayList<String> mData = new ArrayList<>();
    private final Object lock = new Object();

    protected void onRecyclerClicked(@NonNull int poisition) {
    }
    protected void onButtonClicked(int position) {
    }
    protected void onRecyclerLongClicked(@NonNull int poisition) {
    }

    @Override
    public GroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.group_name, parent, false);
        final GroupViewHolder groupViewHolder = new GroupViewHolder(view);
        // グループ名短押し
        groupViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = groupViewHolder.getAdapterPosition();
                //final String name = mData.get(position);
                onRecyclerClicked(position);
            }
        });
        // ボタンクリック
        groupViewHolder.itemView.<View>findViewById(R.id.group_list_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final int position = groupViewHolder.getAdapterPosition();
                onButtonClicked(position);
            }
        });
        // グループ名長押し
        groupViewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final int position = groupViewHolder.getAdapterPosition();
                onRecyclerLongClicked(position);
                return true;
            }
        });
        return groupViewHolder;
    }

    @Override
    public void onBindViewHolder(GroupViewHolder holder, final int position) {

        holder.textView.setText(mData.get(position));
        if (position == 0) {
            holder.imageButton.setClickable(false);
            holder.imageButton.setVisibility(View.INVISIBLE);
        } else {
            holder.imageButton.setClickable(true);
            holder.imageButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        if (mData != null) {
            return mData.size();
        } else {
            return 0;
        }
    }

    public void add(@NonNull String name) {
        final int position;
        synchronized (lock) {
            position = mData.size();
            mData.add(name);
        }
        notifyItemInserted(position);
    }

    public String get(int idx) {
        return mData.get(idx);
    }

    public void remove(int idx) {
        mData.remove(idx);
    }

    public void clear() {
        mData.clear();
    }

    public static class GroupViewHolder extends RecyclerView.ViewHolder {

        static TextView textView;
        static ImageButton imageButton;
        //public static View itemView;

        static GroupViewHolder create(@Nullable LayoutInflater inflater, ViewGroup parent) {
            return new GroupViewHolder((inflater.inflate(R.layout.group_name, parent,false)));
        }

        public GroupViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.group_list_name);
            imageButton = (ImageButton) itemView.findViewById(R.id.group_list_button);
        }

    }
}
