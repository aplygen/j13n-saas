package io.j13n.core.service;

import io.j13n.commons.configuration.service.AbstractMessageService;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.springframework.stereotype.Service;

@Service
public class CoreMessageResourceService extends AbstractMessageService {

    protected CoreMessageResourceService() {
        super(Map.of(Locale.ENGLISH, ResourceBundle.getBundle("messages", Locale.ENGLISH)));
    }
}
