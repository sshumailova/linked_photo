package com.sonya_shum.linkedphotoShSonya.Adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.sonya_shum.linkedphotoShSonya.R;
import com.sonya_shum.linkedphotoShSonya.databinding.AdminAdItemBinding;
import com.sonya_shum.linkedphotoShSonya.db.NewPost;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminAdapter extends ListAdapter<NewPost, AdminAdapter.AdsHolder> {
    private Listener listener;
    public AdminAdapter(Listener listener) {
        super(diffCallback);
        this.listener=listener;
    }

    @NonNull
    @Override
    public AdsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.admin_ad_item, parent, false);

        return new AdsHolder(view,listener);
    }

    @Override
    public void onBindViewHolder(@NonNull AdsHolder holder, int position) {
        holder.setData(getItem(position));
    }

    public static class AdsHolder extends RecyclerView.ViewHolder {
        AdminAdItemBinding binding;
        ImageAdapter adapter;
        List<String> images = new ArrayList<>();
        Listener listener;


        public AdsHolder(@NonNull View itemView, Listener listener) {
            super(itemView);
            binding = AdminAdItemBinding.bind(itemView);
            initViewPager();
            this.listener = listener;
        }

        private void initViewPager() {
            adapter = new ImageAdapter((Activity) binding.getRoot().getContext());
            binding.viewPager.setAdapter(adapter);
        }

        public void setData(NewPost newPost) {
            binding.tvTitle.setText(newPost.getName());
            binding.tvDisc.setText(newPost.getDisc());
            binding.bAccept.setOnClickListener(onClick(newPost, listener));
            binding.bDecline.setOnClickListener(onClick(newPost, listener));
            binding.bDelete.setOnClickListener(onClick(newPost, listener));
            images.clear();
            Log.d("MyLog", "Image 3" + newPost.getImageId3());
            if (!newPost.getImageId().equals("empty")) {
                images.add(newPost.getImageId());
            }
            if (!newPost.getImageId2().equals("empty")) {
                images.add(newPost.getImageId2());
            }
            if (!newPost.getImageId3().equals("empty")) {
                images.add(newPost.getImageId3());
            }
            adapter.updateImages(images);
            setImagesCounter();

        }

        private View.OnClickListener onClick(NewPost newPost, Listener listener) {
            return view -> {
                if (view.getId() == R.id.bAccept) {
                    listener.onAccept(newPost);
                } else if (view.getId() == R.id.bDecline) {
                    listener.onDecline(newPost);
                } else if (view.getId() == R.id.bDelete) {
                    listener.onDelete(newPost);

                }
            };
        }

        private void setImagesCounter() {
            String dataText = 1 + "/" + images.size();
            binding.tvImagedCounter.setText(dataText);
            binding.viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }

                @Override
                public void onPageSelected(int position) {
                    String dataText = position + 1 + "/" + images.size();
                    binding.tvImagedCounter.setText(dataText);
                }

                public void onPageScrollStateChanged(int state) {

                }
            });
        }
    }

    public static final DiffUtil.ItemCallback<NewPost> diffCallback = new DiffUtil.ItemCallback<NewPost>() {
        @Override
        public boolean areItemsTheSame(@NonNull NewPost oldItem, @NonNull NewPost newItem) {
            return oldItem.getKey().equals(newItem.getKey());
        }

        @Override
        public boolean areContentsTheSame(@NonNull NewPost oldItem, @NonNull NewPost newItem) {
            return Objects.equals(oldItem, newItem);
        }
    };

    public interface Listener {
        void onAccept(NewPost newPost);

        void onDecline(NewPost newPost);

        void onDelete(NewPost newPost);

    }
}
