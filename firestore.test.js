const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validOrderPayload(uid) {
  return {
    userId: uid,
    orderCode: "JD-1042",
    categoryId: "uid",
    packageTitleEn: "115 Diamonds",
    packageTitleBn: "১১৫ ডায়মন্ড",
    priceBdt: 85,
    playerUid: "1948572630",
    playerName: "JD_GAMER",
    customerEmail: "player@gmail.com",
    serverRegion: "BD",
    paymentMethodId: "bkash",
    senderNumber: "01711223344",
    trxId: "BL94X72K8M",
    statusId: "pending",
    createdAt: new Date(),
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot read or write orders", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("orders").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1").set(validOrderPayload(ALICE_UID))
  );
});

test("Authenticated owner can create and read own order", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1");
  await assertSucceeds(orderRef.set(validOrderPayload(ALICE_UID)));
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("orders").get());
});

test("Cross-user access is denied", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1").set(validOrderPayload(ALICE_UID))
  );

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("orders").get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_2").set(validOrderPayload(BOB_UID))
  );
});

test("Shadow field and terminal state lock are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const orderRef = aliceDb.collection("users").doc(ALICE_UID).collection("orders").doc("order_1");

  const withShadow = { ...validOrderPayload(ALICE_UID), isAdmin: true };
  await assertFails(orderRef.set(withShadow));

  await assertSucceeds(orderRef.set(validOrderPayload(ALICE_UID)));
  await assertSucceeds(orderRef.update({ statusId: "delivered" }));
  await assertFails(orderRef.update({ statusId: "pending" }));
});
