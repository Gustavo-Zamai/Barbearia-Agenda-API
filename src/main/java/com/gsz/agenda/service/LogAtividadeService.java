package com.gsz.agenda.service;

import com.gsz.agenda.Model.LogAtividade;
import com.gsz.agenda.repositories.LogAtividadeRepository;
import tools.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogAtividadeService {

    private final LogAtividadeRepository repository;
    private final JsonMapper jsonMapper;

    /**
     * Salvar um log de atividade
     */
    @Transactional
    public void salvarLog(String usuario, String acao, String tabela,
                          Integer registroId, Object dadosAnteriores,
                          Object dadosNovos, String ip, String userAgent) {
        try {
            LogAtividade logAtividade = LogAtividade.builder()
                .usuario(usuario)
                .acao(acao)
                .tabela(tabela)
                .registroId(registroId)
                .dadosAnteriores(dadosAnteriores != null ? jsonMapper.writeValueAsString(dadosAnteriores) : null)
                .dadosNovos(dadosNovos != null ? jsonMapper.writeValueAsString(dadosNovos) : null)
                .ip(ip)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

            repository.save(logAtividade);

        } catch (Exception e) {
            log.error("Erro ao salvar log: {}", e.getMessage());
        }
    }

    /**
     * Salvar log de erro
     */
    @Transactional
    public void salvarErro(String usuario, String acao, String tabela,
                           Integer registroId, String erro, String ip, String userAgent) {
        try {
            LogAtividade logAtividade = LogAtividade.builder()
                .usuario(usuario)
                .acao("ERRO_" + acao)
                .tabela(tabela)
                .registroId(registroId)
                .dadosNovos(erro)
                .ip(ip)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .build();

            repository.save(logAtividade);

        } catch (Exception e) {
            log.error("Erro ao salvar log de erro: {}", e.getMessage());
        }
    }
}