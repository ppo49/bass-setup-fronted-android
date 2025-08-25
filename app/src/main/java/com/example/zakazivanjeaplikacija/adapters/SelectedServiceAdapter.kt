package com.example.zakazivanjeaplikacija.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.zakazivanjeaplikacija.R
import com.example.zakazivanjeaplikacija.model.ServiceResponseDTO
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

// adapter za prikaz odabranih servisa u recycleview
class SelectedServiceAdapter(
    private val selectedServices: MutableList<ServiceResponseDTO>,
    private val onRemoveClickListener: (ServiceResponseDTO) -> Unit
) : RecyclerView.Adapter<SelectedServiceAdapter.SelectedServiceViewHolder>() {

    class SelectedServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceName: TextView = itemView.findViewById(R.id.textViewSelectedServiceName)
        val servicePrice: TextView = itemView.findViewById(R.id.textViewSelectedServicePrice)
        val removeButton: Button = itemView.findViewById(R.id.buttonRemoveService)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_selected, parent, false)
        return SelectedServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: SelectedServiceViewHolder, position: Int) {
        val service = selectedServices[position]
        holder.serviceName.text = service.name

        // Formatiranje cene
        val formatter = NumberFormat.getCurrencyInstance(Locale.GERMANY)
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        holder.servicePrice.text = formatter.format(service.price).replace("€", "RSD")

        holder.removeButton.setOnClickListener {
            onRemoveClickListener(service)
        }
    }

    override fun getItemCount(): Int {
        return selectedServices.size
    }


    fun addService(service: ServiceResponseDTO) {
        selectedServices.add(service)
        notifyItemInserted(selectedServices.size - 1)
    }

    fun removeService(service: ServiceResponseDTO) {
        val index = selectedServices.indexOf(service)
        if (index != -1) {
            selectedServices.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun getSelectedServices(): List<ServiceResponseDTO> {
        return selectedServices.toList()
    }
}
