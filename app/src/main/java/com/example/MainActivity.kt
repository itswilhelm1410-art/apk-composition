package com.example

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.Locale

/**
 * Activité principale gérant les opérations CRUD sur les étudiants via SQLite,
 * l'affichage dans un RecyclerView, ainsi que la recherche, les filtres et les statistiques.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var adapter: EtudiantAdapter

    private lateinit var edtNom: EditText
    private lateinit var edtPrenom: EditText
    private lateinit var edtMoyenne: EditText
    private lateinit var txtId: TextView
    private lateinit var txtListeVide: TextView
    private lateinit var recyclerView: RecyclerView

    // Statistiques
    private lateinit var txtTotalEtudiants: TextView
    private lateinit var txtMoyenneGenerale: TextView
    private lateinit var txtTauxAdmis: TextView
    private lateinit var txtNbAjournes: TextView

    // Recherche et filtres
    private lateinit var edtRecherche: EditText
    private lateinit var chipGroupFiltres: ChipGroup

    private var idSelectionne = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialisation de la base SQLite
        dbHelper = DatabaseHelper(this)

        // Récupération des composants graphiques
        edtNom = findViewById(R.id.edtNom)
        edtPrenom = findViewById(R.id.edtPrenom)
        edtMoyenne = findViewById(R.id.edtMoyenne)
        txtId = findViewById(R.id.txtId)
        txtListeVide = findViewById(R.id.txtListeVide)
        recyclerView = findViewById(R.id.recyclerView)

        val btnAjouter = findViewById<Button>(R.id.btnAjouter)
        val btnModifier = findViewById<Button>(R.id.btnModifier)
        val btnSupprimer = findViewById<Button>(R.id.btnSupprimer)
        val btnVider = findViewById<Button>(R.id.btnVider)

        txtTotalEtudiants = findViewById(R.id.txtTotalEtudiants)
        txtMoyenneGenerale = findViewById(R.id.txtMoyenneGenerale)
        txtTauxAdmis = findViewById(R.id.txtTauxAdmis)
        txtNbAjournes = findViewById(R.id.txtNbAjournes)

        edtRecherche = findViewById(R.id.edtRecherche)
        chipGroupFiltres = findViewById(R.id.chipGroupFiltres)

        // Configuration du RecyclerView
        adapter = EtudiantAdapter(dbHelper.getEtudiants()) { etudiant ->
            // Clic sur un étudiant : chargement de ses données dans le formulaire
            idSelectionne = etudiant.id
            edtNom.setText(etudiant.nom)
            edtPrenom.setText(etudiant.prenom)
            edtMoyenne.setText(etudiant.moyenne.toString())
            txtId.text = "ID sélectionné : ${etudiant.id}"
            Toast.makeText(
                this,
                "Étudiant sélectionné : ${etudiant.prenom} ${etudiant.nom}",
                Toast.LENGTH_SHORT
            ).show()
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Écouteurs d'événements pour les boutons CRUD
        btnAjouter.setOnClickListener {
            ajouterEtudiant()
        }

        btnModifier.setOnClickListener {
            modifierEtudiant()
        }

        btnSupprimer.setOnClickListener {
            supprimerEtudiant()
        }

        btnVider.setOnClickListener {
            viderChamps()
        }

        // Recherche en direct
        edtRecherche.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                actualiserListe()
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Filtres et tris
        chipGroupFiltres.setOnCheckedStateChangeListener { _, _ ->
            actualiserListe()
        }

        // Mise à jour initiale des statistiques et de la liste
        actualiserStats()
        verifierListeVide(adapter.itemCount)
    }

    // --- OPÉRATION 1 : AJOUTER UN ÉTUDIANT ---
    private fun ajouterEtudiant() {
        val nom = edtNom.text.toString().trim()
        val prenom = edtPrenom.text.toString().trim()
        val moyenneText = edtMoyenne.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || moyenneText.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val moyenne: Double
        try {
            moyenne = moyenneText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Format de moyenne invalide", Toast.LENGTH_SHORT).show()
            return
        }

        if (moyenne < 0 || moyenne > 20) {
            Toast.makeText(this, "La moyenne doit être entre 0 et 20", Toast.LENGTH_SHORT).show()
            return
        }

        val resultat = dbHelper.ajouter(nom, prenom, moyenne)
        if (resultat != -1L) {
            Toast.makeText(this, "Étudiant ajouté avec succès", Toast.LENGTH_SHORT).show()
            viderChamps()
            actualiserListe()
        } else {
            Toast.makeText(this, "Erreur lors de l'ajout", Toast.LENGTH_SHORT).show()
        }
    }

    // --- OPÉRATION 2 : MODIFIER UN ÉTUDIANT ---
    private fun modifierEtudiant() {
        if (idSelectionne == -1) {
            Toast.makeText(
                this,
                "Veuillez d'abord sélectionner un étudiant dans la liste",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nom = edtNom.text.toString().trim()
        val prenom = edtPrenom.text.toString().trim()
        val moyenneText = edtMoyenne.text.toString().trim()

        if (nom.isEmpty() || prenom.isEmpty() || moyenneText.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show()
            return
        }

        val moyenne: Double
        try {
            moyenne = moyenneText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Format de moyenne invalide", Toast.LENGTH_SHORT).show()
            return
        }

        if (moyenne < 0 || moyenne > 20) {
            Toast.makeText(this, "La moyenne doit être entre 0 et 20", Toast.LENGTH_SHORT).show()
            return
        }

        val rowsAffected = dbHelper.modifier(idSelectionne, nom, prenom, moyenne)
        if (rowsAffected > 0) {
            Toast.makeText(this, "Étudiant modifié avec succès", Toast.LENGTH_SHORT).show()
            viderChamps()
            actualiserListe()
        } else {
            Toast.makeText(this, "Erreur lors de la modification", Toast.LENGTH_SHORT).show()
        }
    }

    // --- OPÉRATION 3 : SUPPRIMER UN ÉTUDIANT (AVEC CONFIRMATION) ---
    private fun supprimerEtudiant() {
        if (idSelectionne == -1) {
            Toast.makeText(
                this,
                "Veuillez d'abord sélectionner un étudiant dans la liste",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val nom = edtNom.text.toString().trim()
        val prenom = edtPrenom.text.toString().trim()

        // Boîte de dialogue de confirmation (Extension suggérée)
        MaterialAlertDialogBuilder(this)
            .setTitle("Confirmation de suppression")
            .setMessage("Voulez-vous vraiment supprimer l'étudiant $prenom $nom (ID: $idSelectionne) ?")
            .setPositiveButton("Supprimer") { _, _ ->
                val rowsDeleted = dbHelper.supprimer(idSelectionne)
                if (rowsDeleted > 0) {
                    Toast.makeText(this, "Étudiant supprimé avec succès", Toast.LENGTH_SHORT).show()
                    viderChamps()
                    actualiserListe()
                } else {
                    Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    // --- ACTUALISER LA LISTE EN FONCTION DE LA RECHERCHE ET DES FILTRES ---
    private fun actualiserListe() {
        val query = edtRecherche.text.toString().trim()
        val baseListe = if (query.isNotEmpty()) {
            dbHelper.rechercher(query)
        } else {
            dbHelper.getEtudiants()
        }

        val checkedChipId = chipGroupFiltres.checkedChipId
        val listeFiltree = when (checkedChipId) {
            R.id.chipAdmis -> ArrayList(baseListe.filter { it.isAdmis })
            R.id.chipAjournes -> ArrayList(baseListe.filter { !it.isAdmis })
            R.id.chipTriMoyenne -> ArrayList(baseListe.sortedByDescending { it.moyenne })
            R.id.chipTriNom -> ArrayList(baseListe.sortedBy { it.nom.lowercase() })
            else -> baseListe
        }

        adapter.updateList(listeFiltree)
        verifierListeVide(listeFiltree.size)
        actualiserStats()
    }

    // --- MISE À JOUR DES STATISTIQUES GLOBALES ---
    private fun actualiserStats() {
        val (total, admis, moyenneGen) = dbHelper.getStatistiques()
        val ajournes = total - admis

        txtTotalEtudiants.text = total.toString()
        txtMoyenneGenerale.text = String.format(Locale.getDefault(), "%.2f", moyenneGen)
        txtTauxAdmis.text = "$admis Admis"
        txtNbAjournes.text = "$ajournes Ajourné${if (ajournes > 1) "s" else ""}"
    }

    private fun verifierListeVide(nbElements: Int) {
        if (nbElements == 0) {
            txtListeVide.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            txtListeVide.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }

    // --- VIDER LES CHAMPS DU FORMULAIRE ---
    private fun viderChamps() {
        edtNom.text.clear()
        edtPrenom.text.clear()
        edtMoyenne.text.clear()
        idSelectionne = -1
        txtId.text = "ID sélectionné : aucun"
        edtNom.requestFocus()
    }
}
