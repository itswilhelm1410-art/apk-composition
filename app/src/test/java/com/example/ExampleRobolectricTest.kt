package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var dbHelper: DatabaseHelper

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        dbHelper = DatabaseHelper(context)
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Gestion des étudiants", appName)
    }

    @Test
    fun `test database CRUD operations`() {
        // Test insertion
        val insertId = dbHelper.ajouter("TEST", "Alice", 18.0)
        assertTrue(insertId > 0)

        // Test lecture
        val etudiants = dbHelper.getEtudiants()
        val found = etudiants.find { it.nom == "TEST" && it.prenom == "Alice" }
        assertTrue(found != null)
        assertEquals(18.0, found!!.moyenne, 0.01)
        assertTrue(found.isAdmis)
        assertEquals("Très Bien", found.mention)

        // Test modification
        val rowsUpdated = dbHelper.modifier(found.id, "TEST", "Alice", 19.5)
        assertEquals(1, rowsUpdated)

        val updatedList = dbHelper.getEtudiants()
        val updated = updatedList.find { it.id == found.id }
        assertEquals(19.5, updated!!.moyenne, 0.01)

        // Test suppression
        val rowsDeleted = dbHelper.supprimer(found.id)
        assertEquals(1, rowsDeleted)

        val finalList = dbHelper.getEtudiants()
        assertTrue(finalList.none { it.id == found.id })
    }
}
