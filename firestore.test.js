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
const CHARLIE_UID = "charlie_789";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function pastTime(offsetMs = 5000) {
  return new Date(Date.now() - offsetMs);
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

function buildValidProfile(uid) {
  const now = pastTime();
  return {
    userId: uid,
    heroName: "Aeloria",
    avatarHair: 1,
    avatarOutfit: 2,
    avatarAura: "CYAN",
    financialClass: "GUARDIAN",
    monthlyNetIncomeCents: 680000,
    checkingBalanceCents: 325000,
    creditCardOpenBillCents: 142000,
    emergencyReserveCents: 1200000,
    savingsGoalCents: 136000,
    languageLevel: 2,
    privacyMode: "CATEGORY_SUMMARY",
    xpPoints: 150,
    hasSeenTour: false,
    hasSeenInstallmentTip: false,
    hasSeenTransferTip: false,
    hasSeenOfxTip: false,
    unlockedLessons: ["lesson_competencia"],
    createdAt: now,
    updatedAt: now,
  };
}

test("Unauthenticated users cannot read or write profiles, budgets, transactions, or guilds", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauth.collection("users").doc(ALICE_UID).set(buildValidProfile(ALICE_UID))
  );
  await assertFails(
    unauth.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
  await assertFails(
    unauth.collection("guilds").where("memberUids", "array-contains", ALICE_UID).get()
  );
});

test("UserProfile: owner create, partial update, cross-user denial, and shadow field rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set(buildValidProfile(ALICE_UID))
  );

  // Partial update on allowed fields succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      languageLevel: 3,
      hasSeenTour: true,
      updatedAt: pastTime(1000),
    })
  );

  // Shadow update with ghost field fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: pastTime(1000),
    })
  );

  // Immutable createdAt tamper fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: pastTime(500),
      updatedAt: pastTime(500),
    })
  );

  // Cross-user read/write fails
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({ heroName: "Hijacked", updatedAt: pastTime(1000) })
  );
});

test("Budgets & Transactions: Kotlin payloads, list queries, and cross-user isolation", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = pastTime();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("budgets").doc("cat_food").set({
      userId: ALICE_UID,
      name: "Alimentação",
      iconKey: "RESTAURANT",
      bucket: "ESSENTIAL",
      percentageCap: 20.0,
      fixedCost: false,
      sortOrder: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("accounts").doc("acc_corrente").set({
      userId: ALICE_UID,
      name: "Bolsa Lunar",
      institution: "Banco Sol",
      accountType: "CHECKING",
      balanceCents: 325000,
      colorHex: "#5EEAD4",
      iconKey: "WALLET",
      primaryAccount: true,
      includeInSafeToSpend: true,
      archived: false,
      sortOrder: 1,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("accounts").doc("acc_corrente").update({
      balanceCents: 410000,
      updatedAt: pastTime(1000),
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("accounts").get()
  );

  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("accounts").get()
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      description: "Mercado Aurora",
      amountCents: 24500,
      type: "EXPENSE",
      paymentMethod: "CREDIT_CARD",
      categoryId: "cat_food",
      accountId: "acc_corrente",
      cardId: "card_cristal",
      tags: ["#Casa"],
      dateEpochDay: 20400,
      competenceMonth: "2026-11",
      installmentIndex: 1,
      installmentTotal: 2,
      installmentGroupId: "grp_1",
      dedupeHash: "hash_1",
      sharedGuild: true,
      paidByUid: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Owner list queries succeed (even on empty collections, no existing() error)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("budgets").get()
  );
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );

  // Reject @DocumentId "id" key inside payload
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_2").set({
      id: "tx_2",
      userId: ALICE_UID,
      description: "Invalid ID key",
      amountCents: 1000,
      type: "EXPENSE",
      paymentMethod: "DEBIT_PIX_CASH",
      categoryId: "cat_food",
      tags: [],
      dateEpochDay: 20400,
      competenceMonth: "2026-11",
      installmentIndex: 1,
      installmentTotal: 1,
      installmentGroupId: "",
      dedupeHash: "hash_2",
      sharedGuild: false,
      paidByUid: ALICE_UID,
      createdAt: now,
      updatedAt: now,
    })
  );

  // Cross-user list and read rejected
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("transactions").get()
  );
});

