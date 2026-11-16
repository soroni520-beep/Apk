package com.example.ui.screens

import android.net.Uri
import android.widget.MediaController
import android.widget.VideoView
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.credentials.CredentialManager
import com.example.R
import com.example.auth.GoogleSignInHelper
import com.example.data.local.CategoryConfigEntity
import com.example.data.local.StoreSettingsEntity
import com.example.data.local.TopUpPackageEntity
import com.example.data.model.TopUpCategory
import com.example.ui.AuthUserInfo
import com.example.ui.LanguageFocus
import com.example.ui.MainUiState
import com.example.ui.components.LocalOrResImage
import com.example.ui.components.MasterJdBrandHeader
import com.example.ui.components.rememberAnimatedGreenBorderBrush
import com.example.ui.theme.ProductGreen
import com.example.ui.theme.ProductGreenBright
import com.example.ui.theme.TitleGreen
import com.example.ui.theme.TitleOrange
import com.example.ui.theme.TitleRed
import java.io.File

@Composable
fun ShopScreen(
  uiState: MainUiState,
  onToggleDarkMode: () -> Unit,
  onLanguageCycle: () -> Unit,
  onSelectCategory: (TopUpCategory) -> Unit,
  onAuthChanged: () -> Unit,
  onOpenAdminPanel: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val packagesByCategory = uiState.packages.groupBy { it.categoryId }
  val configsByCategory = uiState.categoryConfigs.associateBy { it.categoryId }
  val greenBorderBrush = rememberAnimatedGreenBorderBrush()

  Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 820.dp),
      contentPadding = PaddingValues(bottom = 14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      item {
        MasterJdBrandHeader(
          storeName = uiState.storeSettings.storeName,
          isShopOpen = uiState.storeSettings.isShopOpen,
          isDarkMode = uiState.storeSettings.isDarkMode,
          languageFocus = uiState.languageFocus,
          onToggleDarkMode = onToggleDarkMode,
          onLanguageCycle = onLanguageCycle,
        )
      }

      // Colorful Orange + Red + Green Top Title Strip
      item {
        TriColorPromoTitle(
          modifier = Modifier.padding(horizontal = 14.dp),
        )
      }

      // Compact Hero Banner with Animated Green Border
      item {
        HeroStoreBanner(
          storeSettings = uiState.storeSettings,
          borderBrush = greenBorderBrush,
          onQuickUidClick = { onSelectCategory(TopUpCategory.UID) },
          modifier = Modifier.padding(horizontal = 14.dp),
        )
      }

      // Animated Friday Bonus Offer Card with Rate (shown when ticked in Admin)
      item {
        AnimatedVisibility(
          visible = uiState.storeSettings.fridayBonusEnabled,
          enter =
            fadeIn() +
              expandVertically(
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
              ),
          exit = fadeOut() + shrinkVertically(),
        ) {
          FridayBonusOfferCard(
            settings = uiState.storeSettings,
            onClick = {
              val target =
                if (uiState.storeSettings.fridayBonusTarget == "monthly") TopUpCategory.MONTHLY
                else TopUpCategory.WEEKLY
              onSelectCategory(target)
            },
            modifier = Modifier.padding(horizontal = 14.dp),
          )
        }
      }

      // 3 Posters with Animated Green Borders & Minimal Text
      item {
        PosterShowcaseGrid(
          packagesByCategory = packagesByCategory,
          configsByCategory = configsByCategory,
          borderBrush = greenBorderBrush,
          languageFocus = uiState.languageFocus,
          onSelectCategory = onSelectCategory,
          modifier = Modifier.padding(horizontal = 14.dp),
        )
      }

      // How-to-Top-Up Video Section (Managed from Admin Panel)
      item {
        TutorialVideoCard(
          settings = uiState.storeSettings,
          borderBrush = greenBorderBrush,
          modifier = Modifier.padding(horizontal = 14.dp),
        )
      }

      // Ultra-Compact Contact Strip
      item {
        CompactSupportStrip(
          settings = uiState.storeSettings,
          modifier = Modifier.padding(horizontal = 14.dp),
        )
      }

      // Highlighted Login Card at Bottom
      item {
        HighlightedGmailLoginCard(
          authUser = uiState.authUser,
          onAuthChanged = onAuthChanged,
          modifier = Modifier.padding(horizontal = 14.dp),
        )
      }

      // © SA ASIF footer (3 taps opens Admin Panel)
      item {
        SaAsifFooter(
          onTripleTap = onOpenAdminPanel,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
        )
      }
    }
  }
}

