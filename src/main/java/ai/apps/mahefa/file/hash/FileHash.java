package ai.apps.mahefa.file.hash;

import ai.apps.mahefa.PojaGenerated;

@PojaGenerated
public record FileHash(FileHashAlgorithm algorithm, String value) {}
