package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.local.TopUpOrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.TopUpCategory
import com.example.ui.LanguageFocus
import com.example.ui.theme.ProductGreen
import com.example.ui.theme.ProductGreenBright
import com.example.ui.theme.ProductGreenSoft
import com.example.ui.theme.StatusCancelBg
import com.example.ui.theme.StatusCancelFg
import com.example.ui.theme.StatusPendingBg
import com.example.ui.theme.StatusPendingFg
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.StatusSuccessFg
import com.example.ui.theme.TitleOrange
import com.example.ui.theme.TitleRed
import com.example.util.rememberLocalImageBitmap
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val ADMIN_PIN = "52000"

@Composable
fun rememberAnimatedGreenBorderBrush(): Brush {
  val infiniteTransition = rememberInfiniteTransition(label = "greenBorder")
  val colorA by
    infiniteTransition.animateColor(
      initialValue = ProductGreen,
      targetValue = ProductGreenSoft,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 1500, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "greenA",
    )
  val colorB by
    infiniteTransition.animateColor(
      initialValue = ProductGreenSoft,
      targetValue = ProductGreenBright,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 1500, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "greenB",
    )
  return Brush.linearGradient(listOf(colorA, colorB, ProductGreen))
}

@Composable
fun rememberAnimatedOrangeBorderBrush(): Brush {
  val infiniteTransition = rememberInfiniteTransition(label = "orangeBorder")
  val colorA by
    infiniteTransition.animateColor(
      initialValue = TitleOrange,
      targetValue = Color(0xFFFB923C),
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 1400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "orangeA",
    )
  val colorB by
    infiniteTransition.animateColor(
      initialValue = Color(0xFFFDBA74),
      targetValue = TitleRed,
      animationSpec =
        infiniteRepeatable(
          animation = tween(durationMillis = 1400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "orangeB",
    )
  return Brush.linearGradient(listOf(colorA, colorB, TitleOrange))
}

@Composable
fun AdminPasswordDialog(
  onDismiss: () -> Unit,
  onVerified: () -> Unit,
) {
  var pin by remember { mutableStateOf("") }
  var showPin by remember { mutableStateOf(false) }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = MaterialTheme.colorScheme.surface,
    icon = {
      Icon(
        imageVector = Icons.Filled.Lock,
        contentDescription = "Admin Lock",
        tint = ProductGreen,
        modifier = Modifier.size(28.dp),
      )
    },
    title = {
      Text(
        text = "Admin Password • এডমিন পাসওয়ার্ড",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
          value = pin,
          onValueChange = {
            pin = it.filter { ch -> ch.isDigit() }.take(10)
            errorText = null
          },
          label = { Text("Enter PIN") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
          visualTransformation =
            if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { showPin = !showPin }) {
              Icon(
                imageVector = if (showPin) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                contentDescription = "Toggle Password",
              )
            }
          },
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = ProductGreen,
              unfocusedBorderColor = MaterialTheme.colorScheme.outline,
              focusedTextColor = MaterialTheme.colorScheme.onSurface,
              unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            ),
          modifier = Modifier.fillMaxWidth().testTag("admin_pin_input"),
        )

        errorText?.let { err ->
          Text(
            text = err,
            style = MaterialTheme.typography.labelSmall,
            color = StatusCancelFg,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (pin == ADMIN_PIN) {
            onVerified()
          } else {
            errorText = "ভুল পাসওয়ার্ড • Wrong PIN"
          }
        },
        colors =
          ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
        modifier = Modifier.testTag("admin_pin_unlock_button"),
      ) {
        Text("Unlock")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@Composable
fun StepLoadingOverlay(
  visible: Boolean,
  message: String,
) {
  AnimatedVisibility(
    visible = visible,
    enter = fadeIn(tween(180)),
    exit = fadeOut(tween(180)),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(Color.Black.copy(alpha = 0.52f))
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = {},
          )
          .testTag("step_loading_overlay"),
      contentAlignment = Alignment.Center,
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(2.dp, rememberAnimatedGreenBorderBrush()),
        modifier = Modifier.padding(24.dp),
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 28.dp, vertical = 22.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          CircularProgressIndicator(
            color = ProductGreen,
            strokeWidth = 3.5.dp,
            modifier = Modifier.size(36.dp),
          )
          Text(
            text = message,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }
  }
}

@Composable
fun MasterJdBrandHeader(
  storeName: String,
  isShopOpen: Boolean,
  isDarkMode: Boolean,
  languageFocus: LanguageFocus,
  onToggleDarkMode: () -> Unit,
  onLanguageCycle: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 9.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Box(
          modifier =
            Modifier.size(32.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(ProductGreen),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Filled.Diamond,
            contentDescription = "Master JD Logo",
            tint = Color.White,
            modifier = Modifier.size(17.dp),
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
          Text(
            text = storeName,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier.size(6.dp)
                  .clip(CircleShape)
                  .background(if (isShopOpen) ProductGreenBright else StatusCancelFg)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isShopOpen) "Online" else "Offline",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
          }
        }
      }

      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          onClick = onToggleDarkMode,
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.testTag("theme_mode_toggle"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = if (isDarkMode) Icons.Filled.LightMode else Icons.Filled.DarkMode,
              contentDescription = if (isDarkMode) "White Mode" else "Dark Mode",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isDarkMode) "White" else "Dark",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }

        Surface(
          onClick = onLanguageCycle,
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          modifier = Modifier.testTag("language_cycle_button"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.Translate,
              contentDescription = "Language",
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = languageFocus.labelEn,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }
  }
}

