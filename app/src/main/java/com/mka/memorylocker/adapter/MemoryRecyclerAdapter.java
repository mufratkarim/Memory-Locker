package com.mka.memorylocker.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.mka.memorylocker.R;
import com.mka.memorylocker.Util.MemoryListener;
import com.mka.memorylocker.model.Memory;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MemoryRecyclerAdapter extends RecyclerView.Adapter<MemoryRecyclerAdapter.MemoryViewHolder> {

    private Context context;
    private List<Memory> memories;
    private final MemoryListener memoryListener;

    public MemoryRecyclerAdapter(Context context, List<Memory> memories, MemoryListener memoryListener) {
        this.context = context;
        this.memories = memories;
        this.memoryListener = memoryListener;
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context.getApplicationContext())
                .inflate(R.layout.memory_row, parent, false);

        return new MemoryViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {

        Memory newMemory = memories.get(position);
        holder.title.setText(newMemory.getTitle());
        holder.memory.setText(newMemory.getMemory());
        holder.name.setText(newMemory.getUserName());

        // Time ago Source--- https://medium.com/@shaktisinh/time-a-go-in-android-8bad8b171f87
        String timeAgo = (String) DateUtils
                .getRelativeTimeSpanString(newMemory.getTimeAdded().getSeconds() * 1000);
        holder.date.setText(timeAgo);

        Picasso.get().load(newMemory.getImageUrl())
                .placeholder(android.R.drawable.stat_sys_download)
                .error(android.R.drawable.stat_notify_error)
                .fit()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return memories.size();
    }

    public class MemoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView title, memory, date, name;
        private ImageView imageView;
        private ImageButton shareButton;
        private MemoryListener listener;

        public MemoryViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);

            context = ctx;
            title = itemView.findViewById(R.id.recycler_title);
            memory = itemView.findViewById(R.id.recycler_description);
            date = itemView.findViewById(R.id.recycler_date);
            imageView = itemView.findViewById(R.id.recycler_imageView);
            name = itemView.findViewById(R.id.recycler_name);
            shareButton = itemView.findViewById(R.id.recycler_shareButton);

            itemView.setOnClickListener(this);
            listener = memoryListener;

            shareButton.setOnClickListener(view -> {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, title.getText().toString());
                shareIntent.putExtra(Intent.EXTRA_TEXT, memories.get(getAdapterPosition()).toString());
                context.startActivity(Intent.createChooser(shareIntent, "Share it by "));
            });



        }

        @Override
        public void onClick(View view) {
            Memory clickedMemory = memories.get(getAdapterPosition());
            listener.memoryClicked(clickedMemory);
        }
    }
}
