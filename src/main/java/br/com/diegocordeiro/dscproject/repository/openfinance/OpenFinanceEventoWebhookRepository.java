package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.enums.StatusEventoWebhook;
import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceEventoWebhook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceEventoWebhookRepository extends JpaRepository<OpenFinanceEventoWebhook, Long> {

    Optional<OpenFinanceEventoWebhook> findByIdEventoExterno(String idEventoExterno);

    boolean existsByIdEventoExterno(String idEventoExterno);

    List<OpenFinanceEventoWebhook> findByStatus(StatusEventoWebhook status);
}
