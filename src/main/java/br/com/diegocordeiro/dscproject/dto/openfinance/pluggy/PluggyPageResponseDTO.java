package br.com.diegocordeiro.dscproject.dto.openfinance.pluggy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluggyPageResponseDTO<T> {
    private int total;
    private int totalPages;
    private int page;
    private List<T> results = new ArrayList<>();
}
