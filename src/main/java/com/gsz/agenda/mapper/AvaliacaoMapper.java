package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.AvaliacaoRequest;
import com.gsz.agenda.dto.response.AvaliacaoResponse;
import com.gsz.agenda.Model.Avaliacao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface AvaliacaoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agendamento", ignore = true)
    @Mapping(target = "cliente", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Avaliacao toEntity(AvaliacaoRequest request);

    @Mapping(source = "agendamento.id", target = "agendamentoId")
    @Mapping(source = "cliente.id", target = "clienteId")
    @Mapping(source = "cliente.nome", target = "clienteNome")
    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(source = "profissional.nome", target = "profissionalNome")
    AvaliacaoResponse toResponse(Avaliacao avaliacao);
}