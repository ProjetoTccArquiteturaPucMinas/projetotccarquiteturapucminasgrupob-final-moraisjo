package com.seuprojeto.marketplace.presentation.controller;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.infrastructure.repository.InMemoryProdutoRepositorio;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CarrinhoControllerE2ETest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("cenarios")
    void shouldCalculateCartAccordingToBancadaScenarios(String nomeCenario,
                                                        List<SelecaoCarrinho> entrada,
                                                        BigDecimal subtotalEsperado,
                                                        BigDecimal descontoEsperado,
                                                        BigDecimal totalEsperado) {

        CarrinhoController controller = new CarrinhoController(new InMemoryProdutoRepositorio());
        var resumo = controller.calculate(entrada);

        BigDecimal subtotal = resumo.getSubtotal();
        BigDecimal desconto = resumo.getDesconto();
        BigDecimal total = resumo.getTotal();

        assertEquals(0, subtotal.compareTo(subtotalEsperado), "Subtotal divergente");
        assertEquals(0, desconto.compareTo(descontoEsperado), "Desconto divergente");
        assertEquals(0, total.compareTo(totalEsperado), "Total divergente");
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> cenarios() {
        return Stream.of(
                cenario("Cenário 1 - Um único item (PELICULA)",
                        List.of(new SelecaoCarrinho(4L, 1)),
                        "29.90", "0.60", "29.30"),
                cenario("Cenário 2 - Um único item (CAPINHA)",
                        List.of(new SelecaoCarrinho(1L, 1)),
                        "49.90", "1.50", "48.40"),
                cenario("Cenário 3 - Dois itens de categorias diferentes",
                        List.of(new SelecaoCarrinho(4L, 1), new SelecaoCarrinho(5L, 1)),
                        "89.80", "8.08", "81.72"),
                cenario("Cenário 4 - Três itens",
                        List.of(new SelecaoCarrinho(1L, 1), new SelecaoCarrinho(4L, 1), new SelecaoCarrinho(5L, 1)),
                        "139.70", "19.56", "120.14"),
                cenario("Cenário 5 - Quatro itens (desconto máximo NÃO atingido)",
                        List.of(new SelecaoCarrinho(1L, 1), new SelecaoCarrinho(4L, 1), new SelecaoCarrinho(5L, 1), new SelecaoCarrinho(3L, 1)),
                        "339.60", "67.92", "271.68"),
                cenario("Cenário 6 - Quatro itens (desconto máximo ATINGIDO)",
                        List.of(new SelecaoCarrinho(2L, 1), new SelecaoCarrinho(3L, 1), new SelecaoCarrinho(1L, 1), new SelecaoCarrinho(4L, 1)),
                        "399.60", "91.91", "307.69"),
                cenario("Cenário 7 - Cinco itens (testando limite de 25%)",
                        List.of(new SelecaoCarrinho(2L, 1), new SelecaoCarrinho(3L, 1), new SelecaoCarrinho(1L, 1), new SelecaoCarrinho(4L, 1), new SelecaoCarrinho(5L, 1)),
                        "459.50", "114.88", "344.62"),
                cenario("Cenário 8 - Dois itens da mesma categoria",
                        List.of(new SelecaoCarrinho(1L, 2)),
                        "99.80", "10.98", "88.82"),
                cenario("Cenário 9 - Três itens com categorias repetidas",
                        List.of(new SelecaoCarrinho(4L, 2), new SelecaoCarrinho(5L, 1)),
                        "119.70", "15.56", "104.14"),
                cenario("Cenário 10 - Produto mais caro (Fone)",
                        List.of(new SelecaoCarrinho(3L, 1)),
                        "199.90", "6.00", "193.90"),
                cenario("Cenário 11 - Limite de 25% com múltiplos carregadores",
                        List.of(new SelecaoCarrinho(2L, 4)),
                        "479.60", "119.90", "359.70"),
                cenario("Cenário 12 - Dois produtos com desconto baixo",
                        List.of(new SelecaoCarrinho(4L, 1), new SelecaoCarrinho(5L, 1)),
                        "89.80", "8.08", "81.72")
        );
    }

    private static org.junit.jupiter.params.provider.Arguments cenario(String nome,
                                                                        List<SelecaoCarrinho> entrada,
                                                                        String subtotal,
                                                                        String desconto,
                                                                        String total) {
        return org.junit.jupiter.params.provider.Arguments.of(
                nome,
                entrada,
                new BigDecimal(subtotal),
                new BigDecimal(desconto),
                new BigDecimal(total)
        );
    }
}
