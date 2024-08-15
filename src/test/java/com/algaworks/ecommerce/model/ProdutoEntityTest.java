package com.algaworks.ecommerce.model;

import com.algaworks.ecommerce.util.EntityManagerTest;
import org.junit.Assert;
import org.junit.Test;

import javax.persistence.TransactionRequiredException;
import java.math.BigDecimal;

import static org.junit.Assert.*;

public class ProdutoEntityTest extends EntityManagerTest {

    private static final Produto novoProduto;

    static {
        novoProduto = new Produto(
                2,
                "Câmera Canon",
                "A melhor definição para suas fotos.",
                new BigDecimal(5000)
        );
    }

    @Test
    public void buscarPorIdentificadorComSucesso() {
        Produto produtoExistente = entityManager.find(Produto.class, 2);
        assertEquals("Câmera Canon", produtoExistente.getNome());
    }

    @Test
    public void buscarPorIdentificadorSemSucesso() {
        Produto produtoNaoExistente = entityManager.find(Produto.class, 999);
        assertNull(produtoNaoExistente);
    }

    @Test
    public void revertendoAlteracoesComSucesso() {
        Produto produtoExistente = entityManager.find(Produto.class, 2);
        produtoExistente.setNome("trocando para reverter");
        entityManager.refresh(produtoExistente);
        assertEquals("Câmera Canon", produtoExistente.getNome());
    }

    @Test
    public void inserirUmNovoProduto() {
        entityManager.getTransaction().begin();
        entityManager.persist(novoProduto);
        entityManager.getTransaction().commit();

        entityManager.clear();

        Produto produtoVerificacao = entityManager.find(Produto.class, novoProduto.getId());
        assertNotNull(produtoVerificacao);
    }

    @Test
    public void inserirUmNovoProdutoComMerge() {
        entityManager.getTransaction().begin();
        entityManager.merge(new Produto(
                4,
                "Microfone Rode Videmic",
                "A melhor qualidade de som.",
                new BigDecimal(1000))
        );
        entityManager.getTransaction().commit();

        entityManager.clear();

        Produto produtoVerificacao = entityManager.find(Produto.class, 4);
        assertNotNull(produtoVerificacao);
    }

    @Test
    public void inserirUmNovoProdutoSemTransacao() {
        final Produto produtoNovoTemp = new Produto(
                5,
                "Microfone Rode Videmic 2",
                "A melhor qualidade de som 2.",
                new BigDecimal(1100));
        entityManager.persist(produtoNovoTemp);

        entityManager.clear(); // Limpa o 1st Level Cache sem enviar ao BD.

        Produto produtoVerificacao = entityManager.find(Produto.class, produtoNovoTemp.getId());
        assertNull(produtoVerificacao);
    }

    @Test(expected = TransactionRequiredException.class)
    public void enviandoAoBancoSemTransacao() {
        entityManager.persist(novoProduto);
        entityManager.flush(); // Envia ao 2nd Level Cache (BD) sem transação.
    }

    @Test(expected = IllegalArgumentException.class)
    public void removerUmProdutoNaoGerenciado() {
        entityManager.getTransaction().begin();
        entityManager.remove(new Produto(3));
        entityManager.getTransaction().commit();
    }

    @Test
    public void removerUmProdutoInexistente() {
        entityManager.getTransaction().begin();
        entityManager.remove(new Produto(999)); // Só pra lembrar que é feito um find e não encontrando o comando Delete nem é executado (vide log)
        entityManager.getTransaction().commit();
    }

    @Test
    public void removerUmProdutoExistente() {
        final Produto produtoNovoTemp = new Produto(
                5,
                "Microfone Rode Videmic 2",
                "A melhor qualidade de som.",
                new BigDecimal(1100));

        entityManager.getTransaction().begin();
        entityManager.persist(produtoNovoTemp);
        entityManager.getTransaction().commit();

        entityManager.getTransaction().begin();
        Produto produtoParaDeletar = entityManager.find(Produto.class, produtoNovoTemp.getId());
        entityManager.remove(produtoParaDeletar);
        entityManager.getTransaction().commit();

        Produto produtoVerificacao = entityManager.find(Produto.class, produtoNovoTemp.getId());
        assertNull(produtoVerificacao);
    }

    @Test
    public void atualizarObjetoNaoGerenciado() {
        Produto produtoNaoGerenciado = new Produto();

        produtoNaoGerenciado.setId(1);
        produtoNaoGerenciado.setNome("Kindle Paperwhite 2ª Geração"); // Proposital preencher para ver que na cópia do merge, esse atributo será gravado com nulo.

        entityManager.getTransaction().begin();
        entityManager.merge(produtoNaoGerenciado);
        entityManager.getTransaction().commit();

        entityManager.clear();

        Produto produtoVerificacao = entityManager.find(Produto.class, produtoNaoGerenciado.getId());
        assertEquals("Kindle Paperwhite 2ª Geração", produtoVerificacao.getNome());
        assertNull(produtoVerificacao.getDescricao());
    }

    @Test
    public void atualizarObjetoGerenciado() {
        Produto produtoGerenciado = entityManager.find(Produto.class, 1);

        entityManager.getTransaction().begin();
        produtoGerenciado.setNome("Kindle Paperwhite 2ª Geração");
        entityManager.getTransaction().commit();

        entityManager.clear();

        Produto produtoVerificacao = entityManager.find(Produto.class, produtoGerenciado.getId());
        Assert.assertEquals("Kindle Paperwhite 2ª Geração", produtoVerificacao.getNome());
    }
}
