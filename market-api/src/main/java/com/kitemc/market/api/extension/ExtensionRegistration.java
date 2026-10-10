package com.kitemc.market.api.extension;

/** Idempotent owner-bound handle. Closing an old handle cannot remove a replacement registration. */
public interface ExtensionRegistration extends AutoCloseable {
  ExtensionStatus status();
  @Override void close();
}
