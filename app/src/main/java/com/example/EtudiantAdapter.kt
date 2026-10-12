package com.example

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Adaptateur RecyclerView pour l'affichage de la liste des étudiants.
 */
class EtudiantAdapter(
    private var liste: ArrayList<Etudiant>,
    private val onClick: (Etudiant) -> Unit
) : RecyclerView.Adapter<EtudiantAdapter.EtudiantViewHolder>() {

    class EtudiantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nom: TextView = itemView.findViewById(R.id.txtNom)
        val prenom: TextView = itemView.findViewById(R.id.txtPrenom)
        val moyenne: TextView = itemView.findViewById(R.id.txtMoyenne)
        val avatar: TextView = itemView.findViewById(R.id.txtAvatar)
        val idBadge: TextView = itemView.findViewById(R.id.txtIdBadge)
        val statusBadge: TextView = itemView.findViewById(R.id.txtStatusBadge)
        val mention: TextView = itemView.findViewById(R.id.txtMention)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EtudiantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_etudiant, parent, false)
        return EtudiantViewHolder(view)
    }

    override fun onBindViewHolder(holder: EtudiantViewHolder, position: Int) {
        val etudiant = liste[position]

        // Champs exigés dans le TP
        holder.nom.text = "Nom : ${etudiant.nom}"
        holder.prenom.text = "Prénom : ${etudiant.prenom}"
        holder.moyenne.text = "Moyenne : ${etudiant.moyenne} / 20"

        // Éléments visuels additionnels
        val initiale = if (etudiant.nom.isNotBlank()) etudiant.nom.first().uppercase() else "?"
        holder.avatar.text = initiale
        holder.idBadge.text = "#${etudiant.id}"

        // Badge Admis / Ajourné + Mention
        if (etudiant.isAdmis) {
            holder.statusBadge.text = "Admis"
            holder.statusBadge.setBackgroundResource(R.drawable.bg_badge_admis)
            holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.admis_green))
        } else {
            holder.statusBadge.text = "Ajourné"
            holder.statusBadge.setBackgroundResource(R.drawable.bg_badge_ajourne)
            holder.statusBadge.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.ajourne_red))
        }
        holder.mention.text = etudiant.mention

        // Clic sur l'élément pour charger dans le formulaire
        holder.itemView.setOnClickListener {
            onClick(etudiant)
        }
    }

    override fun getItemCount(): Int = liste.size

    fun updateList(nouvelleListe: ArrayList<Etudiant>) {
        liste = nouvelleListe
        notifyDataSetChanged()
    }
}
