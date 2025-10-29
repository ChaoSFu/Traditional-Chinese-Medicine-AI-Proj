package com.example.traditional_chinese_medicine_ai_proj.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.traditional_chinese_medicine_ai_proj.R
import com.example.traditional_chinese_medicine_ai_proj.data.Doctor

class DoctorListAdapter(
    private val doctors: List<Doctor>,
    private val onDoctorClick: (Doctor) -> Unit
) : RecyclerView.Adapter<DoctorListAdapter.DoctorViewHolder>() {

    inner class DoctorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDept: TextView = view.findViewById(R.id.tvDept)
        val tvSpecialty: TextView = view.findViewById(R.id.tvSpecialty)
        val tvYears: TextView = view.findViewById(R.id.tvYears)
        val tvRating: TextView = view.findViewById(R.id.tvRating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_doctor, parent, false)
        return DoctorViewHolder(view)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        val doctor = doctors[position]

        holder.tvName.text = doctor.name
        holder.tvTitle.text = doctor.title
        holder.tvDept.text = doctor.dept
        holder.tvSpecialty.text = "擅长：${doctor.specialty}"
        holder.tvYears.text = "${doctor.years}年经验"
        holder.tvRating.text = doctor.rating.toString()

        // 设置默认头像
        holder.ivAvatar.setImageResource(R.drawable.ic_doctor_avatar)

        holder.itemView.setOnClickListener {
            onDoctorClick(doctor)
        }
    }

    override fun getItemCount() = doctors.size
}
