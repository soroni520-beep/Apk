package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "packages")
data class TopUpPackageEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val categoryId: String,
  val titleEn: String,
  val titleBn: String,
  val descriptionEn: String = "",
  val descriptionBn: String = "",
  val bonusTagEn: String = "",
  val bonusTagBn: String = "",
  val diamondCount: Int = 0,
  val priceBdt: Int,
  val imagePath: String? = null,
  val isFeatured: Boolean = false,
  val sortOrder: Int = 0,
)

@Entity(tableName = "category_configs")
data class CategoryConfigEntity(
  @PrimaryKey val categoryId: String,
  val titleEn: String,
  val titleBn: String,
  val subtitleEn: String,
  val subtitleBn: String,
  val guideEn: String,
  val guideBn: String,
  val posterImagePath: String? = null,
)

@Entity(tableName = "orders")
data class TopUpOrderEntity(
  @PrimaryKey(autoGenerate = true) val id: Int = 0,
  val orderCode: String,
  val categoryId: String,
  val packageTitleEn: String,
  val packageTitleBn: String,
  val priceBdt: Int,
  val playerUid: String,
  val playerName: String,
  val customerEmail: String = "",
  val serverRegion: String,
  val paymentMethodId: String,
  val senderNumber: String,
  val trxId: String,
  val statusId: String,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "store_settings")
data class StoreSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val storeName: String = "MASTER JD TOP UP",
  val siteDescriptionBn: String = "দ্রুত ও কম দামে ফ্রি ফায়ার টপ-আপ",
  val siteDescriptionEn: String = "Fast Free Fire Diamond Top-Up",
  val noticeEn: String = "2–5 min delivery",
  val noticeBn: String = "২–৫ মিনিটে ডেলিভারি",
  val bkashNumber: String = "01700-000000",
  val nagadNumber: String = "01800-000000",
  val rocketNumber: String = "01900-000000",
  val supportContact: String = "+880 1700-000000",
  val heroImagePath: String? = null,
  val isShopOpen: Boolean = true,
  val isDarkMode: Boolean = false,
  val fridayBonusEnabled: Boolean = false,
  val fridayBonusTarget: String = "both", // "weekly", "monthly", "both"
  val fridayBonusRate: String = "৳১৪৫ / ১৫% ছাড়",
  val fridayBonusTitleBn: String = "শুক্রবার স্পেশাল অফার",
  val fridayBonusTitleEn: String = "Friday Bonus Offer",
  val fridayBonusDescBn: String = "উইকলি ও মান্থলি টপ-আপে বিশেষ রেট",
  val fridayBonusDescEn: String = "Special Friday rate on memberships",
  val tutorialVideoPath: String? = null,
  val tutorialVideoTitleBn: String = "কীভাবে ডায়মন্ড টপ-আপ করবেন",
  val tutorialVideoTitleEn: String = "How to Top Up",
  val adminGmail: String = "",
  val gmailAppPassword: String = "",
)
