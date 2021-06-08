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

package com.navercorp.pinpoint.connectionmap.common.vo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class LocalIpVo {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final byte[] srcIp;

    public LocalIpVo(byte[] srcIp) {
        this.srcIp = Objects.requireNonNull(srcIp, "srcIp");
    }

    public byte[] getSrcIp() {
        return srcIp;
    }

    public String getSrcIpString() {
        try {
            return InetAddress.getByAddress(srcIp).getHostAddress();
        } catch (UnknownHostException e) {
            logger.warn("Failed to convert string(). Cause:{}", e.getMessage(), e);
        }

        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LocalIpVo localIpVo = (LocalIpVo) o;

        if (logger != null ? !logger.equals(localIpVo.logger) : localIpVo.logger != null) return false;
        return Arrays.equals(srcIp, localIpVo.srcIp);
    }

    @Override
    public int hashCode() {
        int result = logger != null ? logger.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(srcIp);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LocalIpVo{");
        sb.append("srcIp=").append(Arrays.toString(srcIp));
        sb.append('}');
        return sb.toString();
    }
}
