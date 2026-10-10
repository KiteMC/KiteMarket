package com.kitemc.market.api.extension;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Read-only inspection. Empty means not handled; a preview never makes a container tradable. */
public interface ContainerPreviewProvider extends MarketExtension {
  CompletableFuture<Optional<ContainerPreview>> preview(ItemInput item);
}
