package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.auth.GoogleSignInHelper
import com.example.data.local.AppDatabase
import com.example.data.model.TopUpCategory
import com.example.data.remote.CloudOrderRepository
import com.example.data.repository.TopUpRepository
import com.example.ui.LanguageFocus
import com.example.ui.MainViewModel
import com.example.ui.components.AdminPasswordDialog
import com.example.ui.components.OrderReceiptDialog
import com.example.ui.components.StepLoadingOverlay
import com.example.ui.navigation.AppRoutes
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.TopUpDetailScreen
import com.example.ui.theme.MasterJdTheme
import com.example.ui.theme.ProductGreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val database = AppDatabase.getInstance(applicationContext)
    val repository = TopUpRepository(database.topUpDao())
    val firestore =
      FirebaseFirestore.getInstance(applicationContext.getString(R.string.firestore_database_id))
    val cloudOrderRepository = CloudOrderRepository(firestore)

    setContent {
      val viewModel: MainViewModel =
        viewModel(factory = MainViewModel.Factory(repository, cloudOrderRepository))
      MasterJdApp(viewModel = viewModel)
    }
  }
}

private data class BottomTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

@Composable
fun MasterJdApp(viewModel: MainViewModel) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isStepLoading by remember { mutableStateOf(false) }
  var loadingMessage by remember { mutableStateOf("লোড হচ্ছে...") }
  var showAdminPinDialog by remember { mutableStateOf(false) }

  fun runStepTransition(message: String, block: () -> Unit) {
    if (isStepLoading) return
    scope.launch {
      loadingMessage = message
      isStepLoading = true
      delay(2200L)
      block()
      isStepLoading = false
    }
  }

  DisposableEffect(Unit) {
    val auth = Firebase.auth
    val listener = FirebaseAuth.AuthStateListener { viewModel.syncAuthState() }
    auth.addAuthStateListener(listener)
    onDispose { auth.removeAuthStateListener(listener) }
  }

  LaunchedEffect(Unit) {
    GoogleSignInHelper.attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = { viewModel.syncAuthState() },
      onUnauthenticated = { viewModel.syncAuthState() },
      scope = scope,
    )
  }

  MasterJdTheme(darkTheme = uiState.storeSettings.isDarkMode) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val tabs =
      listOf(
        BottomTab(
          route = AppRoutes.ROUTE_SHOP,
          label = "Shop • শপ",
          selectedIcon = Icons.Filled.Storefront,
          unselectedIcon = Icons.Outlined.Storefront,
          testTag = "nav_shop",
        ),
        BottomTab(
          route = AppRoutes.ROUTE_ORDERS,
          label = "Orders • অর্ডার",
          selectedIcon = Icons.Filled.ReceiptLong,
          unselectedIcon = Icons.Outlined.ReceiptLong,
          testTag = "nav_orders",
        ),
      )

    val showTopLevelNav = currentRoute in tabs.map { it.route }

    fun navigateToTab(route: String) {
      if (currentRoute == route) return
      runStepTransition("লোড হচ্ছে...") {
        navController.navigate(route) {
          popUpTo(navController.graph.findStartDestination().id) { saveState = true }
          launchSingleTop = true
          restoreState = true
        }
      }
    }

    Box(modifier = Modifier.fillMaxSize()) {
      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 840.dp && showTopLevelNav

        Scaffold(
          containerColor = MaterialTheme.colorScheme.background,
          contentWindowInsets = WindowInsets.safeDrawing,
          bottomBar = {
            if (showTopLevelNav && !useRail) {
              NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                  val selected = currentRoute == tab.route
                  NavigationBarItem(
                    selected = selected,
                    onClick = { navigateToTab(tab.route) },
                    icon = {
                      Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                      )
                    },
                    label = { Text(tab.label) },
                    modifier = Modifier.testTag(tab.testTag),
                    colors =
                      NavigationBarItemDefaults.colors(
                        selectedIconColor = ProductGreen,
                        selectedTextColor = ProductGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      ),
                  )
                }
              }
            }
          },
        ) { innerPadding ->
          Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (useRail) {
              NavigationRail(containerColor = MaterialTheme.colorScheme.surface) {
                tabs.forEach { tab ->
                  val selected = currentRoute == tab.route
                  NavigationRailItem(
                    selected = selected,
                    onClick = { navigateToTab(tab.route) },
                    icon = {
                      Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                      )
                    },
                    label = { Text(tab.label) },
                    modifier = Modifier.testTag(tab.testTag),
                    colors =
                      NavigationRailItemDefaults.colors(
                        selectedIconColor = ProductGreen,
                        selectedTextColor = ProductGreen,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                      ),
                  )
                }
              }
            }

            NavHost(
              navController = navController,
              startDestination = AppRoutes.ROUTE_SHOP,
              modifier = Modifier.weight(1f).fillMaxSize(),
            ) {
              composable(AppRoutes.ROUTE_SHOP) {
                ShopScreen(
                  uiState = uiState,
                  onToggleDarkMode = viewModel::toggleDarkMode,
                  onLanguageCycle = {
                    val next =
                      when (uiState.languageFocus) {
                        LanguageFocus.BOTH -> LanguageFocus.BENGALI
                        LanguageFocus.BENGALI -> LanguageFocus.ENGLISH
                        LanguageFocus.ENGLISH -> LanguageFocus.BOTH
                      }
                    viewModel.setLanguageFocus(next)
                  },
                  onSelectCategory = { category ->
                    viewModel.prepareCategoryCheckout(category)
                    runStepTransition("প্যাকেজ লোড হচ্ছে...") {
                      navController.navigate(AppRoutes.detailRoute(category.id))
                    }
                  },
                  onAuthChanged = viewModel::syncAuthState,
                  onOpenAdminPanel = { showAdminPinDialog = true },
                )
              }

              composable(
                route = AppRoutes.ROUTE_DETAIL,
                arguments =
                  listOf(navArgument(AppRoutes.ARG_CATEGORY_ID) { type = NavType.StringType }),
              ) { entry ->
                val category =
                  TopUpCategory.fromId(entry.arguments?.getString(AppRoutes.ARG_CATEGORY_ID))
                TopUpDetailScreen(
                  category = category,
                  uiState = uiState,
                  onBack = { navController.popBackStack() },
                  onLanguageFocusChange = viewModel::setLanguageFocus,
                  onSelectPackage = viewModel::selectPackage,
                  onPlayerUidChange = viewModel::updatePlayerUid,
                  onCustomerEmailChange = viewModel::updateCustomerEmail,
                  onServerRegionChange = viewModel::selectServerRegion,
                  onNextToPayment = {
                    if (viewModel.validateUidStep(category)) {
                      runStepTransition("পেমেন্ট পেজ লোড হচ্ছে...") {
                        navController.navigate(AppRoutes.paymentRoute(category.id))
                      }
                    }
                  },
                )
              }

              composable(
                route = AppRoutes.ROUTE_PAYMENT,
                arguments =
                  listOf(navArgument(AppRoutes.ARG_CATEGORY_ID) { type = NavType.StringType }),
              ) { entry ->
                val category =
                  TopUpCategory.fromId(entry.arguments?.getString(AppRoutes.ARG_CATEGORY_ID))
                PaymentScreen(
                  category = category,
                  uiState = uiState,
                  onBack = { navController.popBackStack() },
                  onPaymentMethodChange = viewModel::selectPaymentMethod,
                  onSenderNumberChange = viewModel::updateSenderNumber,
                  onTrxIdChange = viewModel::updateTrxId,
                  onSubmitOrder = {
                    if (viewModel.validatePaymentStep(category)) {
                      runStepTransition("অর্ডার কনফার্ম হচ্ছে...") {
                        viewModel.submitOrder(category)
                        navController.popBackStack(AppRoutes.ROUTE_SHOP, inclusive = false)
                      }
                    }
                  },
                )
              }

              composable(AppRoutes.ROUTE_ORDERS) {
                OrdersScreen(
                  uiState = uiState,
                  onOrderClick = viewModel::showReceipt,
                  onBrowseShop = { navigateToTab(AppRoutes.ROUTE_SHOP) },
                )
              }

              composable(AppRoutes.ROUTE_ADMIN) {
                AdminPanelScreen(
                  uiState = uiState,
                  onBack = { navController.popBackStack() },
                  onUpdateOrderStatus = viewModel::updateOrderStatus,
                  onResendDeliveryEmail = viewModel::resendDeliveryEmail,
                  onDismissDeliveryBanner = viewModel::dismissDeliveryEmailBanner,
                  onSavePackage = viewModel::savePackage,
                  onDeletePackage = viewModel::deletePackage,
                  onSaveCategoryConfig = viewModel::saveCategoryConfig,
                  onSaveStoreSettings = viewModel::saveStoreSettings,
                )
              }
            }
          }
        }

        uiState.activeReceiptOrder?.let { receiptOrder ->
          OrderReceiptDialog(
            order = receiptOrder,
            onDismiss = viewModel::dismissReceipt,
          )
        }

        if (showAdminPinDialog) {
          AdminPasswordDialog(
            onDismiss = { showAdminPinDialog = false },
            onVerified = {
              showAdminPinDialog = false
              runStepTransition("এডমিন প্যানেল খুলছে...") {
                navController.navigate(AppRoutes.ROUTE_ADMIN)
              }
            },
          )
        }
      }

      StepLoadingOverlay(
        visible = isStepLoading,
        message = loadingMessage,
      )
    }
  }
}
