package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.myapplication.models.Product;

import java.util.List;

public class AdapterProduct extends RecyclerView.Adapter<AdapterProduct.ProductViewHolder> {

    public interface OnAddToCartClickListener {
        void onAddToCartClick(Product product);
    }

    private Context context;
    private List<Product> productList;
    private OnAddToCartClickListener listener;

    public AdapterProduct(Context context, List<Product> productList, OnAddToCartClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_clients, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product p = productList.get(position);

        holder.tvName.setText(p.getName());
        holder.tvPrice.setText(String.format("%.2f MAD", p.getPrice()));
        holder.tvBrand.setText("Marque : " + p.getBrand());
        holder.tvBottleSize.setText("Volume : " + p.getBottleSize());

        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
            ImageSliderAdapter sliderAdapter = new ImageSliderAdapter(context, p.getImageUrls());
            holder.viewPagerImages.setAdapter(sliderAdapter);

            // Animation douce lors du défilement
            holder.viewPagerImages.setPageTransformer((page, position1) -> {
                page.setAlpha(0.25f + (1 - Math.abs(position1)));
                page.setScaleY(0.85f + (1 - Math.abs(position1)) * 0.15f);
            });
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, ProductDetailActivity.class);
                intent.putExtra("product", p);
                context.startActivity(intent);
            });


            // Auto-scroll
            Runnable autoScrollRunnable = new Runnable() {
                int currentPage = 0;

                @Override
                public void run() {
                    if (currentPage >= p.getImageUrls().size()) currentPage = 0;
                    holder.viewPagerImages.setCurrentItem(currentPage++, true);
                    holder.viewPagerImages.postDelayed(this, 3000);
                }
            };
            holder.viewPagerImages.post(autoScrollRunnable);
        } else {
            holder.viewPagerImages.setAdapter(null); // Si aucune image
        }

        holder.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ViewPager2 viewPagerImages;
        TextView tvName, tvPrice, tvBrand, tvBottleSize;
        Button btnAddToCart;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            viewPagerImages = itemView.findViewById(R.id.viewPagerImages);
            tvName = itemView.findViewById(R.id.tvProductName);
            tvPrice = itemView.findViewById(R.id.tvProductPrice);
            tvBrand = itemView.findViewById(R.id.tvProductBrand);
            tvBottleSize = itemView.findViewById(R.id.tvProductBottleSize);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
        }
    }

}
