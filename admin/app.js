import { initializeApp } from "https://www.gstatic.com/firebasejs/10.12.2/firebase-app.js";
import {
  getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-auth.js";
import {
  getDatabase, ref, onValue, set, push, remove, update, get
} from "https://www.gstatic.com/firebasejs/10.12.2/firebase-database.js";

// ─── Firebase 설정 (firebase console → 프로젝트 설정에서 복사) ───────────────
const firebaseConfig = {
  apiKey:            "YOUR_API_KEY",
  authDomain:        "YOUR_PROJECT.firebaseapp.com",
  databaseURL:       "https://YOUR_PROJECT-default-rtdb.asia-southeast1.firebasedatabase.app",
  projectId:         "YOUR_PROJECT",
  storageBucket:     "YOUR_PROJECT.appspot.com",
  messagingSenderId: "YOUR_SENDER_ID",
  appId:             "YOUR_APP_ID"
};
// ──────────────────────────────────────────────────────────────────────────────

const app  = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db   = getDatabase(app);

// ─── DOM 참조 ────────────────────────────────────────────────────────────────
const loginView    = document.getElementById("login-view");
const adminView    = document.getElementById("admin-view");
const emailInput   = document.getElementById("email-input");
const pwInput      = document.getElementById("pw-input");
const loginBtn     = document.getElementById("login-btn");
const loginError   = document.getElementById("login-error");
const logoutBtn    = document.getElementById("logout-btn");
const adminEmail   = document.getElementById("admin-email");

const navBtns      = document.querySelectorAll(".nav-btn");
const sections     = document.querySelectorAll(".section");

const productsList = document.getElementById("products-list");
const addProductBtn = document.getElementById("add-product-btn");

const productModal  = document.getElementById("product-modal");
const modalTitle    = document.getElementById("modal-title");
const modalRoomId   = document.getElementById("modal-room-id");
const fName         = document.getElementById("f-name");
const fDesc         = document.getElementById("f-desc");
const fImageUrl     = document.getElementById("f-image-url");
const fGrade        = document.getElementById("f-grade");
const fStreak       = document.getElementById("f-streak");
const fCapacity     = document.getElementById("f-capacity");
const fType         = document.getElementById("f-type");
const fDirect       = document.getElementById("f-direct");
const fDrawMethod   = document.getElementById("f-draw-method");
const fStatus       = document.getElementById("f-status");
const modalCancelBtn = document.getElementById("modal-cancel-btn");
const modalSaveBtn   = document.getElementById("modal-save-btn");

const confirmModal   = document.getElementById("confirm-modal");
const confirmMsg     = document.getElementById("confirm-msg");
const confirmCancelBtn = document.getElementById("confirm-cancel-btn");
const confirmOkBtn   = document.getElementById("confirm-ok-btn");

const drawsTbody    = document.getElementById("draws-tbody");
const usersStats    = document.getElementById("users-stats");
const toast         = document.getElementById("toast");

const bonusEnabled  = document.getElementById("bonus-enabled");
const bonusTickets  = document.getElementById("bonus-tickets");
const bonusDday     = document.getElementById("bonus-dday");
const bonusDdayLabel = document.getElementById("bonus-dday-label");
const saveBonusBtn  = document.getElementById("save-bonus-btn");

let pendingDeleteId = null;
let allProducts     = {};

// ─── 인증 ──────────────────────────────────────────────────────────────────
onAuthStateChanged(auth, async (user) => {
  if (!user) {
    show(loginView); hide(adminView);
    return;
  }
  const isAdmin = await checkAdmin(user.uid);
  if (!isAdmin) {
    await signOut(auth);
    loginError.textContent = "관리자 권한이 없습니다.";
    show(loginView); hide(adminView);
    return;
  }
  hide(loginView); show(adminView);
  adminEmail.textContent = user.email;
  loadProducts();
  loadUserStats();
  loadBonusConfig();
});

loginBtn.addEventListener("click", async () => {
  loginError.textContent = "";
  loginBtn.disabled = true;
  try {
    await signInWithEmailAndPassword(auth, emailInput.value.trim(), pwInput.value);
  } catch (e) {
    loginError.textContent = "이메일 또는 비밀번호가 올바르지 않습니다.";
    loginBtn.disabled = false;
  }
});

pwInput.addEventListener("keydown", e => { if (e.key === "Enter") loginBtn.click(); });

logoutBtn.addEventListener("click", () => signOut(auth));

async function checkAdmin(uid) {
  const snap = await get(ref(db, `admins/${uid}`));
  return snap.exists() && snap.val() === true;
}

// ─── 네비게이션 ────────────────────────────────────────────────────────────
navBtns.forEach(btn => {
  btn.addEventListener("click", () => {
    navBtns.forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
    const target = btn.dataset.section;
    sections.forEach(s => {
      if (s.id === `section-${target}`) s.classList.remove("hidden");
      else s.classList.add("hidden");
    });
    if (target === "draws") renderDrawsTable();
    if (target === "bonus") loadBonusConfig();
  });
});

// ─── 상품 CRUD ─────────────────────────────────────────────────────────────
function loadProducts() {
  onValue(ref(db, "productRooms"), (snap) => {
    allProducts = snap.val() || {};
    renderProductCards();
    renderDrawsTable();
  });
}

function renderProductCards() {
  productsList.innerHTML = "";
  const items = Object.entries(allProducts);
  if (items.length === 0) {
    productsList.innerHTML = '<p style="color:var(--text-sec)">등록된 상품이 없습니다.</p>';
    return;
  }
  items.forEach(([id, p]) => {
    const fill = Math.round((p.currentCount / p.capacity) * 100);
    const card = document.createElement("div");
    card.className = "product-card";
    card.innerHTML = `
      <div class="product-card-header">
        <span class="grade-badge grade-${p.grade}">${gradeLabel(p.grade)}</span>
        <span class="status-badge status-${p.drawStatus}">${p.drawStatus}</span>
      </div>
      <div class="product-card-name">${esc(p.productName)}</div>
      <div class="product-card-desc">${esc(p.description || "")}</div>
      <div class="progress-bar-wrap">
        <div class="progress-meta">
          <span>참여</span><span>${p.currentCount}/${p.capacity}명</span>
        </div>
        <div class="progress-bar"><div class="progress-fill" style="width:${fill}%"></div></div>
      </div>
      <div class="product-card-footer">
        <span style="font-size:12px;color:var(--text-sec)">${p.requiredStreak}연승 · ${p.round}회차 · ${p.drawMethod || "timestamp"}</span>
        <div class="card-actions">
          <button class="btn-sm edit-btn" data-id="${id}">수정</button>
          <button class="btn-sm danger delete-btn" data-id="${id}">삭제</button>
        </div>
      </div>`;
    productsList.appendChild(card);
  });

  document.querySelectorAll(".edit-btn").forEach(btn => {
    btn.addEventListener("click", () => openEditModal(btn.dataset.id));
  });
  document.querySelectorAll(".delete-btn").forEach(btn => {
    btn.addEventListener("click", () => openConfirmDelete(btn.dataset.id));
  });
}

function renderDrawsTable() {
  drawsTbody.innerHTML = "";
  Object.entries(allProducts).forEach(([id, p]) => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${esc(p.productName)}</td>
      <td>${p.round}회차</td>
      <td>${p.currentCount}/${p.capacity}</td>
      <td><span class="status-badge status-${p.drawStatus}">${p.drawStatus}</span></td>
      <td>${p.drawMethod === "lotto" ? "동행복권" : "타임스탬프"}</td>
      <td>
        ${p.drawStatus === "open" && p.currentCount >= p.capacity
          ? `<button class="btn-sm trigger-draw-btn" data-id="${id}">추첨 실행</button>`
          : "─"}
      </td>`;
    drawsTbody.appendChild(tr);
  });
  document.querySelectorAll(".trigger-draw-btn").forEach(btn => {
    btn.addEventListener("click", () => triggerDraw(btn.dataset.id));
  });
}

async function triggerDraw(roomId) {
  if (!confirm(`${allProducts[roomId]?.productName} 추첨을 실행하시겠습니까?`)) return;
  await update(ref(db, `productRooms/${roomId}`), { drawStatus: "drawn" });
  showToast("추첨 상태가 'drawn'으로 변경됐습니다. 서버 Functions에서 당첨자를 선정합니다.");
}

addProductBtn.addEventListener("click", () => openAddModal());

function openAddModal() {
  modalTitle.textContent = "상품 추가";
  modalRoomId.value = "";
  fName.value = ""; fDesc.value = ""; fImageUrl.value = ""; fGrade.value = "C";
  fStreak.value = 3; fCapacity.value = 100; fType.value = "coupon";
  fDirect.value = ""; fDrawMethod.value = "timestamp"; fStatus.value = "open";
  show(productModal);
}

function openEditModal(id) {
  const p = allProducts[id];
  if (!p) return;
  modalTitle.textContent = "상품 수정";
  modalRoomId.value = id;
  fName.value         = p.productName || "";
  fDesc.value         = p.description || "";
  fImageUrl.value     = p.imageUrl || "";
  fGrade.value        = p.grade || "C";
  fStreak.value       = p.requiredStreak || 3;
  fCapacity.value     = p.capacity || 100;
  fType.value         = p.productType || "coupon";
  fDirect.value       = p.directBuyLabel || "";
  fDrawMethod.value   = p.drawMethod || "timestamp";
  fStatus.value       = p.drawStatus || "open";
  show(productModal);
}

modalCancelBtn.addEventListener("click", () => hide(productModal));
productModal.addEventListener("click", e => { if (e.target === productModal) hide(productModal); });

modalSaveBtn.addEventListener("click", async () => {
  const id      = modalRoomId.value;
  const payload = {
    productName:    fName.value.trim(),
    description:    fDesc.value.trim(),
    imageUrl:       fImageUrl.value.trim(),
    grade:          fGrade.value,
    requiredStreak: Number(fStreak.value),
    capacity:       Number(fCapacity.value),
    productType:    fType.value,
    directBuyLabel: fDirect.value.trim(),
    drawMethod:     fDrawMethod.value,
    drawStatus:     fStatus.value
  };
  if (!payload.productName) { showToast("상품명을 입력해주세요."); return; }

  if (id) {
    await update(ref(db, `productRooms/${id}`), payload);
    showToast("상품이 수정됐습니다.");
  } else {
    const newRef = push(ref(db, "productRooms"));
    await set(newRef, { ...payload, roomId: newRef.key, currentCount: 0, round: 1 });
    showToast("상품이 추가됐습니다.");
  }
  hide(productModal);
});

// ─── 삭제 확인 ─────────────────────────────────────────────────────────────
function openConfirmDelete(id) {
  pendingDeleteId = id;
  confirmMsg.textContent = `"${allProducts[id]?.productName}" 상품을 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.`;
  show(confirmModal);
}
confirmCancelBtn.addEventListener("click", () => { hide(confirmModal); pendingDeleteId = null; });
confirmModal.addEventListener("click", e => { if (e.target === confirmModal) { hide(confirmModal); pendingDeleteId = null; } });
confirmOkBtn.addEventListener("click", async () => {
  if (!pendingDeleteId) return;
  await remove(ref(db, `productRooms/${pendingDeleteId}`));
  showToast("상품이 삭제됐습니다.");
  hide(confirmModal);
  pendingDeleteId = null;
});

// ─── 사용자 현황 ────────────────────────────────────────────────────────────
function loadUserStats() {
  onValue(ref(db, "users"), (snap) => {
    const users = snap.val() || {};
    const list = Object.values(users);
    const total  = list.length;
    const active = list.filter(u => u.status !== "banned").length;
    const tickets = list.reduce((s, u) => s + (u.ticketCount || 0), 0);
    const wins    = list.reduce((s, u) => s + (u.totalWins || 0), 0);
    usersStats.innerHTML = `
      <div class="stat-card"><div class="stat-value">${total}</div><div class="stat-label">총 사용자</div></div>
      <div class="stat-card"><div class="stat-value">${active}</div><div class="stat-label">활성 사용자</div></div>
      <div class="stat-card"><div class="stat-value">${tickets}</div><div class="stat-label">보유 승부권 합계</div></div>
      <div class="stat-card"><div class="stat-value">${wins}</div><div class="stat-label">누적 당첨 수</div></div>`;
  });
}

// ─── 보너스 설정 ─────────────────────────────────────────────────────────────
function loadBonusConfig() {
  get(ref(db, "config/dailyBonus")).then(snap => {
    const cfg = snap.val() || {};
    bonusEnabled.checked  = cfg.enabled  !== false;
    bonusTickets.value    = cfg.rewardTickets ?? 1;
    bonusDday.value       = cfg.dday ?? "";
    bonusDdayLabel.value  = cfg.ddayLabel ?? "";
  });
}

saveBonusBtn.addEventListener("click", async () => {
  const payload = {
    enabled:       bonusEnabled.checked,
    rewardTickets: Number(bonusTickets.value) || 1,
    dday:          bonusDday.value.trim(),
    ddayLabel:     bonusDdayLabel.value.trim()
  };
  await set(ref(db, "config/dailyBonus"), payload);
  showToast("보너스 설정이 저장됐습니다.");
});

// ─── 유틸 ──────────────────────────────────────────────────────────────────
function show(el) { el.classList.remove("hidden"); }
function hide(el) { el.classList.add("hidden"); }
function esc(s) { return String(s).replace(/&/g,"&amp;").replace(/</g,"&lt;").replace(/>/g,"&gt;"); }
function gradeLabel(g) {
  return { C:"데일리찬스", B:"위클리찬스", A:"프라임찬스", S:"스페셜찬스", SS:"프리미엄찬스" }[g] || g;
}

let toastTimer;
function showToast(msg) {
  toast.textContent = msg;
  toast.classList.remove("hidden");
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => toast.classList.add("hidden"), 3000);
}
