package com.seuprojeto.marketplace.application.usecase;

import com.seuprojeto.marketplace.application.dto.SelecaoCarrinho;
import com.seuprojeto.marketplace.domain.model.CategoriaProduto;
import com.seuprojeto.marketplace.domain.model.Produto;
import com.seuprojeto.marketplace.domain.model.ResumoCarrinho;
import com.seuprojeto.marketplace.domain.repository.ProdutoRepositorio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalcularCarrinhoUseCase {

    private final ProdutoRepositorio produtoRepositorio;

    public CalcularCarrinhoUseCase(ProdutoRepositorio produtoRepositorio) {
        this.produtoRepositorio = produtoRepositorio;
    }

    public ResumoCarrinho executar(List<SelecaoCarrinho> selecaoCarrinhos) {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal descontoCategoriaPercentual = BigDecimal.ZERO;
        int quantidadeTotalItens = 0;

        for (SelecaoCarrinho selecao : selecaoCarrinhos) {
            int quantidade = selecao.getQuantidade() == null ? 0 : selecao.getQuantidade();
            if (quantidade <= 0) {
                continue;
            }

            Produto produto = produtoRepositorio.findById(selecao.getIdProduto())
                    .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado: " + selecao.getIdProduto()));

            quantidadeTotalItens += quantidade;
            subtotal = subtotal.add(produto.getPreco().multiply(BigDecimal.valueOf(quantidade)));

            BigDecimal percentualCategoria = percentualPorCategoria(produto.getCategoriaProduto());
            descontoCategoriaPercentual = descontoCategoriaPercentual.add(percentualCategoria.multiply(BigDecimal.valueOf(quantidade)));
        }

        BigDecimal descontoQuantidadePercentual = percentualPorQuantidade(quantidadeTotalItens);
        BigDecimal descontoTotalPercentual = descontoQuantidadePercentual.add(descontoCategoriaPercentual);

        if (descontoTotalPercentual.compareTo(new BigDecimal("25")) > 0) {
            descontoTotalPercentual = new BigDecimal("25");
        }

        BigDecimal valorDesconto = subtotal
                .multiply(descontoTotalPercentual)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        return new ResumoCarrinho(subtotal, valorDesconto);
    }

    private BigDecimal percentualPorQuantidade(int quantidadeTotalItens) {
        if (quantidadeTotalItens >= 4) {
            return new BigDecimal("10");
        }
        if (quantidadeTotalItens == 3) {
            return new BigDecimal("7");
        }
        if (quantidadeTotalItens == 2) {
            return new BigDecimal("5");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal percentualPorCategoria(CategoriaProduto categoriaProduto) {
        return switch (categoriaProduto) {
            case CAPINHA, FONE -> new BigDecimal("3");
            case CARREGADOR -> new BigDecimal("5");
            case PELICULA, SUPORTE -> new BigDecimal("2");
        };
    }
}