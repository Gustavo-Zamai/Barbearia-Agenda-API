package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.ServicoRequest;
import com.gsz.agenda.dto.request.ServicoUpdateRequest;
import com.gsz.agenda.dto.response.ServicoResponse;
import com.gsz.agenda.Model.Servico;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ServicoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    Servico toEntity(ServicoRequest request);

    ServicoResponse toResponse(Servico servico);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    void updateEntity(@MappingTarget Servico servico, ServicoUpdateRequest request);
}