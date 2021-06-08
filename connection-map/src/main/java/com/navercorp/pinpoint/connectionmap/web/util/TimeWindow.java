/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.connectionmap.web.util;

import java.time.temporal.ValueRange;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Taejin Koo
 */
public class TimeWindow {

    private final long windowSlotSize;

    private final ValueRange range;

    private final ValueRange windowRange;

//    public TimeWindow(ValueRange range) {
//        this(range, TimeWindowDownSampler.SAMPLER);
//    }

    public TimeWindow(ValueRange range, Function<ValueRange, Long> getWindowSizeFunction) {
        this.range = Objects.requireNonNull(range, "range");
        Objects.requireNonNull(getWindowSizeFunction, "getWindowSizeFunction");
        this.windowSlotSize = getWindowSizeFunction.apply(range);
        this.windowRange = createWindowRange();
    }

    public Iterator<Long> iterator() {
        return new Itr();
    }

    /**
     * converts the timestamp to the matching window slot's reference timestamp
     *
     * @param timestamp
     * @return
     */
    public long refineTimestamp(long timestamp) {
        long time = (timestamp / windowSlotSize) * windowSlotSize;
        return time;
    }

    public ValueRange getWindowRange() {
        return windowRange;
    }

    public long getWindowSlotSize() {
        return windowSlotSize;
    }

    public long getWindowRangeCount() {
        return (windowRange.getMaximum() - windowRange.getMinimum() / windowSlotSize) + 1;
    }

    private ValueRange createWindowRange() {
        long from = refineTimestamp(range.getMinimum());
        long to = refineTimestamp(range.getMaximum());

        return ValueRange.of(from, to);
    }

    public int getWindowIndex(long time) {
        long index = (time - windowRange.getMinimum()) / this.windowSlotSize;
        return (int) index;
    }

    private class Itr implements Iterator<Long> {

        private long cursor;

        public Itr() {
            this.cursor = windowRange.getMinimum();
        }

        @Override
        public boolean hasNext() {
            if (cursor > windowRange.getMaximum()) {
                return false;
            }
            return true;
        }

        @Override
        public Long next() {
            long current = cursor;
            if (hasNext()) {
                cursor += windowSlotSize;
                return current;
            }
            throw new NoSuchElementException();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

}
