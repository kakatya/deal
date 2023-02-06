package ru.kakatya.deal.mappers.interfasemapper;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import ru.kakatya.deal.entities.Passport;

@Mapper(componentModel = "spring")
@Component
public interface PassportMapper {
    PassportMapper INSTANCE = Mappers.getMapper(PassportMapper.class);

    String getString(Passport passport);
}
