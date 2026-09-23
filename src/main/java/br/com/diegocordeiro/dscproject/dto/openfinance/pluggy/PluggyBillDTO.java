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
public class PluggyBillDTO {
    private String id;
    private LocalDate dueDate;
    private LocalDate closeDate;
    private BigDecimal totalAmount;
    private BigDecimal minimumPaymentAmount;
    private String accountId;
}
