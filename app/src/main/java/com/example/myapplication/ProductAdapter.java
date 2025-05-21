package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.myapplication.models.Product;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private Context context;
    private List<Product> productList;
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(Product product);
        void onDelete(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnItemActionListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.tvName.setText("🛍️ " + product.getName());
        holder.tvBrand.setText("🏷️ " + product.getBrand());
        holder.tvPrice.setText("💰 " + product.getPrice() + " DH");

        List<String> imageUrls = product.getImageUrls();

        RequestOptions requestOptions = new RequestOptions().transform(new RoundedCorners(20));

        if (imageUrls != null && imageUrls.size() > 0) {
            Glide.with(context)
                    .load(imageUrls.get(0))
                    .apply(requestOptions)
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.image1);
        } else {
            holder.image1.setImageResource(android.R.color.darker_gray);
        }

        if (imageUrls != null && imageUrls.size() > 1) {
            Glide.with(context)
                    .load(imageUrls.get(1))
                    .apply(requestOptions)
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.image2);
        } else {
            holder.image2.setImageResource(android.R.color.darker_gray);
        }

        if (imageUrls != null && imageUrls.size() > 2) {
            Glide.with(context)
                    .load(imageUrls.get(2))
                    .apply(requestOptions)
                    .placeholder(android.R.color.darker_gray)
                    .into(holder.image3);
        } else {
            holder.image3.setImageResource(android.R.color.darker_gray);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(product));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(product));
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public void updateList(List<Product> newList) {
        productList = newList;
        notifyDataSetChanged();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBrand, tvPrice;
        ImageView image1, image2, image3;
        Button btnEdit, btnDelete;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvBrand = itemView.findViewById(R.id.tvBrand);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            image1 = itemView.findViewById(R.id.image1);
            image2 = itemView.findViewById(R.id.image2);
            image3 = itemView.findViewById(R.id.image3);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
