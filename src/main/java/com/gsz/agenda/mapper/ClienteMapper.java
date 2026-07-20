package com.gsz.agenda.mapper;

import com.gsz.agenda.dto.request.ClienteRequest;
import com.gsz.agenda.dto.request.ClienteUpdateRequest;
import com.gsz.agenda.dto.response.ClienteResponse;
import com.gsz.agenda.Model.Cliente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ClienteMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senhaHash", ignore = true)
    @Mapping(target = "ativo", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "avaliacoes", ignore = true)
    Cliente toEntity(ClienteRequest request);

    // Removido @Mapping(target = "senhaHash", ignore = true):
    // ClienteResponse (DTO de saída) não tem esse campo, então o MapStruct
    // não tem o que ignorar ali — o alvo do @Mapping precisa existir no tipo
    // de destino do método. Como o campo simplesmente não existe em
    // ClienteResponse, ele já não é mapeado, sem precisar de anotação.
    ClienteResponse toResponse(Cliente cliente);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "senhaHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "avaliacoes", ignore = true)
    void updateEntity(@MappingTarget Cliente cliente, ClienteUpdateRequest request);
}