test("Guilda: batch create with invite, filtered query, partner join, entries, and non-member rejection", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const charlieDb = testEnv.authenticatedContext(CHARLIE_UID).firestore();
  const now = pastTime();

  const guildRef = aliceDb.collection("guilds").doc("guild_solstice_1");
  const inviteRef = aliceDb.collection("guildInvites").doc("SOL123");

  const batch = aliceDb.batch();
  batch.set(guildRef, {
    guildName: "Casa Sol & Lua",
    inviteCode: "SOL123",
    ownerId: ALICE_UID,
    memberUids: [ALICE_UID],
    memberNames: { [ALICE_UID]: "Aeloria" },
    memberIncomesCents: { [ALICE_UID]: 680000 },
    memberPrivacy: { [ALICE_UID]: "CATEGORY_SUMMARY" },
    splitMode: "PROPORTIONAL",
    sharedGoalName: "Reserva Lua Nova",
    sharedGoalTargetCents: 3000000,
    sharedGoalSavedCents: 1200000,
    companionDemoName: "Lyra (Demo)",
    companionDemoIncomeCents: 450000,
    companionDemoPrivacy: "CATEGORY_SUMMARY",
    createdAt: now,
    updatedAt: now,
  });
  batch.set(inviteRef, {
    guildId: "guild_solstice_1",
    ownerId: ALICE_UID,
    guildName: "Casa Sol & Lua",
    createdAt: now,
  });
  await assertSucceeds(batch.commit());

  // Unfiltered guild list fails
  await assertFails(aliceDb.collection("guilds").get());

  // Filtered guild list by memberUids succeeds for Alice
  await assertSucceeds(
    aliceDb.collection("guilds").where("memberUids", "array-contains", ALICE_UID).get()
  );

  // Bob looks up invite and joins Guilda
  await assertSucceeds(bobDb.collection("guildInvites").doc("SOL123").get());
  await assertSucceeds(
    bobDb.collection("guilds").doc("guild_solstice_1").update({
      memberUids: [ALICE_UID, BOB_UID],
      memberNames: { [ALICE_UID]: "Aeloria", [BOB_UID]: "Lyra" },
      memberIncomesCents: { [ALICE_UID]: 680000, [BOB_UID]: 450000 },
      memberPrivacy: { [ALICE_UID]: "CATEGORY_SUMMARY", [BOB_UID]: "TRANSPARENT" },
      updatedAt: pastTime(1000),
    })
  );

  // Charlie (3rd party) cannot read or join full guild
  await assertFails(charlieDb.collection("guilds").doc("guild_solstice_1").get());
  await assertFails(
    charlieDb.collection("guilds").doc("guild_solstice_1").update({
      memberUids: [ALICE_UID, BOB_UID, CHARLIE_UID],
      updatedAt: pastTime(500),
    })
  );

  // Bob posts a shared guild entry
  await assertSucceeds(
    bobDb.collection("guilds").doc("guild_solstice_1").collection("entries").doc("entry_1").set({
      guildId: "guild_solstice_1",
      memberUids: [ALICE_UID, BOB_UID],
      authorUid: BOB_UID,
      authorName: "Lyra",
      description: "Conta de Luz",
      amountCents: 28000,
      categoryId: "cat_housing",
      entryType: "SHARED_EXPENSE",
      dateEpochDay: 20400,
      competenceMonth: "2026-11",
      createdAt: now,
      updatedAt: now,
    })
  );

  // Alice lists entries with array-contains filter
  await assertSucceeds(
    aliceDb.collection("guilds").doc("guild_solstice_1").collection("entries")
      .where("memberUids", "array-contains", ALICE_UID)
      .get()
  );

  // Charlie cannot run unfiltered query or query Alice's entries
  await assertFails(
    charlieDb.collection("guilds").doc("guild_solstice_1").collection("entries").get()
  );
  await assertFails(
    charlieDb.collection("guilds").doc("guild_solstice_1").collection("entries")
      .where("memberUids", "array-contains", ALICE_UID)
      .get()
  );
});

