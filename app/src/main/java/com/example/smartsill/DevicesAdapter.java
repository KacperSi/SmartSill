package com.example.smartsill;

import android.graphics.ColorSpace;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class DevicesAdapter extends RecyclerView.Adapter<DevicesAdapter.ViewHolder> {

    private List<Device> devices;
    private SelectListener listener;

    public DevicesAdapter (List<Device> devices, SelectListener listener){
        this.devices = devices;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_design,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        String name = devices.get(position).getName();
        String ip = devices.get(position).getIp();

        holder.setData(name, ip);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onItemClicked(devices.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {

        private TextView devNameTextView;
        private TextView ipTextView;
        public RelativeLayout cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            devNameTextView = itemView.findViewById(R.id.devNameTextView);
            ipTextView = itemView.findViewById(R.id.ipTextView);
            cardView = itemView.findViewById(R.id.deviceCard);

            cardView.setOnLongClickListener(this);
        }

        @Override
        public boolean onLongClick(View view) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // Pobierz element do usunięcia
                Device deviceToRemove = devices.get(position);

                // Usuń element z listy
                devices.remove(deviceToRemove);

                // Powiadom adapter o usunięciu elementu
                notifyDataSetChanged();

                // Możesz dodać dodatkową logikę, np. wyświetlenie potwierdzenia

                return true; // Zwróć true, aby wskazać, że zdarzenie zostało obsłużone
            }
            return false;
        }

        public void setData(String name, String ip) {
            devNameTextView.setText(name);
            ipTextView.setText(ip);
        }
    }
}
