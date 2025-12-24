import React, { createContext, useState, useContext } from "react";

const CartContext = createContext();

export const CartProvider = ({ children }) => {
  const [cartItems, setCartItems] = useState([]);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [favorites, setFavorites] = useState([]);
  const [currentCategory, setCurrentCategory] = useState("All");

  // 👇 [NEW] 결재 요청 내역 저장소
  const [requests, setRequests] = useState([
    // (예시 데이터)
    {
      id: "REQ-001",
      date: "2024-05-20",
      title: "삼성 모니터 외 2건",
      totalAmount: 1500000,
      status: "approved", // approved, rejected, pending
      memo: "신규 입사자용",
      rejectReason: "",
    },
    {
      id: "REQ-002",
      date: "2024-05-21",
      title: "A4 용지",
      totalAmount: 25000,
      status: "rejected",
      memo: "떨어짐",
      rejectReason: "예산 초과로 인한 반려. 다음 달 재상신 요망.",
    },
  ]);

  // 👇 [NEW] 결재 요청 추가 함수
  const addRequest = (newRequest) => {
    setRequests((prev) => [newRequest, ...prev]); // 최신순 정렬
  };

  // ... (기존 함수들: addToCart, updateQuantity, removeFromCart, toggleFavorite 등 유지) ...
  // ... addToCart, updateQuantity, removeFromCart 코드는 생략 (기존과 동일) ...

  const addToCart = (product) => {
    setCartItems((prev) => {
      const existing = prev.find((item) => item.id === product.id);
      if (existing) {
        return prev.map((item) =>
          item.id === product.id
            ? { ...item, quantity: item.quantity + 1 }
            : item
        );
      }
      return [...prev, { ...product, quantity: 1 }];
    });
  };

  const updateQuantity = (id, newQty) => {
    if (newQty < 1) return;
    setCartItems((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, quantity: newQty } : item
      )
    );
  };

  const removeFromCart = (id) => {
    setCartItems((prev) => prev.filter((item) => item.id !== id));
  };

  const toggleFavorite = (productId) => {
    setFavorites((prev) =>
      prev.includes(productId)
        ? prev.filter((id) => id !== productId)
        : [...prev, productId]
    );
  };

  const toggleDrawer = () => setIsDrawerOpen(!isDrawerOpen);
  const totalPrice = cartItems.reduce(
    (acc, item) => acc + item.price * item.quantity,
    0
  );

  return (
    <CartContext.Provider
      value={{
        cartItems,
        addToCart,
        updateQuantity,
        removeFromCart,
        isDrawerOpen,
        toggleDrawer,
        totalPrice,
        favorites,
        toggleFavorite,
        currentCategory,
        setCurrentCategory,
        requests,
        addRequest, // 👈 내보내기
      }}
    >
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => useContext(CartContext);
