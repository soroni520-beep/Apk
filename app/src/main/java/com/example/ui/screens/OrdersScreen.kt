package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.TopUpOrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.TopUpCategory
import com.example.ui.MainUiState
import com.example.ui.components.OrderStatusChip
import com.example.ui.components.rememberAnimatedGreenBorderBrush
import com.example.ui.theme.ProductGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrdersScreen(
  uiState: MainUiState,
  onOrderClick: (TopUpOrderEntity) -> Unit,
  onBrowseShop: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var statusFilter by remember { mutableStateOf<OrderStatus?>(null) }
  val greenBorderBrush = rememberAnimatedGreenBorderBrush()

  // Merge local Room orders with cloud-synced orders when signed in (deduplicated by orderCode)
  val mergedOrders =
    remember(uiState.orders, uiState.cloudOrders) {
      val localCodes = uiState.orders.map { it.orderCode }.toSet()
      val cloudExtras =
        uiState.cloudOrders
          .filter { it.orderCode !in localCodes }
          .mapIndexed { idx, cloud ->
            TopUpOrderEntity(
              id = -(idx + 1),
              orderCode = cloud.orderCode,
              categoryId = cloud.categoryId,
              packageTitleEn = cloud.packageTitleEn,
              packageTitleBn = cloud.packageTitleBn,
              priceBdt = cloud.priceBdt.toInt(),
              playerUid = cloud.playerUid,
              playerName = cloud.playerName,
              customerEmail = cloud.customerEmail,
              serverRegion = cloud.serverRegion,
              paymentMethodId = cloud.paymentMethodId,
              senderNumber = cloud.senderNumber,
              trxId = cloud.trxId,
              statusId = cloud.statusId,
              createdAt = cloud.createdAt?.toDate()?.time ?: System.currentTimeMillis(),
            )
          }
      (uiState.orders + cloudExtras).sortedByDescending { it.createdAt }
    }

  val filteredOrders =
    remember(mergedOrders, statusFilter) {
      if (statusFilter == null) mergedOrders
      else mergedOrders.filter { it.statusId == statusFilter?.id }
    }

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    Column(modifier = Modifier.fillMaxSize().widthIn(max = 820.dp)) {
      Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Orders • অর্ডার",
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
              text = "আপনার ডায়মন্ড অর্ডার ও রসিদ",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }

          if (uiState.authUser != null) {
            Surface(
              shape = RoundedCornerShape(999.dp),
              color = ProductGreen.copy(alpha = 0.14f),
              border = BorderStroke(1.dp, ProductGreen),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Filled.CloudDone,
                  contentDescription = "Cloud Synced",
                  tint = ProductGreen,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = "Synced",
                  style = MaterialTheme.typography.labelSmall,
                  color = ProductGreen,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          FilterChip(
            selected = statusFilter == null,
            onClick = { statusFilter = null },
            label = {
              Text("All (${mergedOrders.size})", style = MaterialTheme.typography.labelSmall)
            },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = ProductGreen,
                selectedLabelColor = Color.White,
              ),
          )
          OrderStatus.entries.forEach { status ->
            FilterChip(
              selected = statusFilter == status,
              onClick = { statusFilter = status },
              label = { Text(status.labelEn, style = MaterialTheme.typography.labelSmall) },
              colors =
                FilterChipDefaults.filterChipColors(
                  selectedContainerColor = ProductGreen,
                  selectedLabelColor = Color.White,
                ),
            )
          }
        }
      }

      if (filteredOrders.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize().padding(20.dp),
          contentAlignment = Alignment.Center,
        ) {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.6.dp, greenBorderBrush),
          ) {
            Column(
              modifier = Modifier.padding(22.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Filled.ReceiptLong,
                contentDescription = "No Orders",
                tint = ProductGreen,
                modifier = Modifier.size(36.dp),
              )
              Text(
                text = "কোনো অর্ডার নেই • No Orders",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
              )
              Text(
                text = "শপ থেকে টপ-আপ অর্ডার করলে এখানে দেখতে পাবেন।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
              )
              Button(
                onClick = onBrowseShop,
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = ProductGreen,
                    contentColor = Color.White,
                  ),
                modifier = Modifier.height(44.dp).testTag("empty_orders_browse_button"),
              ) {
                Text("Go to Shop • শপে যান", style = MaterialTheme.typography.labelMedium)
              }
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          items(filteredOrders, key = { "${it.id}_${it.orderCode}" }) { order ->
            OrderCard(
              order = order,
              greenBorderBrush = greenBorderBrush,
              onClick = { onOrderClick(order) },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun OrderCard(
  order: TopUpOrderEntity,
  greenBorderBrush: Brush,
  onClick: () -> Unit,
) {
  val category = TopUpCategory.fromId(order.categoryId)
  val payment = PaymentMethod.fromId(order.paymentMethodId)
  val status = OrderStatus.fromId(order.statusId)
  val timeText =
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

  Card(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .testTag("order_card_${order.orderCode}"),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, greenBorderBrush),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${order.orderCode} • ${category.titleEn}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = timeText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        OrderStatusChip(status = status)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${order.packageTitleEn} (${order.packageTitleBn})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = "UID: ${order.playerUid} • ${payment.labelEn} (${order.trxId})",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "৳${order.priceBdt}",
          style = MaterialTheme.typography.titleMedium,
          color = ProductGreen,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}
