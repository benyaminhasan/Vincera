package com.example.vincera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TrainingsplanAdapter(
    private val plaene: MutableList<TrainingsplanModel>,
    private val onEdit: (TrainingsplanModel) -> Unit,
    private val onDelete: (TrainingsplanModel) -> Unit
) : RecyclerView.Adapter<TrainingsplanAdapter.PlanViewHolder>() {

    class PlanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPlanName: TextView = view.findViewById(R.id.tvPlanName)
        val tvPlanUebungen: TextView = view.findViewById(R.id.tvPlanUebungen)
        val btnEditPlan: Button = view.findViewById(R.id.btnEditPlan)
        val btnDeletePlan: Button = view.findViewById(R.id.btnDeletePlan)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trainingsplan, parent, false)
        return PlanViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val plan = plaene[position]
        holder.tvPlanName.text = plan.name
        holder.tvPlanUebungen.text = plan.uebungen

        holder.btnEditPlan.setOnClickListener { onEdit(plan) }
        holder.btnDeletePlan.setOnClickListener { onDelete(plan) }
    }

    override fun getItemCount(): Int = plaene.size

    fun updateData(newList: List<TrainingsplanModel>) {
        plaene.clear()
        plaene.addAll(newList)
        notifyDataSetChanged()
    }
}