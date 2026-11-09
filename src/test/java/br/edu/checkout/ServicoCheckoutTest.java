package br.edu.checkout;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class ServicoCheckoutTest {

    private static final String CLIENTE = "cliente-1";
    private static final String EMAIL = "cliente@exemplo.com";
    private static final String SKU = "SKU-123";
    private static final BigDecimal PRECO = new BigDecimal("19.90");

    @Mock
    private EstoqueGateway estoqueGateway;

    @Mock
    private PagamentoGateway pagamentoGateway;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private Notificador notificador;

    private ServicoCheckout servico;

    @BeforeEach
    void criarServicoReal() {
        servico = new ServicoCheckout(
                estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
    }

    @Test
    void deveRejeitarSolicitacaoNulaSemConsultarColaboradores() {
        // RED inicial: finalizar ainda lança UnsupportedOperationException.
        assertThrows(IllegalArgumentException.class, () -> servico.finalizar(null));
        verifyNoInteractions(
                estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
    }

    static Stream<Arguments> solicitacoesInvalidas() {
        return Stream.of(
                Arguments.of("clienteId nulo", new SolicitacaoCheckout(null, EMAIL, SKU, 1, PRECO)),
                Arguments.of("clienteId vazio", new SolicitacaoCheckout("", EMAIL, SKU, 1, PRECO)),
                Arguments.of("clienteId em branco", new SolicitacaoCheckout("   ", EMAIL, SKU, 1, PRECO)),
                Arguments.of("email nulo", new SolicitacaoCheckout(CLIENTE, null, SKU, 1, PRECO)),
                Arguments.of("email vazio", new SolicitacaoCheckout(CLIENTE, "", SKU, 1, PRECO)),
                Arguments.of("email em branco", new SolicitacaoCheckout(CLIENTE, " \t ", SKU, 1, PRECO)),
                Arguments.of("sku nulo", new SolicitacaoCheckout(CLIENTE, EMAIL, null, 1, PRECO)),
                Arguments.of("sku vazio", new SolicitacaoCheckout(CLIENTE, EMAIL, "", 1, PRECO)),
                Arguments.of("sku em branco", new SolicitacaoCheckout(CLIENTE, EMAIL, "  ", 1, PRECO)),
                Arguments.of("quantidade zero", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 0, PRECO)),
                Arguments.of("quantidade negativa", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, -1, PRECO)),
                Arguments.of("preço nulo", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 1, null)),
                Arguments.of("preço zero", new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 1, BigDecimal.ZERO)),
                Arguments.of("preço negativo",
                        new SolicitacaoCheckout(CLIENTE, EMAIL, SKU, 1, new BigDecimal("-0.01"))));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("solicitacoesInvalidas")
    void deveRejeitarSolicitacaoInvalidaSemConsultarColaboradores(
            String descricao, SolicitacaoCheckout solicitacao) {
        assertThrows(IllegalArgumentException.class, () -> servico.finalizar(solicitacao));
        verifyNoInteractions(
                estoqueGateway, pagamentoGateway, pedidoRepository, notificador);
    }
}
