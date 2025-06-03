package io.j13n.core.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum FileType {
    ARCHIVE(Set.of("zip", "rar", "7z", "tar", "gz", "iso", "dmg")),

    DOCUMENTS(Set.of(
            "doc", "docx", "html", "htm", "odt", "pdf", "ods", "ppt", "pptx", "xls", "xlsx", "xlsm", "xlt", "txt")),

    IMAGES(Set.of(
            "jpg", "jpeg", "jpe", "jif", "jfif", "jfi", "png", "gif", "webp", "tiff", "tif", "raw", "arw", "cr2", "nrw",
            "k25", "bmp", "dib", "heif", "heic", "ind", "indd", "indt", "jp2", "j2k", "jpf", "jpx", "jpm", "mj2", "svg",
            "svgz", "avif", "apng", "ico")),

    VIDEOS(Set.of(
            "webm", "mpg", "mp2", "mpeg", "mpe", "mpv", "ogg", "mp4", "m4p", "m4v", "avi", "wmv", "mov", "qt", "flv",
            "swf")),

    DIRECTORIES(Set.of()),

    FILES(Set.of());

    private final Set<String> availableFileExtensions;

    FileType(Set<String> extensions) {
        this.availableFileExtensions = extensions;
    }

}
