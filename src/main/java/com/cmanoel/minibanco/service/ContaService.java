package com.cmanoel.minibanco.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cmanoel.minibanco.domain.ChavePix;
import com.cmanoel.minibanco.domain.Conta;
import com.cmanoel.minibanco.domain.Transacao;
import com.cmanoel.minibanco.dto.CadastrarChavePixRequest;
import com.cmanoel.minibanco.dto.ChavePixResponse;
import com.cmanoel.minibanco.dto.CriarContaRequest;
import com.cmanoel.minibanco.dto.ExtratoItemResponse;
import com.cmanoel.minibanco.dto.PerfilResponse;
import com.cmanoel.minibanco.dto.PixConfirmRequest;
import com.cmanoel.minibanco.dto.PixPreviewRequest;
import com.cmanoel.minibanco.dto.PixPreviewResponse;
import com.cmanoel.minibanco.dto.PixRequest;
import com.cmanoel.minibanco.exception.ContaNaoEncontradaException;
import com.cmanoel.minibanco.exception.CredenciaisInvalidasException;
import com.cmanoel.minibanco.exception.EmailJaCadastradoException;
import com.cmanoel.minibanco.exception.RegraNegocioException;
import com.cmanoel.minibanco.exception.SaldoInsuficienteException;
import com.cmanoel.minibanco.repository.ChavePixRepository;
import com.cmanoel.minibanco.repository.ContaRepository;
import com.cmanoel.minibanco.repository.TransacaoRepository;
import com.cmanoel.minibanco.security.CpfValidator;
import com.cmanoel.minibanco.security.ScoreService;

import jakarta.transaction.Transactional;

@Service
public class ContaService {

    private final ContaRepository contaRepository;
    private final ChavePixRepository chavePixRepository;
    private final TransacaoRepository transacaoRepository;
    private final PasswordEncoder passwordEncoder;
    private final CpfValidator cpfValidator;
    private final ScoreService scoreService;
    private final BigDecimal pixDailyLimit;
    private final BigDecimal pixHighValueThreshold;
    private final int pixMaxTransfersPerMinute;
    private final Map<String, PixOperacaoPendente> pixOperacoesPendentes = new ConcurrentHashMap<>();

    public ContaService(ContaRepository contaRepository,
                        ChavePixRepository chavePixRepository,
                        TransacaoRepository transacaoRepository,
                        PasswordEncoder passwordEncoder,
                        CpfValidator cpfValidator,
                        ScoreService scoreService,
                        @Value("${pix.rules.daily-limit:5000.00}") BigDecimal pixDailyLimit,
                        @Value("${pix.rules.high-value-threshold:1000.00}") BigDecimal pixHighValueThreshold,
                        @Value("${pix.rules.max-transfers-per-minute:5}") int pixMaxTransfersPerMinute) {
        this.contaRepository = contaRepository;
        this.chavePixRepository = chavePixRepository;
        this.transacaoRepository = transacaoRepository;
        this.passwordEncoder = passwordEncoder;
        this.cpfValidator = cpfValidator;
        this.scoreService = scoreService;
        this.pixDailyLimit = pixDailyLimit;
        this.pixHighValueThreshold = pixHighValueThreshold;
        this.pixMaxTransfersPerMinute = pixMaxTransfersPerMinute;
    }

    public Conta criarConta(CriarContaRequest request) {
        if (contaRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailJaCadastradoException("Email já cadastrado");
        }
        if (contaRepository.existsByCpf(request.getCpf())) {
            throw new RegraNegocioException("CPF já cadastrado");
        }
        if (!cpfValidator.isValid(request.getCpf())) {
            throw new RegraNegocioException("CPF inválido");
        }
        int score = scoreService.calcularScore(request.getCpf());
        if (!scoreService.scoreAprovado(score)) {
            throw new RegraNegocioException("Score insuficiente para abertura da conta: " + score);
        }

        Conta conta = new Conta();
        conta.setNome(request.getNome());
        conta.setEmail(request.getEmail());
        conta.setCpf(request.getCpf());
        conta.setSenha(passwordEncoder.encode(request.getSenha()));
        conta.setSaldo(BigDecimal.ZERO);
        return contaRepository.save(conta);
    }

