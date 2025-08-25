package com.example.zakazivanjeaplikacija.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazivanjeaplikacija.model.ServiceResponseDTO
import com.example.zakazivanjeaplikacija.R
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

//adapte za prikaz dostupnih usluga

class ServiceAdapter(
    private val services: MutableList<ServiceResponseDTO>,
    private val onAddClickListener: (ServiceResponseDTO) -> Unit
) : RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {


    class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceName: TextView = itemView.findViewById(R.id.textViewServiceName)
        val servicePrice: TextView = itemView.findViewById(R.id.textViewServicePrice)
        val serviceDescription: TextView = itemView.findViewById(R.id.textViewServiceDescription)
        val addButton: Button = itemView.findViewById(R.id.buttonAddService)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_available, parent, false)
        return ServiceViewHolder(view)
    }

    // Povezuje podatke sa ViewHolder-om.
    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.serviceName.text = service.name
        holder.serviceDescription.text = service.description


        val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        holder.servicePrice.text = formatter.format(service.price).replace("€", "RSD")

        //listener
        holder.addButton.setOnClickListener {
            onAddClickListener(service)
        }
    }


    override fun getItemCount(): Int {
        return services.size
    }


    fun updateServices(newServices: List<ServiceResponseDTO>) {
        services.clear()
        services.addAll(newServices)
        notifyDataSetChanged()
    }
}
