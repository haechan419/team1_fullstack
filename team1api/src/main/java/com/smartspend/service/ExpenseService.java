package com.smartspend.service;

import com.smartspend.dto.ExpenseDTO;
import com.smartspend.dto.ExpenseSubmitDTO;
import com.smartspend.dto.PageRequestDTO;
import com.smartspend.dto.PageResponseDTO;

import java.time.LocalDate;

public interface ExpenseService {

    PageResponseDTO<ExpenseDTO> getList(Long userId, PageRequestDTO pageRequestDTO, String status, LocalDate startDate, LocalDate endDate);

    ExpenseDTO get(Long id, Long userId, boolean isAdmin);

    Long register(ExpenseDTO expenseDTO, Long userId);

    void modify(ExpenseDTO expenseDTO, Long userId);

    void remove(Long id, Long userId);

    void submit(Long id, Long userId, ExpenseSubmitDTO submitDTO);
}

