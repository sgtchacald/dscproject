package br.com.diegocordeiro.dscproject.dto.openfinance.pluggy;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PluggyItemDTO {
    private String id;
    private String status;
    private String executionStatus;
    private PluggyConnectorDTO connector;
    private PluggyItemErrorDTO error;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PluggyConnectorDTO {
        private Long id;
        private String name;
        private String imageUrl;
        private String primaryColor;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PluggyItemErrorDTO {
        private String code;
        private String message;
    }
}
