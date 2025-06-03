package io.j13n.core.dao;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.j13n.core.commons.base.thread.VirtualThreadWrapper;
import io.j13n.core.commons.base.util.StringUtil;
import io.j13n.core.enums.FileResourceType;
import io.j13n.core.enums.FileSystemType;
import io.j13n.core.enums.FileType;
import io.j13n.core.jooq.tables.records.CoreFileSystemRecord;
import io.j13n.core.model.file.FileDetail;
import io.j13n.core.model.file.FilesPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.DeleteQuery;
import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SortField;
import org.jooq.impl.DSL;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static io.j13n.core.jooq.Tables.CORE_FILE_SYSTEM;

@Service
@Slf4j
public class FileSystemDao {

    public static final String R2_FILE_SEPARATOR_STRING = "/";
    public static final char R2_FILE_SEPARATOR_CHAR = '/';
    private static final Map<String, Field<?>> SORTABLE_FIELDS = Map.of(
            "FILE_RESOURCE_TYPE", CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE,
            "NAME", CORE_FILE_SYSTEM.NAME,
            "SIZE", CORE_FILE_SYSTEM.SIZE,
            "LASTMODIFIED", CORE_FILE_SYSTEM.UPDATED_AT,
            "CREATED", CORE_FILE_SYSTEM.CREATED_AT);
    private final DSLContext context;

    public FileSystemDao(DSLContext context) {
        this.context = context;
    }

    public CompletableFuture<Boolean> exists(FileResourceType fileResourceType, String clientCode, String path) {
        return getId(fileResourceType, clientCode, path)
                .thenApply(Optional::isPresent)
                .exceptionally(ex -> {
                    log.error("Error checking if path exists", ex);
                    return false;
                });
    }

    private Long checkIfPathExists(
            int index, String[] pathParts, Long parentId, Multimap<String, CoreFileSystemRecord> nameIndex) {
        if (index == pathParts.length) return parentId;

        String pathPart = pathParts[index];
        Collection<CoreFileSystemRecord> records = nameIndex.get(pathPart);

        if (records.isEmpty()) return null;

        for (CoreFileSystemRecord rec : records) {
            if ((parentId == null && rec.getParentId() == null)
                    || (parentId != null && parentId.equals(rec.getParentId())))
                return checkIfPathExists(index + 1, pathParts, rec.getId(), nameIndex);
        }

        return null;
    }

    public CompletableFuture<Optional<Long>> getId(FileResourceType fileResourceType, String clientCode, String path) {
        return getFileRecord(fileResourceType, clientCode, path, CoreFileSystemRecord::getId)
                .thenApply(Optional::ofNullable)
                .exceptionally(ex -> {
                    log.error("Error getting ID", ex);
                    return Optional.empty();
                });
    }

    public CompletableFuture<Optional<Long>> getFolderId(
            FileResourceType fileResourceType, String clientCode, String path) {
        if (StringUtil.safeIsBlank(path)) return VirtualThreadWrapper.just(Optional.empty());

        String[] pathParts = path.split(R2_FILE_SEPARATOR_STRING);
        List<String> parts = new ArrayList<>();

        for (String part : pathParts) {
            if (StringUtil.safeIsBlank(part)) continue;

            parts.add(parts.isEmpty() ? part : (parts.getLast() + R2_FILE_SEPARATOR_STRING + part));
        }

        // Process parts sequentially
        List<CompletableFuture<Map.Entry<String, Optional<Long>>>> futures = new ArrayList<>();
        for (String p : parts) {
            futures.add(getId(fileResourceType, clientCode, p).thenApply(result -> Map.entry(p, result)));
        }

        // Create a CompletableFuture that will contain all the results
        CompletableFuture<List<Map.Entry<String, Optional<Long>>>> allFutures = CompletableFuture.allOf(
                        futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream().map(CompletableFuture::join).toList());

        return allFutures.thenCompose(entries -> {
            if (entries.isEmpty()) return VirtualThreadWrapper.just(Optional.empty());

            CompletableFuture<Optional<Long>> folderId = VirtualThreadWrapper.just(Optional.empty());
            int i = 0;
            for (; i < entries.size(); i++) {
                final Optional<Long> currentFolderId = entries.get(i).getValue();
                folderId = VirtualThreadWrapper.just(currentFolderId);
                if (currentFolderId.isEmpty()) break;
            }

            for (; i < entries.size(); i++) {
                final String currentPath = entries.get(i).getKey();
                folderId = folderId.thenCompose(parentId ->
                        createFolder(fileResourceType, clientCode, currentPath).thenApply(Optional::of));
            }

            return folderId;
        });
    }

