import React, { useState, useEffect } from "react";
import { useCart } from "../../context/CartContext";
import "../../styles/cartDrawer.css";

// 👇 [Internal Component] 개별 아이템 (입력 로직 + 스마트 이동)
const DrawerItem = ({
  item,
  updateQuantity,
  removeFromCart,
  toggleDrawer,
  setCurrentCategory,
}) => {
  const [inputValue, setInputValue] = useState(item.quantity);

  useEffect(() => {
    setInputValue(item.quantity);
  }, [item.quantity]);

  // ✨ 입력 중 로직 (2자리 제한: 최대 99)
  const handleChange = (e) => {
    let val = e.target.value;

    // 2자리수 초과 입력 방지
    if (val.length > 2) val = val.slice(0, 2);

    setInputValue(val);

    const numVal = parseInt(val);
    if (!isNaN(numVal) && numVal >= 1) {
      updateQuantity(item.id, numVal);
    }
  };

  // 포커스 잃으면 1로 복구
  const handleBlur = () => {
    if (inputValue === "" || parseInt(inputValue) < 1) {
      setInputValue(1);
      updateQuantity(item.id, 1);
    }
  };

  // ✨ 스마트 이동 (카테고리 변경 -> 스크롤 이동)
  const moveToProduct = () => {
    setCurrentCategory(item.category);
    setTimeout(() => {
      const element = document.getElementById(`product-${item.id}`);
      if (element) {
        element.scrollIntoView({ behavior: "smooth", block: "center" });
        toggleDrawer();
      }
    }, 100);
  };

  return (
    <div className="cart-item">
      {/* 이미지 클릭 시 이동 */}
      <img
        src={item.img}
        alt={item.name}
        className="item-img"
        onClick={moveToProduct}
        style={{ cursor: "pointer" }}
      />
      <div className="item-info">
        {/* 이름 클릭 시 이동 */}
        <div
          className="item-name"
          onClick={moveToProduct}
          style={{ cursor: "pointer", textDecoration: "underline" }}
        >
          {item.name}
        </div>
        <div className="item-price">{item.price.toLocaleString()}원</div>

        <div className="item-controls">
          <div className="qty-group">
            <button onClick={() => updateQuantity(item.id, item.quantity - 1)}>
              -
            </button>
            <input
              type="number"
              className="qty-input-drawer"
              value={inputValue}
              onChange={handleChange}
              onBlur={handleBlur}
            />
            <button onClick={() => updateQuantity(item.id, item.quantity + 1)}>
              +
            </button>
          </div>
          <button
            className="delete-btn"
            onClick={() => removeFromCart(item.id)}
          >
            삭제
          </button>
        </div>
      </div>
    </div>
  );
};

export default function CartDrawer() {
  const {
    cartItems,
    isDrawerOpen,
    toggleDrawer,
    updateQuantity,
    removeFromCart,
    totalPrice,
    setCurrentCategory,
    addRequest, // 👈 HistoryPage로 데이터 보낼 함수
  } = useCart();

  const [memo, setMemo] = useState("");
  const BUDGET_LIMIT = 5000000;
  const vat = Math.round(totalPrice * 0.1);
  const finalTotal = totalPrice + vat;
  const usagePercent = Math.min((finalTotal / BUDGET_LIMIT) * 100, 100);

  // 결재 상신 핸들러
  const handleCheckout = () => {
    if (cartItems.length === 0) return alert("장바구니가 비어있습니다.");

    const msg = `총 ${finalTotal.toLocaleString()}원 (부가세 포함) 결재를 상신하시겠습니까?\n\n📝 사유: ${
      memo ? memo : "없음"
    }`;

    if (window.confirm(msg)) {
      // 1. 요청 데이터 생성
      const newRequest = {
        id: `REQ-${Date.now().toString().slice(-6)}`, // 간단한 ID 생성
        date: new Date().toISOString().split("T")[0], // YYYY-MM-DD
        title: `${cartItems[0].name} 외 ${cartItems.length - 1}건`,
        totalAmount: finalTotal,
        status: "pending", // 초기 상태: 대기
        memo: memo,
        items: [...cartItems], // 현재 장바구니 품목 저장
      };

      // 2. 내 결재함으로 전송
      addRequest(newRequest);

      // 3. 장바구니 비우기 (Context에 clearCart가 없으므로 반복문으로 처리)
      cartItems.forEach((item) => removeFromCart(item.id));

      // 4. 완료 처리
      alert(
        "✅ 결재 승인 요청이 완료되었습니다!\n[내 결재함] 메뉴에서 진행 상황을 확인하세요."
      );
      setMemo("");
      toggleDrawer();
    }
  };

  return (
    <>
      {isDrawerOpen && (
        <div className="cart-overlay" onClick={toggleDrawer}></div>
      )}

      <div className={`cart-drawer ${isDrawerOpen ? "open" : ""}`}>
        <div className="drawer-header">
          <h2>📑 결재 기안 확인</h2>
          <button className="close-btn" onClick={toggleDrawer}>
            ×
          </button>
        </div>

        {/* 예산 현황 바 */}
        <div className="budget-section">
          <div className="budget-label">
            <span>부서 예산 현황 (월 500만)</span>
            <span className={usagePercent > 80 ? "warning-text" : ""}>
              {usagePercent.toFixed(1)}% 사용 예상
            </span>
          </div>
          <div className="budget-track">
            <div
              className="budget-fill"
              style={{
                width: `${usagePercent}%`,
                backgroundColor: usagePercent > 90 ? "#e74c3c" : "#4f79df",
              }}
            ></div>
          </div>
          <div className="budget-limit-text">
            결재 후 잔액: {(BUDGET_LIMIT - finalTotal).toLocaleString()}원
          </div>
        </div>

        {/* 장바구니 목록 */}
        <div className="drawer-body">
          {cartItems.length === 0 ? (
            <div className="empty-cart">
              <p>결재할 품목이 없습니다.</p>
              <p
                className="text-sm"
                style={{ marginTop: "5px", color: "#999" }}
              >
                필요한 비품을 담아보세요.
              </p>
            </div>
          ) : (
            cartItems.map((item) => (
              <DrawerItem
                key={item.id}
                item={item}
                updateQuantity={updateQuantity}
                removeFromCart={removeFromCart}
                toggleDrawer={toggleDrawer}
                setCurrentCategory={setCurrentCategory}
              />
            ))
          )}
        </div>

        {/* 하단 푸터 (메모 + 가격요약 + 버튼) */}
        {cartItems.length > 0 && (
          <div className="drawer-footer-complex">
            <div className="memo-section">
              <label>구매 사유 (필수)</label>
              <textarea
                placeholder="예: 신규 입사자 지급용, 부서 비품 교체 등"
                value={memo}
                onChange={(e) => setMemo(e.target.value)}
              ></textarea>
            </div>

            <div className="price-summary">
              <div className="summary-row">
                <span>공급가액</span>
                <span>{totalPrice.toLocaleString()}원</span>
              </div>
              <div className="summary-row">
                <span>부가세 (10%)</span>
                <span>{vat.toLocaleString()}원</span>
              </div>
              <div className="summary-row total">
                <span>최종 결재 금액</span>
                <span className="total-text">
                  {finalTotal.toLocaleString()}원
                </span>
              </div>
            </div>

            <button className="checkout-btn" onClick={handleCheckout}>
              결재 상신하기
            </button>
          </div>
        )}
      </div>
    </>
  );
}
