package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CategoryConfigEntity
import com.example.data.local.StoreSettingsEntity
import com.example.data.local.TopUpOrderEntity
import com.example.data.local.TopUpPackageEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.TopUpCategory
import com.example.data.remote.CloudOrder
import com.example.data.remote.CloudOrderRepository
import com.example.data.repository.TopUpRepository
import com.example.util.EmailSendResult
import com.example.util.GmailDeliveryMailer
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class LanguageFocus(val labelEn: String, val labelBn: String) {
  BOTH("BN + EN", "বাংলা + EN"),
  BENGALI("বাংলা", "বাংলা"),
  ENGLISH("EN", "EN"),
}

data class CheckoutFormState(
  val selectedPackageId: Int? = null,
  val playerUid: String = "",
  val customerEmail: String = "",
  val serverRegion: String = "BD",
  val paymentMethod: PaymentMethod = PaymentMethod.BKASH,
  val senderNumber: String = "",
  val trxId: String = "",
  val validationError: String? = null,
)

data class AuthUserInfo(
  val uid: String,
  val email: String?,
  val displayName: String?,
)

data class DeliveryEmailBanner(
  val message: String,
  val fallbackOrder: TopUpOrderEntity? = null,
)

data class MainUiState(
  val packages: List<TopUpPackageEntity> = emptyList(),
  val categoryConfigs: List<CategoryConfigEntity> = emptyList(),
  val orders: List<TopUpOrderEntity> = emptyList(),
  val cloudOrders: List<CloudOrder> = emptyList(),
  val storeSettings: StoreSettingsEntity = StoreSettingsEntity(),
  val languageFocus: LanguageFocus = LanguageFocus.BOTH,
  val checkoutForm: CheckoutFormState = CheckoutFormState(),
  val activeReceiptOrder: TopUpOrderEntity? = null,
  val authUser: AuthUserInfo? = null,
  val deliveryEmailBanner: DeliveryEmailBanner? = null,
  val deliveringOrderId: Int? = null,
)

