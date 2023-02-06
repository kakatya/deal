package ru.kakatya.deal.mappers.interfasemapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.kakatya.deal.dtos.ClientDto;
import ru.kakatya.deal.entities.Client;

@Mapper(componentModel = "spring",uses = {PassportMapper.class, EmploymentMapper.class})
public interface ClientMapper {

    ClientMapper INSTANCE = Mappers.getMapper(ClientMapper.class);
    ClientDto getClientDto(Client client);
}
