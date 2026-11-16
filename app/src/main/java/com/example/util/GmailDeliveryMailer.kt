package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Base64
import com.example.data.local.TopUpOrderEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.TopUpCategory
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class EmailSendResult {
  data class Sent(val recipient: String) : EmailSendResult()

  data object NeedsConfig : EmailSendResult()

  data class Failed(val reason: String) : EmailSendResult()
}

object GmailDeliveryMailer {

  fun buildDeliveryHtmlEmail(
    order: TopUpOrderEntity,
    storeName: String,
    supportContact: String,
    businessGmail: String = "",
  ): String {
    val category = TopUpCategory.fromId(order.categoryId)
    val payment = PaymentMethod.fromId(order.paymentMethodId)
    val formattedTime =
      SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    fun esc(text: String): String =
      text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;")

    fun receiptRow(label: String, value: String, bgColor: String, valueColor: String = "#111827", valueSize: String = "18px"): String =
      """
      <tr>
        <td style="padding:14px 16px;background:$bgColor;border-bottom:1px solid #e5e7eb;font-size:16px;color:#4b5563;font-weight:600;vertical-align:top;width:44%;">
          $label
        </td>
        <td style="padding:14px 16px;background:$bgColor;border-bottom:1px solid #e5e7eb;font-size:$valueSize;color:$valueColor;font-weight:800;text-align:right;vertical-align:top;word-break:break-word;">
          $value
        </td>
      </tr>
      """.trimIndent()

    val senderRow =
      if (businessGmail.isNotBlank()) {
        receiptRow("• Business Gmail:", esc(businessGmail), "#f9fafb", "#16a34a", "16px")
      } else ""

    return """
      <!DOCTYPE html>
      <html>
      <body style="margin:0;padding:16px;background-color:#f3f5f7;font-family:Arial,Helvetica,sans-serif;">
        <div style="max-width:620px;margin:0 auto;background:#ffffff;border-radius:18px;overflow:hidden;border:3px solid #16a34a;">
          <!-- Big Colorful Banner -->
          <div style="background:#16a34a;background:linear-gradient(135deg,#f97316 0%,#ef4444 45%,#16a34a 100%);padding:32px 22px;text-align:center;color:#ffffff;">
            <div style="font-size:15px;font-weight:800;letter-spacing:2px;text-transform:uppercase;">${esc(storeName)}</div>
            <h1 style="margin:12px 0 8px 0;font-size:28px;line-height:1.2;font-weight:900;">ORDER COMPLETED</h1>
            <div style="font-size:20px;font-weight:800;line-height:1.35;">আপনার অর্ডার সফলভাবে কমপ্লিট হয়েছে!</div>
            <div style="display:inline-block;margin-top:16px;background:rgba(0,0,0,0.28);padding:8px 18px;border-radius:999px;font-size:17px;font-weight:800;">
              Order ID: ${esc(order.orderCode)}
            </div>
          </div>

          <!-- Large Receipt Details -->
          <div style="padding:22px 20px;color:#111827;">
            <div style="font-size:20px;font-weight:800;color:#111827;margin-bottom:6px;">
              অর্ডার রসিদ • OFFICIAL RECEIPT
            </div>
            <p style="margin:0 0 16px 0;font-size:16px;line-height:1.5;color:#374151;">
              ধন্যবাদ! আপনার ফ্রি ফায়ার ডায়মন্ড টপ-আপ ডেলিভারি সম্পন্ন হয়েছে। নিচে সম্পূর্ণ রসিদ লিস্ট দেওয়া হলো:
            </p>

            <table width="100%" cellpadding="0" cellspacing="0" border="0" style="width:100%;border-collapse:collapse;border:2px solid #d1d5db;border-radius:12px;overflow:hidden;">
              ${receiptRow("• Order ID (অর্ডার আইডি):", esc(order.orderCode), "#ffffff", "#16a34a", "19px")}
              ${receiptRow("• User Name / Tag:", esc(order.playerName), "#f9fafb", "#111827", "18px")}
              ${receiptRow("• User Gmail:", esc(order.customerEmail), "#ffffff", "#111827", "16px")}
              ${receiptRow("• Player UID (গেম আইডি):", "${esc(order.playerUid)} (${esc(order.serverRegion)})", "#f9fafb", "#111827", "19px")}
              ${receiptRow("• Package (প্যাকেজ):", "${esc(category.titleEn)} - ${esc(order.packageTitleEn)}", "#ffffff", "#111827", "18px")}
              ${receiptRow("• Price (মোট টাকা):", "৳${order.priceBdt}", "#f9fafb", "#f97316", "22px")}
              ${receiptRow("• Payment &amp; Number:", "${esc(payment.labelEn)} (${esc(order.senderNumber)})", "#ffffff", "#111827", "17px")}
              ${receiptRow("• TrxID (ট্রানজেকশন):", esc(order.trxId), "#f9fafb", "#111827", "18px")}
              ${receiptRow("• Time (সময়):", esc(formattedTime), "#ffffff", "#111827", "16px")}
              $senderRow
              <tr>
                <td style="padding:16px;background:#e6f4ea;font-size:17px;color:#15803d;font-weight:800;">
                  • Status (অবস্থা):
                </td>
                <td style="padding:16px;background:#e6f4ea;font-size:19px;color:#15803d;font-weight:900;text-align:right;">
                  COMPLETED • সম্পন্ন
                </td>
              </tr>
            </table>

            <div style="margin-top:20px;padding:15px;border-radius:12px;background:#fff7ed;border:2px solid #fdba74;font-size:16px;color:#9a3412;text-align:center;line-height:1.5;">
              সাপোর্ট প্রয়োজন হলে যোগাযোগ করুন: <strong>${esc(supportContact)}</strong>
            </div>
          </div>

          <div style="background:#f3f4f6;padding:14px;text-align:center;font-size:14px;font-weight:700;color:#4b5563;">
            ${esc(storeName)} • © SA ASIF
          </div>
        </div>
      </body>
      </html>
    """.trimIndent()
  }

