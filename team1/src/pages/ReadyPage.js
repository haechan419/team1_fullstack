import React from "react";

export default function ReadyPage({ title }) {
  return (
    <div className="flex flex-col items-center justify-center h-[70vh] text-gray-400">
      <div className="text-8xl mb-5">🚧</div>
      <h2 className="text-2xl font-bold text-gray-800">
        {title}
      </h2>
      <p>현재 기능 구현 준비 중입니다.</p>
    </div>
  );
}

