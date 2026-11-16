package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.CategoryConfigEntity
import com.example.data.local.StoreSettingsEntity
import com.example.data.local.TopUpOrderEntity
import com.example.data.local.TopUpPackageEntity
import com.example.data.model.OrderStatus
import com.example.data.model.TopUpCategory
import com.example.ui.DeliveryEmailBanner
import com.example.ui.MainUiState
import com.example.ui.components.LocalOrResImage
import com.example.ui.components.OrderStatusChip
import com.example.ui.theme.ProductGreen
import com.example.ui.theme.StatusCancelFg
import com.example.ui.theme.StatusSuccessFg
import com.example.ui.theme.TitleOrange
import com.example.util.GmailDeliveryMailer
import com.example.util.ImageStorageHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
  uiState: MainUiState,
  onBack: () -> Unit,
  onUpdateOrderStatus: (TopUpOrderEntity, OrderStatus) -> Unit,
  onResendDeliveryEmail: (TopUpOrderEntity) -> Unit,
  onDismissDeliveryBanner: () -> Unit,
  onSavePackage: (TopUpPackageEntity) -> Unit,
  onDeletePackage: (Int) -> Unit,
  onSaveCategoryConfig: (CategoryConfigEntity) -> Unit,
  onSaveStoreSettings: (StoreSettingsEntity) -> Unit,
) {
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Orders", "Packages", "Posters", "Site & Video")
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "Admin Panel • এডমিন",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text = "Orders, Gmail Auto-Delivery, Diamonds & Video",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("admin_back_button")) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colorScheme.onSurface,
            )
          }
        },
        colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      )
    },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      Column(modifier = Modifier.fillMaxSize().widthIn(max = 820.dp)) {
        TabRow(
          selectedTabIndex = selectedTab,
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = ProductGreen,
        ) {
          tabs.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = {
                Text(
                  text = title,
                  style = MaterialTheme.typography.labelMedium,
                  color =
                    if (selectedTab == index) ProductGreen
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
              },
              modifier = Modifier.testTag("admin_tab_$index"),
            )
          }
        }

        when (selectedTab) {
          0 ->
            AdminOrdersTab(
              orders = uiState.orders,
              settings = uiState.storeSettings,
              signedInEmail = uiState.authUser?.email,
              deliveringOrderId = uiState.deliveringOrderId,
              deliveryBanner = uiState.deliveryEmailBanner,
              onSaveGmailSettings = { updated ->
                onSaveStoreSettings(updated)
                scope.launch { snackbarHostState.showSnackbar("Business Gmail সেভ হয়েছে") }
              },
              onDismissDeliveryBanner = onDismissDeliveryBanner,
              onUpdateOrderStatus = onUpdateOrderStatus,
              onResendDeliveryEmail = onResendDeliveryEmail,
            )
          1 ->
            AdminPackagesTab(
              packages = uiState.packages,
              onSavePackage = { pkg ->
                onSavePackage(pkg)
                scope.launch { snackbarHostState.showSnackbar("প্যাকেজ সেভ হয়েছে • Saved") }
              },
              onDeletePackage = { id ->
                onDeletePackage(id)
                scope.launch { snackbarHostState.showSnackbar("প্যাকেজ মুছে ফেলা হয়েছে") }
              },
            )
          2 ->
            AdminPostersTab(
              configs = uiState.categoryConfigs,
              onSaveConfig = { cfg ->
                onSaveCategoryConfig(cfg)
                scope.launch { snackbarHostState.showSnackbar("পোস্টার সেভ হয়েছে") }
              },
            )
          3 ->
            AdminSiteInfoTab(
              settings = uiState.storeSettings,
              onSaveSettings = { updated ->
                onSaveStoreSettings(updated)
                scope.launch { snackbarHostState.showSnackbar("সেটিংস, Gmail ও ভিডিও সেভ হয়েছে") }
              },
            )
        }
      }
    }
  }
}

