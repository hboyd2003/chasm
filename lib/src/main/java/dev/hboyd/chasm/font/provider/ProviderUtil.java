/*
 * chasm
 * Copyright (c) 2026 Harrison Boyd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.hboyd.chasm.font.provider;

import com.google.common.collect.Range;
import com.google.common.collect.TreeRangeMap;

import java.util.Map;
import java.util.TreeMap;

class ProviderUtil {
    private ProviderUtil() {}

    static <T extends Number> TreeRangeMap<Integer, T> coalesceCharMap(TreeMap<Integer, T> charMap) {
        final TreeRangeMap<Integer, T> charRangeMap = TreeRangeMap.create();

        T currentWidth = charMap.firstEntry().getValue();
        int firstKey = charMap.firstKey();
        int previousKey = firstKey;

        for (Map.Entry<Integer, T> widthEntry : charMap.entrySet()) {
            if (!widthEntry.getValue().equals(currentWidth) || previousKey + 1 != widthEntry.getKey()) {
                charRangeMap.put(Range.closed(firstKey, previousKey), currentWidth);
                currentWidth = widthEntry.getValue();
                firstKey = widthEntry.getKey();
            }

            previousKey = widthEntry.getKey();
        }

        if (firstKey == charMap.firstKey() && previousKey == charMap.lastKey())
            charRangeMap.put(Range.closed(firstKey, previousKey), currentWidth);

        return charRangeMap;
    }
}
