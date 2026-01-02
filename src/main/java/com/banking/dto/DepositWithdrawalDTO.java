package com.banking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositWithdrawalDTO {
    @NotNull(message = "Tutar boş olamaz")
    @DecimalMin(value = "0.01", message = "Tutar 0.01'den büyük olmalıdır")
    private BigDecimal amount;

    private String description;
}

