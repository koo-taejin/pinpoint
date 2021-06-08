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

package com.navercorp.pinpoint.connectionmap.common.util;

import com.navercorp.pinpoint.connectionmap.common.proto.ConnectionFamily;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public enum InetFamily {

    INET_4_ADDRESS(ConnectionFamily.AF_INET_4, 4),
    INET_6_ADDRESS(ConnectionFamily.AF_INET_6, 16);

    private final ConnectionFamily connectionFamily;
    private final int addressSize;

    InetFamily(ConnectionFamily connectionFamily, int addressSize) {
        this.connectionFamily = Objects.requireNonNull(connectionFamily, "connectionFamily");
        this.addressSize = addressSize;
    }

    public int getAddressSize() {
        return addressSize;
    }

    public static InetFamily getFamily(int value) {
        ConnectionFamily connectionFamily = ConnectionFamily.forNumber(value);
        return getFamily(connectionFamily);
    }

    public static InetFamily getFamily(ConnectionFamily connectionFamily) {
        InetFamily[] values = values();
        for (InetFamily value : values) {
            if (connectionFamily == value.connectionFamily) {
                return value;
            }
        }
        return null;
    }

    public static InetFamily getFamilyByAddressSize(int addressSize) {
        InetFamily[] values = values();
        for (InetFamily value : values) {
            if (addressSize == value.addressSize) {
                return value;
            }
        }
        return null;
    }

    public final int getNumber() {
        return connectionFamily.getNumber();
    }

    public final byte getByteNumber() {
        return new Integer(connectionFamily.getNumber()).byteValue();
    }

}
