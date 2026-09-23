package br.com.diegocordeiro.dscproject.dto.openfinance.pluggy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluggyTransactionDTO {
    private String id;
    private String description;
    private BigDecimal amount;
    private LocalDate date;
    private String type; // DEBIT / CREDIT
    private String category;
    private String status; // PENDING / POSTED
    private String accountId;
}
