package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.AgendamentoRequest;
import com.gsz.agenda.dto.request.AgendamentoUpdateRequest;
import com.gsz.agenda.dto.response.AgendamentoDetalhadoResponse;
import com.gsz.agenda.dto.response.AgendamentoResponse;
import com.gsz.agenda.Model.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    uses = {ClienteMapper.class, ProfissionalMapper.class, ServicoMapper.class}
)
public interface AgendamentoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "servico", ignore = true)
    @Mapping(target = "status", constant = "PENDENTE")
    @Mapping(target = "duracaoEstimada", ignore = true)
    @Mapping(target = "precoTotal", ignore = true)
    @Mapping(target = "lembreteEnviado", constant = "false")
    @Mapping(target = "confirmadoCliente", constant = "false")
    @Mapping(target = "canceladoPor", ignore = true)
    @Mapping(target = "motivoCancelamento", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pagamento", ignore = true)
    @Mapping(target = "avaliacao", ignore = true)
    Agendamento toEntity(AgendamentoRequest request);

    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nome", target = "clienteNome")
    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(source = "profissional.nome", target = "profissionalNome")
    @Mapping(source = "servico.id", target = "servicoId")
    @Mapping(source = "servico.nome", target = "servicoNome")
    AgendamentoResponse toResponse(Agendamento agendamento);

    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nome", target = "clienteNome")
    @Mapping(source = "cliente.email", target = "clienteEmail")
    @Mapping(source = "cliente.telefone", target = "clienteTelefone")
    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(source = "profissional.nome", target = "profissionalNome")
    @Mapping(source = "profissional.email", target = "profissionalEmail")
    @Mapping(source = "profissional.telefone", target = "profissionalTelefone")
    @Mapping(source = "profissional.especialidades", target = "profissionalEspecialidades")
    @Mapping(source = "servico.id", target = "servicoId")
    @Mapping(source = "servico.nome", target = "servicoNome")
    @Mapping(source = "servico.descricao", target = "servicoDescricao")
    @Mapping(source = "servico.duracaoMinutos", target = "servicoDuracao")
    @Mapping(target = "pagamento", ignore = true)  // Será preenchido separadamente
    @Mapping(target = "avaliacao", ignore = true)  // Será preenchido separadamente
    AgendamentoDetalhadoResponse toDetalhedResponse(Agendamento agendamento);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "pagamento", ignore = true)
    @Mapping(target = "avaliacao", ignore = true)
    void updateEntity(@MappingTarget Agendamento agendamento, AgendamentoUpdateRequest request);
}