@Composable
private fun AdminOrdersTab(
  orders: List<TopUpOrderEntity>,
  settings: StoreSettingsEntity,
  signedInEmail: String?,
  deliveringOrderId: Int?,
  deliveryBanner: DeliveryEmailBanner?,
  onSaveGmailSettings: (StoreSettingsEntity) -> Unit,
  onDismissDeliveryBanner: () -> Unit,
  onUpdateOrderStatus: (TopUpOrderEntity, OrderStatus) -> Unit,
  onResendDeliveryEmail: (TopUpOrderEntity) -> Unit,
) {
  val context = LocalContext.current
  val deliveredTotal =
    orders.filter { it.statusId == OrderStatus.DELIVERED.id }.sumOf { it.priceBdt }
  val pendingCount = orders.count { it.statusId == OrderStatus.PENDING.id }

  var adminGmailInput by remember(settings.adminGmail) { mutableStateOf(settings.adminGmail) }
  var appPasswordInput by remember(settings.gmailAppPassword) { mutableStateOf(settings.gmailAppPassword) }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        AdminStatBox("Orders", "${orders.size}", Modifier.weight(1f))
        AdminStatBox("Pending", "$pendingCount", Modifier.weight(1f))
        AdminStatBox("Delivered", "৳$deliveredTotal", Modifier.weight(1f))
      }
    }

    // Inline Business Gmail Sender Config Right Beside Delivery Controls
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.4.dp, TitleOrange),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Filled.Email,
                contentDescription = null,
                tint = TitleOrange,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "বিজনেস জিমেইল • Business Gmail",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
            }
            if (settings.adminGmail.isNotBlank() && settings.gmailAppPassword.replace(" ", "").length >= 8) {
              Surface(
                shape = RoundedCornerShape(999.dp),
                color = ProductGreen.copy(alpha = 0.14f),
              ) {
                Text(
                  text = "Auto Ready",
                  style = MaterialTheme.typography.labelSmall,
                  color = ProductGreen,
                  fontWeight = FontWeight.Bold,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = adminGmailInput,
              onValueChange = { adminGmailInput = it.trim() },
              label = { Text("আমাদের জিমেইল (Business Gmail)", style = MaterialTheme.typography.labelSmall) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ProductGreen,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                  focusedTextColor = MaterialTheme.colorScheme.onSurface,
                  unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
              modifier = Modifier.weight(1f).testTag("orders_tab_business_gmail_input"),
            )
            if (!signedInEmail.isNullOrBlank() && adminGmailInput != signedInEmail) {
              OutlinedButton(
                onClick = { adminGmailInput = signedInEmail },
                border = BorderStroke(1.dp, ProductGreen),
                modifier = Modifier.height(48.dp),
              ) {
                Text("Use Mine", style = MaterialTheme.typography.labelSmall, color = ProductGreen)
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            OutlinedTextField(
              value = appPasswordInput,
              onValueChange = { appPasswordInput = it.take(32) },
              label = { Text("App Password (16-digit)", style = MaterialTheme.typography.labelSmall) },
              singleLine = true,
              visualTransformation = PasswordVisualTransformation(),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = ProductGreen,
                  unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                  focusedTextColor = MaterialTheme.colorScheme.onSurface,
                  unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
              modifier = Modifier.weight(1f).testTag("orders_tab_app_password_input"),
            )
            Button(
              onClick = {
                onSaveGmailSettings(
                  settings.copy(
                    adminGmail = adminGmailInput.trim(),
                    gmailAppPassword = appPasswordInput.trim(),
                  )
                )
              },
              colors =
                ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
              modifier = Modifier.height(48.dp).testTag("orders_tab_save_gmail_button"),
            ) {
              Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Save", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    deliveryBanner?.let { banner ->
      item {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = ProductGreen.copy(alpha = 0.12f),
          border = BorderStroke(1.2.dp, ProductGreen),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = banner.message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
              )
              IconButton(onClick = onDismissDeliveryBanner, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
              }
            }
            banner.fallbackOrder?.let { fallbackOrder ->
              Button(
                onClick = {
                  GmailDeliveryMailer.launchGmailComposeFallback(
                    context = context,
                    order = fallbackOrder,
                    storeName = settings.storeName,
                    businessGmail = settings.adminGmail,
                  )
                },
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = TitleOrange,
                    contentColor = Color.White,
                  ),
                modifier = Modifier.fillMaxWidth().height(42.dp),
              ) {
                Icon(Icons.Filled.Email, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Gmail অ্যাপ দিয়ে রসিদ পাঠান", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      }
    }

    if (orders.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        ) {
          Text(
            text = "এখনো কোনো অর্ডার নেই • No incoming orders.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(14.dp),
          )
        }
      }
    } else {
      items(orders, key = { it.id }) { order ->
        val status = OrderStatus.fromId(order.statusId)
        val isSendingThisOrder = deliveringOrderId == order.id
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.2.dp, ProductGreen.copy(alpha = 0.55f)),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${order.orderCode} • UID: ${order.playerUid}",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              OrderStatusChip(status = status)
            }
            Text(
              text = "${order.packageTitleEn} • ৳${order.priceBdt} | TrxID: ${order.trxId}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (order.customerEmail.isNotBlank()) {
              val fromLabel = settings.adminGmail.ifBlank { "Business Gmail দিন" }
              Text(
                text = "From: $fromLabel ➔ To: ${order.customerEmail}",
                style = MaterialTheme.typography.labelSmall,
                color = ProductGreen,
                fontWeight = FontWeight.SemiBold,
              )
            }
            if (status == OrderStatus.PENDING) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Button(
                  onClick = { onUpdateOrderStatus(order, OrderStatus.DELIVERED) },
                  enabled = !isSendingThisOrder,
                  colors =
                    ButtonDefaults.buttonColors(
                      containerColor = StatusSuccessFg,
                      contentColor = Color.White,
                    ),
                  modifier = Modifier.weight(1f).height(44.dp).testTag("deliver_${order.orderCode}"),
                ) {
                  if (isSendingThisOrder) {
                    CircularProgressIndicator(
                      color = Color.White,
                      strokeWidth = 2.dp,
                      modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("পাঠানো হচ্ছে...", style = MaterialTheme.typography.labelSmall)
                  } else {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Deliver & Email", style = MaterialTheme.typography.labelSmall)
                  }
                }
                OutlinedButton(
                  onClick = { onUpdateOrderStatus(order, OrderStatus.CANCELLED) },
                  enabled = !isSendingThisOrder,
                  border = BorderStroke(1.dp, StatusCancelFg),
                  modifier = Modifier.height(44.dp),
                ) {
                  Text("Cancel", color = StatusCancelFg, style = MaterialTheme.typography.labelSmall)
                }
              }
            } else if (status == OrderStatus.DELIVERED && order.customerEmail.isNotBlank()) {
              OutlinedButton(
                onClick = { onResendDeliveryEmail(order) },
                enabled = !isSendingThisOrder,
                border = BorderStroke(1.dp, ProductGreen),
                modifier = Modifier.fillMaxWidth().height(40.dp),
              ) {
                if (isSendingThisOrder) {
                  CircularProgressIndicator(
                    color = ProductGreen,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("পাঠানো হচ্ছে...", style = MaterialTheme.typography.labelSmall, color = ProductGreen)
                } else {
                  Icon(
                    Icons.Filled.Email,
                    contentDescription = null,
                    tint = ProductGreen,
                    modifier = Modifier.size(15.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    "Resend Gmail Receipt • পুনরায় ইমেইল",
                    style = MaterialTheme.typography.labelSmall,
                    color = ProductGreen,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminPackagesTab(
  packages: List<TopUpPackageEntity>,
  onSavePackage: (TopUpPackageEntity) -> Unit,
  onDeletePackage: (Int) -> Unit,
) {
  var editingPkg by remember { mutableStateOf<TopUpPackageEntity?>(null) }
  var quickDiamonds by remember { mutableStateOf("") }
  var quickRate by remember { mutableStateOf("") }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    // Quick Add Many UID Diamond Options + Rate Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, ProductGreen),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Diamond,
              contentDescription = null,
              tint = ProductGreen,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "UID Top Up: দ্রুত ডায়মন্ড ও রেট যোগ করুন",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            OutlinedTextField(
              value = quickDiamonds,
              onValueChange = { quickDiamonds = it.filter { ch -> ch.isDigit() }.take(6) },
              label = { Text("Diamonds", style = MaterialTheme.typography.labelSmall) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f).testTag("quick_uid_diamonds_input"),
            )
            OutlinedTextField(
              value = quickRate,
              onValueChange = { quickRate = it.filter { ch -> ch.isDigit() }.take(6) },
              label = { Text("Rate (৳)", style = MaterialTheme.typography.labelSmall) },
              singleLine = true,
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              modifier = Modifier.weight(1f).testTag("quick_uid_rate_input"),
            )
          }

          Button(
            onClick = {
              val diamonds = quickDiamonds.toIntOrNull() ?: 0
              val rate = quickRate.toIntOrNull() ?: 0
              if (diamonds > 0 && rate > 0) {
                onSavePackage(
                  TopUpPackageEntity(
                    id = 0,
                    categoryId = TopUpCategory.UID.id,
                    titleEn = "$diamonds Diamonds",
                    titleBn = "$diamonds ডায়মন্ড",
                    diamondCount = diamonds,
                    priceBdt = rate,
                    sortOrder = packages.count { it.categoryId == TopUpCategory.UID.id } + 1,
                  )
                )
                quickDiamonds = ""
                quickRate = ""
              }
            },
            colors =
              ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(44.dp).testTag("quick_add_uid_button"),
          ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add UID Diamond + Rate • যোগ করুন", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }

    item {
      OutlinedButton(
        onClick = {
          editingPkg =
            TopUpPackageEntity(
              id = 0,
              categoryId = TopUpCategory.UID.id,
              titleEn = "",
              titleBn = "",
              diamondCount = 0,
              priceBdt = 0,
              sortOrder = packages.size + 1,
            )
        },
        border = BorderStroke(1.2.dp, ProductGreen),
        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("add_package_button"),
      ) {
        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Custom Pack (Photo + Category) • কাস্টম প্যাকেজ", style = MaterialTheme.typography.labelMedium)
      }
    }

    items(packages, key = { it.id }) { pkg ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, ProductGreen.copy(alpha = 0.55f)),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(10.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          if (!pkg.imagePath.isNullOrBlank()) {
            LocalOrResImage(
              localPath = pkg.imagePath,
              fallbackRes = R.drawable.img_poster_uid,
              contentDescription = pkg.titleEn,
              modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
            )
            Spacer(modifier = Modifier.width(10.dp))
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${pkg.titleEn} • ${pkg.titleBn}",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
              text =
                "${pkg.categoryId.uppercase()}" +
                  (if (pkg.diamondCount > 0) " • ${pkg.diamondCount}💎" else "") +
                  " • Rate: ৳${pkg.priceBdt}",
              style = MaterialTheme.typography.bodySmall,
              color = ProductGreen,
              fontWeight = FontWeight.SemiBold,
            )
          }
          IconButton(onClick = { editingPkg = pkg }) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Package", tint = ProductGreen)
          }
          IconButton(onClick = { onDeletePackage(pkg.id) }) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Package", tint = StatusCancelFg)
          }
        }
      }
    }
  }

  editingPkg?.let { pkg ->
    PackageEditorDialog(
      initialPackage = pkg,
      onDismiss = { editingPkg = null },
      onSave = { updated ->
        onSavePackage(updated)
        editingPkg = null
      },
    )
  }
}

@Composable
private fun PackageEditorDialog(
  initialPackage: TopUpPackageEntity,
  onDismiss: () -> Unit,
  onSave: (TopUpPackageEntity) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var categoryId by remember { mutableStateOf(initialPackage.categoryId) }
  var titleEn by remember { mutableStateOf(initialPackage.titleEn) }
  var titleBn by remember { mutableStateOf(initialPackage.titleBn) }
  var diamondsText by remember {
    mutableStateOf(
      if (initialPackage.diamondCount > 0) initialPackage.diamondCount.toString() else ""
    )
  }
  var priceText by remember {
    mutableStateOf(if (initialPackage.priceBdt > 0) initialPackage.priceBdt.toString() else "")
  }
  var imagePath by remember { mutableStateOf(initialPackage.imagePath) }

  val photoPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val saved = ImageStorageHelper.copyAndOptimizeImage(context, uri)
          if (saved != null) imagePath = saved
        }
      }
    }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    title = {
      Text(
        text = if (initialPackage.id == 0) "New Package • নতুন প্যাকেজ" else "Edit Package • এডিট",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          TopUpCategory.entries.forEach { cat ->
            FilterChip(
              selected = categoryId == cat.id,
              onClick = { categoryId = cat.id },
              label = { Text(cat.id.uppercase(), style = MaterialTheme.typography.labelSmall) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ProductGreen,
                  selectedLabelColor = Color.White,
                ),
            )
          }
        }

        if (!imagePath.isNullOrBlank()) {
          LocalOrResImage(
            localPath = imagePath,
            fallbackRes = R.drawable.img_poster_uid,
            contentDescription = "Package Photo",
            modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(8.dp)),
          )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          OutlinedButton(
            onClick = {
              photoPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.weight(1f),
          ) {
            Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Upload Photo • ছবি", style = MaterialTheme.typography.labelSmall)
          }
          if (!imagePath.isNullOrBlank()) {
            TextButton(onClick = { imagePath = null }) {
              Text("Remove", color = StatusCancelFg, style = MaterialTheme.typography.labelSmall)
            }
          }
        }

        AdminTextField(
          diamondsText,
          { diamondsText = it.filter { ch -> ch.isDigit() }.take(6) },
          "Diamonds Count (যেমন: 115)",
          KeyboardType.Number,
        )
        AdminTextField(
          priceText,
          { priceText = it.filter { ch -> ch.isDigit() }.take(6) },
          "Rate in BDT (৳)",
          KeyboardType.Number,
        )
        AdminTextField(titleEn, { titleEn = it }, "Title (English)")
        AdminTextField(titleBn, { titleBn = it }, "নাম (বাংলা)")
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val price = priceText.toIntOrNull() ?: 0
          val diamonds = diamondsText.toIntOrNull() ?: 0
          val resolvedEn =
            titleEn.trim().ifEmpty { if (diamonds > 0) "$diamonds Diamonds" else "" }
          val resolvedBn =
            titleBn.trim().ifEmpty { if (diamonds > 0) "$diamonds ডায়মন্ড" else resolvedEn }
          if (resolvedEn.isNotBlank() && price > 0) {
            onSave(
              initialPackage.copy(
                categoryId = categoryId,
                titleEn = resolvedEn,
                titleBn = resolvedBn,
                diamondCount = diamonds,
                priceBdt = price,
                imagePath = imagePath,
              )
            )
          }
        },
        colors =
          ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
private fun AdminPostersTab(
  configs: List<CategoryConfigEntity>,
  onSaveConfig: (CategoryConfigEntity) -> Unit,
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    items(configs, key = { it.categoryId }) { config ->
      PosterEditorCard(config = config, onSave = onSaveConfig)
    }
  }
}

