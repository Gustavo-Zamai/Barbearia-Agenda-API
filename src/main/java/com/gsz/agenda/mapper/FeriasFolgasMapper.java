package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.FeriasFolgasRequest;
import com.gsz.agenda.dto.response.FeriasFolgasResponse;
import com.gsz.agenda.Model.FeriasFolgas;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface FeriasFolgasMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    FeriasFolgas toEntity(FeriasFolgasRequest request);

    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(source = "profissional.nome", target = "profissionalNome")
    FeriasFolgasResponse toResponse(FeriasFolgas feriasFolgas);
}