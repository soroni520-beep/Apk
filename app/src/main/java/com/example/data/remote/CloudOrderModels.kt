package com.example.data.remote

import android.util.Log
import com.example.data.local.TopUpOrderEntity
import com.example.data.model.OrderStatus
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
  CREATE("create"),
  UPDATE("update"),
  DELETE("delete"),
  LIST("list"),
  GET("get"),
  WRITE("write"),
}

fun handleFirestoreError(
  exception: Throwable,
  operationType: OperationType,
  path: String?,
): String {
  val causes = generateSequence(exception) { it.cause }
  val rootError =
    causes.firstOrNull { it is FirebaseFirestoreException }
      ?: causes.firstOrNull { it !is CancellationException }
      ?: exception

  val currentUser = Firebase.auth.currentUser
  val providerInfoList =
    currentUser?.providerData?.map { provider ->
      JSONObject().apply {
        put("providerId", provider.providerId)
        put("email", provider.email)
      }
    } ?: emptyList()

  val authInfoJson =
    JSONObject().apply {
      put("userId", currentUser?.uid)
      put("email", currentUser?.email)
      put("emailVerified", currentUser?.isEmailVerified)
      put("tenantId", currentUser?.tenantId)
      put("providerInfo", JSONArray(providerInfoList))
    }

  val errorInfoJson =
    JSONObject().apply {
      put("error", rootError.message ?: rootError.toString())
      put("operationType", operationType.value)
      put("path", path)
      put("authInfo", authInfoJson)
    }

  val jsonString = errorInfoJson.toString()
  Log.e("FirestoreError", "Firestore Error: $jsonString")
  return jsonString
}

@IgnoreExtraProperties
data class CloudOrder(
  @DocumentId val id: String = "",
  val userId: String = "",
  val orderCode: String = "",
  val categoryId: String = "",
  val packageTitleEn: String = "",
  val packageTitleBn: String = "",
  val priceBdt: Long = 0L,
  val playerUid: String = "",
  val playerName: String = "",
  val customerEmail: String = "",
  val serverRegion: String = "BD",
  val paymentMethodId: String = "bkash",
  val senderNumber: String = "",
  val trxId: String = "",
  val statusId: String = "pending",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

class CloudOrderRepository(private val db: FirebaseFirestore) {

  private fun ordersCollection(userId: String) =
    db.collection("users").document(userId).collection("orders")

  fun observeUserOrders(userId: String): Flow<List<CloudOrder>> {
    val path = "users/$userId/orders"
    return ordersCollection(userId)
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .snapshots()
      .map { snapshot ->
        snapshot.toObjects(
          CloudOrder::class.java,
          DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
        )
      }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        handleFirestoreError(firestoreError ?: error, OperationType.LIST, path)
        throw error
      }
  }

  suspend fun saveCloudOrder(userId: String, order: TopUpOrderEntity): Result<String> {
    val docRef = ordersCollection(userId).document()
    val payload =
      mapOf(
        "userId" to userId,
        "orderCode" to order.orderCode,
        "categoryId" to order.categoryId,
        "packageTitleEn" to order.packageTitleEn,
        "packageTitleBn" to order.packageTitleBn,
        "priceBdt" to order.priceBdt,
        "playerUid" to order.playerUid,
        "playerName" to order.playerName,
        "customerEmail" to order.customerEmail.ifBlank { "user@gmail.com" },
        "serverRegion" to order.serverRegion,
        "paymentMethodId" to order.paymentMethodId,
        "senderNumber" to order.senderNumber,
        "trxId" to order.trxId,
        "statusId" to order.statusId,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp(),
      )
    return try {
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, docRef.path)
      Result.failure(e)
    }
  }

  suspend fun updateCloudOrderStatus(
    userId: String,
    orderDocId: String,
    status: OrderStatus,
  ): Result<Unit> {
    val docRef = ordersCollection(userId).document(orderDocId)
    return try {
      docRef
        .update(
          mapOf(
            "statusId" to status.id,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, docRef.path)
      Result.failure(e)
    }
  }
}
