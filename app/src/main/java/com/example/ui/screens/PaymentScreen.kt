package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.PaymentMethod
import com.example.data.model.TopUpCategory
import com.example.ui.MainUiState
import com.example.ui.components.rememberAnimatedOrangeBorderBrush
import com.example.ui.theme.StatusCancelBg
import com.example.ui.theme.StatusCancelFg
import com.example.ui.theme.TitleOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
  category: TopUpCategory,
  uiState: MainUiState,
  onBack: () -> Unit,
  onPaymentMethodChange: (PaymentMethod) -> Unit,
  onSenderNumberChange: (String) -> Unit,
  onTrxIdChange: (String) -> Unit,
  onSubmitOrder: () -> Unit,
) {
  val packages = uiState.packages.filter { it.categoryId == category.id }
  val form = uiState.checkoutForm
  val selectedPackage =
    packages.firstOrNull { it.id == form.selectedPackageId } ?: packages.firstOrNull()
  val orangeBorderBrush = rememberAnimatedOrangeBorderBrush()

  val clipboardManager = LocalClipboardManager.current
  val snackbarHostState = remember { SnackbarHostState() }
  val scope = rememberCoroutineScope()

  val paymentNumber =
    when (form.paymentMethod) {
      PaymentMethod.BKASH -> uiState.storeSettings.bkashNumber
      PaymentMethod.NAGAD -> uiState.storeSettings.nagadNumber
      PaymentMethod.ROCKET -> uiState.storeSettings.rocketNumber
    }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Payment • পেমেন্ট",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("payment_back_button")) {
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
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 820.dp),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        // Order Summary Card with Orange Border
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, orangeBorderBrush),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = selectedPackage?.let { "${it.titleEn} • ${it.titleBn}" } ?: category.titleEn,
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text = "UID: ${form.playerUid} (${form.serverRegion})",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (form.customerEmail.isNotBlank()) {
                  Text(
                    text = form.customerEmail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
              Text(
                text = "৳${selectedPackage?.priceBdt ?: 0}",
                style = MaterialTheme.typography.titleLarge,
                color = TitleOrange,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }

        // Payment Methods & Info with Orange Border
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(2.dp, orangeBorderBrush),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Filled.Payments,
                  contentDescription = null,
                  tint = TitleOrange,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "পেমেন্ট মাধ্যম • Select Payment",
                  style = MaterialTheme.typography.titleSmall,
                  color = MaterialTheme.colorScheme.onSurface,
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                PaymentMethod.entries.forEach { method ->
                  val isSelected = form.paymentMethod == method
                  Surface(
                    onClick = { onPaymentMethodChange(method) },
                    modifier =
                      Modifier.weight(1f).height(42.dp).testTag("payment_${method.id}"),
                    shape = RoundedCornerShape(8.dp),
                    color =
                      if (isSelected) TitleOrange else MaterialTheme.colorScheme.surfaceVariant,
                    border =
                      BorderStroke(
                        1.4.dp,
                        if (isSelected) TitleOrange else TitleOrange.copy(alpha = 0.5f),
                      ),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = "${method.labelEn} • ${method.labelBn}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                      )
                    }
                  }
                }
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = TitleOrange.copy(alpha = 0.08f),
                border = BorderStroke(1.4.dp, orangeBorderBrush),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = "Send Money (${form.paymentMethod.labelEn}):",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                      text = paymentNumber,
                      style = MaterialTheme.typography.titleMedium,
                      color = MaterialTheme.colorScheme.onSurface,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                  Surface(
                    onClick = {
                      clipboardManager.setText(AnnotatedString(paymentNumber))
                      scope.launch { snackbarHostState.showSnackbar("Copied $paymentNumber") }
                    },
                    shape = RoundedCornerShape(8.dp),
                    color = TitleOrange,
                    modifier = Modifier.height(38.dp),
                  ) {
                    Row(
                      modifier = Modifier.padding(horizontal = 10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(
                        imageVector = Icons.Filled.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                      )
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Copy", style = MaterialTheme.typography.labelSmall, color = Color.White)
                    }
                  }
                }
              }

              OutlinedTextField(
                value = form.senderNumber,
                onValueChange = onSenderNumberChange,
                label = { Text("Your ${form.paymentMethod.labelEn} Number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                colors = orangeTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("sender_number_input"),
              )

              OutlinedTextField(
                value = form.trxId,
                onValueChange = onTrxIdChange,
                label = { Text("Transaction ID (TrxID)") },
                singleLine = true,
                colors = orangeTextFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("trx_id_input"),
              )
            }
          }
        }

        if (!form.validationError.isNullOrBlank()) {
          item {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = StatusCancelBg,
              border = BorderStroke(1.dp, StatusCancelFg.copy(alpha = 0.4f)),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Filled.ErrorOutline,
                  contentDescription = "Error",
                  tint = StatusCancelFg,
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = form.validationError,
                  style = MaterialTheme.typography.bodySmall,
                  color = StatusCancelFg,
                )
              }
            }
          }
        }

        item {
          Button(
            onClick = onSubmitOrder,
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_order_button"),
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(containerColor = TitleOrange, contentColor = Color.White),
          ) {
            Text(
              text =
                if (selectedPackage != null)
                  "অর্ডার করুন (৳${selectedPackage.priceBdt}) • Confirm"
                else "অর্ডার করুন • Confirm",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
private fun orangeTextFieldColors() =
  OutlinedTextFieldDefaults.colors(
    focusedBorderColor = TitleOrange,
    unfocusedBorderColor = TitleOrange.copy(alpha = 0.65f),
    focusedLabelColor = TitleOrange,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = TitleOrange,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
  )