    public BigDecimal buscarSaldo(String email) {
        Conta conta = contaRepository.findByEmail(email)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));
        return conta.getSaldo();
    }

    @Transactional
    public void depositar(String email, BigDecimal valor) {
        Conta conta = contaRepository.findByEmail(email)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Valor de depósito inválido");
        }

        conta.setSaldo(conta.getSaldo().add(valor));
        contaRepository.save(conta);

        Transacao deposito = new Transacao();
        deposito.setTipo("DEPOSITO");
        deposito.setOrigem(conta);
        deposito.setDestino(conta);
        deposito.setValor(valor);
        transacaoRepository.save(deposito);
    }

    @Transactional
    public void realizarPix(String emailOrigem, PixRequest request) {

        Conta origem = contaRepository.findByEmail(emailOrigem)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));

        Conta destino = contaRepository.findByEmail(request.getEmailDestino())
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));

        if (origem.getEmail().equalsIgnoreCase(destino.getEmail())) {
            throw new RegraNegocioException("Nao e permitido enviar PIX para a propria conta");
        }

        BigDecimal valor = request.getValor();
        validarPixAntesDaTransferencia(origem.getEmail(), valor, request.isConfirmacaoExtra());
        executarTransferenciaPix(origem, destino, valor);
    }

    @Transactional
    public ChavePixResponse cadastrarChavePix(String email, CadastrarChavePixRequest request) {
        Conta conta = contaRepository.findByEmail(email)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

        String tipo = request.getTipo().trim().toUpperCase();
        String chave = request.getChave().trim();
        validarFormatoChavePix(tipo, chave);

        if (chavePixRepository.findByChave(chave).isPresent()) {
            throw new RegraNegocioException("Chave PIX já cadastrada");
        }

        ChavePix chavePix = new ChavePix();
        chavePix.setConta(conta);
        chavePix.setTipo(tipo);
        chavePix.setChave(chave);
        ChavePix salva = chavePixRepository.save(chavePix);

        return new ChavePixResponse(salva.getId(), salva.getTipo(), mascararChavePix(salva.getChave()));
    }

    public List<ChavePixResponse> listarChavesPix(String email) {
        return chavePixRepository.findByContaEmailOrderByIdDesc(email)
            .stream()
            .map(chave -> new ChavePixResponse(chave.getId(), chave.getTipo(), mascararChavePix(chave.getChave())))
            .toList();
    }

    public PixPreviewResponse gerarPixPreview(String emailOrigem, PixPreviewRequest request) {
        Conta origem = contaRepository.findByEmail(emailOrigem)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));

        ChavePix chaveDestino = chavePixRepository.findByChave(request.getChaveDestino().trim())
            .orElseThrow(() -> new ContaNaoEncontradaException("Chave PIX de destino não encontrada"));

        Conta destino = chaveDestino.getConta();
        if (origem.getEmail().equalsIgnoreCase(destino.getEmail())) {
            throw new RegraNegocioException("Nao e permitido enviar PIX para a propria conta");
        }

        BigDecimal valor = request.getValor();
        validarPixAntesDaTransferencia(origem.getEmail(), valor, request.isConfirmacaoExtra());

        String numeroOperacao = gerarNumeroOperacao();
        LocalDateTime expiraEm = LocalDateTime.now().plusMinutes(5);
        pixOperacoesPendentes.put(numeroOperacao, new PixOperacaoPendente(
            numeroOperacao,
            origem.getEmail(),
            destino.getEmail(),
            valor,
            expiraEm
        ));

        return new PixPreviewResponse(
            numeroOperacao,
            valor,
            destino.getNome(),
            mascararCpf(destino.getCpf()),
            mascararChavePix(chaveDestino.getChave()),
            expiraEm
        );
    }

    @Transactional
    public void confirmarPix(String emailOrigem, PixConfirmRequest request) {
        PixOperacaoPendente operacao = pixOperacoesPendentes.get(request.getNumeroOperacao());
        if (operacao == null) {
            throw new RegraNegocioException("Operacao PIX nao encontrada ou expirada");
        }
        if (operacao.expirada()) {
            pixOperacoesPendentes.remove(request.getNumeroOperacao());
            throw new RegraNegocioException("Operacao PIX expirada. Gere uma nova confirmacao");
        }
        if (!operacao.emailOrigem().equalsIgnoreCase(emailOrigem)) {
            throw new RegraNegocioException("Operacao PIX nao pertence ao usuario autenticado");
        }

        Conta origem = contaRepository.findByEmail(operacao.emailOrigem())
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta origem não encontrada"));
        Conta destino = contaRepository.findByEmail(operacao.emailDestino())
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta destino não encontrada"));

        if (!passwordEncoder.matches(request.getSenha(), origem.getSenha())) {
            throw new CredenciaisInvalidasException("Senha invalida para confirmar PIX");
        }

        validarPixAntesDaTransferencia(origem.getEmail(), operacao.valor(), true);
        executarTransferenciaPix(origem, destino, operacao.valor());
        pixOperacoesPendentes.remove(request.getNumeroOperacao());
    }

    private void validarPixAntesDaTransferencia(String emailOrigem, BigDecimal valor, boolean confirmacaoExtra) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException("Valor de PIX invalido");
        }
        if (valor.scale() > 2) {
            throw new RegraNegocioException("PIX aceita no maximo 2 casas decimais");
        }

        if (valor.compareTo(pixHighValueThreshold) >= 0 && !confirmacaoExtra) {
            throw new RegraNegocioException(
                "PIX de valor alto requer confirmacao extra (confirmacaoExtra=true)"
            );
        }

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime inicioDoDia = agora.toLocalDate().atStartOfDay();
        BigDecimal totalEnviadoHoje = transacaoRepository
            .findByOrigemEmailAndTipoAndDataBetween(emailOrigem, "PIX_ENVIADO", inicioDoDia, agora)
            .stream()
            .map(Transacao::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalEnviadoHoje.add(valor).compareTo(pixDailyLimit) > 0) {
            throw new RegraNegocioException(
                "Limite diario de PIX excedido. Limite atual: " + pixDailyLimit
            );
        }

        LocalDateTime ultimoMinuto = agora.minusMinutes(1);
        long quantidadeNoUltimoMinuto = transacaoRepository
            .findByOrigemEmailAndTipoAndDataBetween(emailOrigem, "PIX_ENVIADO", ultimoMinuto, agora)
            .size();

        if (quantidadeNoUltimoMinuto >= pixMaxTransfersPerMinute) {
            throw new RegraNegocioException(
                "Muitas tentativas de PIX em pouco tempo. Tente novamente em instantes"
            );
        }
    }

    private void executarTransferenciaPix(Conta origem, Conta destino, BigDecimal valor) {
        if (origem.getSaldo().compareTo(valor) < 0) {
            throw new SaldoInsuficienteException("Saldo insuficiente");
        }

        origem.setSaldo(origem.getSaldo().subtract(valor));
        destino.setSaldo(destino.getSaldo().add(valor));

        contaRepository.save(origem);
        contaRepository.save(destino);

        Transacao pixEnviado = new Transacao();
        pixEnviado.setTipo("PIX_ENVIADO");
        pixEnviado.setOrigem(origem);
        pixEnviado.setDestino(destino);
        pixEnviado.setValor(valor);

        Transacao pixRecebido = new Transacao();
        pixRecebido.setTipo("PIX_RECEBIDO");
        pixRecebido.setOrigem(origem);
        pixRecebido.setDestino(destino);
        pixRecebido.setValor(valor);

        transacaoRepository.save(pixEnviado);
        transacaoRepository.save(pixRecebido);
    }

    private void validarFormatoChavePix(String tipo, String chave) {
        if (!List.of("CPF", "EMAIL", "TELEFONE", "ALEATORIA").contains(tipo)) {
            throw new RegraNegocioException("Tipo de chave PIX invalido");
        }
        if (tipo.equals("CPF") && !chave.matches("\\d{11}")) {
            throw new RegraNegocioException("Chave CPF deve ter 11 digitos");
        }
        if (tipo.equals("EMAIL") && !chave.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new RegraNegocioException("Chave EMAIL invalida");
        }
        if (tipo.equals("TELEFONE") && !chave.matches("^\\+?\\d{10,13}$")) {
            throw new RegraNegocioException("Chave TELEFONE invalida");
        }
        if (tipo.equals("ALEATORIA") && chave.length() < 8) {
            throw new RegraNegocioException("Chave ALEATORIA deve ter ao menos 8 caracteres");
        }
    }

    private String mascararChavePix(String chave) {
        if (chave == null || chave.length() < 4) {
            return "***";
        }
        return "***" + chave.substring(chave.length() - 4);
    }

    public List<ExtratoItemResponse> buscarExtrato(String email) {
        return transacaoRepository
            .findByOrigemEmailOrDestinoEmailOrderByDataDesc(email, email)
            .stream()
            .map(transacao -> new ExtratoItemResponse(
                transacao.getTipo(),
                transacao.getValor(),
                transacao.getData(),
                transacao.getOrigem() != null ? transacao.getOrigem().getEmail() : null,
                transacao.getDestino() != null ? transacao.getDestino().getEmail() : null
            ))
            .toList();
    }

    public PerfilResponse buscarPerfil(String email) {
        Conta conta = contaRepository.findByEmail(email)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

        return new PerfilResponse(conta.getNome(), conta.getEmail(), mascararCpf(conta.getCpf()));
    }

    @Transactional
    public void alterarSenha(String email, String senhaAtual, String novaSenha) {
        Conta conta = contaRepository.findByEmail(email)
            .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada"));

        if (!passwordEncoder.matches(senhaAtual, conta.getSenha())) {
            throw new CredenciaisInvalidasException("Senha atual inválida");
        }
        if (passwordEncoder.matches(novaSenha, conta.getSenha())) {
            throw new RegraNegocioException("A nova senha deve ser diferente da atual");
        }

        conta.setSenha(passwordEncoder.encode(novaSenha));
        contaRepository.save(conta);
    }

    private String mascararCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "CPF nao cadastrado";
        }
        return "***." + cpf.substring(3, 6) + ".***-**";
    }

    private String gerarNumeroOperacao() {
        return "PIX-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private record PixOperacaoPendente(
        String numeroOperacao,
        String emailOrigem,
        String emailDestino,
        BigDecimal valor,
        LocalDateTime expiraEm
    ) {
        boolean expirada() {
            return LocalDateTime.now().isAfter(expiraEm);
        }
    }
}
