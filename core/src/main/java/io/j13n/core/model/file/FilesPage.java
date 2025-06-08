package io.j13n.core.model.file;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record FilesPage(List<FileDetail> content, int pageNumber, long totalElements) implements Serializable {

    @Serial
    private static final long serialVersionUID = 5060566900542074997L;
}
