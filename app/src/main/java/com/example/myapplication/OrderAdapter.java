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

import com.example.myapplication.models.Order;

import java.util.ArrayList;
import java.util.List;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private List<Order> fullOrderList; // Liste complète pour le filtrage

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = new ArrayList<>(orderList);
        this.fullOrderList = new ArrayList<>(orderList);
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        String fullName = order.getFirstName() + " " + order.getLastName();

        holder.textOrderName.setText("👤 " + fullName);
        holder.textOrderPhone.setText("📞 " + order.getPhone());
        holder.textOrderCity.setText("🏙️ " + order.getCity());
        holder.textOrderAddress.setText("🏠 " + order.getAddress());
        holder.textOrderTotal.setText("💰 " + String.format("%.2f DH", order.getTotal()));
        holder.textOrderStatus.setText("📦 " + order.getStatus());

        // Clic sur bouton détails
        holder.btnViewDetails.setOnClickListener(v -> openOrderDetails(order));

        // Clic sur toute la ligne
        holder.itemView.setOnClickListener(v -> openOrderDetails(order));
    }

    private void openOrderDetails(Order order) {
        Intent intent = new Intent(context, DetailOrderActivity.class);
        intent.putExtra("order", order);
        context.startActivity(intent);
    }


    @Override
    public int getItemCount() {
        return orderList.size();
    }

    /**
     * Filtre la liste des commandes par statut
     * @param status statut à filtrer (ex: "Validée", "En traitement", etc.)
     */
    public void filterByStatus(String status) {
        if (status == null || status.isEmpty()) {
            // Restaure la liste complète
            orderList = new ArrayList<>(fullOrderList);
        } else {
            List<Order> filtered = new ArrayList<>();
            for (Order order : fullOrderList) {
                if (order.getStatus() != null && order.getStatus().equalsIgnoreCase(status)) {
                    filtered.add(order);
                }
            }
            orderList = filtered;
        }
        notifyDataSetChanged();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView textOrderName, textOrderPhone, textOrderCity, textOrderAddress, textOrderTotal, textOrderStatus;
        Button btnViewDetails;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderName = itemView.findViewById(R.id.textOrderName);
            textOrderPhone = itemView.findViewById(R.id.textOrderPhone);
            textOrderCity = itemView.findViewById(R.id.textOrderCity);
            textOrderAddress = itemView.findViewById(R.id.textOrderAddress);
            textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
        }
    }
}
