package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.PagamentoRequest;
import com.gsz.agenda.dto.response.PagamentoResponse;
import com.gsz.agenda.Model.Pagamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PagamentoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "agendamento", ignore = true)
    @Mapping(target = "status", constant = "PENDENTE")
    @Mapping(target = "dataPagamento", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Pagamento toEntity(PagamentoRequest request);

    @Mapping(source = "agendamento.id", target = "agendamentoId")
    PagamentoResponse toResponse(Pagamento pagamento);
}