/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package io.ballerina.messaging.broker.common.data.types;

import io.netty.buffer.ByteBuf;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * AMQP decimal.
 *
 * decimal-value = scale long-uint
 * scale = OCTET ; number of decimal digits
 */
public class Decimal implements EncodableData {

    private final BigDecimal value;

    private Decimal(BigDecimal value) {
        this.value = value;
    }

    @Override
    public long getSize() {
        return 1L + 4L;
    }

    @Override
    public void write(ByteBuf buf) {
        byte places = (byte) value.scale();
        int unscaled = value.intValue();
        buf.writeByte(places);
        buf.writeInt(unscaled);
    }

    public static Decimal parse(BigDecimal value) {
        return new Decimal(value);
    }

    public static Decimal parse(ByteBuf buf) {
        byte places = buf.readByte();
        int unscaled = buf.readInt();
        BigDecimal decimal = new BigDecimal(unscaled).setScale(places);
        return new Decimal(decimal);
    }

    public BigDecimal getDecimal() {
        return value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        return (obj instanceof Decimal) && (value.equals(((Decimal) obj).value));
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
