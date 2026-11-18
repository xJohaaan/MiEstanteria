package com.example.miestanteria.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

/**
 * Copia local (SQLite/Room) de la biblioteca del usuario.
 * Permite ver la estanteria sin conexion a internet; se actualiza cada vez que hay red.
 */
@Entity(tableName = "biblioteca", indices = [Index(value = ["userId", "apiId"], unique = true)])
data class BibliotecaEntity(
    @PrimaryKey val id: Long, // id_biblioteca del servidor
    val userId: Long,
    val apiId: String,
    val titulo: String,
    val autores: String,
    val editorial: String?,
    val fechaPublicacion: String?,
    val portadaUrl: String?,
    val descripcion: String?,
    val categoria: String?,
    val paginas: Int?,
    val estado: String,
    val paginaActual: Int,
    val fechaAgregado: String
)

@Dao
interface BibliotecaDao {

    @Query("SELECT * FROM biblioteca WHERE userId = :userId ORDER BY fechaAgregado DESC")
    fun observarTodo(userId: Long): Flow<List<BibliotecaEntity>>

    @Query("SELECT * FROM biblioteca WHERE userId = :userId AND apiId = :apiId LIMIT 1")
    fun observarPorApiId(userId: Long, apiId: String): Flow<BibliotecaEntity?>

    @Query("SELECT * FROM biblioteca WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): BibliotecaEntity?

    @Upsert
    suspend fun guardar(item: BibliotecaEntity)

    @Upsert
    suspend fun guardarTodos(items: List<BibliotecaEntity>)

    @Query("DELETE FROM biblioteca WHERE id = :id")
    suspend fun eliminar(id: Long)

    @Query("DELETE FROM biblioteca WHERE userId = :userId")
    suspend fun eliminarDeUsuario(userId: Long)

    @Query("DELETE FROM biblioteca")
    suspend fun borrarTodo()

    /** Reemplaza la copia local por lo que tiene el servidor. */
    @Transaction
    suspend fun reemplazar(userId: Long, items: List<BibliotecaEntity>) {
        eliminarDeUsuario(userId)
        guardarTodos(items)
    }
}

@Database(entities = [BibliotecaEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bibliotecaDao(): BibliotecaDao

    companion object {
        fun crear(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "mi_estanteria.db")
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
