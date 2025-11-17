package com.sea.odps.sql.metadata.model.feature;

import java.util.Collections;
import java.util.List;

import lombok.Getter;

/** HINT 元数据。 */
@Getter
public final class HintMetadata {

  /** HINT 文本列表。 */
  private final List<String> hints;

  /**
   * 构造函数。
   *
   * @param hints HINT 文本列表
   */
  public HintMetadata(final List<String> hints) {
    this.hints = hints != null ? Collections.unmodifiableList(hints) : Collections.emptyList();
  }
}
