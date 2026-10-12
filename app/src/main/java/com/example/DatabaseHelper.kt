package com.example

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Classe de gestion de la base de données SQLite pour les étudiants.
 * Gère la création, la mise à jour et toutes les opérations CRUD.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "Etudiants.db"
        const val DATABASE_VERSION = 1

        const val TABLE_ETUDIANT = "etudiant"
        const val COL_ID = "id"
        const val COL_NOM = "nom"
        const val COL_PRENOM = "prenom"
        const val COL_MOYENNE = "moyenne"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE $TABLE_ETUDIANT (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NOM TEXT NOT NULL,
                $COL_PRENOM TEXT NOT NULL,
                $COL_MOYENNE REAL NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableSql)

        // Données d'exemple initiales comme dans le cours (ADJOVI Jean, AHOUANSOU Paul, SOGLO Marie)
        insererDonneesExemple(db)
    }

    private fun insererDonneesExemple(db: SQLiteDatabase) {
        val exemples = listOf(
            Triple("ADJOVI", "Jean", 14.5),
            Triple("AHOUANSOU", "Paul", 12.0),
            Triple("SOGLO", "Marie", 16.5),
            Triple("KOFFI", "Marc", 8.5)
        )
        for ((nom, prenom, moyenne) in exemples) {
            val values = ContentValues().apply {
                put(COL_NOM, nom)
                put(COL_PRENOM, prenom)
                put(COL_MOYENNE, moyenne)
            }
            db.insert(TABLE_ETUDIANT, null, values)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ETUDIANT")
        onCreate(db)
    }

    // --- 1. AJOUTER ---
    fun ajouter(nom: String, prenom: String, moyenne: Double): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOM, nom.trim())
            put(COL_PRENOM, prenom.trim())
            put(COL_MOYENNE, moyenne)
        }
        return db.insert(TABLE_ETUDIANT, null, values)
    }

    // --- 2. LIRE (TOUS LES ÉTUDIANTS) ---
    fun getEtudiants(triParMoyenneDesc: Boolean? = null): ArrayList<Etudiant> {
        val liste = ArrayList<Etudiant>()
        val db = readableDatabase

        val orderBy = when (triParMoyenneDesc) {
            true -> "$COL_MOYENNE DESC, $COL_NOM ASC"
            false -> "$COL_NOM ASC, $COL_PRENOM ASC"
            null -> "$COL_ID DESC"
        }

        val cursor = db.rawQuery(
            "SELECT $COL_ID, $COL_NOM, $COL_PRENOM, $COL_MOYENNE FROM $TABLE_ETUDIANT ORDER BY $orderBy",
            null
        )

        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_ID)
                val nomIdx = it.getColumnIndexOrThrow(COL_NOM)
                val prenomIdx = it.getColumnIndexOrThrow(COL_PRENOM)
                val moyenneIdx = it.getColumnIndexOrThrow(COL_MOYENNE)

                do {
                    val id = it.getInt(idIdx)
                    val nom = it.getString(nomIdx)
                    val prenom = it.getString(prenomIdx)
                    val moyenne = it.getDouble(moyenneIdx)
                    liste.add(Etudiant(id, nom, prenom, moyenne))
                } while (it.moveToNext())
            }
        }
        return liste
    }

    // --- 3. RECHERCHER PAR NOM OU PRÉNOM (Extension proposée) ---
    fun rechercher(texte: String): ArrayList<Etudiant> {
        val liste = ArrayList<Etudiant>()
        val db = readableDatabase
        val pattern = "%${texte.trim()}%"
        val cursor = db.rawQuery(
            "SELECT $COL_ID, $COL_NOM, $COL_PRENOM, $COL_MOYENNE FROM $TABLE_ETUDIANT WHERE $COL_NOM LIKE ? OR $COL_PRENOM LIKE ? ORDER BY $COL_ID DESC",
            arrayOf(pattern, pattern)
        )
        cursor.use {
            if (it.moveToFirst()) {
                val idIdx = it.getColumnIndexOrThrow(COL_ID)
                val nomIdx = it.getColumnIndexOrThrow(COL_NOM)
                val prenomIdx = it.getColumnIndexOrThrow(COL_PRENOM)
                val moyenneIdx = it.getColumnIndexOrThrow(COL_MOYENNE)

                do {
                    val id = it.getInt(idIdx)
                    val nom = it.getString(nomIdx)
                    val prenom = it.getString(prenomIdx)
                    val moyenne = it.getDouble(moyenneIdx)
                    liste.add(Etudiant(id, nom, prenom, moyenne))
                } while (it.moveToNext())
            }
        }
        return liste
    }

    // --- 4. MODIFIER ---
    fun modifier(id: Int, nom: String, prenom: String, moyenne: Double): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NOM, nom.trim())
            put(COL_PRENOM, prenom.trim())
            put(COL_MOYENNE, moyenne)
        }
        return db.update(
            TABLE_ETUDIANT,
            values,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )
    }

    // --- 5. SUPPRIMER ---
    fun supprimer(id: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_ETUDIANT,
            "$COL_ID = ?",
            arrayOf(id.toString())
        )
    }

    // --- STATISTIQUES (Extensions proposées: nb total, moyenne générale, admis) ---
    fun getStatistiques(): Triple<Int, Int, Double> {
        val db = readableDatabase
        var total = 0
        var admis = 0
        var sommeMoyenne = 0.0

        val cursor = db.rawQuery(
            "SELECT $COL_MOYENNE FROM $TABLE_ETUDIANT",
            null
        )
        cursor.use {
            while (it.moveToNext()) {
                val m = it.getDouble(0)
                total++
                if (m >= 10.0) admis++
                sommeMoyenne += m
            }
        }

        val moyenneGenerale = if (total > 0) sommeMoyenne / total else 0.0
        return Triple(total, admis, moyenneGenerale)
    }
}
