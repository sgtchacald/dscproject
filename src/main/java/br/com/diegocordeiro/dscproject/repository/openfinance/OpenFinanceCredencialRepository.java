package br.com.diegocordeiro.dscproject.repository.openfinance;

import br.com.diegocordeiro.dscproject.model.openfinance.OpenFinanceCredencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OpenFinanceCredencialRepository extends JpaRepository<OpenFinanceCredencial, Long> {

    Optional<OpenFinanceCredencial> findByUsuarioIdAndProvedorIdAndDataExclusaoIsNull(Long usuarioId, Long provedorId);

    List<OpenFinanceCredencial> findByUsuarioIdAndDataExclusaoIsNull(Long usuarioId);

    Optional<OpenFinanceCredencial> findByIdAndUsuarioIdAndDataExclusaoIsNull(Long id, Long usuarioId);
}