class MainViewModel(
  private val repository: TopUpRepository,
  private val cloudOrderRepository: CloudOrderRepository,
) : ViewModel() {

  private val languageFocus = MutableStateFlow(LanguageFocus.BOTH)
  private val checkoutForm = MutableStateFlow(CheckoutFormState())
  private val activeReceiptOrder = MutableStateFlow<TopUpOrderEntity?>(null)
  private val authUser = MutableStateFlow<AuthUserInfo?>(null)
  private val deliveryEmailBanner = MutableStateFlow<DeliveryEmailBanner?>(null)
  private val deliveringOrderId = MutableStateFlow<Int?>(null)
  private val cloudOrders = MutableStateFlow<List<CloudOrder>>(emptyList())
  private var cloudOrdersJob: Job? = null

  val uiState: StateFlow<MainUiState> =
    combine(
        repository.allPackages,
        repository.categoryConfigs,
        repository.allOrders,
        cloudOrders,
        repository.storeSettings,
        languageFocus,
        checkoutForm,
        activeReceiptOrder,
        authUser,
        deliveryEmailBanner,
        deliveringOrderId,
      ) { values ->
        @Suppress("UNCHECKED_CAST")
        val packages = values[0] as List<TopUpPackageEntity>
        @Suppress("UNCHECKED_CAST")
        val configs = values[1] as List<CategoryConfigEntity>
        @Suppress("UNCHECKED_CAST")
        val orders = values[2] as List<TopUpOrderEntity>
        @Suppress("UNCHECKED_CAST")
        val remoteOrders = values[3] as List<CloudOrder>
        val settings = (values[4] as StoreSettingsEntity?) ?: StoreSettingsEntity()
        val lang = values[5] as LanguageFocus
        val form = values[6] as CheckoutFormState
        val receipt = values[7] as TopUpOrderEntity?
        val user = values[8] as AuthUserInfo?
        val emailBanner = values[9] as DeliveryEmailBanner?
        val sendingOrderId = values[10] as Int?
        MainUiState(
          packages = packages,
          categoryConfigs = configs,
          orders = orders,
          cloudOrders = remoteOrders,
          storeSettings = settings,
          languageFocus = lang,
          checkoutForm = form,
          activeReceiptOrder = receipt,
          authUser = user,
          deliveryEmailBanner = emailBanner,
          deliveringOrderId = sendingOrderId,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MainUiState(),
      )

  init {
    viewModelScope.launch { repository.ensureSeeded() }
    syncAuthState()
  }

  fun syncAuthState() {
    val current = Firebase.auth.currentUser
    if (current != null) {
      val info =
        AuthUserInfo(uid = current.uid, email = current.email, displayName = current.displayName)
      authUser.value = info
      if (checkoutForm.value.customerEmail.isBlank() && !current.email.isNullOrBlank()) {
        checkoutForm.update { it.copy(customerEmail = current.email.orEmpty()) }
      }
      startCloudOrdersListener(current.uid)
    } else {
      authUser.value = null
      cloudOrdersJob?.cancel()
      cloudOrdersJob = null
      cloudOrders.value = emptyList()
    }
  }

  private fun startCloudOrdersListener(uid: String) {
    cloudOrdersJob?.cancel()
    cloudOrdersJob =
      viewModelScope.launch {
        cloudOrderRepository
          .observeUserOrders(uid)
          .catch { /* Logged in repository; keep UI responsive */ }
          .collect { list -> cloudOrders.value = list }
      }
  }

  fun toggleDarkMode() {
    val current = uiState.value.storeSettings
    viewModelScope.launch {
      repository.saveStoreSettings(current.copy(isDarkMode = !current.isDarkMode))
    }
  }

  fun setLanguageFocus(focus: LanguageFocus) {
    languageFocus.value = focus
  }

  fun prepareCategoryCheckout(category: TopUpCategory) {
    val firstFeatured =
      uiState.value.packages
        .filter { it.categoryId == category.id }
        .let { list -> list.firstOrNull { it.isFeatured } ?: list.firstOrNull() }
    val signedInEmail = authUser.value?.email.orEmpty()
    checkoutForm.update {
      it.copy(
        selectedPackageId = firstFeatured?.id,
        customerEmail = it.customerEmail.ifBlank { signedInEmail },
        validationError = null,
      )
    }
  }

  fun selectPackage(packageId: Int) {
    checkoutForm.update { it.copy(selectedPackageId = packageId, validationError = null) }
  }

  fun updatePlayerUid(uid: String) {
    val digitsOnly = uid.filter { it.isDigit() }.take(13)
    checkoutForm.update { it.copy(playerUid = digitsOnly, validationError = null) }
  }

  fun updateCustomerEmail(email: String) {
    checkoutForm.update { it.copy(customerEmail = email.trim().take(100), validationError = null) }
  }

  fun selectServerRegion(server: String) {
    checkoutForm.update { it.copy(serverRegion = server) }
  }

  fun selectPaymentMethod(method: PaymentMethod) {
    checkoutForm.update { it.copy(paymentMethod = method, validationError = null) }
  }

  fun updateSenderNumber(number: String) {
    val cleaned = number.filter { it.isDigit() || it == '+' || it == '-' }.take(16)
    checkoutForm.update { it.copy(senderNumber = cleaned, validationError = null) }
  }

  fun updateTrxId(trxId: String) {
    checkoutForm.update { it.copy(trxId = trxId.uppercase().take(20), validationError = null) }
  }

  fun validateUidStep(category: TopUpCategory): Boolean {
    val state = uiState.value
    val form = state.checkoutForm
    val selectedPkg =
      state.packages.firstOrNull { it.id == form.selectedPackageId && it.categoryId == category.id }
        ?: state.packages.firstOrNull { it.categoryId == category.id }

    if (selectedPkg == null) {
      checkoutForm.update { it.copy(validationError = "প্যাকেজ বাছুন • Select package") }
      return false
    }
    if (form.playerUid.length < 7) {
      checkoutForm.update { it.copy(validationError = "সঠিক Player UID দিন • Valid UID required") }
      return false
    }
    val email = form.customerEmail.trim()
    val emailPattern = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    if (!emailPattern.matches(email)) {
      checkoutForm.update { it.copy(validationError = "সঠিক Gmail দিন • Valid Gmail required") }
      return false
    }
    checkoutForm.update { it.copy(selectedPackageId = selectedPkg.id, validationError = null) }
    return true
  }

  fun validatePaymentStep(category: TopUpCategory): Boolean {
    val state = uiState.value
    val form = state.checkoutForm
    val selectedPkg =
      state.packages.firstOrNull { it.id == form.selectedPackageId && it.categoryId == category.id }
        ?: state.packages.firstOrNull { it.categoryId == category.id }

    if (selectedPkg == null || form.playerUid.length < 7) {
      checkoutForm.update { it.copy(validationError = "আগে UID ও প্যাকেজ দিন") }
      return false
    }
    if (form.senderNumber.filter { it.isDigit() }.length < 11) {
      checkoutForm.update { it.copy(validationError = "১১ ডিজিটের নম্বর দিন • 11-digit number") }
      return false
    }
    if (form.trxId.trim().length < 4) {
      checkoutForm.update { it.copy(validationError = "সঠিক TrxID দিন • Valid TrxID") }
      return false
    }
    checkoutForm.update { it.copy(validationError = null) }
    return true
  }

  fun submitOrder(category: TopUpCategory) {
    val state = uiState.value
    val form = state.checkoutForm
    val selectedPkg =
      state.packages.firstOrNull { it.id == form.selectedPackageId && it.categoryId == category.id }
        ?: state.packages.firstOrNull { it.categoryId == category.id }
        ?: return

    val orderCode = "JD-${Random.nextInt(1000, 9999)}"
    val displayUserName =
      authUser.value?.displayName?.takeIf { it.isNotBlank() } ?: "UID-${form.playerUid.trim()}"
    val email = form.customerEmail.trim().ifBlank { authUser.value?.email.orEmpty() }

    val newOrder =
      TopUpOrderEntity(
        orderCode = orderCode,
        categoryId = category.id,
        packageTitleEn = selectedPkg.titleEn,
        packageTitleBn = selectedPkg.titleBn,
        priceBdt = selectedPkg.priceBdt,
        playerUid = form.playerUid.trim(),
        playerName = displayUserName.take(36),
        customerEmail = email,
        serverRegion = form.serverRegion,
        paymentMethodId = form.paymentMethod.id,
        senderNumber = form.senderNumber.trim(),
        trxId = form.trxId.trim(),
        statusId = OrderStatus.PENDING.id,
        createdAt = System.currentTimeMillis(),
      )

    viewModelScope.launch {
      val insertedId = repository.placeOrder(newOrder)
      val savedOrder = newOrder.copy(id = insertedId.toInt())
      val uid = Firebase.auth.currentUser?.uid
      if (uid != null) {
        cloudOrderRepository.saveCloudOrder(uid, savedOrder)
      }
      checkoutForm.update {
        CheckoutFormState(
          selectedPackageId = selectedPkg.id,
          customerEmail = email,
          serverRegion = form.serverRegion,
        )
      }
      activeReceiptOrder.value = savedOrder
    }
  }

  fun updateOrderStatus(order: TopUpOrderEntity, status: OrderStatus) {
    viewModelScope.launch {
      if (order.id > 0) {
        repository.updateOrderStatus(order.id, status)
      }
      val uid = Firebase.auth.currentUser?.uid
      val matchingCloudOrder =
        uiState.value.cloudOrders.firstOrNull { it.orderCode == order.orderCode }
      if (uid != null && matchingCloudOrder != null && matchingCloudOrder.id.isNotBlank()) {
        cloudOrderRepository.updateCloudOrderStatus(uid, matchingCloudOrder.id, status)
      }
      if (status == OrderStatus.DELIVERED) {
        sendReceiptEmailForOrder(order.copy(statusId = OrderStatus.DELIVERED.id))
      }
    }
  }

  fun resendDeliveryEmail(order: TopUpOrderEntity) {
    viewModelScope.launch {
      sendReceiptEmailForOrder(order.copy(statusId = OrderStatus.DELIVERED.id))
    }
  }

  private suspend fun sendReceiptEmailForOrder(deliveredOrder: TopUpOrderEntity) {
    if (deliveredOrder.customerEmail.isBlank()) {
      deliveryEmailBanner.value =
        DeliveryEmailBanner(
          message = "${deliveredOrder.orderCode} ডেলিভারড হয়েছে (ইউজারের ইমেইল দেওয়া নেই)",
          fallbackOrder = null,
        )
      return
    }
    deliveringOrderId.value = deliveredOrder.id
    try {
      val settings = uiState.value.storeSettings
      when (
        val result =
          GmailDeliveryMailer.sendAutomatedOrderCompletedEmail(
            adminGmail = settings.adminGmail,
            appPassword = settings.gmailAppPassword,
            order = deliveredOrder,
            storeName = settings.storeName,
            supportContact = settings.supportContact,
          )
      ) {
        is EmailSendResult.Sent -> {
          deliveryEmailBanner.value =
            DeliveryEmailBanner(
              message =
                "✅ ডেলিভারড! ${settings.adminGmail.trim()} থেকে ${result.recipient}-এ বড় রসিদ ও ব্যানারসহ ইমেইল পাঠানো হয়েছে।",
              fallbackOrder = null,
            )
        }
        EmailSendResult.NeedsConfig -> {
          deliveryEmailBanner.value =
            DeliveryEmailBanner(
              message =
                "অর্ডার ডেলিভারড! অটো ইমেইলের জন্য উপরে Business Gmail ও 16-digit App Password সেভ করুন, অথবা এখনই Gmail অ্যাপ দিয়ে পাঠান:",
              fallbackOrder = deliveredOrder,
            )
        }
        is EmailSendResult.Failed -> {
          deliveryEmailBanner.value =
            DeliveryEmailBanner(
              message =
                "অর্ডার ডেলিভারড! অটো ইমেইল যায়নি (${result.reason})। Gmail অ্যাপ দিয়ে পাঠান:",
              fallbackOrder = deliveredOrder,
            )
        }
      }
    } finally {
      deliveringOrderId.value = null
    }
  }

  fun dismissDeliveryEmailBanner() {
    deliveryEmailBanner.value = null
  }

  fun showReceipt(order: TopUpOrderEntity) {
    activeReceiptOrder.value = order
  }

  fun dismissReceipt() {
    activeReceiptOrder.value = null
  }

  fun savePackage(pkg: TopUpPackageEntity) {
    viewModelScope.launch { repository.savePackage(pkg) }
  }

  fun deletePackage(packageId: Int) {
    viewModelScope.launch { repository.deletePackage(packageId) }
  }

  fun saveCategoryConfig(config: CategoryConfigEntity) {
    viewModelScope.launch { repository.saveCategoryConfig(config) }
  }

  fun saveStoreSettings(settings: StoreSettingsEntity) {
    viewModelScope.launch { repository.saveStoreSettings(settings) }
  }

  class Factory(
    private val repository: TopUpRepository,
    private val cloudOrderRepository: CloudOrderRepository,
  ) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return MainViewModel(repository, cloudOrderRepository) as T
    }
  }
}
