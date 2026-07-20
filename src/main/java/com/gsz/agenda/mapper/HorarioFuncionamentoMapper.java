package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.HorarioFuncionamentoRequest;
import com.gsz.agenda.dto.response.HorarioFuncionamentoResponse;
import com.gsz.agenda.Model.HorarioFuncionamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface HorarioFuncionamentoMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    HorarioFuncionamento toEntity(HorarioFuncionamentoRequest request);

    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(source = "profissional.nome", target = "profissionalNome")
    HorarioFuncionamentoResponse toResponse(HorarioFuncionamento horario);
}