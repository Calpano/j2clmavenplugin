/*
 * Copyright © 2019 Dominokit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.util;

import java.io.Serializable;

/**
 * Hand-written UUID class. Taken from DominoKit org\dominokit\domino-jackson-super\1.0.0-RC3\domino-jackson-super-1.0.0-RC3-sources
 * and simplified.
 *
 * Note the JDK version has e.g. a "public long node()" method which returns 48 bits of the UUID.
 * This version lacks that method.
 * This version lacks validation, so UUID.fromString("hello") works.
 * The JDK version throws IllegalArgumentException, e.g. if the dashes are missing.
 */
public class UUID implements Serializable, Comparable<UUID> {

  private static final long serialVersionUID = 7373345728974414241L;

  private String value;

  private UUID() {}

  /**
   * Emulated version without validation
   * @param uuidString
   * @return
   */
  public static UUID fromString(String uuidString) {
    final UUID uuid = new UUID();
    uuid.value = uuidString;
    return uuid;
  }

  @Override
  public int compareTo(UUID arg0) {
    return value.compareTo(arg0.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    UUID other = (UUID) obj;
    if (value == null) {
      if (other.value != null) return false;
    } else if (!value.equals(other.value)) return false;
    return true;
  }

  @Override
  public String toString() {
    return value;
  }
}
