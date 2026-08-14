package com.example.vincera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkoutEntryAdapter(
    private val eintraege: MutableList<WorkoutEntryModel>,
    private val onDetails: (WorkoutEntryModel) -> Unit
) : RecyclerView.Adapter<WorkoutEntryAdapter.EntryViewHolder>() {

    class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvEntryDatum: TextView = view.findViewById(R.id.tvEntryDatum)
        val tvEntryPlanUebung: TextView = view.findViewById(R.id.tvEntryPlanUebung)
        val btnEntryDetails: Button = view.findViewById(R.id.btnEntryDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_entry, parent, false)
        return EntryViewHolder(view)
    }

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val eintrag = eintraege[position]
        val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY)

        holder.tvEntryDatum.text = sdf.format(Date(eintrag.timestamp))
        holder.tvEntryPlanUebung.text = "${eintrag.plan} - ${eintrag.uebung}"
        holder.btnEntryDetails.setOnClickListener { onDetails(eintrag) }
    }

    override fun getItemCount(): Int = eintraege.size

    fun updateData(newList: List<WorkoutEntryModel>) {
        eintraege.clear()
        eintraege.addAll(newList)
        notifyDataSetChanged()
    }
}