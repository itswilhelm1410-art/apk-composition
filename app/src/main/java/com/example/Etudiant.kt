package com.example

/**
 * Modèle de données représentant un étudiant.
 * Contient id, nom, prenom et moyenne, ainsi que des helpers
 * pour le statut d'admission et la mention.
 */
data class Etudiant(
    val id: Int,
    var nom: String,
    var prenom: String,
    var moyenne: Double
) {
    val isAdmis: Boolean
        get() = moyenne >= 10.0

    val statut: String
        get() = if (isAdmis) "Admis" else "Ajourné"

    val mention: String
        get() = when {
            moyenne >= 16.0 -> "Très Bien"
            moyenne >= 14.0 -> "Bien"
            moyenne >= 12.0 -> "Assez Bien"
            moyenne >= 10.0 -> "Passable"
            moyenne >= 8.0 -> "Rattrapage possible"
            else -> "Ajourné"
        }

    val nomComplet: String
        get() = "${nom.uppercase()} $prenom"
}
