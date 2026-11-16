package com.example

import com.example.data.local.TopUpOrderEntity
import com.example.data.model.OrderStatus
import com.example.util.GmailDeliveryMailer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun deliveryMailer_buildsBannerAndOrderDetailsList() {
    val order =
      TopUpOrderEntity(
        id = 1,
        orderCode = "JD-5200",
        categoryId = "uid",
        packageTitleEn = "115 Diamonds",
        packageTitleBn = "১১৫ ডায়মন্ড",
        priceBdt = 85,
        playerUid = "1948572630",
        playerName = "SA_ASIF",
        customerEmail = "buyer@gmail.com",
        serverRegion = "BD",
        paymentMethodId = "bkash",
        senderNumber = "01711223344",
        trxId = "TRX998877",
        statusId = OrderStatus.DELIVERED.id,
      )

    val html =
      GmailDeliveryMailer.buildDeliveryHtmlEmail(
        order = order,
        storeName = "MASTER JD TOP UP",
        supportContact = "+880 1700-000000",
        businessGmail = "shop@gmail.com",
      )
    assertTrue(html.contains("ORDER COMPLETED"))
    assertTrue(html.contains("OFFICIAL RECEIPT"))
    assertTrue(html.contains("JD-5200"))
    assertTrue(html.contains("SA_ASIF"))
    assertTrue(html.contains("buyer@gmail.com"))
    assertTrue(html.contains("shop@gmail.com"))
    assertTrue(html.contains("1948572630"))
    assertTrue(html.contains("TRX998877"))

    val plain = GmailDeliveryMailer.buildPlainTextSummary(order, "MASTER JD TOP UP", "shop@gmail.com")
    assertTrue(plain.contains("JD-5200"))
    assertTrue(plain.contains("buyer@gmail.com"))
    assertTrue(plain.contains("shop@gmail.com"))
    assertTrue(plain.contains("1948572630"))
  }
}
