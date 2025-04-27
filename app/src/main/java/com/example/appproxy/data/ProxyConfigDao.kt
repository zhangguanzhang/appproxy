package com.example.appproxy.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProxyConfigDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDirect(config: ProxyConfig)

    @Delete()
    suspend fun deleteDirect(config: ProxyConfig)

    @Update
    suspend fun update(config: ProxyConfig)

    @Query("UPDATE proxy_configs SET selected = 1 WHERE id = (SELECT id FROM proxy_configs ORDER BY id LIMIT 1)")
    suspend fun updateSelection()

    @Transaction
    @Query("UPDATE proxy_configs SET selected = CASE WHEN id = :selectedId THEN 1 ELSE 0 END")
    suspend fun updateSelections(selectedId: Int)

    @Query("SELECT * FROM proxy_configs WHERE id = :id")
    fun getConfig(id: Int): Flow<ProxyConfig>

    @Query("SELECT * FROM proxy_configs ORDER BY name ASC")
    fun getAllConfig(): Flow<List<ProxyConfig>>

    @Query("SELECT * FROM proxy_configs ORDER BY id ASC LIMIT 1")
    suspend fun getFirstConfig(): ProxyConfig?

    @Query("SELECT COUNT(*) FROM proxy_configs")
    suspend fun getConfigCount(): Int

    @Query("SELECT * FROM proxy_configs WHERE selected = 1 LIMIT 1")
    suspend fun getSelectedConfig(): ProxyConfig?

    @Transaction
    suspend fun insert(config: ProxyConfig) {
        insertDirect(config)
        val count = getConfigCount()
        if (count == 1) {
            updateSelection()
        }
    }

    @Transaction
    suspend fun delete(config: ProxyConfig) {
        val wasSelected = config.selected
        deleteDirect(config)
        if (wasSelected) {
            getFirstConfig()?.let { firstConfig ->
                updateSelections(firstConfig.id)
            }
        }
    }
}