package br.com.diegocordeiro.dscproject.web.sistema.validator.despesa;

import br.com.diegocordeiro.dscproject.dto.despesa.DespesaFormDTO;
import br.com.diegocordeiro.dscproject.dto.despesa.DespesaRateioDTO;
import br.com.diegocordeiro.dscproject.enums.AplicaA;
import br.com.diegocordeiro.dscproject.enums.OrigemLancamento;
import br.com.diegocordeiro.dscproject.enums.StatusPagamento;
import br.com.diegocordeiro.dscproject.enums.TipoConta;
import br.com.diegocordeiro.dscproject.model.cartao.CartaoCredito;
import br.com.diegocordeiro.dscproject.model.categoria.Categoria;
import br.com.diegocordeiro.dscproject.model.conta.Conta;
import br.com.diegocordeiro.dscproject.model.contato.Contato;
import br.com.diegocordeiro.dscproject.model.despesa.Despesa;
import br.com.diegocordeiro.dscproject.repository.cartao.CartaoCreditoRepository;
import br.com.diegocordeiro.dscproject.repository.categoria.CategoriaRepository;
import br.com.diegocordeiro.dscproject.repository.conta.ContaRepository;
import br.com.diegocordeiro.dscproject.repository.contato.ContatoRepository;
import br.com.diegocordeiro.dscproject.repository.despesa.DespesaRepository;
import org.springframework.context.MessageSource;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class DespesaValidator implements Validator {

    private final DespesaRepository despesaRepository;
    private final ContaRepository contaRepository;
    private final CartaoCreditoRepository cartaoCreditoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ContatoRepository contatoRepository;
    private final MessageSource messageSource;
    private final Locale locale;
    private final Long usuarioId;
    private final boolean podeRatear;

    public DespesaValidator(
            DespesaRepository despesaRepository,
            ContaRepository contaRepository,
            CartaoCreditoRepository cartaoCreditoRepository,
            CategoriaRepository categoriaRepository,
            ContatoRepository contatoRepository,
            MessageSource messageSource,
            Locale locale,
            Long usuarioId,
            boolean podeRatear) {
        this.despesaRepository = despesaRepository;
        this.contaRepository = contaRepository;
        this.cartaoCreditoRepository = cartaoCreditoRepository;
        this.categoriaRepository = categoriaRepository;
        this.contatoRepository = contatoRepository;
        this.messageSource = messageSource;
        this.locale = locale;
        this.usuarioId = usuarioId;
        this.podeRatear = podeRatear;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return DespesaFormDTO.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        DespesaFormDTO dto = (DespesaFormDTO) target;

        validarFormaPagamento(dto, errors);

        if (dto.getCategoriaId() != null && !errors.hasFieldErrors("categoriaId")) {
            validarCategoria(dto, errors);
        }

        validarStatusPagamento(dto, errors);
        validarParcelamento(dto, errors);
        validarRecorrencia(dto, errors);
        validarRateio(dto, errors);
    }

    private void validarFormaPagamento(DespesaFormDTO dto, Errors errors) {
        if (edicaoDeDespesaImportada(dto)) {
            return;
        }

        String forma = dto.getFormaPagamento();
        if (forma == null || forma.isBlank()) {
            errors.rejectValue("formaPagamento", "NotBlank.despesaFormDTO.formaPagamento",
                    messageSource.getMessage("despesa.validacao.formaPagamento.obrigatoria", null, locale));
            return;
        }

        switch (forma.toUpperCase()) {
            case "CONTA":
                if (dto.getContaId() == null) {
                    errors.rejectValue("contaId", "NotNull.despesaFormDTO.contaId",
                            messageSource.getMessage("msg.despesa.forma-pagamento.invalida", null, locale));
                } else {
                    Optional<Conta> contaOpt = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId);
                    if (contaOpt.isEmpty() || !contaOpt.get().isAtivo()) {
                        errors.rejectValue("contaId", "Invalid.despesaFormDTO.contaId",
                                messageSource.getMessage("msg.despesa.conta.invalida", null, locale));
                    }
                }
                break;

            case "CARTAO":
                if (dto.getCartaoId() == null) {
                    errors.rejectValue("cartaoId", "NotNull.despesaFormDTO.cartaoId",
                            messageSource.getMessage("msg.despesa.forma-pagamento.invalida", null, locale));
                } else {
                    Optional<CartaoCredito> cartaoOpt = cartaoCreditoRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getCartaoId(), usuarioId);
                    if (cartaoOpt.isEmpty() || !cartaoOpt.get().isAtivo()) {
                        errors.rejectValue("cartaoId", "Invalid.despesaFormDTO.cartaoId",
                                messageSource.getMessage("msg.despesa.cartao.invalido", null, locale));
                    }
                }
                break;

            case "DINHEIRO":
                if (dto.getContaId() == null) {
                    errors.rejectValue("contaId", "NotNull.despesaFormDTO.contaId",
                            messageSource.getMessage("msg.despesa.forma-pagamento.invalida", null, locale));
                } else {
                    Optional<Conta> contaOpt = contaRepository.findByIdAndUsuarioIdAndDataExclusaoIsNull(dto.getContaId(), usuarioId);
                    if (contaOpt.isEmpty() || !contaOpt.get().isAtivo()) {
                        errors.rejectValue("contaId", "Invalid.despesaFormDTO.contaId",
                                messageSource.getMessage("msg.despesa.conta.invalida", null, locale));
                    } else if (contaOpt.get().getTipo() != TipoConta.CARTEIRA) {
                        errors.rejectValue("contaId", "Invalid.despesaFormDTO.contaId",
                                messageSource.getMessage("msg.despesa.carteira.inexistente", null, locale));
                    }
                }
                break;

            default:
                errors.rejectValue("formaPagamento", "Invalid.despesaFormDTO.formaPagamento",
                        messageSource.getMessage("msg.despesa.forma-pagamento.invalida", null, locale));
        }
    }

    private boolean edicaoDeDespesaImportada(DespesaFormDTO dto) {
        if (dto.getId() == null) {
            return false;
        }
        return despesaRepository.buscarPorIdEUsuario(dto.getId(), usuarioId)
                .map(Despesa::getOrigem)
                .map(origem -> origem != OrigemLancamento.MANUAL)
                .orElse(false);
    }

    private void validarCategoria(DespesaFormDTO dto, Errors errors) {
        Optional<Categoria> categoriaOpt = categoriaRepository.findByIdAndDataExclusaoIsNull(dto.getCategoriaId());
        boolean valida = categoriaOpt.isPresent()
                && categoriaOpt.get().isAtivo()
                && (categoriaOpt.get().getAplicaA() == AplicaA.DESPESA || categoriaOpt.get().getAplicaA() == AplicaA.AMBOS);
        if (!valida) {
            errors.rejectValue("categoriaId", "Invalid.despesaFormDTO.categoriaId",
                    messageSource.getMessage("msg.despesa.categoria.invalida", null, locale));
        }
    }

    private void validarStatusPagamento(DespesaFormDTO dto, Errors errors) {
        if (dto.getStatusPagamento() == StatusPagamento.SIM && dto.getDataPagamento() == null) {
            errors.rejectValue("dataPagamento", "NotNull.despesaFormDTO.dataPagamento",
                    messageSource.getMessage("despesa.validacao.dataPagamento.obrigatoria", null, locale));
        }
    }

    private void validarParcelamento(DespesaFormDTO dto, Errors errors) {
        if (dto.isParcelada()) {
            Integer qtd = dto.getQtdParcelas();
            if (qtd == null || qtd < 2 || qtd > 72) {
                errors.rejectValue("qtdParcelas", "Invalid.despesaFormDTO.qtdParcelas",
                        messageSource.getMessage("msg.despesa.parcelas.intervalo", new Object[]{72}, locale));
            }
        }
    }

    private void validarRecorrencia(DespesaFormDTO dto, Errors errors) {
        if (dto.isParcelada() && dto.isRecorrente()) {
            errors.rejectValue("recorrente", "Invalid.despesaFormDTO.recorrente",
                    messageSource.getMessage("msg.despesa.recorrente-parcelada.incompativel", null, locale));
            return;
        }
        if (dto.isRecorrente()) {
            Integer meses = dto.getQtdMesesRecorrencia();
            if (meses == null || meses < 2 || meses > 36) {
                errors.rejectValue("qtdMesesRecorrencia", "Invalid.despesaFormDTO.qtdMesesRecorrencia",
                        messageSource.getMessage("msg.despesa.recorrencia.meses.intervalo", null, locale));
            }
        }
    }

    private void validarRateio(DespesaFormDTO dto, Errors errors) {
        if (dto.getRateio() == null || dto.getRateio().isEmpty()) {
            return;
        }

        if (!podeRatear) {
            errors.rejectValue("rateio", "Forbidden.despesaFormDTO.rateio",
                    messageSource.getMessage("msg.despesa.rateio.recurso-plano", null, locale));
            return;
        }

        BigDecimal somaFatias = BigDecimal.ZERO;
        Set<Long> contatosNoRateio = new HashSet<>();

        for (int i = 0; i < dto.getRateio().size(); i++) {
            DespesaRateioDTO item = dto.getRateio().get(i);
            Long cid = item.getContatoId();
            if (cid == null) {
                errors.rejectValue("rateio[" + i + "].contatoId", "NotNull.despesaFormDTO.rateio.contatoId",
                        messageSource.getMessage("msg.despesa.rateio.usuario-invalido", null, locale));
                continue;
            }

            if (!contatosNoRateio.add(cid)) {
                errors.rejectValue("rateio[" + i + "].contatoId", "Duplicate.despesaFormDTO.rateio.contatoId",
                        messageSource.getMessage("msg.despesa.rateio.usuario-invalido", null, locale));
                continue;
            }

            Optional<Contato> contatoOpt = contatoRepository.findByIdAndUsuarioDonoIdAndDataExclusaoIsNull(cid, usuarioId);
            if (contatoOpt.isEmpty() || !contatoOpt.get().isAtivo()) {
                errors.rejectValue("rateio[" + i + "].contatoId", "Invalid.despesaFormDTO.rateio.contatoId",
                        messageSource.getMessage("msg.despesa.rateio.usuario-invalido", null, locale));
            }

            if (item.getValor() != null) {
                if (item.getValor().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.rejectValue("rateio[" + i + "].valor", "Invalid.despesaFormDTO.rateio.valor",
                            messageSource.getMessage("despesa.validacao.valor.positivo", null, locale));
                } else {
                    somaFatias = somaFatias.add(item.getValor());
                }
            }
        }

        BigDecimal valorReferencia = dto.isParcelada() && dto.getValorTotalCompra() != null
                ? dto.getValorTotalCompra()
                : dto.getValor();

        if (valorReferencia != null && somaFatias.compareTo(valorReferencia) > 0) {
            errors.rejectValue("rateio", "Max.despesaFormDTO.rateio",
                    messageSource.getMessage("msg.despesa.rateio.soma-excede", new Object[]{somaFatias, valorReferencia}, locale));
        }
    }
}