    public CompletableFuture<FileDetail> getFileDetail(
            FileResourceType fileResourceType, String clientCode, String path) {
        String[] pathParts =
                (path.startsWith(R2_FILE_SEPARATOR_STRING) ? path.substring(1) : path).split(R2_FILE_SEPARATOR_STRING);

        if (pathParts.length == 0) return VirtualThreadWrapper.just(null);

        return getFileRecord(fileResourceType, clientCode, pathParts, r -> new FileDetail()
                .setId(r.getId())
                .setName(r.getName())
                .setDirectory(r.getFileSystemType() == FileSystemType.DIRECTORY)
                .setSize(r.getSize() == null ? 0L : r.getSize())
                .setCreatedDate(r.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                .setLastModifiedTime(r.getUpdatedAt().toEpochSecond(ZoneOffset.UTC)));
    }

    private <T> CompletableFuture<T> getFileRecord(
            FileResourceType fileResourceType,
            String clientCode,
            String path,
            Function<CoreFileSystemRecord, T> mapper) {
        if (StringUtil.safeIsBlank(path)) return VirtualThreadWrapper.just(null);

        String[] pathParts =
                (path.startsWith(R2_FILE_SEPARATOR_STRING) ? path.substring(1) : path).split(R2_FILE_SEPARATOR_STRING);

        return getFileRecord(fileResourceType, clientCode, pathParts, mapper);
    }

    private <T> CompletableFuture<T> getFileRecord(
            FileResourceType fileResourceType,
            String clientCode,
            String[] pathParts,
            Function<CoreFileSystemRecord, T> mapper) {
        return VirtualThreadWrapper.fromCallable(() -> {
            Result<CoreFileSystemRecord> result = this.context
                    .selectFrom(CORE_FILE_SYSTEM)
                    .where(DSL.and(
                            CORE_FILE_SYSTEM.CODE.eq(clientCode),
                            CORE_FILE_SYSTEM.NAME.in(pathParts),
                            CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE.eq(fileResourceType)))
                    .fetch();

            if (result.isEmpty() || pathParts.length > result.size()) return null;

            Multimap<String, CoreFileSystemRecord> nameIndex = ArrayListMultimap.create();
            Map<Long, CoreFileSystemRecord> idIndex = new HashMap<>();

            for (CoreFileSystemRecord record : result) {
                CoreFileSystemRecord rec = record.into(CoreFileSystemRecord.class);
                nameIndex.put(rec.getName(), rec);
                idIndex.put(rec.getId(), rec);
            }

            Long id = checkIfPathExists(0, pathParts, null, nameIndex);

            if (id == null) return null;

            return mapper.apply(idIndex.get(id));
        });
    }

    public CompletableFuture<FilesPage> list(
            FileResourceType fileResourceType,
            String clientCode,
            String path,
            FileType[] fileType,
            String filter,
            Pageable page) {
        log.debug("FileSystemDao.list (with path)");

        return getId(fileResourceType, clientCode, path).thenCompose(folderId -> {
            if (folderId.isEmpty() && !StringUtil.safeIsBlank(path))
                return VirtualThreadWrapper.just(new FilesPage(new ArrayList<>(), page.getPageNumber(), 0L));

            return listInternal(fileResourceType, clientCode, folderId.orElse(null), fileType, filter, page);
        });
    }

    private CompletableFuture<FilesPage> listInternal(
            FileResourceType fileResourceType,
            String clientCode,
            Long folderId,
            FileType[] fileType,
            String filter,
            Pageable page) {
        log.debug("FileSystemDao.list (with folder Id)");

        List<Condition> conditions = new ArrayList<>();

        conditions.add(CORE_FILE_SYSTEM.CODE.eq(clientCode));
        if (folderId == null) conditions.add(CORE_FILE_SYSTEM.PARENT_ID.isNull());
        else conditions.add(CORE_FILE_SYSTEM.PARENT_ID.eq(folderId));

        conditions.add(CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE.eq(fileResourceType));

        if (!StringUtil.safeIsBlank(filter)) conditions.add(CORE_FILE_SYSTEM.NAME.like("%" + filter + "%"));

        if (fileType != null && fileType.length > 0) conditions.add(getConditionForFileTypes(fileType));

        return VirtualThreadWrapper.fromCallable(() -> {
                    var records = this.context
                            .selectFrom(CORE_FILE_SYSTEM)
                            .where(DSL.and(conditions))
                            .orderBy(getOrderList(page))
                            .limit(page.getPageSize())
                            .offset(page.getPageNumber() * page.getPageSize())
                            .fetch();

                    List<FileDetail> files = new ArrayList<>();
                    for (var rec : records) {
                        var r = rec.into(CoreFileSystemRecord.class);
                        files.add(new FileDetail()
                                .setId(r.getId())
                                .setName(r.getName())
                                .setDirectory(r.getFileSystemType() == FileSystemType.DIRECTORY)
                                .setSize(r.getSize() == null ? 0L : r.getSize())
                                .setCreatedDate(r.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                                .setLastModifiedTime(r.getUpdatedAt().toEpochSecond(ZoneOffset.UTC)));
                    }

                    return files;
                })
                .thenCompose(files -> VirtualThreadWrapper.fromCallable(() -> this.context
                                .selectCount()
                                .from(CORE_FILE_SYSTEM)
                                .where(DSL.and(conditions))
                                .fetchOne(0, Long.class))
                        .thenApply(count -> new FilesPage(files, page.getPageNumber(), count)));
    }

    private List<SortField<?>> getOrderList(Pageable page) {
        if (page == null || page.getSort().isEmpty() || page.getSort().isUnsorted())
            return List.of(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE.desc(), CORE_FILE_SYSTEM.NAME.asc());

        return page.getSort().stream()
                .filter(e -> SORTABLE_FIELDS.containsKey(e.getProperty().toUpperCase()))
                .map(e -> {
                    if (e.getDirection().isDescending()) {
                        return SORTABLE_FIELDS
                                .get(e.getProperty().toUpperCase())
                                .desc();
                    }
                    return SORTABLE_FIELDS.get(e.getProperty().toUpperCase()).asc();
                })
                .toList();
    }

    private Condition getConditionForFileTypes(FileType[] fileType) {
        if (fileType == null || fileType.length == 0) return DSL.trueCondition();

        List<Condition> conditions = new ArrayList<>();

        for (FileType ft : fileType) {
            switch (ft) {
                case DIRECTORIES -> conditions.add(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE.eq(FileSystemType.DIRECTORY));
                case FILES -> conditions.add(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE.eq(FileSystemType.FILE));
                default ->
                    conditions.add(DSL.or(ft.getAvailableFileExtensions().stream()
                            .map(e -> CORE_FILE_SYSTEM.NAME.like("%." + e))
                            .toList()));
            }
        }

        return DSL.or(conditions);
    }

    public CompletableFuture<Boolean> deleteFile(FileResourceType fileResourceType, String clientCode, String path) {
        log.debug("FileSystemDao.deleteFile");

        return getId(fileResourceType, clientCode, path).thenCompose(optionalId -> {
            if (optionalId.isEmpty()) return VirtualThreadWrapper.just(false);

            Long id = optionalId.get();
            return VirtualThreadWrapper.fromCallable(() -> {
                DeleteQuery<CoreFileSystemRecord> query = this.context.deleteQuery(CORE_FILE_SYSTEM);
                query.addConditions(CORE_FILE_SYSTEM.ID.eq(id));
                return query.execute() > 0;
            });
        });
    }

    public CompletableFuture<FileDetail> createOrUpdateFile(
            FileResourceType fileResourceType,
            String clientCode,
            String path,
            String fileName,
            Long fileLength,
            boolean exists) {
        log.debug("FileSystemDao.createOrUpdateFile");

        int index = path.lastIndexOf(R2_FILE_SEPARATOR_STRING);
        String parentPath = index == -1 ? "" : path.substring(0, index);
        String name;

        if (!StringUtil.safeIsBlank(fileName) && index != -1) {
            path = parentPath + R2_FILE_SEPARATOR_STRING + fileName;
            name = fileName;
        } else {
            name = path.substring(index + 1);
        }

        String finalPath = path;
        String finalName = name;

        return getFolderId(fileResourceType, clientCode, parentPath)
                .thenCompose(parentId -> {
                    if (exists) {
                        return VirtualThreadWrapper.fromCallable(() -> {
                            int updated = this.context
                                    .update(CORE_FILE_SYSTEM)
                                    .set(CORE_FILE_SYSTEM.UPDATED_AT, LocalDateTime.now(ZoneOffset.UTC))
                                    .where(DSL.and(
                                            parentId.map(CORE_FILE_SYSTEM.PARENT_ID::eq)
                                                    .orElseGet(CORE_FILE_SYSTEM.PARENT_ID::isNull),
                                            CORE_FILE_SYSTEM.NAME.eq(finalName)))
                                    .execute();
                            return updated > 0;
                        });
                    }

                    return VirtualThreadWrapper.fromCallable(() -> {
                        int inserted = this.context
                                .insertInto(CORE_FILE_SYSTEM)
                                .set(CORE_FILE_SYSTEM.CODE, clientCode)
                                .set(CORE_FILE_SYSTEM.PARENT_ID, parentId.orElse(null))
                                .set(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE, FileSystemType.FILE)
                                .set(CORE_FILE_SYSTEM.NAME, finalName)
                                .set(CORE_FILE_SYSTEM.SIZE, fileLength)
                                .set(CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE, fileResourceType)
                                .execute();
                        return inserted > 0;
                    });
                })
                .thenCompose(updatedCreated -> {
                    if (Boolean.TRUE.equals(updatedCreated))
                        return getFileDetail(fileResourceType, clientCode, finalPath);

                    return VirtualThreadWrapper.just(null);
                });
    }

    public CompletableFuture<Boolean> createOrUpdateFileForZipUpload(
            FileResourceType fileResourceType,
            String clientCode,
            Long folderId,
            String path,
            String fileName,
            Long fileLength) {
        log.debug("FileSystemDao.createOrUpdateFileForZipUpload");

        int index = path.lastIndexOf(R2_FILE_SEPARATOR_STRING);
        String parentPath = index == -1 ? "" : path.substring(0, index);
        String name;

        if (!StringUtil.safeIsBlank(fileName) && index != -1) {
            path = parentPath + R2_FILE_SEPARATOR_STRING + fileName;
            name = fileName;
        } else {
            name = path.substring(index + 1);
        }

        String finalPath = path;
        String finalName = name;

        return VirtualThreadWrapper.just(Optional.ofNullable(folderId))
                .thenCompose(parentId -> getId(fileResourceType, clientCode, finalPath)
                        .thenCompose(existingId -> {
                            if (existingId.isPresent()) {
                                return VirtualThreadWrapper.fromCallable(() -> {
                                    int updated = this.context
                                            .update(CORE_FILE_SYSTEM)
                                            .set(CORE_FILE_SYSTEM.UPDATED_AT, LocalDateTime.now(ZoneOffset.UTC))
                                            .set(CORE_FILE_SYSTEM.SIZE, fileLength)
                                            .where(DSL.and(
                                                    parentId.map(CORE_FILE_SYSTEM.PARENT_ID::eq)
                                                            .orElseGet(CORE_FILE_SYSTEM.PARENT_ID::isNull),
                                                    CORE_FILE_SYSTEM.NAME.eq(finalName)))
                                            .execute();
                                    return updated > 0;
                                });
                            }

                            return VirtualThreadWrapper.fromCallable(() -> {
                                int inserted = this.context
                                        .insertInto(CORE_FILE_SYSTEM)
                                        .set(CORE_FILE_SYSTEM.CODE, clientCode)
                                        .set(CORE_FILE_SYSTEM.PARENT_ID, parentId.orElse(null))
                                        .set(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE, FileSystemType.FILE)
                                        .set(CORE_FILE_SYSTEM.NAME, finalName)
                                        .set(CORE_FILE_SYSTEM.SIZE, fileLength)
                                        .set(CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE, fileResourceType)
                                        .execute();
                                return inserted > 0;
                            });
                        }));
    }

    public CompletableFuture<Long> createFolder(FileResourceType fileResourceType, String clientCode, String path) {
        log.debug("FileSystemDao.createFolder");

        String resourcePath = path.startsWith(R2_FILE_SEPARATOR_STRING) ? path.substring(1) : path;

        int lastIndex = resourcePath.lastIndexOf(R2_FILE_SEPARATOR_STRING);
        String parentPath = lastIndex == -1 ? null : resourcePath.substring(0, lastIndex);
        String name = lastIndex == -1 ? resourcePath : resourcePath.substring(lastIndex + 1);

        return VirtualThreadWrapper.fromCallable(() -> {
                    if (parentPath == null) return Optional.<Long>empty();

                    return getId(fileResourceType, clientCode, parentPath).join();
                })
                .thenCompose(parentId -> VirtualThreadWrapper.fromCallable(() -> {
                            // Using transaction
                            return this.context.transactionResult(configuration -> {
                                DSLContext dsl = DSL.using(configuration);
                                Record1<Long> result = dsl.insertInto(CORE_FILE_SYSTEM)
                                        .set(CORE_FILE_SYSTEM.CODE, clientCode)
                                        .set(CORE_FILE_SYSTEM.PARENT_ID, parentId.orElse(null))
                                        .set(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE, FileSystemType.DIRECTORY)
                                        .set(CORE_FILE_SYSTEM.NAME, name)
                                        .set(CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE, fileResourceType)
                                        .returningResult(CORE_FILE_SYSTEM.ID)
                                        .fetchOne();

                                return result.value1();
                            });
                        })
                        .thenCompose(id ->
                                // Simulate the delay that was in the original code
                                VirtualThreadWrapper.delay(1000).thenApply(v -> id)));
    }

    public CompletableFuture<Map<String, Long>> createFolders(
            FileResourceType fileResourceType, String clientCode, List<String> paths) {
        Map<String, Node> nodeMap = new HashMap<>();

        for (String path : paths) {
            String[] pathParts = path.split(R2_FILE_SEPARATOR_STRING);
            StringBuilder sb = new StringBuilder();
            for (String part : pathParts) {
                Node parentNode = null;
                if (!sb.isEmpty()) {
                    parentNode = nodeMap.get(sb.toString());
                    sb.append(R2_FILE_SEPARATOR_STRING);
                }

                sb.append(part);
                if (!nodeMap.containsKey(sb.toString()))
                    nodeMap.put(sb.toString(), new Node(null, part, sb.toString(), parentNode));
            }
        }

        List<Node> nodes = new ArrayList<>();

        for (Node node : nodeMap.values()) {
            if (node.getParent() != null) continue;
            nodes.add(node);
        }

        // Process nodes in a depth-first manner
        List<CompletableFuture<Node>> nodeFutures = new ArrayList<>();
        for (Node node : nodes) nodeFutures.add(processNode(fileResourceType, clientCode, node, nodeMap));

        // Create a CompletableFuture that will contain all the processed nodes
        CompletableFuture<List<Node>> allNodesFuture = CompletableFuture.allOf(
                        nodeFutures.toArray(new CompletableFuture[0]))
                .thenApply(
                        v -> nodeFutures.stream().map(CompletableFuture::join).toList());

        return allNodesFuture.thenApply(processedNodes -> {
            Map<String, Long> map = new HashMap<>();
            for (String p : paths) {
                Node n = nodeMap.get(p);
                map.put(p, n.getId());
            }
            return map;
        });
    }

    private CompletableFuture<Node> processNode(
            FileResourceType fileResourceType, String clientCode, Node node, Map<String, Node> nodeMap) {
        return VirtualThreadWrapper.fromCallable(() -> {
            if (node.getId() != null) return node;

            Long id = selectFolderId(fileResourceType, clientCode, node).join();
            if (id == null)
                id = insertFolder(fileResourceType, clientCode, node).join();

            node.setId(id);

            List<Node> children =
                    nodeMap.values().stream().filter(n -> n.getParent() == node).toList();

            for (Node child : children)
                processNode(fileResourceType, clientCode, child, nodeMap).join();

            return node;
        });
    }

    private CompletableFuture<Long> selectFolderId(FileResourceType fileResourceType, String clientCode, Node node) {
        return VirtualThreadWrapper.fromCallable(() -> {
            Record1<Long> rec = this.context
                    .select(CORE_FILE_SYSTEM.ID)
                    .from(CORE_FILE_SYSTEM)
                    .where(DSL.and(
                            CORE_FILE_SYSTEM.CODE.eq(clientCode),
                            CORE_FILE_SYSTEM.NAME.eq(node.getName()),
                            CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE.eq(fileResourceType),
                            node.getParent() == null
                                    ? CORE_FILE_SYSTEM.PARENT_ID.isNull()
                                    : CORE_FILE_SYSTEM.PARENT_ID.eq(
                                            node.getParent().getId()),
                            CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE.eq(FileSystemType.DIRECTORY)))
                    .fetchOne();

            return rec != null ? rec.value1() : null;
        });
    }

    private CompletableFuture<Long> insertFolder(FileResourceType fileResourceType, String clientCode, Node node) {
        return VirtualThreadWrapper.fromCallable(() -> {
            Record1<Long> rec = this.context
                    .insertInto(CORE_FILE_SYSTEM)
                    .set(CORE_FILE_SYSTEM.CODE, clientCode)
                    .set(
                            CORE_FILE_SYSTEM.PARENT_ID,
                            node.getParent() == null ? null : node.getParent().getId())
                    .set(CORE_FILE_SYSTEM.FILE_SYSTEM_TYPE, FileSystemType.DIRECTORY)
                    .set(CORE_FILE_SYSTEM.NAME, node.getName())
                    .set(CORE_FILE_SYSTEM.FILE_RESOURCE_TYPE, fileResourceType)
                    .returningResult(CORE_FILE_SYSTEM.ID)
                    .fetchOne();

            return rec.value1();
        });
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Accessors(chain = true)
    @ToString
    private static class Node {
        private Long id;
        private String name;
        private String path;
        private Node parent;
    }
}
