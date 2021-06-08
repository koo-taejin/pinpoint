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

package com.navercorp.pinpoint.connectionmap.web.vo;

import com.navercorp.pinpoint.common.util.Assert;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class ConnectionAddress {

    private final byte[] ipAddress;
    private final int port;

    public ConnectionAddress(byte[] ipAddress, int port) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "ipAddress");
        Assert.isTrue(port > 0 && port <= 65535, "port is invalid.");
        this.port = port;
    }

    public byte[] getIpAddress() {
        return ipAddress;
    }

    public String getAddressString() {
        try {
            return InetAddress.getByAddress(ipAddress).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return Arrays.toString(ipAddress);
    }

    public int getPort() {
        return port;
    }

    public boolean equals(byte[] ipAddress, int port) {
        if (this.port != port) {
            return false;
        }
        return Arrays.equals(this.ipAddress, ipAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConnectionAddress that = (ConnectionAddress) o;

        if (port != that.port) return false;
        return Arrays.equals(ipAddress, that.ipAddress);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(ipAddress);
        result = 31 * result + port;
        return result;
    }

    @Override
    public String toString() {
        return "ConnectionAddress{" +
                "ipAddress=" + Arrays.toString(ipAddress) +
                ", port=" + port +
                '}';
    }

}