@Composable
private fun TriColorPromoTitle(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    color = Color.Transparent,
  ) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(Brush.horizontalGradient(listOf(TitleOrange, TitleRed, TitleGreen)))
          .padding(horizontal = 12.dp, vertical = 9.dp),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = "সবার থেকে কম দামে ডায়মন্ড টপ আপ করুন এবং আমাদের সাপোর্ট করুন",
        style = MaterialTheme.typography.titleSmall,
        color = Color.White,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun HeroStoreBanner(
  storeSettings: StoreSettingsEntity,
  borderBrush: Brush,
  onQuickUidClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onQuickUidClick),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.8.dp, borderBrush),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Box(modifier = Modifier.fillMaxWidth().height(118.dp)) {
      LocalOrResImage(
        localPath = storeSettings.heroImagePath,
        fallbackRes = R.drawable.img_hero_banner,
        contentDescription = "MASTER JD TOP UP Banner",
        modifier = Modifier.fillMaxSize(),
      )

      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(Color.Black.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.72f))
              )
            )
      )

      Column(
        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(12.dp),
      ) {
        Text(
          text = storeSettings.storeName,
          style = MaterialTheme.typography.headlineSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = storeSettings.siteDescriptionBn,
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.9f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun FridayBonusOfferCard(
  settings: StoreSettingsEntity,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val targetLabel =
    when (settings.fridayBonusTarget) {
      "weekly" -> "WEEKLY"
      "monthly" -> "MONTHLY"
      else -> "WEEKLY + MONTHLY"
    }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .clickable(onClick = onClick)
        .testTag("friday_bonus_card"),
    shape = RoundedCornerShape(12.dp),
    border =
      BorderStroke(
        1.8.dp,
        Brush.horizontalGradient(listOf(TitleOrange, TitleRed, ProductGreenBright)),
      ),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(36.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(TitleOrange.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Filled.CardGiftcard,
          contentDescription = "Friday Bonus",
          tint = TitleOrange,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Surface(shape = RoundedCornerShape(4.dp), color = TitleRed) {
            Text(
              text = "FRIDAY",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
            )
          }
          Text(
            text = targetLabel,
            style = MaterialTheme.typography.labelSmall,
            color = ProductGreen,
            fontWeight = FontWeight.Bold,
          )
          if (settings.fridayBonusRate.isNotBlank()) {
            Surface(shape = RoundedCornerShape(4.dp), color = TitleOrange) {
              Text(
                text = settings.fridayBonusRate,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
              )
            }
          }
        }
        Text(
          text = settings.fridayBonusTitleBn,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open Bonus Offer",
        tint = TitleOrange,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun PosterShowcaseGrid(
  packagesByCategory: Map<String, List<TopUpPackageEntity>>,
  configsByCategory: Map<String, CategoryConfigEntity>,
  borderBrush: Brush,
  languageFocus: LanguageFocus,
  onSelectCategory: (TopUpCategory) -> Unit,
  modifier: Modifier = Modifier,
) {
  val posters =
    listOf(
      PosterMeta(TopUpCategory.UID, R.drawable.img_poster_uid, "poster_uid"),
      PosterMeta(TopUpCategory.MONTHLY, R.drawable.img_poster_monthly, "poster_monthly"),
      PosterMeta(TopUpCategory.WEEKLY, R.drawable.img_poster_weekly, "poster_weekly"),
    )

  BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
    val isWide = maxWidth >= 600.dp
    if (isWide) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        posters.forEach { meta ->
          TopUpPosterCard(
            meta = meta,
            config = configsByCategory[meta.category.id],
            packages = packagesByCategory[meta.category.id].orEmpty(),
            borderBrush = borderBrush,
            languageFocus = languageFocus,
            onClick = { onSelectCategory(meta.category) },
            modifier = Modifier.weight(1f),
          )
        }
      }
    } else {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        posters.forEach { meta ->
          TopUpPosterCard(
            meta = meta,
            config = configsByCategory[meta.category.id],
            packages = packagesByCategory[meta.category.id].orEmpty(),
            borderBrush = borderBrush,
            languageFocus = languageFocus,
            onClick = { onSelectCategory(meta.category) },
          )
        }
      }
    }
  }
}

private data class PosterMeta(
  val category: TopUpCategory,
  @DrawableRes val fallbackRes: Int,
  val testTag: String,
)

@Composable
private fun TopUpPosterCard(
  meta: PosterMeta,
  config: CategoryConfigEntity?,
  packages: List<TopUpPackageEntity>,
  borderBrush: Brush,
  languageFocus: LanguageFocus,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val category = meta.category
  val titleEn = config?.titleEn ?: category.titleEn
  val titleBn = config?.titleBn ?: category.titleBn
  val minPrice = packages.minOfOrNull { it.priceBdt } ?: 0

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
        .testTag(meta.testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.8.dp, borderBrush),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      LocalOrResImage(
        localPath = config?.posterImagePath,
        fallbackRes = meta.fallbackRes,
        contentDescription = titleEn,
        modifier = Modifier.size(width = 86.dp, height = 66.dp).clip(RoundedCornerShape(10.dp)),
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.spacedBy(3.dp),
      ) {
        Text(
          text = if (languageFocus == LanguageFocus.ENGLISH) titleEn else "$titleEn • $titleBn",
          style = MaterialTheme.typography.titleMedium,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (minPrice > 0) {
            Surface(shape = RoundedCornerShape(6.dp), color = ProductGreen.copy(alpha = 0.14f)) {
              Text(
                text = "৳$minPrice+",
                style = MaterialTheme.typography.labelSmall,
                color = ProductGreen,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              )
            }
          }
          Text(
            text = "${packages.size} Packs",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open $titleEn",
        tint = ProductGreen,
        modifier = Modifier.size(18.dp),
      )
    }
  }
}

@Composable
private fun TutorialVideoCard(
  settings: StoreSettingsEntity,
  borderBrush: Brush,
  modifier: Modifier = Modifier,
) {
  val videoFile = settings.tutorialVideoPath?.let { File(it) }?.takeIf { it.exists() }
  var isPlaying by remember(settings.tutorialVideoPath) { mutableStateOf(false) }

  Card(
    modifier = modifier.fillMaxWidth().testTag("tutorial_video_card"),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.6.dp, borderBrush),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.VideoLibrary,
          contentDescription = null,
          tint = ProductGreen,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = "${settings.tutorialVideoTitleBn} • ${settings.tutorialVideoTitleEn}",
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      if (videoFile != null) {
        if (isPlaying) {
          AndroidView(
            factory = { ctx ->
              VideoView(ctx).apply {
                setVideoURI(Uri.fromFile(videoFile))
                val controller = MediaController(ctx)
                controller.setAnchorView(this)
                setMediaController(controller)
                setOnPreparedListener { mp ->
                  mp.isLooping = false
                  start()
                }
                setOnErrorListener { _, _, _ ->
                  isPlaying = false
                  true
                }
              }
            },
            modifier =
              Modifier.fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(10.dp)),
          )
        } else {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable { isPlaying = true },
            contentAlignment = Alignment.Center,
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                imageVector = Icons.Filled.PlayCircleFilled,
                contentDescription = "Play Tutorial Video",
                tint = ProductGreenBright,
                modifier = Modifier.size(44.dp),
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "ভিডিও দেখুন • Play Video",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
              )
            }
          }
        }
      } else {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Filled.PlayCircleFilled,
              contentDescription = null,
              tint = ProductGreen,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "এডমিন প্যানেল থেকে গাইড ভিডিও যুক্ত করা যাবে",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun CompactSupportStrip(
  settings: StoreSettingsEntity,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(10.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Icon(
          imageVector = Icons.Filled.SupportAgent,
          contentDescription = "Support",
          tint = ProductGreen,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = settings.supportContact,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      Text(
        text = "bKash • Nagad • Rocket",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun HighlightedGmailLoginCard(
  authUser: AuthUserInfo?,
  onAuthChanged: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var authError by remember { mutableStateOf<String?>(null) }

  Card(
    modifier = modifier.fillMaxWidth().testTag("bottom_login_card"),
    shape = RoundedCornerShape(14.dp),
    border =
      BorderStroke(
        width = 1.8.dp,
        brush =
          if (authUser == null) Brush.horizontalGradient(listOf(TitleOrange, ProductGreenBright))
          else Brush.horizontalGradient(listOf(ProductGreen, ProductGreenBright)),
      ),
    colors =
      CardDefaults.cardColors(
        containerColor =
          if (authUser == null) TitleOrange.copy(alpha = 0.08f)
          else ProductGreen.copy(alpha = 0.08f)
      ),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      if (authUser == null) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = Icons.Filled.AccountCircle,
              contentDescription = null,
              tint = TitleOrange,
              modifier = Modifier.size(26.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "লগইন করুন • Gmail Login",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "ফোনের Gmail দিয়ে লগইন",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          Button(
            onClick = {
              isLoading = true
              authError = null
              GoogleSignInHelper.onGoogleSignInClicked(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {
                  isLoading = false
                  onAuthChanged()
                },
                onAuthError = { msg ->
                  isLoading = false
                  authError = msg
                },
                scope = scope,
                onAuthCancelled = { isLoading = false },
              )
            },
            enabled = !isLoading,
            colors =
              ButtonDefaults.buttonColors(
                containerColor = TitleOrange,
                contentColor = Color.White,
              ),
            modifier = Modifier.height(42.dp).testTag("gmail_login_button"),
          ) {
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp,
              )
            } else {
              Icon(Icons.Filled.Login, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(5.dp))
              Text(
                "লগইন",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
        authError?.let { err ->
          Text(text = err, style = MaterialTheme.typography.labelSmall, color = TitleRed)
        }
      } else {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = "Logged In",
              tint = ProductGreen,
              modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = authUser.displayName ?: "Signed In",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
              )
              Text(
                text = authUser.email ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
          OutlinedButton(
            onClick = {
              GoogleSignInHelper.signOut(credentialManager, scope) { onAuthChanged() }
            },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.height(38.dp).testTag("sign_out_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.Logout,
              contentDescription = "Sign out",
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Logout", style = MaterialTheme.typography.labelSmall)
          }
        }
      }
    }
  }
}

@Composable
private fun SaAsifFooter(
  onTripleTap: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var tapCount by remember { mutableIntStateOf(0) }
  var lastTapTime by remember { mutableLongStateOf(0L) }

  Box(
    modifier =
      modifier
        .minimumInteractiveComponentSize()
        .clip(RoundedCornerShape(8.dp))
        .clickable {
          val now = System.currentTimeMillis()
          if (now - lastTapTime > 2000L) {
            tapCount = 1
          } else {
            tapCount += 1
          }
          lastTapTime = now
          if (tapCount >= 3) {
            tapCount = 0
            onTripleTap()
          }
        }
        .testTag("footer_sa_asif"),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = "© SA ASIF",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(vertical = 6.dp, horizontal = 16.dp),
    )
  }
}
