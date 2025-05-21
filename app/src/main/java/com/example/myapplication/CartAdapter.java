package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.models.CartItem;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface OnCartItemChangedListener {
        void onQuantityChanged(CartItem item, int newQuantity);
        void onRemoveItem(CartItem item);
    }

    private Context context;
    private List<CartItem> cartItems;
    private OnCartItemChangedListener listener;

    public CartAdapter(Context context, List<CartItem> cartItems, OnCartItemChangedListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.tvName.setText(item.getName());
        holder.tvPrice.setText(String.format("%.2f €", item.getPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        holder.btnAdd.setOnClickListener(v -> {
            int newQty = item.getQuantity() + 1;
            listener.onQuantityChanged(item, newQty);
        });

        holder.btnRemove.setOnClickListener(v -> {
            int newQty = item.getQuantity() - 1;
            if (newQty > 0) {
                listener.onQuantityChanged(item, newQty);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            listener.onRemoveItem(item);
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvPrice, tvQuantity;
        Button btnAdd, btnRemove, btnDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnAdd = itemView.findViewById(R.id.btnAdd);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
