package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.TopUpPackageEntity
import com.example.data.model.TopUpCategory
import com.example.ui.LanguageFocus
import com.example.ui.MainUiState
import com.example.ui.components.BilingualTextBlock
import com.example.ui.components.LocalOrResImage
import com.example.ui.components.rememberAnimatedGreenBorderBrush
import com.example.ui.theme.ProductGreen
import com.example.ui.theme.StatusCancelBg
import com.example.ui.theme.StatusCancelFg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopUpDetailScreen(
  category: TopUpCategory,
  uiState: MainUiState,
  onBack: () -> Unit,
  onLanguageFocusChange: (LanguageFocus) -> Unit,
  onSelectPackage: (Int) -> Unit,
  onPlayerUidChange: (String) -> Unit,
  onCustomerEmailChange: (String) -> Unit,
  onServerRegionChange: (String) -> Unit,
  onNextToPayment: () -> Unit,
) {
  val packages = uiState.packages.filter { it.categoryId == category.id }
  val config = uiState.categoryConfigs.firstOrNull { it.categoryId == category.id }
  val titleEn = config?.titleEn ?: category.titleEn
  val titleBn = config?.titleBn ?: category.titleBn
  val guideEn = config?.guideEn ?: category.guideEn
  val guideBn = config?.guideBn ?: category.guideBn
  val greenBorderBrush = rememberAnimatedGreenBorderBrush()

  val form = uiState.checkoutForm
  val selectedPackage =
    packages.firstOrNull { it.id == form.selectedPackageId } ?: packages.firstOrNull()

  val posterDrawable =
    when (category) {
      TopUpCategory.UID -> R.drawable.img_poster_uid
      TopUpCategory.MONTHLY -> R.drawable.img_poster_monthly
      TopUpCategory.WEEKLY -> R.drawable.img_poster_weekly
    }

  Scaffold(
    containerColor = MaterialTheme.colorScheme.background,
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "$titleEn • $titleBn",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("detail_back_button")) {
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
        // Compact Poster Header with Green Border
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.8.dp, greenBorderBrush),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          ) {
            Box(modifier = Modifier.fillMaxWidth().height(98.dp)) {
              LocalOrResImage(
                localPath = config?.posterImagePath,
                fallbackRes = posterDrawable,
                contentDescription = titleEn,
                modifier = Modifier.fillMaxSize(),
              )
              Box(
                modifier =
                  Modifier.fillMaxSize()
                    .background(
                      Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                      )
                    )
              )
              Text(
                text = "$titleEn • $titleBn",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.align(Alignment.BottomStart).padding(12.dp),
              )
            }
          }
        }

        // Ultra-Minimal Guide + Language Chip
        item {
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(10.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                LanguageFocus.entries.forEach { focus ->
                  FilterChip(
                    selected = uiState.languageFocus == focus,
                    onClick = { onLanguageFocusChange(focus) },
                    label = { Text(focus.labelBn, style = MaterialTheme.typography.labelSmall) },
                  )
                }
              }
              BilingualTextBlock(
                bengali = guideBn,
                english = guideEn,
                languageFocus = uiState.languageFocus,
              )
            }
          }
        }

        // Packages Grid (Green Border)
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              text = "১. ডায়মন্ড বাছুন • Select Pack",
              style = MaterialTheme.typography.titleSmall,
              color = MaterialTheme.colorScheme.onBackground,
            )

            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
              val columns = if (maxWidth >= 560.dp) 3 else 2
              val rows = packages.chunked(columns)
              Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowPackages ->
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                  ) {
                    rowPackages.forEach { pkg ->
                      PackageOptionCard(
                        pkg = pkg,
                        selected = pkg.id == selectedPackage?.id,
                        greenBorderBrush = greenBorderBrush,
                        languageFocus = uiState.languageFocus,
                        onClick = { onSelectPackage(pkg.id) },
                        modifier = Modifier.weight(1f),
                      )
                    }
                    repeat(columns - rowPackages.size) { Spacer(modifier = Modifier.weight(1f)) }
                  }
                }
              }
            }
          }
        }

        // Player UID + User Gmail Directly Below UID
        item {
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.5.dp, greenBorderBrush),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "২. প্লেয়ার UID ও জিমেইল • UID & Gmail",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )

              OutlinedTextField(
                value = form.playerUid,
                onValueChange = onPlayerUidChange,
                label = { Text("Free Fire UID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = greenFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("uid_input"),
              )

              OutlinedTextField(
                value = form.customerEmail,
                onValueChange = onCustomerEmailChange,
                label = { Text("আপনার জিমেইল • Your Gmail") },
                supportingText = {
                  Text(
                    text = "ডেলিভারি হলে এই জিমেইলে মেসেজ যাবে",
                    style = MaterialTheme.typography.labelSmall,
                  )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = greenFieldColors(),
                modifier = Modifier.fillMaxWidth().testTag("customer_email_input"),
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("BD" to "BD", "IND" to "IND", "SG" to "SG").forEach { (code, label) ->
                  FilterChip(
                    selected = form.serverRegion == code,
                    onClick = { onServerRegionChange(code) },
                    label = { Text(label, style = MaterialTheme.typography.labelSmall) },
                    colors =
                      FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ProductGreen,
                        selectedLabelColor = Color.White,
                      ),
                  )
                }
              }
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

        // Next -> Payment Page
        item {
          Button(
            onClick = onNextToPayment,
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("next_to_payment_button"),
            shape = RoundedCornerShape(12.dp),
            colors =
              ButtonDefaults.buttonColors(containerColor = ProductGreen, contentColor = Color.White),
          ) {
            Text(
              text =
                if (selectedPackage != null)
                  "পরবর্তী (পেমেন্ট ৳${selectedPackage.priceBdt}) • Next"
                else "পরবর্তী • Next",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowForward,
              contentDescription = null,
              modifier = Modifier.size(18.dp),
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
        }
      }
    }
  }
}

@Composable
private fun greenFieldColors() =
  OutlinedTextFieldDefaults.colors(
    focusedBorderColor = ProductGreen,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    focusedLabelColor = ProductGreen,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    cursorColor = ProductGreen,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
  )

@Composable
private fun PackageOptionCard(
  pkg: TopUpPackageEntity,
  selected: Boolean,
  greenBorderBrush: Brush,
  languageFocus: LanguageFocus,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .testTag("package_${pkg.id}"),
    shape = RoundedCornerShape(12.dp),
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (selected) ProductGreen.copy(alpha = 0.12f)
          else MaterialTheme.colorScheme.surface
      ),
    border =
      if (selected) BorderStroke(2.2.dp, greenBorderBrush)
      else BorderStroke(1.4.dp, ProductGreen.copy(alpha = 0.65f)),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
      if (!pkg.imagePath.isNullOrBlank()) {
        LocalOrResImage(
          localPath = pkg.imagePath,
          fallbackRes = R.drawable.img_poster_uid,
          contentDescription = pkg.titleEn,
          modifier = Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(8.dp)),
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = if (languageFocus == LanguageFocus.BENGALI) pkg.titleBn else pkg.titleEn,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.weight(1f),
        )
        if (selected) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = "Selected",
            tint = ProductGreen,
            modifier = Modifier.size(16.dp),
          )
        }
      }

      if (languageFocus == LanguageFocus.BOTH) {
        Text(
          text = pkg.titleBn,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Text(
        text = "৳${pkg.priceBdt}",
        style = MaterialTheme.typography.titleMedium,
        color = ProductGreen,
        fontWeight = FontWeight.Bold,
      )
    }
  }
}
