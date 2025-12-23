import React from "react";
import AppLayout from "../../components/layout/AppLayout";


export default function ReadyPage({title}) {
    return (
        <AppLayout>
            <div
                style={{
                    height: "70vh",
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    color: "#aaa",
                }}
            >
                <div style={{fontSize: "80px", marginBottom: "20px"}}>🚧</div>
                <h2 style={{fontSize: "24px", fontWeight: 700, color: "#333"}}>
                    {title}
                </h2>
                <p>현재 기능 구현 준비 중입니다.</p>
            </div>
        </AppLayout>
    );
}
