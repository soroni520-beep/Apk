package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TopUpDao {
  @Query("SELECT * FROM packages ORDER BY sortOrder ASC, priceBdt ASC")
  fun observeAllPackages(): Flow<List<TopUpPackageEntity>>

  @Query("SELECT COUNT(*) FROM packages")
  suspend fun getPackageCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPackages(packages: List<TopUpPackageEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPackage(pkg: TopUpPackageEntity): Long

  @Update
  suspend fun updatePackage(pkg: TopUpPackageEntity)

  @Query("DELETE FROM packages WHERE id = :packageId")
  suspend fun deletePackageById(packageId: Int)

  @Query("SELECT * FROM category_configs")
  fun observeCategoryConfigs(): Flow<List<CategoryConfigEntity>>

  @Query("SELECT COUNT(*) FROM category_configs")
  suspend fun getCategoryConfigCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategoryConfigs(configs: List<CategoryConfigEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCategoryConfig(config: CategoryConfigEntity)

  @Query("SELECT * FROM orders ORDER BY createdAt DESC")
  fun observeAllOrders(): Flow<List<TopUpOrderEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrder(order: TopUpOrderEntity): Long

  @Query("UPDATE orders SET statusId = :statusId WHERE id = :orderId")
  suspend fun updateOrderStatus(orderId: Int, statusId: String)

  @Query("SELECT * FROM store_settings WHERE id = 1")
  fun observeStoreSettings(): Flow<StoreSettingsEntity?>

  @Query("SELECT * FROM store_settings WHERE id = 1")
  suspend fun getStoreSettingsOnce(): StoreSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertStoreSettings(settings: StoreSettingsEntity)
}