test("createHeroAndSeedDemo: full single-batch onboarding write (profile, budgets, txs, guild, invite, entries) succeeds", async () => {
  const uid = "s7kHIaw8MrbF87sYjNXtH3N9FXG3";
  const db = testEnv.authenticatedContext(uid).firestore();
  const now = pastTime();
  const guildId = "guild_" + uid.slice(0, 10);
  const inviteCode = ("SOL" + uid.slice(0, 8)).toUpperCase();

  const batch = db.batch();
  const userRef = db.collection("users").doc(uid);
  const profile = buildValidProfile(uid);
  profile.guildId = guildId;
  batch.set(userRef, profile);

  const budgets = [
    ["cat_moradia", "Moradia & Contas", "HOME", "ESSENTIAL", 28.0, true, 1],
    ["cat_alimentacao", "Alimentação", "RESTAURANT", "ESSENTIAL", 15.0, false, 2],
    ["cat_saude_pet", "Saúde & Transporte", "HEALTH", "ESSENTIAL", 7.0, false, 3],
    ["cat_lazer", "Lazer & Viagens", "STAR", "LIFESTYLE", 20.0, false, 4],
    ["cat_reserva", "Reserva & Metas", "SHIELD", "RESERVE", 30.0, false, 5],
  ];
  for (const [bId, name, iconKey, bucket, percentageCap, fixedCost, sortOrder] of budgets) {
    batch.set(userRef.collection("budgets").doc(bId), {
      userId: uid,
      name,
      iconKey,
      bucket,
      percentageCap,
      fixedCost,
      sortOrder,
      createdAt: now,
      updatedAt: now,
    });
  }

  for (let i = 1; i <= 11; i++) {
    batch.set(userRef.collection("transactions").doc("demo_tx_" + i), {
      userId: uid,
      description: "Lançamento Demo " + i,
      amountCents: 25000 + i * 100,
      type: i === 11 ? "TRANSFERENCIA_INTERNA" : "EXPENSE",
      paymentMethod: i % 2 === 0 ? "CREDIT_CARD" : "DEBIT_PIX_CASH",
      categoryId: "cat_alimentacao",
      tags: i % 2 === 0 ? [] : ["#Casa"],
      dateEpochDay: 20400,
      competenceMonth: "2026-11",
      installmentIndex: 1,
      installmentTotal: 1,
      installmentGroupId: "",
      dedupeHash: "hash_" + i,
      sharedGuild: i <= 3,
      paidByUid: uid,
      createdAt: now,
      updatedAt: now,
    });
  }

  const guildRef = db.collection("guilds").doc(guildId);
  batch.set(guildRef, {
    guildName: "Guilda Sol & Lua",
    inviteCode,
    ownerId: uid,
    memberUids: [uid],
    memberNames: { [uid]: "Aeloria" },
    memberIncomesCents: { [uid]: 680000 },
    memberPrivacy: { [uid]: "CATEGORY_SUMMARY" },
    splitMode: "PROPORTIONAL",
    sharedGoalName: "Viagem & Reserva da Casa",
    sharedGoalTargetCents: 2500000,
    sharedGoalSavedCents: 1050000,
    companionDemoName: "Lyra (Companheira)",
    companionDemoIncomeCents: 480000,
    companionDemoPrivacy: "CATEGORY_SUMMARY",
    createdAt: now,
    updatedAt: now,
  });

  batch.set(db.collection("guildInvites").doc(inviteCode), {
    guildId,
    ownerId: uid,
    guildName: "Guilda Sol & Lua",
    createdAt: now,
  });

  const entries = [
    ["entry_moradia", "Aeloria", "Aluguel & Condomínio", 170000, "cat_moradia"],
    ["entry_mercado", "Aeloria", "Supermercado Estrela", 64000, "cat_alimentacao"],
    ["entry_lyra_feirinha", "Lyra (Companheira)", "Feira Orgânica & Pet", 48000, "cat_alimentacao"],
  ];
  for (const [eId, authorName, description, amountCents, categoryId] of entries) {
    batch.set(guildRef.collection("entries").doc(eId), {
      guildId,
      memberUids: [uid],
      authorUid: uid,
      authorName,
      description,
      amountCents,
      categoryId,
      entryType: "SHARED_EXPENSE",
      dateEpochDay: 20400,
      competenceMonth: "2026-11",
      createdAt: now,
      updatedAt: now,
    });
  }

  await assertSucceeds(batch.commit());
});

