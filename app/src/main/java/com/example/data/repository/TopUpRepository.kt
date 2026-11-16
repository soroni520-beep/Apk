package com.example.data.repository

import com.example.data.local.CategoryConfigEntity
import com.example.data.local.StoreSettingsEntity
import com.example.data.local.TopUpDao
import com.example.data.local.TopUpOrderEntity
import com.example.data.local.TopUpPackageEntity
import com.example.data.model.OrderStatus
import com.example.data.model.TopUpCategory
import kotlinx.coroutines.flow.Flow

class TopUpRepository(private val dao: TopUpDao) {

  val allPackages: Flow<List<TopUpPackageEntity>> = dao.observeAllPackages()
  val categoryConfigs: Flow<List<CategoryConfigEntity>> = dao.observeCategoryConfigs()
  val allOrders: Flow<List<TopUpOrderEntity>> = dao.observeAllOrders()
  val storeSettings: Flow<StoreSettingsEntity?> = dao.observeStoreSettings()

  suspend fun ensureSeeded() {
    if (dao.getCategoryConfigCount() == 0) {
      dao.insertCategoryConfigs(defaultCategoryConfigs())
    }
    if (dao.getPackageCount() == 0) {
      dao.insertPackages(defaultPackages())
    }
    if (dao.getStoreSettingsOnce() == null) {
      dao.upsertStoreSettings(StoreSettingsEntity())
    }
  }

  suspend fun placeOrder(order: TopUpOrderEntity): Long = dao.insertOrder(order)

  suspend fun updateOrderStatus(orderId: Int, status: OrderStatus) {
    dao.updateOrderStatus(orderId = orderId, statusId = status.id)
  }

  suspend fun savePackage(pkg: TopUpPackageEntity) {
    if (pkg.id == 0) {
      dao.insertPackage(pkg)
    } else {
      dao.updatePackage(pkg)
    }
  }

  suspend fun deletePackage(packageId: Int) {
    dao.deletePackageById(packageId)
  }

  suspend fun saveCategoryConfig(config: CategoryConfigEntity) {
    dao.upsertCategoryConfig(config)
  }

  suspend fun saveStoreSettings(settings: StoreSettingsEntity) {
    dao.upsertStoreSettings(settings)
  }

  private fun defaultCategoryConfigs(): List<CategoryConfigEntity> =
    TopUpCategory.entries.map { cat ->
      CategoryConfigEntity(
        categoryId = cat.id,
        titleEn = cat.titleEn,
        titleBn = cat.titleBn,
        subtitleEn = cat.subtitleEn,
        subtitleBn = cat.subtitleBn,
        guideEn = cat.guideEn,
        guideBn = cat.guideBn,
      )
    }

