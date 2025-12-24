import { configureStore } from "@reduxjs/toolkit";
import expenseReducer from "./slices/expenseSlice";
import receiptReducer from "./slices/receiptSlice";
import approvalReducer from "./slices/approvalSlice";
import userReducer from "./slices/userSlice";

export const store = configureStore({
  reducer: {
    expense: expenseReducer,
    receipt: receiptReducer,
    approval: approvalReducer,
    user: userReducer,
  },
});
