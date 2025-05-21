package io.j13n.core.commons.jooq.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import io.j13n.core.commons.base.configuration.service.AbstractMessageService;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;
import org.jooq.types.UShort;

import java.io.Serial;

public class UnsignedNumbersSerializationModule extends SimpleModule {

    @Serial
    private static final long serialVersionUID = 6367988430700197837L;

    public UnsignedNumbersSerializationModule(AbstractMessageService messageResourceService) {
        super();

        this.addDeserializer(ULong.class, new UNumberDeserializer<>(ULong.class, messageResourceService));
        this.addDeserializer(UShort.class, new UNumberDeserializer<>(UShort.class, messageResourceService));
        this.addDeserializer(UInteger.class, new UNumberDeserializer<>(UInteger.class, messageResourceService));
        this.addSerializer(new UNumberSerializer());
    }
}
