import React, { useState, useEffect, useRef, useMemo } from "react";
import AppLayout from "../../components/layout/AppLayout";
import { mockProducts } from "../../data/mockData";
import "../../styles/shop.css";
import { useCart } from "../../context/CartContext";

import CartDrawer from "../../components/common/CartDrawer";
import FloatingUI from "../../components/common/FloatingUI";

// 👇 [Internal Component] 우측 고정 사이드바용 아이템
const SidebarItem = ({ item, updateQuantity, removeFromCart }) => {
  const [inputValue, setInputValue] = useState(item.quantity);

  useEffect(() => {
    setInputValue(item.quantity);
  }, [item.quantity]);

  // ✨ 입력 중 로직 (2자리 제한: 최대 99)
  const handleChange = (e) => {
    let val = e.target.value;

    // 👇 [수정됨] 3자리 -> 2자리로 제한 변경
    if (val.length > 2) val = val.slice(0, 2);

    setInputValue(val);

    const numVal = parseInt(val);
    if (!isNaN(numVal) && numVal >= 1) {
      updateQuantity(item.id, numVal);
    }
  };

  const handleBlur = () => {
    if (inputValue === "" || parseInt(inputValue) < 1) {
      setInputValue(1);
      updateQuantity(item.id, 1);
    }
  };

  return (
    <div className="sidebar-item">
      <div style={{ flex: 1 }}>
        <div className="sidebar-item-name">{item.name}</div>
        <div style={{ fontSize: "12px", color: "#666" }}>
          {item.price.toLocaleString()}원
        </div>
      </div>

      <div className="qty-control">
        <button
          onClick={() => updateQuantity(item.id, item.quantity - 1)}
          style={{ cursor: "pointer", padding: "2px 6px" }}
        >
          -
        </button>

        <input
          type="number"
          className="qty-input"
          value={inputValue}
          onChange={handleChange}
          onBlur={handleBlur}
        />

        <button
          onClick={() => updateQuantity(item.id, item.quantity + 1)}
          style={{ cursor: "pointer", padding: "2px 6px" }}
        >
          +
        </button>
        <button
          onClick={() => removeFromCart(item.id)}
          style={{
            color: "red",
            border: "none",
            background: "none",
            cursor: "pointer",
            marginLeft: "2px",
          }}
        >
          x
        </button>
      </div>
    </div>
  );
};

export default function ShopPage() {
  const {
    addToCart,
    cartItems,
    updateQuantity,
    removeFromCart,
    totalPrice,
    favorites,
    toggleFavorite,
    currentCategory,
    setCurrentCategory,
  } = useCart();

  const [visibleItems, setVisibleItems] = useState(12);
  const [isLoading, setIsLoading] = useState(false);
  const observerTarget = useRef(null);

  const filteredAll = useMemo(() => {
    if (currentCategory === "Favorites") {
      return mockProducts.filter((p) => favorites.includes(p.id));
    }
    return currentCategory === "All"
      ? mockProducts
      : mockProducts.filter((p) => p.category === currentCategory);
  }, [currentCategory, favorites]);

  const displayProducts = filteredAll.slice(0, visibleItems);

  useEffect(() => {
    setVisibleItems(12);
    window.scrollTo(0, 0);
  }, [currentCategory]);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (
          entries[0].isIntersecting &&
          visibleItems < filteredAll.length &&
          !isLoading
        ) {
          setIsLoading(true);
          setTimeout(() => {
            setVisibleItems((prev) => prev + 12);
            setIsLoading(false);
          }, 500);
        }
      },
      { threshold: 1.0 }
    );
    if (observerTarget.current) observer.observe(observerTarget.current);
    return () => {
      if (observerTarget.current) observer.unobserve(observerTarget.current);
    };
  }, [visibleItems, filteredAll.length, isLoading]);

  const handleCheckout = () => {
    if (cartItems.length === 0) return alert("장바구니가 비어있습니다!");
    if (window.confirm(`총 ${totalPrice.toLocaleString()}원 결재 올릴까요?`)) {
      alert("승인 요청 완료!");
    }
  };

  return (
    <AppLayout>
      {/*장바구니 아이콘 활성화*/}
      <CartDrawer />
      <FloatingUI />
      <div className="page-header">
        <h2 className="page-title">📦 비품 구매</h2>
        <p className="text-gray">
          필요한 물품을 담으면 우측 목록에 즉시 추가됩니다.
        </p>
      </div>

      <div className="shop-container">
        <div className="shop-main">
          <div className="shop-header">
            <div className="shop-filter">
              {[
                "All",
                "Favorites",
                "전자기기",
                "사무용품",
                "가구",
                "탕비실",
              ].map((cat) => (
                <button
                  key={cat}
                  className={`filter-btn ${
                    currentCategory === cat ? "active" : ""
                  }`}
                  onClick={() => setCurrentCategory(cat)}
                  style={
                    cat === "Favorites"
                      ? { color: "#f1c40f", borderColor: "#f1c40f" }
                      : {}
                  }
                >
                  {cat === "Favorites" ? "★ 즐겨찾기" : cat}
                </button>
              ))}
            </div>
          </div>

          <div className="product-grid">
            {displayProducts.map((product) => {
              const isFav = favorites.includes(product.id);

              return (
                <div
                  key={product.id}
                  id={`product-${product.id}`}
                  className="product-card"
                  style={{ position: "relative" }}
                >
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      toggleFavorite(product.id);
                    }}
                    style={{
                      position: "absolute",
                      top: "10px",
                      right: "10px",
                      background: "white",
                      border: "1px solid #ddd",
                      borderRadius: "50%",
                      width: "32px",
                      height: "32px",
                      cursor: "pointer",
                      fontSize: "18px",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      color: isFav ? "#f1c40f" : "#ddd",
                      zIndex: 5,
                    }}
                  >
                    ★
                  </button>

                  <img
                    src={product.img}
                    alt={product.name}
                    className="card-img"
                  />
                  <div className="card-body">
                    <span className="card-category">{product.category}</span>
                    <div className="card-title">{product.name}</div>
                    <div className="card-price">
                      {product.price.toLocaleString()}원
                    </div>
                    <div className="card-footer">
                      <button
                        className="add-cart-btn"
                        onClick={() => addToCart(product)}
                      >
                        담기
                      </button>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          <div
            ref={observerTarget}
            style={{ height: "50px", textAlign: "center", marginTop: "20px" }}
          >
            {isLoading && <div className="spinner"></div>}
          </div>
        </div>

        <aside className="shop-sidebar">
          <div className="sidebar-title">
            장바구니 현황 ({cartItems.length})
          </div>

          <div className="sidebar-list">
            {cartItems.length === 0 ? (
              <div
                style={{
                  color: "#999",
                  textAlign: "center",
                  marginTop: "50px",
                }}
              >
                텅 비었습니다.
                <br />
                왼쪽에서 담아보세요!
              </div>
            ) : (
              cartItems.map((item) => (
                <SidebarItem
                  key={item.id}
                  item={item}
                  updateQuantity={updateQuantity}
                  removeFromCart={removeFromCart}
                />
              ))
            )}
          </div>

          <div className="sidebar-footer">
            <div className="sidebar-total">
              <span>합계</span>
              <span>{totalPrice.toLocaleString()}원</span>
            </div>
            <button className="sidebar-checkout-btn" onClick={handleCheckout}>
              결재 요청하기
            </button>
          </div>
        </aside>
      </div>
    </AppLayout>
  );
}
