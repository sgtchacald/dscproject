package br.com.diegocordeiro.dscproject.dto.openfinance.pluggy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluggyAccountDTO {
    private String id;
    private String type;
    private String subtype;
    private String name;
    private String number;
    private BigDecimal balance;
    private String currencyCode;
    private String itemId;
}
