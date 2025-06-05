package io.j13n.core.model.file;

import java.util.List;

public record FilesPage(List<FileDetail> content, int pageNumber, long totalElements) {}
