package com.viana.agentic.memory;

import java.util.List;

public record EmbeddingResponse(List<List<Double>> vectors) {}