  fun buildPlainTextSummary(
    order: TopUpOrderEntity,
    storeName: String,
    businessGmail: String = "",
  ): String {
    val category = TopUpCategory.fromId(order.categoryId)
    val payment = PaymentMethod.fromId(order.paymentMethodId)
    val formattedTime =
      SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(order.createdAt))

    return buildString {
      appendLine("======================================")
      appendLine("   $storeName - ORDER COMPLETED")
      appendLine("   আপনার অর্ডার সফলভাবে কমপ্লিট হয়েছে!")
      appendLine("======================================")
      appendLine()
      appendLine("• Order ID: ${order.orderCode}")
      appendLine("• User Name: ${order.playerName}")
      appendLine("• User Gmail: ${order.customerEmail}")
      appendLine("• Player UID: ${order.playerUid} (${order.serverRegion})")
      appendLine("• Service: ${category.titleEn} (${category.titleBn})")
      appendLine("• Package: ${order.packageTitleEn} (${order.packageTitleBn})")
      appendLine("• Amount: ৳${order.priceBdt}")
      appendLine("• Payment: ${payment.labelEn} | Sender: ${order.senderNumber}")
      appendLine("• TrxID: ${order.trxId}")
      appendLine("• Time: $formattedTime")
      if (businessGmail.isNotBlank()) {
        appendLine("• Sent From: $businessGmail")
      }
      appendLine("• Status: COMPLETED (সম্পন্ন)")
      appendLine()
      appendLine("ধন্যবাদ! আমাদের সাথে থাকার জন্য।")
    }
  }

  suspend fun sendAutomatedOrderCompletedEmail(
    adminGmail: String,
    appPassword: String,
    order: TopUpOrderEntity,
    storeName: String,
    supportContact: String,
  ): EmailSendResult =
    withContext(Dispatchers.IO) {
      val cleanFrom = adminGmail.trim()
      val cleanPass = appPassword.replace(" ", "").trim()
      val cleanTo = order.customerEmail.trim()

      if (cleanTo.isBlank() || !cleanTo.contains("@")) {
        return@withContext EmailSendResult.Failed("Customer Gmail missing")
      }
      if (cleanFrom.isBlank() || !cleanFrom.contains("@") || cleanPass.length < 8) {
        return@withContext EmailSendResult.NeedsConfig
      }

      var socket: SSLSocket? = null
      try {
        val factory = SSLSocketFactory.getDefault() as SSLSocketFactory
        socket =
          (factory.createSocket("smtp.gmail.com", 465) as SSLSocket).apply {
            soTimeout = 15_000
            startHandshake()
          }

        val reader = BufferedReader(InputStreamReader(socket.inputStream, StandardCharsets.UTF_8))
        val writer = BufferedWriter(OutputStreamWriter(socket.outputStream, StandardCharsets.UTF_8))

        fun readReply(expectedPrefix: String): String {
          val lines = mutableListOf<String>()
          while (true) {
            val line = reader.readLine() ?: error("SMTP connection closed unexpectedly")
            lines.add(line)
            if (line.length >= 4 && line[3] == ' ') break
            if (line.length < 4) break
          }
          val last = lines.lastOrNull().orEmpty()
          if (!last.startsWith(expectedPrefix)) {
            error("SMTP error: $last")
          }
          return last
        }

        fun sendCmd(cmd: String, expectedPrefix: String) {
          writer.write(cmd)
          writer.write("\r\n")
          writer.flush()
          readReply(expectedPrefix)
        }

        fun b64(value: String): String =
          Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), Base64.NO_WRAP)

        fun b64Crlf(value: String): String =
          Base64.encodeToString(value.toByteArray(StandardCharsets.UTF_8), Base64.DEFAULT)
            .replace("\r\n", "\n")
            .replace("\n", "\r\n")
            .trimEnd('\r', '\n')

        readReply("220")
        sendCmd("EHLO localhost", "250")
        sendCmd("AUTH LOGIN", "334")
        sendCmd(b64(cleanFrom), "334")
        sendCmd(b64(cleanPass), "235")
        sendCmd("MAIL FROM:<$cleanFrom>", "250")
        sendCmd("RCPT TO:<$cleanTo>", "250")
        sendCmd("DATA", "354")

        val safeStoreName = storeName.ifBlank { "MASTER JD TOP UP" }
        val encodedFromName = "=?UTF-8?B?${b64(safeStoreName)}?="
        val subject = "$safeStoreName - Order Completed (${order.orderCode})"
        val encodedSubject = "=?UTF-8?B?${b64(subject)}?="
        val rfcDate =
          SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US).format(Date())
        val messageId = "<${UUID.randomUUID()}@masterjdtopup.app>"
        val boundary = "----=_MasterJD_${System.currentTimeMillis()}"
        val plainBody = buildPlainTextSummary(order, safeStoreName, cleanFrom)
        val htmlBody = buildDeliveryHtmlEmail(order, safeStoreName, supportContact, cleanFrom)

        val mime = buildString {
          append("Date: $rfcDate\r\n")
          append("Message-ID: $messageId\r\n")
          append("From: \"$encodedFromName\" <$cleanFrom>\r\n")
          append("Reply-To: <$cleanFrom>\r\n")
          append("To: <$cleanTo>\r\n")
          append("Subject: $encodedSubject\r\n")
          append("MIME-Version: 1.0\r\n")
          append("Content-Type: multipart/alternative; boundary=\"$boundary\"\r\n")
          append("\r\n")
          append("--$boundary\r\n")
          append("Content-Type: text/plain; charset=UTF-8\r\n")
          append("Content-Transfer-Encoding: base64\r\n\r\n")
          append(b64Crlf(plainBody))
          append("\r\n\r\n--$boundary\r\n")
          append("Content-Type: text/html; charset=UTF-8\r\n")
          append("Content-Transfer-Encoding: base64\r\n\r\n")
          append(b64Crlf(htmlBody))
          append("\r\n\r\n--$boundary--\r\n")
          append(".")
        }

        sendCmd(mime, "250")
        runCatching { sendCmd("QUIT", "221") }
        EmailSendResult.Sent(cleanTo)
      } catch (e: Exception) {
        EmailSendResult.Failed(e.localizedMessage ?: "SMTP failed")
      } finally {
        runCatching { socket?.close() }
      }
    }

  fun launchGmailComposeFallback(
    context: Context,
    order: TopUpOrderEntity,
    storeName: String,
    businessGmail: String = "",
  ) {
    val subject = "$storeName - Order Completed (${order.orderCode})"
    val body = buildPlainTextSummary(order, storeName, businessGmail)
    val uri =
      Uri.parse(
        "mailto:${Uri.encode(order.customerEmail)}?subject=${Uri.encode(subject)}&body=${Uri.encode(body)}"
      )
    val intent = Intent(Intent.ACTION_SENDTO, uri).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
    runCatching { context.startActivity(intent) }
  }
}