  private fun defaultPackages(): List<TopUpPackageEntity> =
    listOf(
      TopUpPackageEntity(
        categoryId = TopUpCategory.UID.id,
        titleEn = "25 Diamonds",
        titleBn = "২৫ ডায়মন্ড",
        descriptionEn = "Instant direct UID diamond delivery",
        descriptionBn = "সরাসরি UID-তে ২৫ ডায়মন্ড",
        bonusTagEn = "Starter",
        bonusTagBn = "স্টার্টার",
        diamondCount = 25,
        priceBdt = 25,
        sortOrder = 1,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.UID.id,
        titleEn = "50 Diamonds",
        titleBn = "৫০ ডায়মন্ড",
        descriptionEn = "Quick UID top-up pack",
        descriptionBn = "দ্রুত ৫০ ডায়মন্ড টপ-আপ",
        bonusTagEn = "Quick",
        bonusTagBn = "কুইক",
        diamondCount = 50,
        priceBdt = 45,
        sortOrder = 2,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.UID.id,
        titleEn = "115 Diamonds",
        titleBn = "১১৫ ডায়মন্ড",
        descriptionEn = "Most popular Free Fire UID diamond pack",
        descriptionBn = "সবচেয়ে জনপ্রিয় ১১৫ ডায়মন্ড প্যাক",
        bonusTagEn = "Popular",
        bonusTagBn = "জনপ্রিয়",
        diamondCount = 115,
        priceBdt = 85,
        isFeatured = true,
        sortOrder = 3,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.UID.id,
        titleEn = "240 Diamonds",
        titleBn = "২৪০ ডায়মন্ড",
        descriptionEn = "Great value 240 diamonds via UID",
        descriptionBn = "সাশ্রয়ী ২৪০ ডায়মন্ড প্যাক",
        bonusTagEn = "Value",
        bonusTagBn = "সাশ্রয়ী",
        diamondCount = 240,
        priceBdt = 170,
        sortOrder = 4,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.UID.id,
        titleEn = "610 Diamonds",
        titleBn = "৬১০ ডায়মন্ড",
        descriptionEn = "610 diamonds for bundles & evo skins",
        descriptionBn = "বান্ডেল ও ইভো স্কিনের জন্য ৬১০ ডায়মন্ড",
        bonusTagEn = "Hot Deal",
        bonusTagBn = "হট ডিল",
        diamondCount = 610,
        priceBdt = 425,
        isFeatured = true,
        sortOrder = 5,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.UID.id,
        titleEn = "1240 Diamonds",
        titleBn = "১২৪০ ডায়মন্ড",
        descriptionEn = "Large diamond bundle for events",
        descriptionBn = "বড় ইভেন্টের জন্য ১২৪০ ডায়মন্ড",
        bonusTagEn = "Pro",
        bonusTagBn = "প্রো",
        diamondCount = 1240,
        priceBdt = 850,
        sortOrder = 6,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.MONTHLY.id,
        titleEn = "1x Monthly Membership",
        titleBn = "১x মান্থলি মেম্বারশিপ",
        descriptionEn = "500 instant + 50 daily diamonds for 30 days",
        descriptionBn = "সাথে সাথে ৫০০ + ৩০ দিন প্রতিদিন ৫০ ডায়মন্ড",
        bonusTagEn = "2000+ Diamonds",
        bonusTagBn = "২০০০+ ডায়মন্ড",
        diamondCount = 2000,
        priceBdt = 780,
        isFeatured = true,
        sortOrder = 1,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.MONTHLY.id,
        titleEn = "2x Monthly Membership",
        titleBn = "২x মান্থলি মেম্বারশিপ",
        descriptionEn = "Double monthly pass (4000+ total diamonds)",
        descriptionBn = "ডাবল মান্থলি পাস (মোট ৪০০০+ ডায়মন্ড)",
        bonusTagEn = "VIP",
        bonusTagBn = "ভিআইপি",
        diamondCount = 4000,
        priceBdt = 1540,
        sortOrder = 2,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.MONTHLY.id,
        titleEn = "Monthly + 115 Diamonds",
        titleBn = "মান্থলি + ১১৫ ডায়মন্ড",
        descriptionEn = "Monthly membership plus extra 115 diamonds",
        descriptionBn = "মান্থলি মেম্বারশিপ + অতিরিক্ত ১১৫ ডায়মন্ড",
        bonusTagEn = "Combo",
        bonusTagBn = "কম্বো",
        diamondCount = 2115,
        priceBdt = 855,
        sortOrder = 3,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.WEEKLY.id,
        titleEn = "1x Weekly Membership",
        titleBn = "১x উইকলি মেম্বারশিপ",
        descriptionEn = "100 instant + 50 daily diamonds for 7 days",
        descriptionBn = "সাথে সাথে ১০০ + ৭ দিন প্রতিদিন ৫০ ডায়মন্ড",
        bonusTagEn = "450 Diamonds",
        bonusTagBn = "৪৫০ ডায়মন্ড",
        diamondCount = 450,
        priceBdt = 160,
        isFeatured = true,
        sortOrder = 1,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.WEEKLY.id,
        titleEn = "2x Weekly Membership",
        titleBn = "২x উইকলি মেম্বারশিপ",
        descriptionEn = "Double weekly pass (900 total diamonds)",
        descriptionBn = "ডাবল উইকলি পাস (মোট ৯০০ ডায়মন্ড)",
        bonusTagEn = "900 Diamonds",
        bonusTagBn = "৯০০ ডায়মন্ড",
        diamondCount = 900,
        priceBdt = 315,
        sortOrder = 2,
      ),
      TopUpPackageEntity(
        categoryId = TopUpCategory.WEEKLY.id,
        titleEn = "Weekly + 115 Diamonds",
        titleBn = "উইকলি + ১১৫ ডায়মন্ড",
        descriptionEn = "Weekly membership plus 115 UID diamonds",
        descriptionBn = "উইকলি মেম্বারশিপ + ১১৫ ডায়মন্ড কম্বো",
        bonusTagEn = "Combo",
        bonusTagBn = "কম্বো",
        diamondCount = 565,
        priceBdt = 240,
        sortOrder = 3,
      ),
    )
}
