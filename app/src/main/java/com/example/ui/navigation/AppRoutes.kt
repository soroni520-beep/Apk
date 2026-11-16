package com.example.ui.navigation

object AppRoutes {
  const val ROUTE_SHOP = "shop"
  const val ROUTE_ORDERS = "orders"
  const val ROUTE_ADMIN = "admin"
  const val ARG_CATEGORY_ID = "categoryId"
  const val ROUTE_DETAIL = "detail/{$ARG_CATEGORY_ID}"
  const val ROUTE_PAYMENT = "payment/{$ARG_CATEGORY_ID}"

  fun detailRoute(categoryId: String): String = "detail/$categoryId"

  fun paymentRoute(categoryId: String): String = "payment/$categoryId"
}
