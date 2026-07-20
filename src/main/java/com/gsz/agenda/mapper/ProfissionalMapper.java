package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.ProfissionalRequest;
import com.gsz.agenda.dto.request.ProfissionalUpdateRequest;
import com.gsz.agenda.dto.response.ProfissionalResponse;
import com.gsz.agenda.Model.Profissional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ProfissionalMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "horariosFuncionamento", ignore = true)
    @Mapping(target = "feriasFolgas", ignore = true)
    @Mapping(target = "avaliacoes", ignore = true)
    Profissional toEntity(ProfissionalRequest request);

    ProfissionalResponse toResponse(Profissional profissional);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "horariosFuncionamento", ignore = true)
    @Mapping(target = "feriasFolgas", ignore = true)
    @Mapping(target = "avaliacoes", ignore = true)
    void updateEntity(@MappingTarget Profissional profissional, ProfissionalUpdateRequest request);
}