@Composable
fun LocalOrResImage(
  localPath: String?,
  @DrawableRes fallbackRes: Int,
  contentDescription: String,
  modifier: Modifier = Modifier,
  contentScale: ContentScale = ContentScale.Crop,
) {
  val customBitmap = rememberLocalImageBitmap(localPath)
  if (customBitmap != null) {
    Image(
      bitmap = customBitmap,
      contentDescription = contentDescription,
      contentScale = contentScale,
      modifier = modifier,
    )
  } else {
    Image(
      painter = painterResource(id = fallbackRes),
      contentDescription = contentDescription,
      contentScale = contentScale,
      modifier = modifier,
    )
  }
}

@Composable
fun BilingualTextBlock(
  bengali: String,
  english: String,
  languageFocus: LanguageFocus,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    if (languageFocus == LanguageFocus.BOTH || languageFocus == LanguageFocus.BENGALI) {
      Text(
        text = "বাংলা: $bengali",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }

    if (languageFocus == LanguageFocus.BOTH || languageFocus == LanguageFocus.ENGLISH) {
      Text(
        text = "EN: $english",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun OrderStatusChip(status: OrderStatus, modifier: Modifier = Modifier) {
  val (bg, fg, icon) =
    when (status) {
      OrderStatus.PENDING -> Triple(StatusPendingBg, StatusPendingFg, Icons.Filled.Schedule)
      OrderStatus.DELIVERED -> Triple(StatusSuccessBg, StatusSuccessFg, Icons.Filled.CheckCircle)
      OrderStatus.CANCELLED -> Triple(StatusCancelBg, StatusCancelFg, Icons.Filled.Schedule)
    }
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(999.dp),
    color = bg,
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = status.labelEn,
        tint = fg,
        modifier = Modifier.size(12.dp),
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "${status.labelEn} • ${status.labelBn}",
        style = MaterialTheme.typography.labelSmall,
        color = fg,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
fun OrderReceiptDialog(
  order: TopUpOrderEntity,
  onDismiss: () -> Unit,
) {
  val clipboardManager = LocalClipboardManager.current
  val category = TopUpCategory.fromId(order.categoryId)
  val payment = PaymentMethod.fromId(order.paymentMethodId)
  val status = OrderStatus.fromId(order.statusId)
  val formattedTime =
    SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = BorderStroke(1.5.dp, ProductGreen),
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column {
            Text(
              text = "MASTER JD TOP UP",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Receipt • রসিদ",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
          OrderStatusChip(status = status)
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        ReceiptRow("Order", order.orderCode, highlight = true)
        ReceiptRow("Type", "${category.titleEn} • ${category.titleBn}")
        ReceiptRow("Pack", "${order.packageTitleEn} (${order.packageTitleBn})")
        ReceiptRow("UID", "${order.playerUid} (${order.serverRegion})")
        if (order.customerEmail.isNotBlank()) {
          ReceiptRow("Gmail", order.customerEmail)
        }
        ReceiptRow("Pay", "${payment.labelEn} • ৳${order.priceBdt}")
        ReceiptRow("TrxID", order.trxId)
        ReceiptRow("Time", formattedTime)

        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              val summary =
                "MASTER JD TOP UP\nOrder: ${order.orderCode}\nPack: ${order.packageTitleEn} (৳${order.priceBdt})\nUID: ${order.playerUid} (${order.serverRegion})\nGmail: ${order.customerEmail}\nTrxID: ${order.trxId}"
              clipboardManager.setText(AnnotatedString(summary))
            },
            modifier = Modifier.weight(1f).height(42.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Icon(
              imageVector = Icons.Filled.ContentCopy,
              contentDescription = "Copy",
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Copy", style = MaterialTheme.typography.labelMedium)
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(1f).height(42.dp).testTag("close_receipt_button"),
            colors =
              ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
          ) {
            Text("Done", style = MaterialTheme.typography.labelMedium)
          }
        }
      }
    }
  }
}

@Composable
private fun ReceiptRow(label: String, value: String, highlight: Boolean = false) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall,
      color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
      fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
    )
  }
}
