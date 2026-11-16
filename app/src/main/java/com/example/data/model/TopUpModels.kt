package com.example.data.model

enum class TopUpCategory(
  val id: String,
  val titleEn: String,
  val titleBn: String,
  val subtitleEn: String,
  val subtitleBn: String,
  val badgeEn: String,
  val badgeBn: String,
  val guideEn: String,
  val guideBn: String,
) {
  UID(
    id = "uid",
    titleEn = "UID Top Up",
    titleBn = "UID টপ-আপ",
    subtitleEn = "Direct UID Delivery",
    subtitleBn = "শুধু UID দিয়ে ডায়মন্ড",
    badgeEn = "2-5 MIN",
    badgeBn = "দ্রুত",
    guideEn = "Select diamond & enter Player UID only.",
    guideBn = "ডায়মন্ড বাছুন ও শুধু আপনার Player UID দিন।",
  ),
  MONTHLY(
    id = "monthly",
    titleEn = "Monthly Top Up",
    titleBn = "মান্থলি টপ-আপ",
    subtitleEn = "30-Day VIP Pass",
    subtitleBn = "৩০ দিনের মেম্বারশিপ",
    badgeEn = "BEST VALUE",
    badgeBn = "সেরা",
    guideEn = "30-day VIP pass via Player UID.",
    guideBn = "UID দিয়ে ৩০ দিনের মান্থলি মেম্বারশিপ।",
  ),
  WEEKLY(
    id = "weekly",
    titleEn = "Weekly Top Up",
    titleBn = "উইকলি টপ-আপ",
    subtitleEn = "7-Day Pass",
    subtitleBn = "৭ দিনের মেম্বারশিপ",
    badgeEn = "POPULAR",
    badgeBn = "জনপ্রিয়",
    guideEn = "7-day Weekly pass via Player UID.",
    guideBn = "UID দিয়ে ৭ দিনের উইকলি মেম্বারশিপ।",
  );

  companion object {
    fun fromId(id: String?): TopUpCategory =
      entries.firstOrNull { it.id == id } ?: UID
  }
}

enum class PaymentMethod(
  val id: String,
  val labelEn: String,
  val labelBn: String,
) {
  BKASH("bkash", "bKash", "বিকাশ"),
  NAGAD("nagad", "Nagad", "নগদ"),
  ROCKET("rocket", "Rocket", "রকেট");

  companion object {
    fun fromId(id: String): PaymentMethod = entries.firstOrNull { it.id == id } ?: BKASH
  }
}

enum class OrderStatus(
  val id: String,
  val labelEn: String,
  val labelBn: String,
) {
  PENDING("pending", "Pending", "অপেক্ষায়"),
  DELIVERED("delivered", "Delivered", "সম্পন্ন"),
  CANCELLED("cancelled", "Cancelled", "বাতিল");

  companion object {
    fun fromId(id: String): OrderStatus = entries.firstOrNull { it.id == id } ?: PENDING
  }
}
