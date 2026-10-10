package com.kitemc.market.api.extension;

import java.util.Objects;

/** Detached diagnostics. A present API is not evidence of all-version compatibility. */
public final class ExtensionStatus {
  public enum Kind { ECONOMY, ITEM_IDENTITY, CONTAINER_PREVIEW, RULE, REVIEW, NOTIFICATION }
  private final String id, owner, problem;
  private final Kind kind;
  private final boolean available;
  private final long revision;

  public ExtensionStatus(
      String id, String owner, Kind kind, boolean available, String problem, long revision) {
    this.id = Objects.requireNonNull(id, "id");
    this.owner = Objects.requireNonNull(owner, "owner");
    this.kind = Objects.requireNonNull(kind, "kind");
    this.available = available;
    this.problem = problem;
    this.revision = revision;
  }

  public String getId() { return id; }
  public String getOwner() { return owner; }
  public Kind getKind() { return kind; }
  public boolean isAvailable() { return available; }
  /** Null only while available. */
  public String getProblem() { return problem; }
  public long getRevision() { return revision; }
}
