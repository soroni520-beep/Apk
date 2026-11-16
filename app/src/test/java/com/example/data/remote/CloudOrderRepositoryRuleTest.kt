package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.local.TopUpOrderEntity
import com.example.data.model.OrderStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.UUID
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CloudOrderRepositoryRuleTest : FirestoreEmulatorTestBase() {

  private fun sampleOrder(): TopUpOrderEntity =
    TopUpOrderEntity(
      orderCode = "JD-${UUID.randomUUID().toString().take(4).uppercase()}",
      categoryId = "uid",
      packageTitleEn = "115 Diamonds",
      packageTitleBn = "১১৫ ডায়মন্ড",
      priceBdt = 85,
      playerUid = "1948572630",
      playerName = "JD_GAMER",
      customerEmail = "player@gmail.com",
      serverRegion = "BD",
      paymentMethodId = "bkash",
      senderNumber = "01711223344",
      trxId = "BL94X72K8M",
      statusId = OrderStatus.PENDING.id,
    )

  @Test
  fun saveAndObserveOrder_authenticatedOwner_succeeds() = runBlocking {
    val uid = signInTestUser("alice@test.com")
    val repo = CloudOrderRepository(firestore)

    val result = withTimeout(5000L) { repo.saveCloudOrder(uid, sampleOrder()) }
    assertTrue(result.isSuccess)
    val docId = result.getOrThrow()

    val orders =
      withTimeout(4000L) {
        repo.observeUserOrders(uid).first { list -> list.any { it.id == docId } }
      }
    assertTrue(orders.any { it.id == docId && it.playerUid == "1948572630" })
  }

  @Test
  fun observeOrders_crossUser_failsWithPermissionDenied() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    val repo = CloudOrderRepository(firestore)
    withTimeout(5000L) { repo.saveCloudOrder(aliceUid, sampleOrder()).getOrThrow() }

    signInTestUser("bob@test.com")
    val thrown =
      try {
        withTimeout(3000L) { repo.observeUserOrders(aliceUid).first() }
        null
      } catch (e: Exception) {
        e
      }
    val firestoreException = checkNotNull(thrown?.unwrapFirestoreException())
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
  }

  @Test
  fun observeOrders_unauthenticated_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    val repo = CloudOrderRepository(firestore)

    val thrown =
      try {
        withTimeout(3000L) { repo.observeUserOrders("alice_uid").first() }
        null
      } catch (e: Exception) {
        e
      }
    val firestoreException = checkNotNull(thrown?.unwrapFirestoreException())
    assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
  }
}