@Composable
private fun PosterEditorCard(
  config: CategoryConfigEntity,
  onSave: (CategoryConfigEntity) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var titleEn by remember(config.categoryId) { mutableStateOf(config.titleEn) }
  var titleBn by remember(config.categoryId) { mutableStateOf(config.titleBn) }
  var guideEn by remember(config.categoryId) { mutableStateOf(config.guideEn) }
  var guideBn by remember(config.categoryId) { mutableStateOf(config.guideBn) }
  var posterImagePath by remember(config.categoryId) { mutableStateOf(config.posterImagePath) }

  val fallbackRes =
    when (TopUpCategory.fromId(config.categoryId)) {
      TopUpCategory.UID -> R.drawable.img_poster_uid
      TopUpCategory.MONTHLY -> R.drawable.img_poster_monthly
      TopUpCategory.WEEKLY -> R.drawable.img_poster_weekly
    }

  val photoPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val saved = ImageStorageHelper.copyAndOptimizeImage(context, uri)
          if (saved != null) posterImagePath = saved
        }
      }
    }

  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.2.dp, ProductGreen.copy(alpha = 0.6f)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "${config.categoryId.uppercase()} Poster",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurface,
      )

      LocalOrResImage(
        localPath = posterImagePath,
        fallbackRes = fallbackRes,
        contentDescription = titleEn,
        modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(8.dp)),
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = {
            photoPicker.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          modifier = Modifier.weight(1f),
        ) {
          Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Change Photo", style = MaterialTheme.typography.labelSmall)
        }
        if (!posterImagePath.isNullOrBlank()) {
          TextButton(onClick = { posterImagePath = null }) {
            Text("Reset", style = MaterialTheme.typography.labelSmall)
          }
        }
      }

      AdminTextField(titleEn, { titleEn = it }, "Title (EN)")
      AdminTextField(titleBn, { titleBn = it }, "শিরোনাম (বাংলা)")
      AdminTextField(guideBn, { guideBn = it }, "Short Note (বাংলা)")
      AdminTextField(guideEn, { guideEn = it }, "Short Note (EN)")

      Button(
        onClick = {
          onSave(
            config.copy(
              titleEn = titleEn.trim(),
              titleBn = titleBn.trim(),
              guideEn = guideEn.trim(),
              guideBn = guideBn.trim(),
              posterImagePath = posterImagePath,
            )
          )
        },
        colors =
          ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
        modifier = Modifier.fillMaxWidth().height(42.dp),
      ) {
        Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Save ${config.categoryId.uppercase()}", style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}

@Composable
private fun AdminSiteInfoTab(
  settings: StoreSettingsEntity,
  onSaveSettings: (StoreSettingsEntity) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()

  var storeName by remember { mutableStateOf(settings.storeName) }
  var siteDescBn by remember { mutableStateOf(settings.siteDescriptionBn) }
  var bkash by remember { mutableStateOf(settings.bkashNumber) }
  var nagad by remember { mutableStateOf(settings.nagadNumber) }
  var rocket by remember { mutableStateOf(settings.rocketNumber) }
  var supportContact by remember { mutableStateOf(settings.supportContact) }
  var heroImagePath by remember { mutableStateOf(settings.heroImagePath) }
  var isShopOpen by remember { mutableStateOf(settings.isShopOpen) }
  var isDarkMode by remember { mutableStateOf(settings.isDarkMode) }

  // Friday Bonus Offer states + Rate
  var fridayBonusEnabled by remember { mutableStateOf(settings.fridayBonusEnabled) }
  var fridayBonusTarget by remember { mutableStateOf(settings.fridayBonusTarget) }
  var fridayBonusRate by remember { mutableStateOf(settings.fridayBonusRate) }
  var fridayBonusTitleBn by remember { mutableStateOf(settings.fridayBonusTitleBn) }
  var fridayBonusTitleEn by remember { mutableStateOf(settings.fridayBonusTitleEn) }

  // Tutorial Video states
  var tutorialVideoPath by remember { mutableStateOf(settings.tutorialVideoPath) }
  var tutorialVideoTitleBn by remember { mutableStateOf(settings.tutorialVideoTitleBn) }

  // Auto Gmail Delivery states
  var adminGmail by remember { mutableStateOf(settings.adminGmail) }
  var gmailAppPassword by remember { mutableStateOf(settings.gmailAppPassword) }

  LaunchedEffect(settings) {
    storeName = settings.storeName
    siteDescBn = settings.siteDescriptionBn
    bkash = settings.bkashNumber
    nagad = settings.nagadNumber
    rocket = settings.rocketNumber
    supportContact = settings.supportContact
    heroImagePath = settings.heroImagePath
    isShopOpen = settings.isShopOpen
    isDarkMode = settings.isDarkMode
    fridayBonusEnabled = settings.fridayBonusEnabled
    fridayBonusTarget = settings.fridayBonusTarget
    fridayBonusRate = settings.fridayBonusRate
    fridayBonusTitleBn = settings.fridayBonusTitleBn
    fridayBonusTitleEn = settings.fridayBonusTitleEn
    tutorialVideoPath = settings.tutorialVideoPath
    tutorialVideoTitleBn = settings.tutorialVideoTitleBn
    adminGmail = settings.adminGmail
    gmailAppPassword = settings.gmailAppPassword
  }

  val bannerPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val saved = ImageStorageHelper.copyAndOptimizeImage(context, uri)
          if (saved != null) heroImagePath = saved
        }
      }
    }

  val videoPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val saved = ImageStorageHelper.copyVideoToInternalStorage(context, uri)
          if (saved != null) tutorialVideoPath = saved
        }
      }
    }

  fun buildUpdatedSettings(): StoreSettingsEntity =
    settings.copy(
      storeName = storeName.trim(),
      siteDescriptionBn = siteDescBn.trim(),
      bkashNumber = bkash.trim(),
      nagadNumber = nagad.trim(),
      rocketNumber = rocket.trim(),
      supportContact = supportContact.trim(),
      heroImagePath = heroImagePath,
      isShopOpen = isShopOpen,
      isDarkMode = isDarkMode,
      fridayBonusEnabled = fridayBonusEnabled,
      fridayBonusTarget = fridayBonusTarget,
      fridayBonusRate = fridayBonusRate.trim(),
      fridayBonusTitleBn = fridayBonusTitleBn.trim(),
      fridayBonusTitleEn = fridayBonusTitleEn.trim(),
      tutorialVideoPath = tutorialVideoPath,
      tutorialVideoTitleBn = tutorialVideoTitleBn.trim(),
      adminGmail = adminGmail.trim(),
      gmailAppPassword = gmailAppPassword.trim(),
    )

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = PaddingValues(14.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    // 1. Auto Gmail Delivery Configuration Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, TitleOrange),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.Email,
              contentDescription = null,
              tint = TitleOrange,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "অটো ইমেইল সেটিংস • Auto Gmail Delivery",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
          }
          Text(
            text = "ডেলিভার বাটনে চাপলেই ইউজারের Gmail-এ ব্যানার ও অর্ডার লিস্ট চলে যাবে (Google App Password দিন)।",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          AdminTextField(
            adminGmail,
            { adminGmail = it },
            "Our Gmail • আমাদের জিমেইল",
            KeyboardType.Email,
          )
          OutlinedTextField(
            value = gmailAppPassword,
            onValueChange = { gmailAppPassword = it.take(32) },
            label = { Text("Gmail App Password (16-digit)", style = MaterialTheme.typography.labelSmall) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ProductGreen,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
              ),
            modifier = Modifier.fillMaxWidth().testTag("admin_gmail_app_password_input"),
          )
        }
      }
    }

    // 2. Friday Bonus Offer + Rate Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border =
          BorderStroke(
            1.5.dp,
            if (fridayBonusEnabled) TitleOrange else MaterialTheme.colorScheme.outline,
          ),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable {
                  fridayBonusEnabled = !fridayBonusEnabled
                  onSaveSettings(buildUpdatedSettings().copy(fridayBonusEnabled = fridayBonusEnabled))
                }
                .padding(vertical = 4.dp)
                .testTag("friday_bonus_checkbox_row"),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Checkbox(
              checked = fridayBonusEnabled,
              onCheckedChange = { checked ->
                fridayBonusEnabled = checked
                onSaveSettings(buildUpdatedSettings().copy(fridayBonusEnabled = checked))
              },
              colors =
                CheckboxDefaults.colors(
                  checkedColor = TitleOrange,
                  checkmarkColor = Color.White,
                ),
              modifier = Modifier.testTag("friday_bonus_checkbox"),
            )
            Icon(
              imageVector = Icons.Filled.CardGiftcard,
              contentDescription = null,
              tint = TitleOrange,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "শুক্রবার বোনাস অফার (টিক দিন)",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "টিক দিলে হোমে রেটসহ অফার দেখাবে",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("weekly" to "Weekly", "monthly" to "Monthly", "both" to "Both").forEach {
              (id, label) ->
              FilterChip(
                selected = fridayBonusTarget == id,
                onClick = { fridayBonusTarget = id },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                colors =
                  FilterChipDefaults.filterChipColors(
                    selectedContainerColor = TitleOrange,
                    selectedLabelColor = Color.White,
                  ),
              )
            }
          }

          AdminTextField(
            fridayBonusRate,
            { fridayBonusRate = it },
            "Friday Offer Rate (৳) • শুক্রবার অফার রেট",
          )
          AdminTextField(fridayBonusTitleBn, { fridayBonusTitleBn = it }, "Offer Title (বাংলা)")
          AdminTextField(fridayBonusTitleEn, { fridayBonusTitleEn = it }, "Offer Title (EN)")
        }
      }
    }

    // 3. How-to-Top-Up Tutorial Video Upload Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.4.dp, ProductGreen),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.VideoLibrary,
              contentDescription = null,
              tint = ProductGreen,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "টপ-আপ গাইড ভিডিও • Tutorial Video",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
              fontWeight = FontWeight.Bold,
            )
          }

          Text(
            text =
              if (!tutorialVideoPath.isNullOrBlank()) "ভিডিও আপলোড করা আছে (নিচে হোমে দেখাবে)"
              else "কোনো ভিডিও দেওয়া হয়নি",
            style = MaterialTheme.typography.labelSmall,
            color =
              if (!tutorialVideoPath.isNullOrBlank()) ProductGreen
              else MaterialTheme.colorScheme.onSurfaceVariant,
          )

          AdminTextField(
            tutorialVideoTitleBn,
            { tutorialVideoTitleBn = it },
            "Video Title (বাংলা)",
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = {
                videoPicker.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                )
              },
              modifier = Modifier.weight(1f).testTag("upload_video_button"),
            ) {
              Icon(
                Icons.Filled.VideoLibrary,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text("Upload Video • ভিডিও দিন", style = MaterialTheme.typography.labelSmall)
            }
            if (!tutorialVideoPath.isNullOrBlank()) {
              TextButton(onClick = { tutorialVideoPath = null }) {
                Text("Remove", color = StatusCancelFg, style = MaterialTheme.typography.labelSmall)
              }
            }
          }
        }
      }
    }

    // 4. Storefront Info & Payment Numbers
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, ProductGreen.copy(alpha = 0.6f)),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Shop Open • স্টোর চালু",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
              checked = isShopOpen,
              onCheckedChange = { isShopOpen = it },
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = ProductGreen,
                ),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Dark Mode • ডার্ক মোড",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Switch(
              checked = isDarkMode,
              onCheckedChange = { isDarkMode = it },
              colors =
                SwitchDefaults.colors(
                  checkedThumbColor = Color.White,
                  checkedTrackColor = ProductGreen,
                ),
            )
          }

          LocalOrResImage(
            localPath = heroImagePath,
            fallbackRes = R.drawable.img_hero_banner,
            contentDescription = "Hero Banner",
            modifier = Modifier.fillMaxWidth().height(90.dp).clip(RoundedCornerShape(8.dp)),
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
              onClick = {
                bannerPicker.launch(
                  PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
              },
              modifier = Modifier.weight(1f),
            ) {
              Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Banner Photo", style = MaterialTheme.typography.labelSmall)
            }
            if (!heroImagePath.isNullOrBlank()) {
              TextButton(onClick = { heroImagePath = null }) {
                Text("Reset", style = MaterialTheme.typography.labelSmall)
              }
            }
          }

          AdminTextField(storeName, { storeName = it }, "Site Name")
          AdminTextField(siteDescBn, { siteDescBn = it }, "Short Subtitle (বাংলা)")
          AdminTextField(supportContact, { supportContact = it }, "Contact Number")
          AdminTextField(bkash, { bkash = it }, "bKash Number", KeyboardType.Phone)
          AdminTextField(nagad, { nagad = it }, "Nagad Number", KeyboardType.Phone)
          AdminTextField(rocket, { rocket = it }, "Rocket Number", KeyboardType.Phone)

          Button(
            onClick = { onSaveSettings(buildUpdatedSettings()) },
            colors =
              ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_settings_button"),
          ) {
            Icon(Icons.Filled.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save All • সব সেভ করুন", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }
  }
}

@Composable
private fun AdminStatBox(label: String, value: String, modifier: Modifier = Modifier) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}

@Composable
private fun AdminTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  keyboardType: KeyboardType = KeyboardType.Text,
  singleLine: Boolean = true,
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
    singleLine = singleLine,
    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    colors =
      OutlinedTextFieldDefaults.colors(
        focusedBorderColor = ProductGreen,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        focusedLabelColor = ProductGreen,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
      ),
    modifier = Modifier.fillMaxWidth(),
  )
}
