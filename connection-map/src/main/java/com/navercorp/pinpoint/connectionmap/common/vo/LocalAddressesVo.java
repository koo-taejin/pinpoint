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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class LocalAddressesVo {

    private final String agentId;

    private final List<LocalIpVo> localIpAddressList;

    private LocalAddressesVo(Builder builder) {
        this.agentId = builder.agentId;
        this.localIpAddressList = builder.localIpAddressList;
    }

    public String getAgentId() {
        return agentId;
    }

    public List<LocalIpVo> getLocalIpAddressList() {
        return localIpAddressList;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LocalAddressesVo{");
        sb.append("agentId='").append(agentId).append('\'');
        sb.append(", localIpAddressList=").append(localIpAddressList);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {

        private String agentId;

        private final List<LocalIpVo> localIpAddressList = new ArrayList<>();

        public Builder agentId(String agentId) {
            this.agentId = agentId;
            return this;
        }

        public Builder addLocalIpVo(LocalIpVo localIpVo) {
            Objects.requireNonNull(localIpVo, "localIpVo");
            localIpAddressList.add(localIpVo);
            return this;
        }

        public LocalAddressesVo build() {
            Objects.requireNonNull(agentId, "agentId");
            return new LocalAddressesVo(this);
        }

    }

}
